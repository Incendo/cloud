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
package org.incendo.cloud.state;

import org.apiguardian.api.API;

/**
 * The point in the registration lifecycle for this commands manager
 *
 */
@API(status = API.Status.STABLE)
public enum RegistrationState implements State {

    /**
     * The point when no commands have been registered yet.
     * <p>
     * At this point, all configuration options can be changed.
     */
    BEFORE_REGISTRATION,
    /**
     * When at least one command has been registered, and more commands have been registered.
     * <p>
     * In this state, some options that affect how commands are registered with the platform are frozen. Some platforms
     * will remain in this state for their lifetime.
     */
    REGISTERING,
    /**
     * Once registration has been completed.
     * <p>
     * At this point, the command manager is effectively immutable. On platforms where command registration happens via
     * callback, this state is achieved the first time the manager's callback is executed for registration.
     */
    AFTER_REGISTRATION
}
