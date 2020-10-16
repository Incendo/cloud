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
import com.google.inject.Key;
import com.google.inject.util.Types;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.service.permission.Subject;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Injection module that allows for {@link SpongeCommandManager} to be injectable
 *
 * @param <C> Command sender type
 */
public final class CloudInjectionModule<C> extends AbstractModule {

    private final Class<C> commandSenderType;
    private final Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator;
    private final Function<@NonNull C, @NonNull Subject> subjectMapper;
    private final Function<@NonNull CommandCause, @NonNull C> backwardsSubjectMapper;

    /**
     * Create a new child injection module
     *
     * @param commandSenderType            Your command sender type
     * @param commandExecutionCoordinator  Command execution coordinator
     * @param subjectMapper                Mapper to command source from the custom command sender type
     * @param backwardsSubjectMapper       Mapper from the custom command sender type to a velocity command source
     */
    public CloudInjectionModule(
            final @NonNull Class<C> commandSenderType,
            final @NonNull Function<@NonNull CommandTree<C>, @NonNull CommandExecutionCoordinator<C>> commandExecutionCoordinator,
            final @NonNull Function<@NonNull C, @NonNull Subject> subjectMapper,
            final @NonNull Function<@NonNull CommandCause, @NonNull C> backwardsSubjectMapper
    ) {
        this.commandSenderType = commandSenderType;
        this.commandExecutionCoordinator = commandExecutionCoordinator;
        this.subjectMapper = subjectMapper;
        this.backwardsSubjectMapper = backwardsSubjectMapper;
    }

    @Override
    protected void configure() {
        final Type commandTreeType = Types.newParameterizedType(CommandTree.class, this.commandSenderType);
        final Type commandExecutionCoordinatorType = Types.newParameterizedType(CommandExecutionCoordinator.class,
                this.commandSenderType);
        final Type executorFunction = Types.newParameterizedType(Function.class, commandTreeType,
                commandExecutionCoordinatorType);
        final Key executorFunctionKey = Key.get(executorFunction);
        this.bind(executorFunctionKey).toInstance(this.commandExecutionCoordinator);
        final Type commandSenderMapperFunction = Types.newParameterizedType(Function.class,
                this.commandSenderType, Subject.class);
        final Key commandSenderMapperFunctionKey = Key.get(commandSenderMapperFunction);
        this.bind(commandSenderMapperFunctionKey).toInstance(this.subjectMapper);
        final Type backwardsCommandSenderMapperFunction = Types.newParameterizedType(Function.class, CommandCause.class,
                this.commandSenderType);
        final Key backwardsCommandSenderMapperFunctionKey = Key.get(backwardsCommandSenderMapperFunction);
        this.bind(backwardsCommandSenderMapperFunctionKey).toInstance(this.backwardsSubjectMapper);
    }

}
