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

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.arguments.CommandArgument;
import com.intellectualsites.commands.arguments.parser.ArgumentParser;
import com.intellectualsites.commands.arguments.parser.ParserParameters;
import com.intellectualsites.commands.arguments.parser.StandardParameters;
import com.intellectualsites.commands.execution.CommandExecutionHandler;
import com.intellectualsites.commands.meta.CommandMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Parser that parses class instances {@link com.intellectualsites.commands.Command commands}
 *
 * @param <C> Command sender type
 */
public final class AnnotationParser<C> {

    private static final Predicate<String> PATTERN_ARGUMENT_LITERAL = Pattern.compile("([A-Za-z0-9]+)(|([A-Za-z0-9]+))*")
                                                                             .asPredicate();
    private static final Predicate<String> PATTERN_ARGUMENT_REQUIRED = Pattern.compile("<([A-Za-z0-9]+)>")
                                                                              .asPredicate();
    private static final Predicate<String> PATTERN_ARGUMENT_OPTIONAL = Pattern.compile("\\[([A-Za-z0-9]+)]")
                                                                              .asPredicate();


    private final Function<ParserParameters, CommandMeta> metaMapper;
    private final CommandManager<C> manager;
    private final Map<Class<? extends Annotation>, Function<? extends Annotation, ParserParameters>> annotationMappers;
    private final Class<C> commandSenderClass;

    /**
     * Construct a new annotation parser
     *
     * @param manager            Command manager instance
     * @param commandSenderClass Command sender class
     * @param metaMapper         Function that is used to create {@link CommandMeta} instances from annotations on the
     *                           command methods. These annotations will be mapped to
     *                           {@link com.intellectualsites.commands.arguments.parser.ParserParameter}. Mappers for the
     *                           parser parameters can be registered using {@link #registerAnnotationMapper(Class, Function)}
     */
    public AnnotationParser(@Nonnull final CommandManager<C> manager,
                            @Nonnull final Class<C> commandSenderClass,
                            @Nonnull final Function<ParserParameters, CommandMeta> metaMapper) {
        this.commandSenderClass = commandSenderClass;
        this.manager = manager;
        this.metaMapper = metaMapper;
        this.annotationMappers = Maps.newHashMap();
        this.registerAnnotationMapper(Description.class, d -> ParserParameters.single(StandardParameters.DESCRIPTION, d.value()));
    }

    /**
     * Register an annotation mapper
     *
     * @param annotation Annotation class
     * @param mapper     Mapping function
     * @param <A>        Annotation type
     */
    public <A extends Annotation> void registerAnnotationMapper(@Nonnull final Class<A> annotation,
                                                                @Nonnull final Function<A, ParserParameters> mapper) {
        this.annotationMappers.put(annotation, mapper);
    }

