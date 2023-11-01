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
package cloud.commandframework;

import cloud.commandframework.annotations.AnnotationAccessor;
import cloud.commandframework.annotations.injection.GuiceInjectionService;
import cloud.commandframework.annotations.injection.ParameterInjectorRegistry;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandContextFactory;
import cloud.commandframework.context.StandardCommandContextFactory;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth8.assertThat;

public class ParameterInjectorRegistryTest {

    private static final int INJECTED_INTEGER = 5;

    private ParameterInjectorRegistry<TestCommandSender> parameterInjectorRegistry;
    private TestCommandSender commandSender;
    private CommandContextFactory<TestCommandSender> commandContextFactory;
    private CommandManager<TestCommandSender> commandManager;
    private Injector injector;

    @BeforeEach
    void setup() {
        this.commandSender = new TestCommandSender();
        this.commandManager = createManager();
        this.commandContextFactory = new StandardCommandContextFactory<>();
        this.parameterInjectorRegistry = new ParameterInjectorRegistry<>();
        this.parameterInjectorRegistry.registerInjector(Integer.class, (context, annotationAccessor) -> INJECTED_INTEGER);
        this.commandSender = new TestCommandSender();
        this.injector = Guice.createInjector(new TestModule());
    }

    private @NonNull CommandContext<TestCommandSender> createContext() {
        return this.commandContextFactory.create(false, this.commandSender, this.commandManager);
    }

    @Test
    void testSimpleInjection() {
        assertThat(
                parameterInjectorRegistry.getInjectable(
                        Integer.class,
                        this.createContext(),
                        AnnotationAccessor.empty()
                )
        ).hasValue(INJECTED_INTEGER);
    }

    @Test
    void testGuiceInjection() throws NoSuchMethodException {
        this.parameterInjectorRegistry.registerInjectionService(GuiceInjectionService.create(this.injector));

        assertThat(
                parameterInjectorRegistry.getInjectable(
                        Integer.class,
                        this.createContext(),
                        AnnotationAccessor.empty()
                )
        ).hasValue(TestModule.INJECTED_INTEGER);

        final Method testAnnotatedMethod = this.getClass().getDeclaredMethod("testAnnotatedMethod", Integer.class);
        assertThat(
                parameterInjectorRegistry.getInjectable(
                        Integer.class,
                        this.createContext(),
                        AnnotationAccessor.of(testAnnotatedMethod.getParameters()[0])
                )
        ).hasValue(TestModule.ANNOTATED_INTEGER);
    }

    @SuppressWarnings("unused")
    private static void testAnnotatedMethod(@TestAnnotation final Integer ignored) {}

    @Test
    void testNonExistentInjection() {
        assertThat(
                parameterInjectorRegistry.getInjectable(
                        String.class,
                        this.createContext(),
                        AnnotationAccessor.empty()
                )
        ).isEmpty();
    }

    private static final class TestModule extends AbstractModule {

        private static final int INJECTED_INTEGER = 10;

        private static final int ANNOTATED_INTEGER = 17;

        @Override
        protected void configure() {
            bind(Integer.class).toInstance(INJECTED_INTEGER);
            bind(Integer.class).annotatedWith(TestAnnotation.class).toInstance(ANNOTATED_INTEGER);
        }
    }

    @BindingAnnotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
    public @interface TestAnnotation {}
}
