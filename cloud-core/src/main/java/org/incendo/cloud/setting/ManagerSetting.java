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
package org.incendo.cloud.setting;

import org.apiguardian.api.API;
import org.incendo.cloud.CommandManager;

/**
 * Configurable command related settings
 *
 * @see CommandManager#settings() to access the settings
 */
@API(status = API.Status.STABLE)
public enum ManagerSetting implements Setting {
    /**
     * Force sending of an empty suggestion (i.e. a singleton list containing an empty string)
     * when no suggestions are present
     */
    FORCE_SUGGESTION,

    /**
     * Allow registering commands even when doing so has the potential to produce inconsistent results.
     * <p>
     * For example, if a platform serializes the command tree and sends it to clients,
     * this will allow modifying the command tree after it has been sent, as long as these modifications are not blocked by
     * the underlying platform
     */
    @API(status = API.Status.STABLE)
    ALLOW_UNSAFE_REGISTRATION,

    /**
     * Enables overriding of existing commands on supported platforms.
     */
    @API(status = API.Status.STABLE)
    OVERRIDE_EXISTING_COMMANDS,

    /**
     * Allows parsing flags at any position after the last literal by appending flag argument nodes between each command node.
     * It can have some conflicts when integrating with other command systems like Brigadier,
     * and code inspecting the command tree may need to be adjusted.
     */
    @API(status = API.Status.EXPERIMENTAL)
    LIBERAL_FLAG_PARSING
}
