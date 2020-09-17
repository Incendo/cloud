//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.annotations;

import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.arguments.parser.ParserParameters;
import com.intellectualsites.commands.meta.CommandMeta;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Parser that parses class instances {@link Command commands}
 *
 * @param <C> Command sender type
 * @param <M> Command meta type
 */
public class AnnotationParser<C, M extends CommandMeta> {

    private static final Predicate<String> PATTERN_ARGUMENT_LITERAL  = Pattern.compile("([A-Za-z0-9]+)(|([A-Za-z0-9]+))*")
                                                                              .asPredicate();
    private static final Predicate<String> PATTERN_ARGUMENT_REQUIRED = Pattern.compile("<(A-Za-z0-9]+)>").asPredicate();
    private static final Predicate<String> PATTERN_ARGUMENT_OPTIONAL = Pattern.compile("\\[([A-Za-z0-9]+)\\]").asPredicate();

    private final CommandManager<C, M> manager;

    /**
     * Construct a new annotation parser
     *
     * @param manager Command manager instance
     */
    public AnnotationParser(@Nonnull final CommandManager<C, M> manager) {
        this.manager = manager;
    }

    /**
     * Scan a class instance of {@link CommandMethod} annotations and attempt to
     * compile them into {@link Command} instances
     *
     * @param instance Instance to scan
     * @param <T>      Type of the instance
     * @return Collection of parsed annotations
     */
    @Nonnull
    public <T> Collection<Command<C, M>> parse(@Nonnull final T instance) {
        final Method[] methods = instance.getClass().getMethods();
        final Collection<CommandMethodPair> commandMethodPairs = new ArrayList<>();
        for (final Method method : methods) {
            final CommandMethod commandMethod = method.getAnnotation(CommandMethod.class);
            if (commandMethod == null) {
                continue;
            }
            if (method.getReturnType() != Void.TYPE) {
                throw new IllegalArgumentException(String.format("@CommandMethod annotated method '%s' has non-void return type",
                                                                 method.getName()));
            }
            commandMethodPairs.add(new CommandMethodPair(method, commandMethod));
        }
        return this.construct(commandMethodPairs);
    }

    @Nonnull
    private Collection<Command<C, M>> construct(@Nonnull final Collection<CommandMethodPair> methodPairs) {
        final Collection<Command<C, M>> commands = new ArrayList<>();
        for (final CommandMethodPair commandMethodPair : methodPairs) {
            final CommandMethod commandMethod = commandMethodPair.getCommandMethod();
            final Method method = commandMethodPair.getMethod();
            final Map<String, ArgumentMode> tokens = this.parseSyntax(commandMethod.value());
            Command.Builder<C, M> builder = this.manager.commandBuilder(commandMethod.value(),
                                                                        Collections.emptyList(),
                                                                        manager.createDefaultCommandMeta());
            final Collection<ArgumentParameterPair> arguments = this.getArguments(method);
            for (final ArgumentParameterPair argumentPair : arguments) {
                final Parameter parameter = argumentPair.getParameter();
                final Collection<Annotation> annotations = Arrays.asList(parameter.getAnnotations());
                final TypeToken<?> token = TypeToken.of(parameter.getParameterizedType());
                final ParserParameters parameters = this.manager.getParserRegistry()
                                                                .parseAnnotations(token, annotations);
                final ArgumentParser<C, ?> parser = this.manager.getParserRegistry()
                                                                .createParser(token, parameters)
                                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                        String.format("Parameter '%s' in method '%s' "
                                                                                    + "has parser '%s' but no parser exists "
                                                                                    + "for that type",
                                                                                      parameter.getName(), method.getName(),
                                                                                      token.toString())));
            }

        }
        return commands;
    }

    @Nonnull
    private LinkedHashMap<String, ArgumentMode> parseSyntax(@Nonnull final String syntax) {
        final StringTokenizer stringTokenizer = new StringTokenizer(syntax, " ");
        final LinkedHashMap<String, ArgumentMode> map = new LinkedHashMap<>();
        while (stringTokenizer.hasMoreTokens()) {
            final String token = stringTokenizer.nextToken();
            if (PATTERN_ARGUMENT_REQUIRED.test(token)) {
                map.put(token.substring(1, token.length() - 1), ArgumentMode.REQUIRED);
            } else if (PATTERN_ARGUMENT_OPTIONAL.test(token)) {
                map.put(token.substring(1, token.length() - 1), ArgumentMode.OPTIONAL);
            } else {
                final String[] literals = token.split("\\|");
                /* Actually use the other literals as well */
                map.put(literals[0], ArgumentMode.LITERAL);
            }
        }
        return map;
    }

    @Nonnull
    private Collection<ArgumentParameterPair> getArguments(@Nonnull final Method method) {
        final Collection<ArgumentParameterPair> arguments = new ArrayList<>();
        for (final Parameter parameter : method.getParameters()) {
            if (!parameter.isAnnotationPresent(Argument.class)) {
                continue;
            }
            arguments.add(new ArgumentParameterPair(parameter, parameter.getAnnotation(Argument.class)));
        }
        return arguments;
    }


    private static final class CommandMethodPair {

        private final Method method;
        private final CommandMethod commandMethod;

        private CommandMethodPair(@Nonnull final Method method, @Nonnull final CommandMethod commandMethod) {
            this.method = method;
            this.commandMethod = commandMethod;
        }

        @Nonnull
        private Method getMethod() {
            return this.method;
        }

        @Nonnull
        private CommandMethod getCommandMethod() {
            return this.commandMethod;
        }

    }


    private static final class ArgumentParameterPair {

        private final Parameter parameter;
        private final Argument argument;

        private ArgumentParameterPair(@Nonnull final Parameter parameter, @Nonnull final Argument argument) {
            this.parameter = parameter;
            this.argument = argument;
        }

        @Nonnull
        private Parameter getParameter() {
            return this.parameter;
        }

        @Nonnull
        private Argument getArgument() {
            return this.argument;
        }

    }


    private enum ArgumentMode {
        LITERAL, OPTIONAL, REQUIRED
    }

}
