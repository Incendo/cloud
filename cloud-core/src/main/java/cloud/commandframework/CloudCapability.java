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
package cloud.commandframework;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a capability that a cloud implementation may have.
 *
 */
@API(status = API.Status.STABLE)
public interface CloudCapability {

    /**
     * Returns the friendly name of this capability.
     *
     * @return the name of the capability
     */
    @Override
    @NonNull String toString();


    /**
     * Standard {@link CloudCapability capabilities}.
     *
     */
    @API(status = API.Status.STABLE)
    enum StandardCapabilities implements CloudCapability {
        /**
         * The capability to delete root commands using {@link CommandManager#deleteRootCommand(String)}.
         */
        ROOT_COMMAND_DELETION;

        @Override
        public @NonNull String toString() {
            return name();
        }
    }


    /**
     * Exception thrown when a {@link CloudCapability} is missing, when a method that requires the presence of that
     * capability is invoked.
     *
     */
    @API(status = API.Status.STABLE)
    final class CloudCapabilityMissingException extends RuntimeException {


        /**
         * Create a new cloud capability missing exception instance.
         *
         * @param capability the missing capability
         */
        public CloudCapabilityMissingException(final @NonNull CloudCapability capability) {
            super(String.format("Missing capability '%s'", capability));
        }
    }
}
