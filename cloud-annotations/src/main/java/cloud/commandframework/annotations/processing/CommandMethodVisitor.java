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
package cloud.commandframework.annotations.processing;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.ArgumentMode;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.SyntaxFragment;
import cloud.commandframework.annotations.SyntaxParser;
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

class CommandMethodVisitor implements ElementVisitor<Void, Void> {

    private final ProcessingEnvironment processingEnvironment;
    private final SyntaxParser syntaxParser;

    CommandMethodVisitor(final @NonNull ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
        this.syntaxParser = new SyntaxParser();
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
                            "@CommandMethod annotated methods should be public (%s)",
                            e.getSimpleName()
                    ),
                    e
            );
        }

        if (e.getModifiers().contains(Modifier.STATIC)) {
            this.processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@CommandMethod annotated methods should be non-static (%s)",
                            e.getSimpleName()
                    ),
                    e
            );
        }

        if (e.getReturnType().toString().equals("Void")) {
            this.processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@CommandMethod annotated methods should return void (%s)",
                            e.getSimpleName()
                    ),
                    e
            );
        }

        final CommandMethod commandMethod = e.getAnnotation(CommandMethod.class);
        final List<String> parameterArgumentNames = e.getParameters()
                .stream()
                .map(parameter -> parameter.getAnnotation(Argument.class))
                .filter(Objects::nonNull)
                .map(Argument::value)
                .collect(Collectors.toList());
        final List<String> parsedArgumentNames = new ArrayList<>(parameterArgumentNames.size());

        final List<SyntaxFragment> syntaxFragments = this.syntaxParser.apply(commandMethod.value());

        boolean foundOptional = false;
        for (final SyntaxFragment fragment : syntaxFragments) {
            if (fragment.getArgumentMode() == ArgumentMode.LITERAL) {
                continue;
            }

            if (!parameterArgumentNames.contains(fragment.getMajor())) {
                this.processingEnvironment.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format(
                                "@Argument(\"%s\") is missing from @CommandMethod (%s)",
                                fragment.getMajor(),
                                e.getSimpleName()
                        ),
                        e
                );
            }

            if (fragment.getArgumentMode() == ArgumentMode.REQUIRED) {
                if (foundOptional) {
                    this.processingEnvironment.getMessager().printMessage(
                            Diagnostic.Kind.ERROR,
                            String.format(
                                    "Required argument '%s' cannot succeed an optional argument (%s)",
                                    fragment.getMajor(),
                                    e.getSimpleName()
                            ),
                            e
                    );
                }
            } else {
                foundOptional = true;
            }

            parsedArgumentNames.add(fragment.getMajor());
        }

        for (final String argument : parameterArgumentNames) {
            if (!parsedArgumentNames.contains(argument)) {
                this.processingEnvironment.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format(
                                "Argument '%s' is missing from the @CommandMethod syntax (%s)",
                                argument,
                                e.getSimpleName()
                        ),
                        e
                );
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
