/*
 * Copyright 2017-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hellojavaer.ddal.ddr.shard.simple;

import org.hellojavaer.ddal.ddr.expression.el.function.ELFunctionManager;
import org.hellojavaer.ddal.ddr.shard.ShardRouteRule;
import org.hellojavaer.ddal.ddr.shard.ShardRouteRuleContext;
import org.hellojavaer.ddal.ddr.shard.exception.ExpressionValueNotFoundException;
import org.hellojavaer.ddal.ddr.utils.DDRToStringBuilder;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.ReflectionHelper;
import org.springframework.expression.spel.support.ReflectiveMethodResolver;
import org.springframework.expression.spel.support.ReflectivePropertyAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author <a href="mailto:hellojavaer@gmail.com">Kaiming Zou</a>,created on 25/04/2017.
 */
public class SpelShardRouteRule implements ShardRouteRule {

    private String     scRouteRule;
    private String     tbRouteRule;

    private Expression scRouteRuleExpression;
    private Expression tbRouteRuleExpression;

    // used for spring bean
    private SpelShardRouteRule() {
    }

    public SpelShardRouteRule(String scRouteRule, String tbRouteRule) {
        setScRouteRule(scRouteRule);
        setTbRouteRule(tbRouteRule);
    }

    private void setScRouteRule(String scRouteRule) {
        scRouteRule = filter(scRouteRule);
        this.scRouteRule = scRouteRule;
        if (scRouteRule != null) {
            ExpressionParser parser = new SpelExpressionParser();
            this.scRouteRuleExpression = parser.parseExpression(scRouteRule, PARSER_CONTEXT);
        }
    }

    public String getScRouteRule() {
        return scRouteRule;
    }

    private void setTbRouteRule(String tbRouteRule) {
        tbRouteRule = filter(tbRouteRule);
        this.tbRouteRule = tbRouteRule;
        if (tbRouteRule != null) {
            ExpressionParser parser = new SpelExpressionParser();
            this.tbRouteRuleExpression = parser.parseExpression(tbRouteRule, PARSER_CONTEXT);
        }
    }

    public String getTbRouteRule() {
        return tbRouteRule;
    }

    private String filter(String string) {
        if (string != null) {
            string = string.trim();
            if (string.length() == 0) {
                string = null;
            }
        }
        return string;
    }

    @Override
    public String parseScName(ShardRouteRuleContext context) {
        if (scRouteRuleExpression == null) {
            return context.getScName();
        } else {
            EvaluationContext elContext = buildEvaluationContext(scRouteRule);
            elContext.setVariable("scName", context.getScName());
            elContext.setVariable("tbName", context.getTbName());
            elContext.setVariable("sdValue", context.getSdValue());
            return scRouteRuleExpression.getValue(elContext, String.class);
        }
    }

    @Override
    public String parseTbName(ShardRouteRuleContext context) {
        if (tbRouteRuleExpression == null) {
            return context.getTbName();
        } else {
            EvaluationContext elContext = buildEvaluationContext(tbRouteRule);
            elContext.setVariable("scName", context.getScName());
            elContext.setVariable("tbName", context.getTbName());
            elContext.setVariable("sdValue", context.getSdValue());
            return tbRouteRuleExpression.getValue(elContext, String.class);
        }
    }

    @Override
    public String toString() {
        return new DDRToStringBuilder()//
        .append("scRouteRule", scRouteRule)//
        .append("tbRouteRule", tbRouteRule)//
        .toString();
    }

    private static final Set<String> RESERVED_WORDS = new HashSet<String>();

