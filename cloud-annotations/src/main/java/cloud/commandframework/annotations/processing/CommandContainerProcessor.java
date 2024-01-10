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
package cloud.commandframework.annotations.processing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import org.checkerframework.checker.nullness.qual.NonNull;

@SupportedAnnotationTypes(CommandContainer.ANNOTATION_PATH)
public final class CommandContainerProcessor extends AbstractProcessor {

    /**
     * The file in which all command container names are stored.
     */
    public static final String PATH = "META-INF/commands/cloud.commandframework.annotations.processing.CommandContainer";

    @Override
    public boolean process(
            final @NonNull Set<? extends TypeElement> annotations,
            final @NonNull RoundEnvironment roundEnv
    ) {
        final List<String> validTypes = new ArrayList<>();

        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CommandContainer.class);
        if (elements.isEmpty()) {
            return false; // Nothing to process...
        }

        for (final Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                this.processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        String.format(
                                "@CommandMethod found on unsupported element type '%s' (%s)",
                                element.getKind().name(),
                                element.getSimpleName().toString()
                        ),
                        element
                );
                return false;
            }

            element.accept(new CommandContainerVisitor(this.processingEnv, validTypes), null);
        }

        for (final String type : validTypes) {
            this.processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    String.format(
                            "Found valid @CommandMethod annotated class: %s",
                            type
                    )
            );
        }
        this.writeCommandFile(validTypes);

        // https://errorprone.info/bugpattern/DoNotClaimAnnotations
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @SuppressWarnings({"unused", "try"})
    private void writeCommandFile(final @NonNull List<String> types) {
        try (BufferedWriter writer = new BufferedWriter(this.processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                PATH
        ).openWriter())) {
            for (final String t : types) {
                writer.write(t);
                writer.newLine();
            }
            writer.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
