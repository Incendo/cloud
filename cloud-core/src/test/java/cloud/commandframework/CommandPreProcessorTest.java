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

import cloud.commandframework.execution.preprocessor.CommandPreprocessingContext;
import cloud.commandframework.execution.preprocessor.CommandPreprocessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.services.types.ConsumerService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.arguments.standard.EnumParser.enumParser;
import static cloud.commandframework.util.TestUtils.createManager;

public class CommandPreProcessorTest {

    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void newTree() {
        manager = createManager();
        manager.command(manager.commandBuilder("test", CommandMeta.empty())
                .required("enum", enumParser(SampleEnum.class))
                .handler(
                        commandContext -> System.out.printf(
                                "enum = %s | integer = %d\n",
                                commandContext.<SampleEnum>optional(
                                        "enum").orElse(
                                        SampleEnum.VALUE1),
                                commandContext.<Integer>optional(
                                        "int").orElseThrow(
                                        () -> new NullPointerException(
                                                "int"))
                        ))
                .build());
        manager.registerCommandPreProcessor(new SamplePreprocessor());
    }

    @Test
    void testPreprocessing() {
        Assertions.assertEquals(10, manager.commandExecutor().executeCommand(new TestCommandSender(), "10 test value1")
                .join().commandContext().<Integer>optional("int").orElse(0));
        manager.commandExecutor().executeCommand(new TestCommandSender(), "aa test value1").join();
    }


    enum SampleEnum {
        VALUE1
    }


    static final class SamplePreprocessor implements CommandPreprocessor<TestCommandSender> {

        @Override
        public void accept(final @NonNull CommandPreprocessingContext<TestCommandSender> context) {
            try {
                final int num = context.commandInput().readInteger();
                context.commandContext().store("int", num);
            } catch (final Exception ignored) {
                /* Will prevent execution */
                ConsumerService.interrupt();
            }
        }
    }
}
