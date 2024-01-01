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
package cloud.commandframework.velocity;

import cloud.commandframework.SenderMapper;
import cloud.commandframework.execution.ExecutionCoordinator;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.util.Types;
import com.velocitypowered.api.command.CommandSource;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Injection module that allows for {@link VelocityCommandManager} to be injectable
 *
 * @param <C> Command sender type
 * @since 1.1.0
 */
public final class CloudInjectionModule<C> extends AbstractModule {

    private final Class<C> commandSenderType;
    private final @NonNull ExecutionCoordinator<C> executionCoordinator;
    private final SenderMapper<CommandSource, C> senderMapper;

    /**
     * Create a new child injection module
     *
     * @param commandSenderType            Your command sender type
     * @param executionCoordinator         Command execution coordinator
     * @param senderMapper                 Mapper from command source to the custom command sender type
     */
    public CloudInjectionModule(
            final @NonNull Class<C> commandSenderType,
            final @NonNull ExecutionCoordinator<C> executionCoordinator,
            final @NonNull SenderMapper<CommandSource, C> senderMapper
    ) {
        this.commandSenderType = commandSenderType;
        this.executionCoordinator = executionCoordinator;
        this.senderMapper = senderMapper;
    }

    /**
     * Create a new child injection module using Velocity's {@link CommandSource} as the sender type.
     *
     * @param commandExecutionCoordinator Command execution coordinator
     * @return new injection module
     * @since 1.5.0
     */
    public static @NonNull CloudInjectionModule<@NonNull CommandSource> createNative(
            final @NonNull ExecutionCoordinator<CommandSource> commandExecutionCoordinator
    ) {
        return new CloudInjectionModule<>(
                CommandSource.class,
                commandExecutionCoordinator,
                SenderMapper.identity()
        );
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void configure() {
        final Type commandExecutionCoordinatorType = Types.newParameterizedType(
                ExecutionCoordinator.class,
                this.commandSenderType
        );
        final Key executorFunctionKey = Key.get(commandExecutionCoordinatorType);
        this.bind(executorFunctionKey).toInstance(this.executionCoordinator);
        final Type commandSenderMapperFunction = Types.newParameterizedType(SenderMapper.class, CommandSource.class,
                this.commandSenderType
        );
        final Key commandSenderMapperFunctionKey = Key.get(commandSenderMapperFunction);
        this.bind(commandSenderMapperFunctionKey).toInstance(this.senderMapper);
    }
}
