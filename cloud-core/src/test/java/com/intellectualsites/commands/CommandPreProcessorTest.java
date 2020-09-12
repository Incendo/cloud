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
package com.intellectualsites.commands;

import com.intellectualsites.commands.components.standard.EnumComponent;
import com.intellectualsites.commands.execution.preprocessor.CommandPreprocessingContext;
import com.intellectualsites.commands.execution.preprocessor.CommandPreprocessor;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import com.intellectualsites.commands.sender.CommandSender;
import com.intellectualsites.services.types.ConsumerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

public class CommandPreProcessorTest {

    private static CommandManager<CommandSender, SimpleCommandMeta> manager;

    @BeforeAll
    static void newTree() {
        manager = new TestCommandManager();
        manager.registerCommand(manager.commandBuilder("test", SimpleCommandMeta.empty())
                                       .withComponent(EnumComponent.required(SampleEnum.class, "enum"))
                                       .withHandler(
                                               commandContext -> System.out.printf("enum = %s | integer = %d\n",
                                                                                   commandContext.<SampleEnum>get(
                                                                                           "enum").orElse(
                                                                                           SampleEnum.VALUE1),
                                                                                   commandContext.<Integer>get(
                                                                                           "int").orElseThrow(
                                                                                           () -> new NullPointerException(
                                                                                                   "int"))))
                                       .build());
        manager.registerCommandPreProcessor(new SamplePreprocessor());
    }

    @Test
    void testPreprocessing() {
        Assertions.assertEquals(10, manager.executeCommand(new TestCommandSender(), "10 test value1")
                                           .join().getCommandContext().<Integer>get("int").orElse(0));
        manager.executeCommand(new TestCommandSender(), "aa test value1").join();
    }


    enum SampleEnum {
        VALUE1
    }


    static final class SamplePreprocessor implements CommandPreprocessor<CommandSender> {

        @Override
        public void accept(@Nonnull final CommandPreprocessingContext<CommandSender> context) {
            try {
                final int num = Integer.parseInt(context.getInputQueue().removeFirst());
                context.getCommandContext().store("int", num);
            } catch (final Exception ignored) {
                /* Will prevent execution */
                ConsumerService.interrupt();
            }
        }

    }

}
