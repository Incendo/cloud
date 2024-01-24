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
package cloud.commandframework.annotations.extractor;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.Command;
import cloud.commandframework.annotations.descriptor.CommandDescriptor;
import cloud.commandframework.annotations.descriptor.ImmutableCommandDescriptor;
import cloud.commandframework.util.annotation.AnnotationAccessor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.annotations.*")
public final class CommandExtractorImpl implements CommandExtractor {

    private final AnnotationParser<?> annotationParser;

    /**
     * Creates a new command extractor.
     *
     * @param annotationParser annotation parser
     */
    public CommandExtractorImpl(final @NonNull AnnotationParser<?> annotationParser) {
        this.annotationParser = annotationParser;
    }

    @Override
    public @NonNull Collection<@NonNull CommandDescriptor> extractCommands(final @NonNull Object instance) {
        final AnnotationAccessor classAnnotations = AnnotationAccessor.of(instance.getClass());
        final Command classCommand = classAnnotations.annotation(Command.class);

        final String syntaxPrefix;
        if (classCommand == null) {
            syntaxPrefix = "";
        } else {
            syntaxPrefix = this.annotationParser.processString(classCommand.value()) + " ";
        }

        final Method[] methods = instance.getClass().getDeclaredMethods();
        final Collection<CommandDescriptor> commandDescriptors = new ArrayList<>();
        for (final Method method : methods) {
            final Command[] commands = method.getAnnotationsByType(Command.class);
            if (commands.length == 0) {
                continue;
            }

            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException(String.format(
                        "@Command annotated method '%s' is static! @Command annotated methods should not be static.",
                        method.getName()
                ));
            }

            for (final Command command : commands) {
                final String syntax = syntaxPrefix + this.annotationParser.processString(command.value());
                commandDescriptors.add(
                        ImmutableCommandDescriptor.builder()
                                .method(method)
                                .syntax(this.annotationParser.syntaxParser().parseSyntax(method, syntax))
                                .commandToken(syntax.split(" ")[0].split("\\|")[0])
                                .requiredSender(command.requiredSender())
                                .build()
                );
            }
        }
        return commandDescriptors;
    }
}
