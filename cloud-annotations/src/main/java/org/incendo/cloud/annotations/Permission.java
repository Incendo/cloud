//
// MIT License
//
// Copyright (c) 2024 Incendo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package org.incendo.cloud.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Equivalent to {@link org.incendo.cloud.Command.Builder#permission(String)}
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {

    /**
     * Returns the command permissions.
     *
     * @return command permissions
     */
    @NonNull String[] value() default {};

    /**
     * Returns the permission mode. This is only used in {@link #value()} contains more than one permission.
     *
     * @return the permission mode
     */
    @NonNull Mode mode() default Mode.ANY_OF;


    enum Mode {
        /**
         * At least one of the permissions defined in {@link #value()} is required.
         *
         * @see org.incendo.cloud.permission.Permission#anyOf(org.incendo.cloud.permission.Permission...)
         */
        ANY_OF,
        /**
         * All permissions defined in {@link #value()} are required.
         *
         * @see org.incendo.cloud.permission.Permission#allOf(org.incendo.cloud.permission.Permission...)
         */
        ALL_OF;

        org.incendo.cloud.permission.@NonNull Permission combine(
                final @NonNull Stream<org.incendo.cloud.permission.Permission> permissions
        ) {
            final List<org.incendo.cloud.permission.Permission> permissionList = permissions.collect(Collectors.toList());
            if (this == ANY_OF) {
                return org.incendo.cloud.permission.Permission.anyOf(permissionList);
            }
            return org.incendo.cloud.permission.Permission.allOf(permissionList);
        }
    }
}
