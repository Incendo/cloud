//
// MIT License
//
// Copyright (c) 2021 Alexander Söderberg & Contributors
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
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        this.commandManager = new TestCommandManager();
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
        Assertions.assertEquals(INJECTED_INTEGER, parameterInjectorRegistry.getInjectable(
                Integer.class,
                this.createContext(),
                AnnotationAccessor.empty()
        ).orElse(-1));
    }

    @Test
    void testGuiceInjection() {
        this.parameterInjectorRegistry.registerInjectionService(GuiceInjectionService.create(this.injector));
        Assertions.assertEquals(TestModule.INJECTED_INTEGER, parameterInjectorRegistry.getInjectable(
                Integer.class,
                this.createContext(),
                AnnotationAccessor.empty()
        ).orElse(-1));
    }

    @Test
    void testNonExistentInjection() {
        Assertions.assertNull(parameterInjectorRegistry.getInjectable(
                String.class,
                this.createContext(),
                AnnotationAccessor.empty()
        ).orElse(null));
    }

    private static final class TestModule extends AbstractModule {

        private static final int INJECTED_INTEGER = 10;

        @Override
        protected void configure() {
            bind(Integer.class).toInstance(INJECTED_INTEGER);
        }

    }

}
