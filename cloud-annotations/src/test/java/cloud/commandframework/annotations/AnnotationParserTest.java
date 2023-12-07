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

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.annotations.specifier.Greedy;
import cloud.commandframework.annotations.specifier.Quoted;
import cloud.commandframework.annotations.specifier.Range;
import cloud.commandframework.annotations.suggestions.Suggestions;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.arguments.standard.IntegerParser;
import cloud.commandframework.arguments.standard.StringParser;
import cloud.commandframework.arguments.suggestion.Suggestion;
import cloud.commandframework.arguments.suggestion.SuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.google.common.truth.Truth.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnnotationParserTest {

    private static final List<Suggestion> NAMED_SUGGESTIONS = Arrays.asList("Dancing-Queen", "Gimme!-Gimme!-Gimme!",
            "Waterloo").stream().map(Suggestion::simple).collect(Collectors.toList());

    private CommandManager<TestCommandSender> manager;
    private AnnotationParser<TestCommandSender> annotationParser;
    private Collection<Command<TestCommandSender>> commands;

    @BeforeAll
    void setup() {
        manager = new TestCommandManager();
        annotationParser = new AnnotationParser<>(manager, TestCommandSender.class);
        manager.parserRegistry().registerNamedParserSupplier("potato", p -> new StringParser<>(StringParser.StringMode.SINGLE));
        /* Register a suggestion provider */
        manager.parserRegistry().registerSuggestionProvider(
                "some-name",
                (context, input) -> NAMED_SUGGESTIONS
        );
        /* Register a parameter injector */
        annotationParser.getParameterInjectorRegistry().registerInjector(
                InjectableValue.class,
                (context, annotations) -> new InjectableValue("Hello World!")
        );
        /* Register a builder modifier */
        annotationParser.registerBuilderModifier(
                IntegerArgumentInjector.class,
                (injector, builder) -> builder.required(injector.value(), IntegerParser.integerParser())
        );
        /* Parse the class. Required for both testMethodConstruction() and testNamedSuggestionProvider() */
        commands = new ArrayList<>();
        commands.addAll(annotationParser.parse(this));
        commands.addAll(annotationParser.parse(new ClassCommandMethod()));
    }

    @Test
    void testMethodConstruction() {
        Assertions.assertFalse(commands.isEmpty());
        manager.executeCommand(new TestCommandSender(), "test literal 10").join();
        manager.executeCommand(new TestCommandSender(), "t literal 10 o").join();
        manager.executeCommand(new TestCommandSender(), "proxycommand 10").join();
        Assertions.assertThrows(CompletionException.class, () ->
                manager.executeCommand(new TestCommandSender(), "test 101").join());
        manager.executeCommand(new TestCommandSender(), "flagcommand -p").join();
        manager.executeCommand(new TestCommandSender(), "flagcommand --print --word peanut").join();
        manager.executeCommand(new TestCommandSender(), "parserflagcommand -s \"Hello World\"").join();
        manager.executeCommand(new TestCommandSender(), "parserflagcommand -s \"Hello World\" -o This is a test").join();
        manager.executeCommand(new TestCommandSender(), "class method").join();
    }

    @Test
    void testNamedSuggestionProvider() {
        Assertions.assertEquals(
                NAMED_SUGGESTIONS,
                manager.suggestionFactory().suggestImmediately(new TestCommandSender(), "namedsuggestions ")
        );
    }

    @Test
    void testAnnotationResolver() throws Exception {
        final Class<AnnotatedClass> annotatedClass = AnnotatedClass.class;
        final Method annotatedMethod = annotatedClass.getDeclaredMethod("annotatedMethod");

        System.out.println("Looking for @CommandDescription");
        final CommandDescription commandDescription = AnnotationParser.getMethodOrClassAnnotation(
                annotatedMethod,
                CommandDescription.class
        );
        Assertions.assertNotNull(commandDescription);
        Assertions.assertEquals("Hello World!", commandDescription.value());

        System.out.println("Looking for @CommandPermission");
        final CommandPermission commandPermission = AnnotationParser.getMethodOrClassAnnotation(
                annotatedMethod,
                CommandPermission.class
        );
        Assertions.assertNotNull(commandPermission);
        Assertions.assertEquals("some.permission", commandPermission.value());

        System.out.println("Looking for @CommandMethod");
        final CommandMethod commandMethod = AnnotationParser.getMethodOrClassAnnotation(
                annotatedMethod,
                CommandMethod.class
        );
        Assertions.assertNotNull(commandMethod);
        Assertions.assertEquals("method", commandMethod.value());

        System.out.println("Looking for @Regex");
        @SuppressWarnings("unused") final Regex regex = AnnotationParser.getMethodOrClassAnnotation(annotatedMethod, Regex.class);
    }

    @Test
    void testParameterInjection() {
        manager.executeCommand(new TestCommandSender(), "injected 10").join();
    }

    @Test
    void testAnnotatedSuggestionProviders() {
        final SuggestionProvider<TestCommandSender> suggestionProvider =
                this.manager.parserRegistry().getSuggestionProvider("cows").orElse(null);
        Assertions.assertNotNull(suggestionProvider);
        Assertions.assertTrue(suggestionProvider.suggestions(new CommandContext<>(new TestCommandSender(), manager), "")
                .contains(Suggestion.simple("Stella")));
    }

    @Test
    void testAnnotatedArgumentParser() {
        final ArgumentParser<TestCommandSender, CustomType> parser = this.manager.parserRegistry().createParser(
                TypeToken.get(CustomType.class),
                ParserParameters.empty()
        ).orElseThrow(() -> new NullPointerException("Could not find CustomType parser"));
        final CommandContext<TestCommandSender> context = new CommandContext<>(
                new TestCommandSender(),
                this.manager
        );
        Assertions.assertEquals("yay", parser.parse(
                context,
                CommandInput.empty()
        ).getParsedValue().orElse(new CustomType("")).toString());
        Assertions.assertTrue(parser.suggestions(
                context,
                ""
        ).contains(Suggestion.simple("Stella")));
    }

    @Test
    @SuppressWarnings("unchecked_cast")
    void testMultiAliasedCommands() {
        final Collection<Command<TestCommandSender>> commands = annotationParser.parse(new AliasedCommands());

        // Find the root command that we are looking for.
        for (final Command<TestCommandSender> command : commands) {
            if (command.rootComponent().aliases().contains("acommand")) {
                assertThat(command.rootComponent().aliases()).containsExactly("acommand", "analias", "anotheralias");

                return;
            }
        }

        throw new IllegalStateException("Couldn't find the root command 'acommand'");
    }

    @Test
    void testInjectedCommand() {
        manager.executeCommand(new TestCommandSender(), "injected 10").join();
    }

    @Suggestions("cows")
    public List<String> cowSuggestions(final CommandContext<TestCommandSender> context, final String input) {
        return Arrays.asList("Stella", "Bella", "Agda");
    }

    @Parser(suggestions = "cows")
    public CustomType customTypeParser(final CommandContext<TestCommandSender> context, final CommandInput input) {
        return new CustomType("yay");
    }

    @IntegerArgumentInjector
    @CommandMethod("injected")
    public void injectedCommand(final CommandContext<TestCommandSender> context) {
        System.out.printf("Got an integer: %d\n", context.<Integer>get("number"));
    }

    @ProxiedBy("proxycommand")
    @CommandMethod("test|t literal <int> [string]")
    public void testCommand(
            final TestCommandSender sender,
            @Argument("int") @Range(max = "100") final int argument,
            @Argument(value = "string", defaultValue = "potato", parserName = "potato") final String string
    ) {
        System.out.printf("Received int: %d and string '%s'\n", argument, string);
    }

    @CommandMethod("flagcommand")
    public void testFlags(
            final TestCommandSender sender,
            @Flag(value = "print", aliases = "p") final boolean print,
            @Flag(value = "word", aliases = "w") final String word
    ) {
        if (print) {
            System.out.println(word);
        }
    }

    @CommandMethod("parserflagcommand")
    public void testQuotedFlags(
            final TestCommandSender sender,
            @Flag(value = "sentence", aliases = "s") @Quoted final String sentence,
            @Flag(value = "other", aliases = "o") @Greedy final String otherStuff
    ) {
        System.out.println(sentence + (otherStuff == null ? "" : " " + otherStuff));
    }

    @CommandMethod("namedsuggestions <input>")
    public void testNamedSuggestionProviders(
            @Argument(value = "input", suggestions = "some-name") final String argument
    ) {
    }

    @CommandMethod("inject")
    public void testInjectedParameters(
            final InjectableValue injectableValue
    ) {
        System.out.printf("Injected value: %s\n", injectableValue.toString());
    }

    @CommandMethod("class")
    private static class ClassCommandMethod {

        @CommandMethod("method")
        public void annotatedMethod() {
            System.out.println("kekw");
        }
    }

    @CommandPermission("some.permission")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface AnnotatedAnnotation {

    }


    @Bad1
    @CommandDescription("Hello World!")
    private static class AnnotatedClass {

        @CommandMethod("method")
        @AnnotatedAnnotation
        public static void annotatedMethod() {
        }
    }


    @Bad2
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Bad1 {

    }


    @Bad1
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Bad2 {

    }


    private static final class InjectableValue {

        private final String value;

        private InjectableValue(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }


    private static final class CustomType {

        private final String value;

        private CustomType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface IntegerArgumentInjector {

        /**
         * The name of the integer argument to insert
         *
         * @return Integer argument name
         */
        String value() default "number";
    }


    private static final class AliasedCommands {

        private static final String COMMAND_ALIASES = "acommand|analias|anotheralias";

        @CommandMethod("acommand")
        public void commandOne() {
        }

        @CommandMethod(COMMAND_ALIASES + " sub1")
        public void commandTwo() {
        }

        @CommandMethod(COMMAND_ALIASES + " sub2")
        public void commandThree() {
        }
    }
}
