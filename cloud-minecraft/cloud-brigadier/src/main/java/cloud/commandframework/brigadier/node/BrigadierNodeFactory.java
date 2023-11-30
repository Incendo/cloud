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
package cloud.commandframework.brigadier.node;

import cloud.commandframework.brigadier.permission.BrigadierPermissionChecker;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = API.Status.INTERNAL, consumers = "cloud.commandframework.brigadier.*")
public interface BrigadierNodeFactory<C, S, N extends CommandNode<S>> {

    /**
     * Creates a Brigadier command node.
     *
     * @param cloudCommand       the cloud command to create the node from
     * @param root               the Brigadier root node to attach the created node to
     * @param suggestionProvider the Brigadier suggestion provider
     * @param executor           the Brigadier command execution handler
     * @param permissionChecker  function that determines whether a sender has access to the command
     * @return the created command node
     */
    @NonNull N createNode(
            cloud.commandframework.internal.@NonNull CommandNode<C> cloudCommand,
            @NonNull LiteralCommandNode<S> root,
            @NonNull SuggestionProvider<S> suggestionProvider,
            @NonNull Command<S> executor,
            @NonNull BrigadierPermissionChecker<S> permissionChecker
    );

    /**
     * Creates a Brigadier command node.
     *
     * @param label             the command label
     * @param cloudCommand      the cloud command
     * @param permissionChecker function that determines whether a sender has access to the command
     * @param forceRegister     whether to force-register an executor at every node
     * @param executor          the Brigadier command execution handler
     * @return the created command node
     */
    @NonNull N createNode(
            @NonNull String label,
            cloud.commandframework.@NonNull Command<C> cloudCommand,
            @NonNull BrigadierPermissionChecker<S> permissionChecker,
            boolean forceRegister,
            @NonNull Command<S> executor
    );
}
