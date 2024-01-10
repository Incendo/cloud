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
package cloud.commandframework.annotations;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CommandExtractorImpl implements CommandExtractor {

    private final AnnotationParser<?> annotationParser;

    CommandExtractorImpl(final @NonNull AnnotationParser<?> annotationParser) {
        this.annotationParser = annotationParser;
    }

    @Override
    public @NonNull Collection<@NonNull CommandDescriptor> extractCommands(final @NonNull Object instance) {
        final AnnotationAccessor classAnnotations = AnnotationAccessor.of(instance.getClass());
        final CommandMethod classCommandMethod = classAnnotations.annotation(CommandMethod.class);

        final String syntaxPrefix;
        if (classCommandMethod == null) {
            syntaxPrefix = "";
        } else {
            syntaxPrefix = this.annotationParser.processString(classCommandMethod.value()) + " ";
        }

        final Method[] methods = instance.getClass().getDeclaredMethods();
        final Collection<CommandDescriptor> commandDescriptors = new ArrayList<>();
        for (final Method method : methods) {
            final CommandMethod[] commands = method.getAnnotationsByType(CommandMethod.class);
            if (commands.length == 0) {
                continue;
            }

            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException(String.format(
                        "@CommandMethod annotated method '%s' is static! @CommandMethod annotated methods should not be static.",
                        method.getName()
                ));
            }

            for (final CommandMethod commandMethod : commands) {
                final String syntax = syntaxPrefix + this.annotationParser.processString(commandMethod.value());
                commandDescriptors.add(
                        ImmutableCommandDescriptor.builder()
                                .method(method)
                                .syntax(this.annotationParser.syntaxParser().parseSyntax(method, syntax))
                                .commandToken(syntax.split(" ")[0].split("\\|")[0])
                                .requiredSender(commandMethod.requiredSender())
                                .build()
                );
            }
        }
        return commandDescriptors;
    }
}
