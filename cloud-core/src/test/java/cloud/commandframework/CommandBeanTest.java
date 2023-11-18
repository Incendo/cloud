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

import cloud.commandframework.arguments.standard.IntegerParser;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.meta.CommandMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class CommandBeanTest {

    private static final CommandMeta.Key<String> META_KEY = CommandMeta.Key.of(String.class, "key");

    private CommandManager<TestCommandSender> commandManager;

    @BeforeEach
    void setup() {
        this.commandManager = createManager();
    }

    @Test
    void testCommandBeanRegistration() {
        // Arrange
        final TestCommandBean testCommandBean = new TestCommandBean();

        // Act
        this.commandManager.command(testCommandBean);

        // Assert
        final CommandNode<TestCommandSender> node = this.commandManager.commandTree().getNamedNode("test");
        assertThat(node).isNotNull();

        final CommandComponent<TestCommandSender> component = node.component();
        assertThat(component).isNotNull();

        final Command<TestCommandSender> command = component.owningCommand();
        assertThat(command).isNotNull();
        assertThat(command.nonFlagArguments().get(0).aliases()).containsExactly("t", "test");
        assertThat(command.getCommandMeta().getOrDefault(META_KEY, "otherValue")).isEqualTo("value");
        assertThat(command.getCommandExecutionHandler()).isEqualTo(testCommandBean);
    }

    public static class TestCommandBean extends CommandBean<TestCommandSender> {

        @Override
        protected @NonNull CommandProperties properties() {
            return CommandProperties.of("test", "t");
        }

        @Override
        protected @NonNull CommandMeta meta() {
            return CommandMeta.simple().with(META_KEY, "value").build();
        }

        @Override
        protected void configure(final Command.@NonNull Builder<TestCommandSender> builder) {
            builder.required("argument", IntegerParser.integer());
        }
    }
}
