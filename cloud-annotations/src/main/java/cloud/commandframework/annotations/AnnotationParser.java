//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
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

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.Description;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameter;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.arguments.preprocessor.RegexPreprocessor;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionHandler;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Parser that parses class instances {@link Command commands}
 *
 * @param <C> Command sender type
 */
public final class AnnotationParser<C> {

    private final SyntaxParser syntaxParser = new SyntaxParser();
    private final ArgumentExtractor argumentExtractor = new ArgumentExtractor();

    private final CommandManager<C> manager;
    private final Map<Class<? extends Annotation>, Function<? extends Annotation, ParserParameters>> annotationMappers;
    private final Map<Class<? extends Annotation>, Function<? extends Annotation, BiFunction<@NonNull CommandContext<C>,
            @NonNull Queue<@NonNull String>, @NonNull ArgumentParseResult<Boolean>>>> preprocessorMappers;
    private final Class<C> commandSenderClass;
    private final MetaFactory metaFactory;
    private final FlagExtractor flagExtractor;

    /**
     * Construct a new annotation parser
     *
     * @param manager            Command manager instance
     * @param commandSenderClass Command sender class
     * @param metaMapper         Function that is used to create {@link CommandMeta} instances from annotations on the
     *                           command methods. These annotations will be mapped to
     *                           {@link ParserParameter}. Mappers for the
     *                           parser parameters can be registered using {@link #registerAnnotationMapper(Class, Function)}
     */
    public AnnotationParser(
            final @NonNull CommandManager<C> manager,
            final @NonNull Class<C> commandSenderClass,
            final @NonNull Function<@NonNull ParserParameters, @NonNull CommandMeta> metaMapper
    ) {
        this.commandSenderClass = commandSenderClass;
        this.manager = manager;
        this.metaFactory = new MetaFactory(this, metaMapper);
        this.annotationMappers = new HashMap<>();
        this.preprocessorMappers = new HashMap<>();
        this.flagExtractor = new FlagExtractor(manager);
        this.registerAnnotationMapper(CommandDescription.class, d ->
                ParserParameters.single(StandardParameters.DESCRIPTION, d.value()));
        this.registerPreprocessorMapper(Regex.class, annotation -> RegexPreprocessor.of(
                annotation.value(),
                Caption.of(annotation.failureCaption())
        ));
    }

    @SuppressWarnings("unchecked")
    static <A extends Annotation> @Nullable A getAnnotationRecursively(
            final @NonNull Annotation[] annotations,
            final @NonNull Class<A> clazz,
            final @NonNull Set<Class<? extends Annotation>> checkedAnnotations
    ) {
        A innerCandidate = null;
        for (final Annotation annotation : annotations) {
            if (checkedAnnotations.contains(annotation.annotationType())) {
                continue;
            } else {
                checkedAnnotations.add(annotation.annotationType());
            }
            if (annotation.annotationType().equals(clazz)) {
                return (A) annotation;
            }
            if (annotation.annotationType().getPackage().getName().startsWith("java.lang")) {
                continue;
            }
            final A inner = getAnnotationRecursively(annotation.annotationType().getAnnotations(), clazz, checkedAnnotations);
            if (inner != null) {
                innerCandidate = inner;
            }
        }
        return innerCandidate;
    }

    static <A extends Annotation> @Nullable A getMethodOrClassAnnotation(
            final @NonNull Method method,
            final @NonNull Class<A> clazz
    ) {
        A annotation = getAnnotationRecursively(method.getAnnotations(), clazz, new HashSet<>());
        if (annotation == null) {
            annotation = getAnnotationRecursively(method.getDeclaringClass().getAnnotations(), clazz, new HashSet<>());
        }
        return annotation;
    }

    static <A extends Annotation> boolean methodOrClassHasAnnotation(
            final @NonNull Method method,
            final @NonNull Class<A> clazz
    ) {
        return getMethodOrClassAnnotation(method, clazz) != null;
    }

    /**
     * Register an annotation mapper
     *
     * @param annotation Annotation class
     * @param mapper     Mapping function
     * @param <A>        Annotation type
     */
    public <A extends Annotation> void registerAnnotationMapper(
            final @NonNull Class<A> annotation,
            final @NonNull Function<@NonNull A,
                    @NonNull ParserParameters> mapper
    ) {
        this.annotationMappers.put(annotation, mapper);
    }