    static {
        RESERVED_WORDS.add("db");
        RESERVED_WORDS.add("dbName");
        RESERVED_WORDS.add("dbValue");
        RESERVED_WORDS.add("dbRoute");
        RESERVED_WORDS.add("dbFormat");

        RESERVED_WORDS.add("sc");
        RESERVED_WORDS.add("scName");
        RESERVED_WORDS.add("scValue");
        RESERVED_WORDS.add("scRoute");
        RESERVED_WORDS.add("scFormat");

        RESERVED_WORDS.add("tb");
        RESERVED_WORDS.add("tbName");
        RESERVED_WORDS.add("tbValue");
        RESERVED_WORDS.add("tbRoute");
        RESERVED_WORDS.add("tbFormat");

        RESERVED_WORDS.add("sd");
        RESERVED_WORDS.add("sdName");
        RESERVED_WORDS.add("sdKey");
        RESERVED_WORDS.add("sdValue");
        RESERVED_WORDS.add("sdRoute");
        RESERVED_WORDS.add("sdFormat");

        RESERVED_WORDS.add("col");
        RESERVED_WORDS.add("colName");
        RESERVED_WORDS.add("colValue");
        RESERVED_WORDS.add("colRoute");
        RESERVED_WORDS.add("colFormat");
    }

    private static boolean isReservedWords(String str) {
        if (str == null) {
            return false;
        } else {
            return RESERVED_WORDS.contains(str);
        }
    }

    /**
     * load order
     * 1.reserved words
     * 2.function
     * 3.user-define var
     */
    private static EvaluationContext buildEvaluationContext(final String expression) {
        StandardEvaluationContext context = new StandardEvaluationContext(rootObject) {

            @Override
            public Object lookupVariable(String name) {
                Object val = null;
                if (isReservedWords(name)) {
                    val = super.lookupVariable(name);
                    if (val == null) {
                        throw new ExpressionValueNotFoundException("Value of '" + name
                                                                   + "' is not found when parsing expression '"
                                                                   + expression + "'");
                    }
                } else {
                    val = ELFunctionManager.getRegisteredFunction(name);
                    // TODO: user-define var
                }
                return val;
            }

            @Override
            public List<PropertyAccessor> getPropertyAccessors() {
                return propertyAccessors;
            }

            @Override
            public List<MethodResolver> getMethodResolvers() {
                return methodResolvers;
            }
        };
        return context;
    }

    private static final TypedValue             rootObject        = new TypedValue(null);

    private static final List<PropertyAccessor> propertyAccessors = new ArrayList<PropertyAccessor>(1);

    private static final List<MethodResolver>   methodResolvers   = new ArrayList<>(2);

    private static final ParserContext          PARSER_CONTEXT    = new ParserContext() {

                                                                      public boolean isTemplate() {
                                                                          return true;
                                                                      }

                                                                      public String getExpressionPrefix() {
                                                                          return "{";
                                                                      }

                                                                      public String getExpressionSuffix() {
                                                                          return "}";
                                                                      }
                                                                  };

    static {
        //
        propertyAccessors.add(new ReflectivePropertyAccessor());
        propertyAccessors.add(new PropertyAccessor() {

            public Class<?>[] getSpecificTargetClasses() {
                return null;
            }

            public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
                return true;
            }

            public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
                return new TypedValue(context.lookupVariable(name));
            }

            public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
                return false;
            }

            public void write(EvaluationContext context, Object target, String name, Object newValue)
                                                                                                     throws AccessException {

            }
        });
        //
        methodResolvers.add(new ReflectiveMethodResolver());
        methodResolvers.add(new MethodResolver() {

            @Override
            public MethodExecutor resolve(EvaluationContext context, Object targetObject, final String name,
                                          List<TypeDescriptor> argumentTypes) throws AccessException {
                final Method method = (Method) context.lookupVariable(name);
                if (method == null) {
                    return null;
                }
                return new MethodExecutor() {

                    @Override
                    public TypedValue execute(EvaluationContext context, Object target, Object... arguments)
                                                                                                            throws AccessException {
                        try {
                            if (method.isVarArgs()) {
                                arguments = ReflectionHelper.setupArgumentsForVarargsInvocation(method.getParameterTypes(),
                                                                                                arguments);
                            }
                            ReflectionUtils.makeAccessible(method);
                            Object value = method.invoke(target, arguments);
                            return new TypedValue(value,
                                                  new TypeDescriptor(new MethodParameter(method, -1)).narrow(value));
                        } catch (Exception ex) {
                            throw new AccessException("Problem invoking method: " + method, ex);
                        }
                    }
                };
            }
        });
    }
}
