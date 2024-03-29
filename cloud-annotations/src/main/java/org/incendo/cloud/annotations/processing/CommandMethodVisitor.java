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
package org.incendo.cloud.annotations.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.ArgumentMode;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.SyntaxFragment;
import org.incendo.cloud.annotations.SyntaxParserImpl;

class CommandMethodVisitor implements ElementVisitor<Void, Void> {

    private final ProcessingEnvironment processingEnvironment;
    private final SyntaxParserImpl syntaxParser;

    CommandMethodVisitor(final @NonNull ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.syntaxParser = new SyntaxParserImpl();
    }

    @Override
    public Void visit(final Element e) {
        return this.visit(e, null);
    }

    @Override
    public Void visit(final Element e, final Void unused) {
        return null;
    }

    @Override
    public Void visitPackage(final PackageElement e, final Void unused) {
        return null;
    }

    @Override
    public Void visitType(final TypeElement e, final Void unused) {
        return null;
    }

    @Override
    public Void visitVariable(final VariableElement e, final Void unused) {
        return null;
    }

    @Override
    public Void visitExecutable(final ExecutableElement e, final Void unused) {
        if (!e.getModifiers().contains(Modifier.PUBLIC)) {
            this.processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.WARNING,
                    String.format(
                            "@Command annotated methods should be public (%s)",
                            e.getSimpleName()
                    ),
                    e
            );
        }

        if (e.getModifiers().contains(Modifier.STATIC)) {
            this.processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@Command annotated methods should be non-static (%s)",
                            e.getSimpleName()
                    ),
                    e
            );
        }

        if (e.getReturnType().toString().equals("Void")) {
            this.processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@Command annotated methods should return void (%s)",
                            e.getSimpleName()
                    ),
                    e
            );
        }


        final Command[] commands = e.getAnnotationsByType(Command.class);
        for (final Command command : commands) {
            final List<String> annotatedArgumentNames = e.getParameters()
                    .stream()
                    .map(parameter -> parameter.getAnnotation(Argument.class))
                    .filter(Objects::nonNull)
                    .map(Argument::value)
                    .filter(name -> !name.equals(AnnotationParser.INFERRED_ARGUMENT_NAME))
                    .collect(Collectors.toList());
            final List<String> parameterArgumentNames = new ArrayList<>(annotatedArgumentNames);

            e.getParameters()
                    .stream()
                    .filter(parameter -> {
                        final Argument argument = parameter.getAnnotation(Argument.class);
                        return argument == null || AnnotationParser.INFERRED_ARGUMENT_NAME.equals(argument.value());
                    })
                    .map(parameter -> parameter.getSimpleName().toString())
                    .forEach(parameterArgumentNames::add);

            final List<String> parsedArgumentNames = new ArrayList<>(parameterArgumentNames.size());
            final List<SyntaxFragment> syntaxFragments = this.syntaxParser.parseSyntax(null, command.value());

            boolean foundOptional = false;
            for (final SyntaxFragment fragment : syntaxFragments) {
                if (fragment.argumentMode() == ArgumentMode.LITERAL) {
                    continue;
                }

                if (!parameterArgumentNames.contains(fragment.major())) {
                    this.processingEnvironment.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            String.format(
                                    "@Argument(\"%s\") is missing from @Command (%s)",
                                    fragment.major(),
                                    e.getSimpleName()
                            ),
                            e
                    );
                }

                if (fragment.argumentMode() == ArgumentMode.REQUIRED) {
                    if (foundOptional) {
                        this.processingEnvironment.getMessager().printMessage(
                                Diagnostic.Kind.ERROR,
                                String.format(
                                        "Required argument '%s' cannot succeed an optional argument (%s)",
                                        fragment.major(),
                                        e.getSimpleName()
                                ),
                                e
                        );
                    }
                } else {
                    foundOptional = true;
                }

                parsedArgumentNames.add(fragment.major());
            }

            for (final String argument : annotatedArgumentNames) {
                if (!parsedArgumentNames.contains(argument)) {
                    this.processingEnvironment.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            String.format(
                                    "Argument '%s' is missing from the @Command syntax (%s)",
                                    argument,
                                    e.getSimpleName()
                            ),
                            e
                    );
                }
            }
        }
        return null;
    }

    @Override
    public Void visitTypeParameter(final TypeParameterElement e, final Void unused) {
        return null;
    }

    @Override
    public Void visitUnknown(final Element e, final Void unused) {
        return null;
    }
}
