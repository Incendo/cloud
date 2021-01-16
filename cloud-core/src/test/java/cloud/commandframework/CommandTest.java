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
package cloud.commandframework;

import cloud.commandframework.arguments.StaticArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.SimpleCommandMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommandTest {

    @Test()
    void noArguments() {
        Assertions.assertEquals(1, Command.newBuilder("test", SimpleCommandMeta.empty()).build().getArguments().size());
    }

    @Test
    void ensureOrdering() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                Command.newBuilder("test", SimpleCommandMeta.empty()).argument(StringArgument.optional("something"))
                        .argument(StaticArgument.of("somethingelse")).build());
    }

}
