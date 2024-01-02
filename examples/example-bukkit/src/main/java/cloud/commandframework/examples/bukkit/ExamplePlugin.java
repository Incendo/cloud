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
package cloud.commandframework.examples.bukkit;

import cloud.commandframework.SenderMapper;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.examples.bukkit.annotations.AnnotationParserExample;
import cloud.commandframework.examples.bukkit.builder.BuilderExample;
import cloud.commandframework.execution.ExecutionCoordinator;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import static net.kyori.adventure.text.Component.text;

/**
 * Example plugin class
 */
@SuppressWarnings("unused")
public final class ExamplePlugin extends JavaPlugin {

    private BukkitAudiences bukkitAudiences;
    private MinecraftHelp<CommandSender> minecraftHelp;

    @Override
    public void onEnable() {
        //
        // (1) The execution coordinator determines how commands are executed. The simple execution coordinator will
        //     run the command on the thread that is calling it. In the case of Bukkit, this is the primary server thread.
        //     It is possible to execute (and parse!) commands asynchronously by using the
        //     AsynchronousCommandExecutionCoordinator.
        // (2) This function maps the Bukkit CommandSender to your custom sender type and back. If you're not using a custom
        //     type, then SenderMapper.identity() maps CommandSender to itself.
        //
        final PaperCommandManager<CommandSender> manager = new PaperCommandManager<>(
                /* Owning plugin */ this,
                /* (1) */ ExecutionCoordinator.simpleCoordinator(),
                /* (2) */ SenderMapper.identity()
        );
        //
        // Configure based on capabilities
        //
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            // Register Brigadier mappings for rich completions
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            // Use Paper async completions API (see Javadoc for why we don't use this with Brigadier)
            manager.registerAsynchronousCompletions();
        }
        //
        // Create the Bukkit audiences that maps command senders to adventure audience. This is not needed
        // if you're using paper-api instead of Bukkit.
        //
        this.bukkitAudiences = BukkitAudiences.create(this);
        //
        // Override the default exception handlers and use the exception handlers from cloud-minecraft-extras instead.
        //
        MinecraftExceptionHandler.create(this.bukkitAudiences::sender)
                .defaultInvalidSyntaxHandler()
                .defaultInvalidSenderHandler()
                .defaultNoPermissionHandler()
                .defaultArgumentParsingHandler()
                .defaultCommandExecutionHandler()
                .decorator(
                        component -> text()
                                .append(text("[", NamedTextColor.DARK_GRAY))
                                .append(text("Example", NamedTextColor.GOLD))
                                .append(text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                )
                .registerTo(manager);
        //
        // Create a help instance which is used in TextColorExample and HelpExample.
        //
        this.minecraftHelp = MinecraftHelp.create(
                // The help command. This gets prefixed onto all the clickable queries.
                "/builder help",
                // The command manager instance that is used to look up the commands.
                manager,
                // Tells the help manager how to map command senders to adventure audiences.
                this.bukkitAudiences()::sender
        );
        //
        // Create the annotation examples.
        //
        new AnnotationParserExample(this, manager);
        //
        // Create the builder examples.
        //
        new BuilderExample(this, manager);
    }

    /**
     * Returns the {@link BukkitAudiences} instance.
     *
     * @return audiences
     */
    public @NonNull BukkitAudiences bukkitAudiences() {
        return this.bukkitAudiences;
    }

    /**
     * Returns the {@link MinecraftHelp} instance.
     *
     * @return minecraft help
     */
    public @NonNull MinecraftHelp<CommandSender> minecraftHelp() {
        return this.minecraftHelp;
    }

    /**
     * Replaces the {@link MinecraftHelp} instance.
     *
     * @param minecraftHelp the new instance
     */
    public void minecraftHelp(final @NonNull MinecraftHelp<CommandSender> minecraftHelp) {
        this.minecraftHelp = minecraftHelp;
    }
}
