package com.intellectualsites.commands.components.standard;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.parser.ComponentParseResult;
import com.intellectualsites.commands.components.parser.ComponentParser;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.sender.CommandSender;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("unused")
public class BooleanComponent<C extends CommandSender> extends CommandComponent<C, Boolean> {
    private final boolean liberal;

    public BooleanComponent(final boolean required, @Nonnull final String name,
                            final boolean liberal, @Nonnull final String defaultValue) {
        super(required, name, new BooleanParser<>(liberal), defaultValue);
        this.liberal = liberal;
    }

    @Nonnull
    public static <C extends CommandSender> Builder<C> newBuilder(@Nonnull final String name) {
        return new Builder<>(name);
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Boolean> required(@Nonnull final String name) {
        return BooleanComponent.<C>newBuilder(name).asRequired().build();
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Boolean> optional(@Nonnull final String name) {
        return BooleanComponent.<C>newBuilder(name).asOptional().build();
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, Boolean> optional(@Nonnull final String name,
                                                                                  final String defaultNum) {
        return BooleanComponent.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }

    public static final class Builder<C extends CommandSender> extends CommandComponent.Builder<C, Boolean> {

        private boolean liberal = false;

        protected Builder(@Nonnull final String name) {
            super(name);
        }

        @Nonnull
        public Builder<C> withLiberal(final boolean liberal) {
            this.liberal = liberal;
            return this;
        }

        @Nonnull
        @Override
        public BooleanComponent<C> build() {
            return new BooleanComponent<>(this.required, this.name, this.liberal, this.defaultValue);
        }

    }

    /**
     * Get the greedy boolean
     *
     * @return Greedy boolean
     */
    public boolean isLiberal() {
        return liberal;
    }

    private static final class BooleanParser<C extends CommandSender> implements ComponentParser<C, Boolean> {
        // todo
        private static final List<String> LIBERAL = Arrays.asList("TRUE", "YES", "ON", "FALSE", "NO", "OFF");
        private static final List<String> LIBERAL_TRUE = Arrays.asList("TRUE", "YES", "ON");
        private static final List<String> LIBERAL_FALSE = Arrays.asList("FALSE", "NO", "OFF");

        private final boolean liberal;

        public BooleanParser(final boolean liberal) {
            this.liberal = liberal;
        }

        @Nonnull
        @Override
        public ComponentParseResult<Boolean> parse(@Nonnull final CommandContext<C> commandContext,
                                                   @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ComponentParseResult.failure(new NullPointerException("No input was provided"));
            }
            inputQueue.remove();

            if (!liberal) {
                if (input.equalsIgnoreCase("true")) {
                    return ComponentParseResult.success(true);
                }

                if (input.equalsIgnoreCase("false")) {
                    return ComponentParseResult.success(false);
                }

                return ComponentParseResult.failure(new BooleanParseException(input, false));
            }

            final String uppercaseInput = input.toUpperCase();

            if (LIBERAL_TRUE.contains(uppercaseInput)) {
                return ComponentParseResult.success(true);
            }

            if (LIBERAL_FALSE.contains(uppercaseInput)) {
                return ComponentParseResult.success(false);
            }

            return ComponentParseResult.failure(new BooleanParseException(input, true));
        }

        @Nonnull
        @Override
        public List<String> suggestions(@Nonnull final CommandContext<C> commandContext,
                                        @Nonnull final String input) {
            if (!liberal) {
                return Arrays.asList("TRUE", "FALSE");
            }

            return LIBERAL;
        }
    }

    public static final class BooleanParseException extends IllegalArgumentException {

        private final boolean liberal;

        public BooleanParseException(@Nonnull final String input, final boolean liberal) {
            super(input);
            this.liberal = liberal;
        }

        public boolean isLiberal() {
            return liberal;
        }
    }
}
