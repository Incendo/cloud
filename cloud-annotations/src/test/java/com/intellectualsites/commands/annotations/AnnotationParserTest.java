//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg
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
package com.intellectualsites.commands.annotations;

import com.google.common.collect.Maps;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.meta.SimpleCommandMeta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

class AnnotationParserTest {

    private static CommandManager<TestCommandSender, SimpleCommandMeta> manager;
    private static AnnotationParser<TestCommandSender, SimpleCommandMeta> annotationParser;

    @BeforeAll
    static void setup() {
        manager = new TestCommandManager();
        annotationParser = new AnnotationParser<>(manager);
    }

    @Test
    void testSyntaxParsing() {
        final String text = "literal <required> [optional]";
        final Map<String, AnnotationParser.ArgumentMode> arguments = annotationParser.parseSyntax(text);
        final Map<String, AnnotationParser.ArgumentMode> map = Maps.newLinkedHashMap();
        map.put("literal", AnnotationParser.ArgumentMode.LITERAL);
        map.put("required", AnnotationParser.ArgumentMode.REQUIRED);
        map.put("optional", AnnotationParser.ArgumentMode.OPTIONAL);
        Assertions.assertEquals(map, arguments);
    }

}
