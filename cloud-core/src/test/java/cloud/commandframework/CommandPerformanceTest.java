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

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;

final class CommandPerformanceTest {

    private static CommandManager<TestCommandSender> manager;
    private static String literalChain;

    @BeforeAll
    static void setup() {
        manager = new TestCommandManager();

        final StringBuilder literalBuilder = new StringBuilder("literals");

        /* Create 100 literals */
        Command.Builder<TestCommandSender> builder = manager.commandBuilder("literals");
        for (int i = 1; i < 101; i++) {
            final String literal = Integer.toString(i);
            builder = builder.literal(literal);
            literalBuilder.append(' ').append(literal);
        }
        manager.command(builder.build());
        literalChain = literalBuilder.toString();

    }

    @Test
    void testLiterals() {
        final CommandResult<TestCommandSender> result = manager.executeCommand(new TestCommandSender(), literalChain).join();

        long elapsedTime = 0L;
        int amount = 0;
        for (int i = 0; i < 100000; i++) {
            for (final CommandContext.ArgumentTiming argumentTiming : result.getCommandContext().getArgumentTimings().values()) {
                elapsedTime += argumentTiming.getElapsedTime();
                amount += 1;
            }
        }
        double averageTime = elapsedTime / (double) amount;

        System.out.printf("Average literal parse time: %fns (%f ms) | %d samples & %d iterations\n",
                averageTime, averageTime / 10e6, 101, 100000
        );
    }

    @Test
    void testCompleteExecution() throws Exception {
        if (System.getProperty("verboseBenchmarks", "false").equalsIgnoreCase("false")) {
            return;
        }
        final Options options = new OptionsBuilder()
                .include(ExecutionBenchmark.class.getSimpleName())
                .build();
        final Collection<RunResult> results = new Runner(options).run();
        Assertions.assertFalse(results.isEmpty());
    }

}
