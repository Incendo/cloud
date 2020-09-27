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
package cloud.commandframework.annotations;

import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.meta.CommandMeta;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.function.Function;

class MetaFactory implements Function<Annotation[], CommandMeta> {

    private final AnnotationParser<?> annotationParser;
    private final Function<ParserParameters, CommandMeta> metaMapper;

    MetaFactory(@Nonnull final AnnotationParser<?> annotationParser,
                @Nonnull final Function<ParserParameters, CommandMeta> metaMapper) {
        this.annotationParser = annotationParser;
        this.metaMapper = metaMapper;
    }

    @Override
    public CommandMeta apply(@Nonnull final Annotation[] annotations) {
        final ParserParameters parameters = ParserParameters.empty();
        for (final Annotation annotation : annotations) {
            @SuppressWarnings("ALL") final Function function = this.annotationParser.getAnnotationMappers()
                                                                                    .get(annotation.annotationType());
            if (function == null) {
                continue;
            }
            //noinspection unchecked
            parameters.merge((ParserParameters) function.apply(annotation));
        }
        return this.metaMapper.apply(parameters);
    }

}
