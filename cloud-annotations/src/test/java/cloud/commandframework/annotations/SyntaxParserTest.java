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
package cloud.commandframework.annotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the correct functioning of the {@link SyntaxParser}, which parses
 * command syntax into a List of {@link SyntaxFragment}
 */
public class SyntaxParserTest {

    @Test
    void testParseWithAliases() {
        List<SyntaxFragment> fragments = new SyntaxParser().apply(
                "literal|litalias1|litalias2 <requirement> [optional]");

        Assertions.assertEquals(3, fragments.size());

        final SyntaxFragment literal = fragments.get(0);
        Assertions.assertEquals("literal", literal.getMajor());
        Assertions.assertEquals(Arrays.asList("litalias1", "litalias2"), literal.getMinor());
        Assertions.assertEquals(ArgumentMode.LITERAL, literal.getArgumentMode());

        final SyntaxFragment requirement = fragments.get(1);
        Assertions.assertEquals("requirement", requirement.getMajor());
        Assertions.assertEquals(Collections.emptyList(), requirement.getMinor());
        Assertions.assertEquals(ArgumentMode.REQUIRED, requirement.getArgumentMode());

        final SyntaxFragment optional = fragments.get(2);
        Assertions.assertEquals("optional", optional.getMajor());
        Assertions.assertEquals(Collections.emptyList(), optional.getMinor());
        Assertions.assertEquals(ArgumentMode.OPTIONAL, optional.getArgumentMode());
    }

    @Test
    void testParse() {
        List<SyntaxFragment> fragments = new SyntaxParser().apply(
                "literal <requirement> [optional]");

        Assertions.assertEquals(3, fragments.size());

        final SyntaxFragment literal = fragments.get(0);
        Assertions.assertEquals("literal", literal.getMajor());
        Assertions.assertEquals(Collections.emptyList(), literal.getMinor());
        Assertions.assertEquals(ArgumentMode.LITERAL, literal.getArgumentMode());

        final SyntaxFragment requirement = fragments.get(1);
        Assertions.assertEquals("requirement", requirement.getMajor());
        Assertions.assertEquals(Collections.emptyList(), requirement.getMinor());
        Assertions.assertEquals(ArgumentMode.REQUIRED, requirement.getArgumentMode());

        final SyntaxFragment optional = fragments.get(2);
        Assertions.assertEquals("optional", optional.getMajor());
        Assertions.assertEquals(Collections.emptyList(), optional.getMinor());
        Assertions.assertEquals(ArgumentMode.OPTIONAL, optional.getArgumentMode());
    }

    @Test
    void testParseSpecialCharacters() {
        List<SyntaxFragment> fragments = new SyntaxParser().apply(
                "l_itera-l|with_ali-as <r_equiremen-t> [o_ptiona-l]");

        Assertions.assertEquals(3, fragments.size());

        final SyntaxFragment literal = fragments.get(0);
        Assertions.assertEquals("l_itera-l", literal.getMajor());
        Assertions.assertEquals(Collections.singletonList("with_ali-as"), literal.getMinor());
        Assertions.assertEquals(ArgumentMode.LITERAL, literal.getArgumentMode());

        final SyntaxFragment requirement = fragments.get(1);
        Assertions.assertEquals("r_equiremen-t", requirement.getMajor());
        Assertions.assertEquals(Collections.emptyList(), requirement.getMinor());
        Assertions.assertEquals(ArgumentMode.REQUIRED, requirement.getArgumentMode());

        final SyntaxFragment optional = fragments.get(2);
        Assertions.assertEquals("o_ptiona-l", optional.getMajor());
        Assertions.assertEquals(Collections.emptyList(), optional.getMinor());
        Assertions.assertEquals(ArgumentMode.OPTIONAL, optional.getArgumentMode());
    }
}
