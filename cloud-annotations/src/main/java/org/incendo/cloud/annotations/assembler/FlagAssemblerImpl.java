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
package org.incendo.cloud.annotations.assembler;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.descriptor.FlagDescriptor;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserRegistry;
import org.incendo.cloud.parser.flag.CommandFlag;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = API.Status.INTERNAL, consumers = "org.incendo.cloud.annotations.*")
public final class FlagAssemblerImpl implements FlagAssembler {

    private final CommandManager<?> commandManager;

    /**
     * Creates a new flag assembler.
     *
     * @param commandManager command manager
     */
    public FlagAssemblerImpl(final @NonNull CommandManager<?> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @NonNull CommandFlag<?> assembleFlag(final @NonNull FlagDescriptor descriptor) {
        final Description description;
        if (descriptor.description() == null) {
            description = Description.empty();
        } else {
            description = descriptor.description();
        }

        final Permission permission;
        if (descriptor.permission() == null) {
            permission = Permission.empty();
        } else {
            permission = descriptor.permission();
        }

        CommandFlag.Builder<Void> builder = this.commandManager
                .flagBuilder(descriptor.name())
                .withDescription(description)
                .withAliases(descriptor.aliases())
                .withPermission(permission);
        if (descriptor.repeatable()) {
            builder = builder.asRepeatable();
        }

        if (descriptor.parameter().getType().equals(boolean.class)) {
            return builder.build();
        }

        final TypeToken<?> token;
        if (descriptor.repeatable() && Collection.class.isAssignableFrom(descriptor.parameter().getType())) {
            token = TypeToken.get(GenericTypeReflector.getTypeParameter(
                    descriptor.parameter().getParameterizedType(),
                    Collection.class.getTypeParameters()[0]
            ));
        } else {
            token = TypeToken.get(descriptor.parameter().getType());
        }

        if (token.equals(TypeToken.get(boolean.class))) {
            return builder.build();
        }

        final Collection<Annotation> annotations = Arrays.asList(descriptor.parameter().getAnnotations());
        final ParserRegistry<?> registry = this.commandManager.parserRegistry();
        final ArgumentParser<?, ?> parser;
        if (descriptor.parserName() == null) {
            parser = registry.createParser(token, registry.parseAnnotations(token, annotations))
                    .orElse(null);
        } else {
            parser = registry.createParser(descriptor.parserName(), registry.parseAnnotations(token, annotations))
                    .orElse(null);
        }
        if (parser == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot find parser for type '%s' for flag '%s'",
                            descriptor.parameter().getType().getCanonicalName(),
                            descriptor.name()
                    ));
        }
        final SuggestionProvider<?> suggestionProvider;
        if (descriptor.suggestions() != null) {
            suggestionProvider = registry.getSuggestionProvider(descriptor.suggestions()).orElse(null);
        } else {
            suggestionProvider = null;
        }
        final CommandComponent.Builder componentBuilder = CommandComponent.builder();
        componentBuilder.commandManager(this.commandManager)
                .name(descriptor.name())
                .valueType(descriptor.parameter().getType())
                .parser(parser);
        if (suggestionProvider != null) {
            componentBuilder.suggestionProvider(suggestionProvider);
        }
        return builder.withComponent(componentBuilder.build()).build();
    }
}
