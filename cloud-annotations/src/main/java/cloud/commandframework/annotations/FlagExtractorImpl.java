//
// MIT License
//
// Copyright (c) 2022 Alexander Söderberg & Contributors
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
package cloud.commandframework.annotations;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.permission.Permission;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import org.checkerframework.checker.nullness.qual.NonNull;

final class FlagExtractorImpl implements FlagExtractor {

    private final AnnotationParser<?> annotationParser;

    FlagExtractorImpl(final @NonNull AnnotationParser<?> annotationParser) {
        this.annotationParser = annotationParser;
    }

    @Override
    public @NonNull Collection<@NonNull FlagDescriptor> extractFlags(final @NonNull Method method) {
        final Collection<FlagDescriptor> flags = new LinkedList<>();
        for (final Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Flag.class)) {
                continue;
            }
            final Flag flag = parameter.getAnnotation(Flag.class);
            final String flagName = this.annotationParser.processString(flag.value());

            final FlagDescriptor flagDescriptor = FlagDescriptor.builder()
                    .parameter(parameter)
                    .name(flagName)
                    .description(ArgumentDescription.of(this.annotationParser.processString(flag.description())))
                    .aliases(this.annotationParser.processStrings(Arrays.asList(flag.aliases())))
                    .permission(Permission.of(this.annotationParser.processString(flag.permission())))
                    .repeatable(flag.repeatable())
                    .parserName(this.annotationParser.processString(flag.parserName()))
                    .suggestions(this.annotationParser.processString(flag.suggestions()))
                    .build();
            flags.add(flagDescriptor);
        }
        return flags;
    }
}