    /**
     * Register a preprocessor mapper
     *
     * @param annotation         Annotation class
     * @param preprocessorMapper Preprocessor mapper
     * @param <A>                Annotation type
     */
    public <A extends Annotation> void registerPreprocessorMapper(
            final @NonNull Class<A> annotation,
            final @NonNull Function<A, BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                    @NonNull ArgumentParseResult<Boolean>>> preprocessorMapper
    ) {
        this.preprocessorMappers.put(annotation, preprocessorMapper);
    }

    /**
     * Scan a class instance of {@link CommandMethod} annotations and attempt to
     * compile them into {@link Command} instances
     *
     * @param instance Instance to scan
     * @param <T>      Type of the instance
     * @return Collection of parsed annotations
     */
    @SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
    public <T> @NonNull Collection<@NonNull Command<C>> parse(final @NonNull T instance) {
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
                throw new IllegalArgumentException(String.format(
                        "@CommandMethod annotated method '%s' has non-void return type",
                        method.getName()
                ));
            }
            commandMethodPairs.add(new CommandMethodPair(method, commandMethod));
        }
        final Collection<Command<C>> commands = this.construct(instance, commandMethodPairs);
        for (final Command<C> command : commands) {
            ((CommandManager) this.manager).command(command);
        }
        return commands;
    }

    @SuppressWarnings("unchecked")
    private @NonNull Collection<@NonNull Command<C>> construct(
            final @NonNull Object instance,
            final @NonNull Collection<@NonNull CommandMethodPair> methodPairs
    ) {
        final Collection<Command<C>> commands = new ArrayList<>();
        for (final CommandMethodPair commandMethodPair : methodPairs) {
            final CommandMethod commandMethod = commandMethodPair.getCommandMethod();
            final Method method = commandMethodPair.getMethod();
            final LinkedHashMap<String, SyntaxFragment> tokens = this.syntaxParser.apply(commandMethod.value());
            /* Determine command name */
            final String commandToken = commandMethod.value().split(" ")[0].split("\\|")[0];
            @SuppressWarnings("rawtypes") final CommandManager manager = this.manager;
            final SimpleCommandMeta.Builder metaBuilder = SimpleCommandMeta.builder()
                    .with(this.metaFactory.apply(method));
            if (methodOrClassHasAnnotation(method, Confirmation.class)) {
                metaBuilder.with(CommandConfirmationManager.CONFIRMATION_REQUIRED_META, "true");
            }

            @SuppressWarnings("rawtypes")
            Command.Builder builder = manager.commandBuilder(
                    commandToken,
                    tokens.get(commandToken).getMinor(),
                    metaBuilder.build()
            );
            final Collection<ArgumentParameterPair> arguments = this.argumentExtractor.apply(method);
            final Collection<CommandFlag<?>> flags = this.flagExtractor.apply(method);
            final Map<String, CommandArgument<C, ?>> commandArguments = new HashMap<>();
            final Map<CommandArgument<C, ?>, String> argumentDescriptions = new HashMap<>();
            /* Go through all annotated parameters and build up the argument tree */
            for (final ArgumentParameterPair argumentPair : arguments) {
                final CommandArgument<C, ?> argument = this.buildArgument(
                        method,
                        tokens.get(argumentPair.getArgument().value()),
                        argumentPair
                );
                commandArguments.put(argument.getName(), argument);
                argumentDescriptions.put(argument, argumentPair.getArgument().description());
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

                    final String description = argumentDescriptions.getOrDefault(argument, "");
                    builder = builder.argument(argument, Description.of(description));
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

            final CommandPermission commandPermission = getMethodOrClassAnnotation(method, CommandPermission.class);
            if (commandPermission != null) {
                builder = builder.permission(commandPermission.value());
            }

            if (commandMethod.requiredSender() != Object.class) {
                builder = builder.senderType(commandMethod.requiredSender());
            } else if (senderType != null) {
                builder = builder.senderType(senderType);
            }
            try {
                /* Construct the handler */
                final CommandExecutionHandler<C> commandExecutionHandler
                        = new MethodCommandExecutionHandler<>(instance, commandArguments, method);
                builder = builder.handler(commandExecutionHandler);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to construct command execution handler", e);
            }
            /* Check if the command should be hidden */
            if (methodOrClassHasAnnotation(method, Hidden.class)) {
                builder = builder.hidden();
            }
            /* Apply flags */
            for (final CommandFlag<?> flag : flags) {
                builder = builder.flag(flag);
            }
            /* Construct and register the command */
            final Command<C> builtCommand = builder.build();
            commands.add(builtCommand);
            /* Check if we need to construct a proxy */
            if (method.isAnnotationPresent(ProxiedBy.class)) {
                final ProxiedBy proxyAnnotation = method.getAnnotation(ProxiedBy.class);
                final String proxy = proxyAnnotation.value();
                if (proxy.contains(" ")) {
                    throw new IllegalArgumentException("@ProxiedBy proxies may only contain single literals");
                }
                Command.Builder<C> proxyBuilder = manager.commandBuilder(proxy, builtCommand.getCommandMeta())
                        .proxies(builtCommand);
                if (proxyAnnotation.hidden()) {
                    proxyBuilder = proxyBuilder.hidden();
                }
                manager.command(proxyBuilder.build());
            }
        }
        return commands;
    }

    @SuppressWarnings("unchecked")
    private @NonNull CommandArgument<C, ?> buildArgument(
            final @NonNull Method method,
            final @Nullable SyntaxFragment syntaxFragment,
            final @NonNull ArgumentParameterPair argumentPair
    ) {
        final Parameter parameter = argumentPair.getParameter();
        final Collection<Annotation> annotations = Arrays.asList(parameter.getAnnotations());
        final TypeToken<?> token = TypeToken.get(parameter.getParameterizedType());
        final ParserParameters parameters = this.manager.getParserRegistry()
                .parseAnnotations(token, annotations);
        /* Create the argument parser */
        final ArgumentParser<C, ?> parser;
        if (argumentPair.getArgument().parserName().isEmpty()) {
            parser = this.manager.getParserRegistry()
                    .createParser(token, parameters)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Parameter '%s' in method '%s' "
                                            + "has parser '%s' but no parser exists "
                                            + "for that type",
                                    parameter.getName(), method.getName(),
                                    token.toString()
                            )));
        } else {
            parser = this.manager.getParserRegistry()
                    .createParser(argumentPair.getArgument().parserName(), parameters)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Parameter '%s' in method '%s' "
                                            + "has parser '%s' but no parser exists "
                                            + "for that type",
                                    parameter.getName(), method.getName(),
                                    token.toString()
                            )));
        }
        /* Check whether or not the corresponding method parameter actually exists */
        if (syntaxFragment == null || syntaxFragment.getArgumentMode() == ArgumentMode.LITERAL) {
            throw new IllegalArgumentException(String.format(
                    "Invalid command argument '%s' in method '%s': "
                            + "Missing syntax mapping", argumentPair.getArgument().value(), method.getName()));
        }
        final Argument argument = argumentPair.getArgument();
        /* Create the argument builder */
        @SuppressWarnings("rawtypes") final CommandArgument.Builder argumentBuilder = CommandArgument.ofType(
                parameter.getType(),
                argument.value()
        );
        /* Set the argument requirement status */
        if (syntaxFragment.getArgumentMode() == ArgumentMode.OPTIONAL) {
            if (argument.defaultValue().isEmpty()) {
                argumentBuilder.asOptional();
            } else {
                argumentBuilder.asOptionalWithDefault(argument.defaultValue());
            }
        } else {
            argumentBuilder.asRequired();
        }
        /* Check whether or not a suggestion provider should be set */
        if (!argument.suggestions().isEmpty()) {
            final String suggestionProviderName = argument.suggestions();
            final Optional<BiFunction<CommandContext<C>, String, List<String>>> suggestionsFunction =
                    this.manager.getParserRegistry().getSuggestionProvider(suggestionProviderName);
            argumentBuilder.withSuggestionsProvider(
                    suggestionsFunction.orElseThrow(() ->
                    new IllegalArgumentException(String.format(
                            "There is no suggestion provider with name '%s'. Did you forget to register it?",
                            suggestionProviderName
                    )))
            );
        }
        /* Build the argument */
        final CommandArgument<C, ?> builtArgument = argumentBuilder.manager(this.manager).withParser(parser).build();
        /* Add preprocessors */
        for (final Annotation annotation : annotations) {
            @SuppressWarnings("rawtypes") final Function preprocessorMapper =
                    this.preprocessorMappers.get(annotation.annotationType());
            if (preprocessorMapper != null) {
                final BiFunction<@NonNull CommandContext<C>, @NonNull Queue<@NonNull String>,
                        @NonNull ArgumentParseResult<Boolean>> preprocessor = (BiFunction<CommandContext<C>,
                        Queue<String>, ArgumentParseResult<Boolean>>) preprocessorMapper.apply(annotation);
                builtArgument.addPreprocessor(preprocessor);
            }
        }
        /* Yay, we're done */
        return builtArgument;
    }

    @NonNull Map<@NonNull Class<@NonNull ? extends Annotation>,
            @NonNull Function<@NonNull ? extends Annotation, @NonNull ParserParameters>> getAnnotationMappers() {
        return this.annotationMappers;
    }

}
