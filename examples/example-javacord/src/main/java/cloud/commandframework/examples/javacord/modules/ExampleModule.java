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
package cloud.commandframework.examples.javacord.modules;

import com.google.inject.Inject;
import com.google.inject.Provides;
import dev.simplix.core.common.aop.AbstractSimplixModule;
import dev.simplix.core.common.aop.ApplicationModule;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static cloud.commandframework.examples.javacord.ExampleBot.APPLICATION_NAME;

@ApplicationModule(APPLICATION_NAME)
public class ExampleModule extends AbstractSimplixModule {

    /**
     * Creates a new {@link ScheduledExecutorService} instance. Done automatically
     *
     * @return Instance of the ExecutorService
     */
    @Provides
    @Inject
    public final @NonNull ScheduledExecutorService provideExecutorService() {
        return Executors.newScheduledThreadPool(0);
    }

}
