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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic;
import org.checkerframework.checker.nullness.qual.NonNull;

final class CommandContainerVisitor implements ElementVisitor<Void, Void> {

    private static final Collection<String> PERMITTED_CONSTRUCTOR_PARAMETER_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "org.incendo.cloud.annotations.AnnotationParser"
            ))
    );

    private final ProcessingEnvironment processingEnvironment;
    private final Collection<String> validTypes;
    private final Map<Element, List<String>> errorsByElement = new HashMap<>();

    private boolean suitableConstructorFound;

    CommandContainerVisitor(
            final @NonNull ProcessingEnvironment processingEnvironment,
            final @NonNull Collection<@NonNull String> validTypes
    ) {
        this.processingEnvironment = processingEnvironment;
        this.validTypes = validTypes;
        this.suitableConstructorFound = false;
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
        if (!e.getModifiers().contains(Modifier.PUBLIC)) {
            this.processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format("@CommandContainer-annotated class must be public (%s)", e.getSimpleName()),
                    e
            );
            return null;
        }

        for (final Element enclosedElement : e.getEnclosedElements()) {
            if (enclosedElement.getKind() != ElementKind.CONSTRUCTOR) {
                continue;
            }

            // Visit the constructor.
            enclosedElement.accept(this, null);

            // If we've already found a suitable constructor, there's no
            // need to search for more.
            if (this.suitableConstructorFound) {
                break;
            }
        }

        // When we've visited every constructor, we verify that we found
        // at least one suitable constructor.
        if (!this.suitableConstructorFound) {
            this.processingEnvironment.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    String.format(
                            "@CommandContainer-annotated class must have a suitable constructor (%s)",
                            e.getSimpleName()
                    ),
                    e
            );
            for (final Map.Entry<Element, List<String>> entry : this.errorsByElement.entrySet()) {
                this.processingEnvironment.getMessager().printMessage(
                        Diagnostic.Kind.WARNING,
                        "Constructor not suitable: " + String.join(", ", entry.getValue()),
                        entry.getKey()
                );
            }
            return null;
        }

        // We know it's valid, so we'll add it to the list of valid types.
        this.validTypes.add(e.asType().toString());

        return null;
    }

    @Override
    public Void visitVariable(final VariableElement e, final Void unused) {
        return null;
    }

    private void logError(final Element element, final String err) {
        this.errorsByElement.computeIfAbsent(element, $ -> new ArrayList<>()).add(err);
    }

    private void logInvalidParam(final VariableElement param) {
        final List<String> errors = this.errorsByElement.computeIfAbsent(param, $ -> new ArrayList<>());
        final String allowedParams = "Recognized parameter types: " + PERMITTED_CONSTRUCTOR_PARAMETER_TYPES;
        if (!errors.contains(allowedParams)) {
            errors.add(allowedParams);
        }
        errors.add("Parameter '" + param + "' is not a recognized type");
    }

    @Override
    public Void visitExecutable(final ExecutableElement e, final Void unused) {
        // We only want to process public constructors.
        if (!e.getModifiers().contains(Modifier.PUBLIC)) {
            this.logError(e, "Is not public.");
            return null;
        }

        // Now we need to verify that the parameters are correct.
        boolean illegalParams = false;
        for (final VariableElement variableElement : e.getParameters()) {
            final boolean isDeclared = variableElement.asType() instanceof DeclaredType;
            if (!isDeclared) {
                this.logInvalidParam(variableElement);
                illegalParams = true;
                continue;
            }
            final DeclaredType type = (DeclaredType) variableElement.asType();
            final Element typeElement = type.asElement();
            final String typeName = typeElement.asType().toString().split("<")[0];
            if (!PERMITTED_CONSTRUCTOR_PARAMETER_TYPES.contains(typeName)) {
                this.logInvalidParam(variableElement);
                illegalParams = true;
            }
        }
        if (illegalParams) {
            return null;
        }

        // We now know that there's a constructor which accepts the permitted types,
        // and is public - Yay.
        this.suitableConstructorFound = true;

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
