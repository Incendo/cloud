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

import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.services.types.ConsumerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommandPostProcessorTest {

    private static final boolean[] state = new boolean[]{false};
    private static CommandManager<TestCommandSender> manager;

    @BeforeAll
    static void newTree() {
        manager = new TestCommandManager();
        manager.command(manager.commandBuilder("test", SimpleCommandMeta.empty())
                .handler(c -> state[0] = true)
                .build());
        manager.registerCommandPostProcessor(new SamplePostprocessor());
    }

    @Test
    void testPreprocessing() {
        manager.executeCommand(new TestCommandSender(), "test").join();
        Assertions.assertEquals(false, state[0]);
    }

    static final class SamplePostprocessor implements CommandPostprocessor<TestCommandSender> {

        @Override
        public void accept(final CommandPostprocessingContext<TestCommandSender> context) {
            ConsumerService.interrupt();
        }

    }

}
