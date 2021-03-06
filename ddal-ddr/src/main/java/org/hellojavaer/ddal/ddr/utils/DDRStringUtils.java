/*
 * Copyright 2016-2017 the original author or authors.
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
package org.hellojavaer.ddal.ddr.utils;

/**
 *
 * @author <a href="mailto:hellojavaer@gmail.com">Kaiming Zou</a>,created on 19/11/2016.
 */
public class DDRStringUtils {

    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            if (str.length() == 0) {
                return null;
            } else {
                return str;
            }
        }
    }

    public static String toLowerCase(String str) {
        if (str == null) {
            return null;
        } else {
            str = str.trim();
            if (str.length() == 0) {
                return null;
            } else {
                return str.toLowerCase();
            }
        }
    }

    public static boolean equals(String s0, String s1) {
        if (s0 == null && s1 == null) {
            return true;
        } else if (s0 != null) {
            return s0.equals(s1);
        } else if (s1 != null) {
            return s1.equals(s0);
        } else {
            return false;
        }
    }
}
