package com.intellectualsites.commands.components.standard;

import com.intellectualsites.commands.components.CommandComponent;
import com.intellectualsites.commands.components.parser.ComponentParseResult;
import com.intellectualsites.commands.components.parser.ComponentParser;
import com.intellectualsites.commands.context.CommandContext;
import com.intellectualsites.commands.sender.CommandSender;

import java.util.Queue;
import java.util.StringJoiner;
import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class StringComponent<C extends CommandSender> extends CommandComponent<C, String> {
    private final boolean greedy;

    public StringComponent(final boolean required, @Nonnull final String name,
                           final boolean greedy, @Nonnull final String defaultValue) {
        super(required, name, new StringParser<>(greedy), defaultValue);
        this.greedy = greedy;
    }

    @Nonnull
    public static <C extends CommandSender> StringComponent.Builder<C> newBuilder(@Nonnull final String name) {
        return new StringComponent.Builder<>(name);
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, String> required(@Nonnull final String name) {
        return StringComponent.<C>newBuilder(name).asRequired().build();
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, String> optional(@Nonnull final String name) {
        return StringComponent.<C>newBuilder(name).asOptional().build();
    }

    @Nonnull
    public static <C extends CommandSender> CommandComponent<C, String> optional(@Nonnull final String name,
                                                                                 final String defaultNum) {
        return StringComponent.<C>newBuilder(name).asOptionalWithDefault(defaultNum).build();
    }

    public static final class Builder<C extends CommandSender> extends CommandComponent.Builder<C, String> {

        private boolean greedy = false;

        protected Builder(@Nonnull final String name) {
            super(name);
        }

        @Nonnull
        public Builder<C> withGreedy(final boolean greedy) {
            this.greedy = greedy;
            return this;
        }

        @Nonnull
        @Override
        public StringComponent<C> build() {
            return new StringComponent<>(this.required, this.name, this.greedy, this.defaultValue);
        }

    }

    /**
     * Get the greedy boolean
     *
     * @return Greedy boolean
     */
    public boolean isGreedy() {
        return greedy;
    }

    private static final class StringParser<C extends CommandSender> implements ComponentParser<C, String> {

        private final boolean greedy;

        public StringParser(final boolean greedy) {
            this.greedy = greedy;
        }

        @Nonnull
        @Override
        public ComponentParseResult<String> parse(@Nonnull final CommandContext<C> commandContext,
                                                  @Nonnull final Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ComponentParseResult.failure(new NullPointerException("No input was provided"));
            }

            if (!greedy) {
                inputQueue.remove();
                return ComponentParseResult.success(input);
            }

            final StringJoiner sj = new StringJoiner(" ");
            final int size = inputQueue.size();

            for (int i = 0; i < size; i++) {
                sj.add(inputQueue.remove());
            }

            return ComponentParseResult.success(sj.toString());
        }
    }
}
