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
package cloud.commandframework.services.types;

import cloud.commandframework.services.State;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Service implementation that alters the state of the owning application in some way. A
 * SideEffectService does not return a generated result, instead it returns a response, indicating
 * whether or not the context was consumed
 *
 * @param <Context> Context type.
 */
@FunctionalInterface
public interface SideEffectService<Context> extends Service<Context, State> {

    /**
     * Consumes the context, if possible. Returns {@link State#ACCEPTED} if the input was consumed,
     * else {@link State#REJECTED}
     *
     * @param context context used in the generation of the response
     * @return Response. If the response isn't {@link State#ACCEPTED}, the next service in the service
     *         chain will get to act on the context. Otherwise the execution halts, and the provided response
     *         is the final response.
     * @throws Exception Any exception that occurs during the handling can be thrown, and will be
     *                   wrapped by a {@link cloud.commandframework.services.PipelineException}
     */
    @Override
    @NonNull State handle(@NonNull Context context) throws Exception;

}
