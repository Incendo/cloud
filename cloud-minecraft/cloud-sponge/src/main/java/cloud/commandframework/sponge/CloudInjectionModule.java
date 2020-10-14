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
package cloud.commandframework.sponge;

import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * Guice injection module that allows you to inject into {@link SpongeCommandManager}
 *
 * @param <C> Command sender type
 */
public final class CloudInjectionModule<C> extends AbstractModule {

    private final Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinatorFunction;

    /**
     * Create a new injection module
     *
     * @param coordinatorFunction Execution coordinator instance factory
     */
    public CloudInjectionModule(
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> coordinatorFunction
    ) {
        this.commandExecutionCoordinatorFunction = coordinatorFunction;
    }

    @Override
    protected void configure() {
        this.bind(new TypeLiteral<Function<CommandTree<C>, CommandExecutionCoordinator<C>>>(){})
                .toInstance(this.commandExecutionCoordinatorFunction);
    }

}