    @Nonnull
    private CommandMeta createMeta(@Nonnull final Annotation[] annotations) {
        final ParserParameters parameters = ParserParameters.empty();
        for (final Annotation annotation : annotations) {
            @SuppressWarnings("ALL") final Function function = this.annotationMappers.get(annotation.annotationType());
            if (function == null) {
                continue;
            }
            //noinspection unchecked
            parameters.merge((ParserParameters) function.apply(annotation));
        }
        return this.metaMapper.apply(parameters);
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
    public <T> Collection<Command<C>> parse(@Nonnull final T instance) {
        final Method[] methods = instance.getClass().getDeclaredMethods();
        final Collection<CommandMethodPair> commandMethodPairs = new ArrayList<>();
        for (final Method method : methods) {
            final CommandMethod commandMethod = method.getAnnotation(CommandMethod.class);
            if (commandMethod == null) {
                continue;
            }
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            if (method.getReturnType() != Void.TYPE) {
                throw new IllegalArgumentException(String.format("@CommandMethod annotated method '%s' has non-void return type",
                                                                 method.getName()));
            }
            commandMethodPairs.add(new CommandMethodPair(method, commandMethod));
        }
        final Collection<Command<C>> commands = this.construct(instance, commandMethodPairs);
        for (final Command<C> command : commands) {
            @SuppressWarnings("ALL") final CommandManager commandManager = this.manager;
            //noinspection all
            commandManager.command(command);
        }
        return commands;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private Collection<Command<C>> construct(@Nonnull final Object instance,
                                             @Nonnull final Collection<CommandMethodPair> methodPairs) {
        final Collection<Command<C>> commands = new ArrayList<>();
        for (final CommandMethodPair commandMethodPair : methodPairs) {
            final CommandMethod commandMethod = commandMethodPair.getCommandMethod();
            final Method method = commandMethodPair.getMethod();
            final LinkedHashMap<String, SyntaxFragment> tokens = this.parseSyntax(commandMethod.value());
            /* Determine command name */
            final String commandToken = commandMethod.value().split(" ")[0].split("\\|")[0];
            @SuppressWarnings("ALL") final CommandManager manager = this.manager;
            @SuppressWarnings("ALL")
            Command.Builder builder = manager.commandBuilder(commandToken,
                                                             tokens.get(commandToken).getMinor(),
                                                             this.createMeta(method.getAnnotations()));
            final Collection<ArgumentParameterPair> arguments = this.getArguments(method);
            final Map<String, CommandArgument<C, ?>> commandArguments = Maps.newHashMap();
            /* Go through all annotated parameters and build up the argument tree */
            for (final ArgumentParameterPair argumentPair : arguments) {
                final CommandArgument<C, ?> argument = this.buildArgument(method,
                                                                          tokens.get(argumentPair.getArgument().value()),
                                                                          argumentPair);
                commandArguments.put(argument.getName(), argument);
            }
            boolean commandNameFound = false;
            /* Build the command tree */
            for (final Map.Entry<String, SyntaxFragment> entry : tokens.entrySet()) {
                if (!commandNameFound) {
                    commandNameFound = true;
                    continue;
                }
                if (entry.getValue().getArgumentMode() == ArgumentMode.LITERAL) {
                    builder = builder.literal(entry.getKey(), entry.getValue().getMinor().toArray(new String[0]));
                } else {
                    final CommandArgument<C, ?> argument = commandArguments.get(entry.getKey());
                    if (argument == null) {
                        throw new IllegalArgumentException(String.format(
                                "Found no mapping for argument '%s' in method '%s'",
                                entry.getKey(), method.getName()
                        ));
                    }
                    builder = builder.argument(argument);
                }
            }
            /* Try to find the command sender type */
            Class<? extends C> senderType = null;
            for (final Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(Argument.class)) {
                    continue;
                }
                if (this.commandSenderClass.isAssignableFrom(parameter.getType())) {
                    senderType = (Class<? extends C>) parameter.getType();
                    break;
                }
            }
            /* Decorate command data */
            builder = builder.withPermission(commandMethod.permission());
            if (commandMethod.requiredSender() != Object.class) {
                builder = builder.withSenderType(commandMethod.requiredSender());
            } else if (senderType != null) {
                builder = builder.withSenderType(senderType);
            }
            /* Construct the handler */
            final CommandExecutionHandler<C> commandExecutionHandler = commandContext -> {
                final List<Object> parameters = new ArrayList<>(method.getParameterCount());
                for (final Parameter parameter : method.getParameters()) {
                    if (parameter.isAnnotationPresent(Argument.class)) {
                        final Argument argument = parameter.getAnnotation(Argument.class);
                        final CommandArgument<C, ?> commandArgument = commandArguments.get(argument.value());
                        if (commandArgument.isRequired()) {
                            parameters.add(commandContext.getRequired(argument.value()));
                        } else {
                            final Object optional = commandContext.get(argument.value()).orElse(null);
                            parameters.add(optional);
                        }
                    } else {
                        if (parameter.getType().isAssignableFrom(commandContext.getSender().getClass())) {
                            parameters.add(commandContext.getSender());
                        } else {
                            throw new IllegalArgumentException(String.format(
                                    "Unknown command parameter '%s' in method '%s'",
                                    parameter.getName(), method.getName()
                            ));
                        }
                    }
                }
                try {
                    method.invoke(instance, parameters.toArray());
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            };
            builder = builder.handler(commandExecutionHandler);
            commands.add(builder.build());
        }
        return commands;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    private CommandArgument<C, ?> buildArgument(@Nonnull final Method method,
                                                @Nullable final SyntaxFragment syntaxFragment,
                                                @Nonnull final ArgumentParameterPair argumentPair) {
        final Parameter parameter = argumentPair.getParameter();
        final Collection<Annotation> annotations = Arrays.asList(parameter.getAnnotations());
        final TypeToken<?> token = TypeToken.of(parameter.getParameterizedType());
        final ParserParameters parameters = this.manager.getParserRegistry()
                                                        .parseAnnotations(token, annotations);

        final ArgumentParser<C, ?> parser;
        if (argumentPair.getArgument().parserName().isEmpty()) {
            parser = this.manager.getParserRegistry()
                                 .createParser(token, parameters)
                                 .orElseThrow(() -> new IllegalArgumentException(
                                         String.format("Parameter '%s' in method '%s' "
                                                               + "has parser '%s' but no parser exists "
                                                               + "for that type",
                                                       parameter.getName(), method.getName(),
                                                       token.toString())));
        } else {
            parser = this.manager.getParserRegistry()
                                 .createParser(argumentPair.getArgument().parserName(), parameters)
                                 .orElseThrow(() -> new IllegalArgumentException(
                                         String.format("Parameter '%s' in method '%s' "
                                                               + "has parser '%s' but no parser exists "
                                                               + "for that type",
                                                       parameter.getName(), method.getName(),
                                                       token.toString())));
        }

        if (syntaxFragment == null || syntaxFragment.getArgumentMode() == ArgumentMode.LITERAL) {
            throw new IllegalArgumentException(String.format(
                    "Invalid command argument '%s' in method '%s': "
                            + "Missing syntax mapping", argumentPair.getArgument().value(), method.getName()));
        }
        final Argument argument = argumentPair.getArgument();
        @SuppressWarnings("ALL") final CommandArgument.Builder argumentBuilder = CommandArgument.ofType(parameter.getType(),
                                                                                                        argument.value());
        if (syntaxFragment.getArgumentMode() == ArgumentMode.OPTIONAL) {
            if (argument.defaultValue().isEmpty()) {
                argumentBuilder.asOptional();
            } else {
                argumentBuilder.asOptionalWithDefault(argument.defaultValue());
            }
        } else {
            argumentBuilder.asRequired();
        }
        return argumentBuilder.manager(this.manager).withParser(parser).build();
    }

    @Nonnull
    LinkedHashMap<String, SyntaxFragment> parseSyntax(@Nonnull final String syntax) {
        final StringTokenizer stringTokenizer = new StringTokenizer(syntax, " ");
        final LinkedHashMap<String, SyntaxFragment> map = new LinkedHashMap<>();
        while (stringTokenizer.hasMoreTokens()) {
            final String token = stringTokenizer.nextToken();
            String major;
            List<String> minor = new ArrayList<>();
            ArgumentMode mode;
            if (PATTERN_ARGUMENT_REQUIRED.test(token)) {
                major = token.substring(1, token.length() - 1);
                mode = ArgumentMode.REQUIRED;
            } else if (PATTERN_ARGUMENT_OPTIONAL.test(token)) {
                major = token.substring(1, token.length() - 1);
                mode = ArgumentMode.OPTIONAL;
            } else if (PATTERN_ARGUMENT_LITERAL.test(token)) {
                final String[] literals = token.split("\\|");
                /* Actually use the other literals as well */
                major = literals[0];
                minor.addAll(Arrays.asList(literals).subList(1, literals.length));
                mode = ArgumentMode.LITERAL;
            } else {
                throw new IllegalArgumentException(String.format("Unrecognizable syntax token '%s'", syntax));
            }
            map.put(major, new SyntaxFragment(major, minor, mode));
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


    enum ArgumentMode {
        LITERAL,
        OPTIONAL,
        REQUIRED
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

    private static final class SyntaxFragment {

        private final String major;
        private final List<String> minor;
        private final ArgumentMode argumentMode;

        private SyntaxFragment(@Nonnull final String major,
                               @Nonnull final List<String> minor,
                               @Nonnull final ArgumentMode argumentMode) {
            this.major = major;
            this.minor = minor;
            this.argumentMode = argumentMode;
        }

        @Nonnull
        private String getMajor() {
            return this.major;
        }

        @Nonnull
        private List<String> getMinor() {
            return this.minor;
        }

        @Nonnull
        private ArgumentMode getArgumentMode() {
            return this.argumentMode;
        }

    }

}
