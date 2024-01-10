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
package cloud.commandframework.examples.bukkit.annotations.feature;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.examples.bukkit.ExamplePlugin;
import cloud.commandframework.examples.bukkit.annotations.AnnotationFeature;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Example showcasing how to use command containers. Command containers are recognized at compile-type by
 * using cloud-annotations as an annotation processor. These containers can then be loaded without explicitly
 * parsing each class.
 * <p>
 * See {@link cloud.commandframework.examples.bukkit.annotations.ExampleCommandContainer} to see how the container is set up.
 */
public final class CommandContainerExample implements AnnotationFeature {

    @Override
    public void registerFeature(
            final @NonNull ExamplePlugin examplePlugin,
            final @NonNull AnnotationParser<CommandSender> annotationParser
    ) {
        try {
            annotationParser.parseContainers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
