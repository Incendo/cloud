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
package cloud.commandframework.examples.javacord.components;

import cloud.commandframework.examples.javacord.modules.ExampleModule;
import com.google.common.base.Preconditions;
import dev.simplix.core.common.aop.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(ExampleModule.class)
public class DiscordApiComponent {

    private static final Logger LOG = LoggerFactory.getLogger(DiscordApiComponent.class);

    private final DiscordApiBuilder builder;

    private DiscordState discordState;
    private DiscordApi api;

    /**
     * Creates a new {@link DiscordApiComponent} instance. Done automatically
     */
    public DiscordApiComponent() {
        this.builder = new DiscordApiBuilder();
        this.discordState = DiscordState.DISCONNECTED;
    }

    /**
     * Logs in to Discord using the provided token
     *
     * @param token The Discord Bot token
     */
    public final void login(final @NonNull String token) {
        LOG.info("Logging in to Discord...");

        this.api = this.builder.setToken(token).login().join();
        this.discordState = DiscordState.CONNECTED;

        LOG.info("Successfully connected to Discord!");
    }

    /**
     * Shuts of the {@link DiscordApi} instance
     */
    public final void shutdown() {
        LOG.info("Shutting down DiscordApi instance...");

        this.api.disconnect();
        this.discordState = DiscordState.DISCONNECTED;

        LOG.info("Successfully shut of the DiscordApi");
    }

    /**
     * Gets the current {@link DiscordApi} instance
     *
     * @return Instance of the DiscordApi
     */
    public final @NonNull DiscordApi getDiscordApi() {
        Preconditions.checkArgument(this.discordState == DiscordState.CONNECTED, "Not connected to Discord");
        return this.api;
    }

    /**
     * Gets the current Api State
     *
     * @return Current Api State
     */
    public final @NonNull DiscordState getDiscordState() {
        return this.discordState;
    }

    public enum DiscordState {
        CONNECTED,
        DISCONNECTED
    }

}
