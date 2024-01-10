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
package cloud.commandframework.fabric;

import cloud.commandframework.exceptions.handling.ExceptionContext;
import cloud.commandframework.exceptions.handling.ExceptionHandler;
import net.minecraft.commands.SharedSuggestionProvider;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, since = "2.0.0")
final class FabricExceptionHandler<C, S extends SharedSuggestionProvider, T extends Throwable> implements ExceptionHandler<C, T> {

    private final FabricCommandManager<C, S> fabricCommandManager;
    private final ExceptionConsumer<C, S, T> consumer;

    FabricExceptionHandler(
            final @NonNull FabricCommandManager<C, S> fabricCommandManager,
            final @NonNull ExceptionConsumer<C, S, T> consumer
    ) {
        this.fabricCommandManager = fabricCommandManager;
        this.consumer = consumer;
    }

    @Override
    public void handle(final @NonNull ExceptionContext<C, T> context) throws Throwable {
        this.consumer.handle(
                this.fabricCommandManager.senderMapper().reverse(context.context().sender()),
                context.context().sender(),
                context.exception()
        );
    }

    interface ExceptionConsumer<C, S, T extends Throwable> {

        void handle(@NonNull S source, @NonNull C sender, @NonNull T exception);
    }
}
