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
package cloud.commandframework.bean;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.TestCommandSender;
import cloud.commandframework.bean.CommandBean;
import cloud.commandframework.bean.CommandProperties;
import cloud.commandframework.component.CommandComponent;
import cloud.commandframework.internal.CommandNode;
import cloud.commandframework.key.CloudKey;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.parser.standard.IntegerParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cloud.commandframework.util.TestUtils.createManager;
import static com.google.common.truth.Truth.assertThat;

class CommandBeanTest {

    private static final CloudKey<String> META_KEY = CloudKey.of("key", String.class);

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

        final CommandComponent<TestCommandSender> component = node.children().get(0).component();
        assertThat(component).isNotNull();

        final Command<TestCommandSender> command = node.children().get(0).command();
        assertThat(command).isNotNull();
        assertThat(command.nonFlagArguments().get(0).aliases()).containsExactly("t", "test");
        assertThat(command.commandMeta().getOrDefault(META_KEY, "otherValue")).isEqualTo("value");
        assertThat(command.commandExecutionHandler()).isEqualTo(testCommandBean);
    }


    public static final class TestCommandBean extends CommandBean<TestCommandSender> {

        @Override
        protected @NonNull CommandProperties properties() {
            return CommandProperties.of("test", "t");
        }

        @Override
        protected @NonNull CommandMeta meta() {
            return CommandMeta.builder().with(META_KEY, "value").build();
        }

        @Override
        protected Command.@NonNull Builder<? extends TestCommandSender> configure(
                final Command.@NonNull Builder<TestCommandSender> builder
        ) {
            return builder.senderType(ChildTestCommandSender.class)
                    .required("argument", IntegerParser.integerParser());
        }
    }


    public static final class ChildTestCommandSender extends TestCommandSender {

    }
}
