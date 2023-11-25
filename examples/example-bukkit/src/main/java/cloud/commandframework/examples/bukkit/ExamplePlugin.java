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

import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.examples.bukkit.annotations.AnnotationParserExample;
import cloud.commandframework.examples.bukkit.builder.BuilderExample;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import java.util.function.Function;
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
        final BukkitCommandManager<CommandSender> manager;
        try {
            //
            // (1) The execution coordinator determines how commands are executed. The simple execution coordinator will
            //     run the command on the thread that is calling it. In the case of Bukkit, this is the primary server thread.
            //     It is possible to execute (and parse!) commands asynchronously by using the
            //     AsynchronousCommandExecutionCoordinator.
            // (2) This functions maps the Bukkit command sender to your custom sender type. If you're not using a custom
            //     type, then Function.identity() maps CommandSender to itself.
            // (3) The same concept as (2), but mapping from your custom type to a Bukkit command sender.
            //
            manager = new PaperCommandManager<>(
                    /* Owning plugin */ this,
                    /* (1) */ CommandExecutionCoordinator.simpleCoordinator(),
                    /* (2) */ Function.identity(),
                    /* (3) */ Function.identity()
            );
        } catch (final Exception e) {
            this.getLogger().severe("Failed to initialize the command this.manager");
            /* Disable the plugin */
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //
        // Use contains to filter suggestions instead of default startsWith
        //
        manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
        ));
        //
        // Register Brigadier mappings. The capability tells us whether Brigadier is natively available
        // on the current server. If it is, we can safely register the Brigadier integration.
        //
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        }
        //
        // Register asynchronous completions. The capability tells us whether asynchronous completions
        // are available on the server that is running the plugin. The asynchronous completion method
        // is only available in cloud-paper, not cloud-bukkit.
        //
        if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) manager).registerAsynchronousCompletions();
        }
        //
        // Create the Bukkit audiences that maps command senders to adventure audience. This is not needed
        // if you're using paper-api instead of Bukkit.
        //
        this.bukkitAudiences = BukkitAudiences.create(this);
        //
        // Override the default exception handlers and use the exception handlers from cloud-minecraft-extras instead.
        //
        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(
                        component -> text()
                                .append(text("[", NamedTextColor.DARK_GRAY))
                                .append(text("Example", NamedTextColor.GOLD))
                                .append(text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                ).apply(manager, this.bukkitAudiences::sender);
        //
        // Create a help instance which is used in TextColorExample and HelpExample.
        //
        this.minecraftHelp = new MinecraftHelp<>(
                // The help command. This gets prefixed onto all the clickable queries.
                "/builder help",
                // Tells the help manager how to map command senders to adventure audiences.
                this.bukkitAudiences()::sender,
                // The command manager instance that is used to look up the commands.
                manager
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
}
