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
package cloud.commandframework.services.types;

import cloud.commandframework.services.ExecutionOrder;
import cloud.commandframework.services.PipelineException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * A service is anything that can take in a context, and produce a response. Most service
 * implementations will be side effect free, although some service implementations will have side
 * effects. Those that do, should be clearly labeled
 *
 * @param <Context> Context type, this will be the input that is used to generate the response
 * @param <Result>  Response type, this is what is produced by the service ("provided")
 */
@FunctionalInterface
public interface Service<Context, Result> extends Function<Context, Result> {

  /**
   * Provide a response for the given context. If the service implementation cannot provide a
   * response for the given context, it should return {@code null}
   *
   * @param context Context used in the generation of the response
   * @return Response. If the response isn't {@code null}, the next service in the service chain
   * will get to act on the context. Otherwise the execution halts, and the provided response is the
   * final response.
   * @throws Exception Any exception that occurs during the handling can be thrown, and will be
   *                   wrapped by a {@link PipelineException}
   */
  @Nullable
  Result handle(@Nonnull Context context) throws Exception;

  @Override
  default Result apply(@Nonnull Context context) {
    try {
      return this.handle(context);
    } catch (final Exception exception) {
      throw new PipelineException(exception);
    }
  }

  /**
   * Get the execution order of the service. This should not be overridden, unless you know what you
   * are doing
   *
   * @return Execution order
   */
  @Nullable
  default ExecutionOrder order() {
    return null;
  }

}
