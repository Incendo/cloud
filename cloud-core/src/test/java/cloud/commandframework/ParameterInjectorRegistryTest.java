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
package cloud.commandframework;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandContextFactory;
import cloud.commandframework.context.StandardCommandContextFactory;
import cloud.commandframework.injection.GuiceInjectionService;
import cloud.commandframework.injection.ParameterInjectorRegistry;
import cloud.commandframework.util.annotation.AnnotationAccessor;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class ParameterInjectorRegistryTest {

    private ParameterInjectorRegistry<TestCommandSender> parameterInjectorRegistry;
    private CommandContextFactory<TestCommandSender> commandContextFactory;

    @BeforeEach
    void setup() {
        this.commandContextFactory = new StandardCommandContextFactory<>(createManager());
        this.parameterInjectorRegistry = new ParameterInjectorRegistry<>();
    }

    private @NonNull CommandContext<TestCommandSender> createContext() {
        return this.commandContextFactory.create(false, new TestCommandSender());
    }

    @Test
    void testSimpleInjection() {
        // Arrange
        this.parameterInjectorRegistry.registerInjector(Integer.class, (context, annotationAccessor) -> 5);

        // Act
        final Optional<Integer> result = this.parameterInjectorRegistry.getInjectable(
                Integer.class,
                this.createContext(),
                AnnotationAccessor.empty()
        );

        // Assert
        assertThat(result).hasValue(5);
    }

    @Test
    void testGuiceInjection() {
        // Arrange
        final Injector injector = Guice.createInjector(new TestModule());
        this.parameterInjectorRegistry.registerInjectionService(GuiceInjectionService.create(injector));

        // Act
        final Optional<Integer> result = this.parameterInjectorRegistry.getInjectable(
                Integer.class,
                this.createContext(),
                AnnotationAccessor.empty()
        );

        // Assert
        assertThat(result).hasValue(TestModule.INJECTED_INTEGER);
    }

    @Test
    void testAnnotatedGuiceInjection() throws NoSuchMethodException {
        // Arrange
        final Injector injector = Guice.createInjector(new TestModule());
        this.parameterInjectorRegistry.registerInjectionService(GuiceInjectionService.create(injector));
        final Method testAnnotatedMethod = this.getClass().getDeclaredMethod("testAnnotatedMethod", Integer.class);

        // Act
        final Optional<Integer> result = this.parameterInjectorRegistry.getInjectable(
                Integer.class,
                this.createContext(),
                AnnotationAccessor.of(testAnnotatedMethod.getParameters()[0])
        );

        // Assert
        assertThat(result).hasValue(TestModule.ANNOTATED_INTEGER);
    }

    @Test
    void testNonExistentInjection() {
        // Act
        final Optional<String> result = this.parameterInjectorRegistry.getInjectable(
                String.class,
                this.createContext(),
                AnnotationAccessor.empty()
        );

        // Assert
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unused")
    private static void testAnnotatedMethod(@TestAnnotation final Integer ignored) {}


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
    @interface TestAnnotation {}
}
