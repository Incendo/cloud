package cloud.commandframework.internal.registration;

import cloud.commandframework.arguments.StaticArgument;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * Candidate command that is yet to be registered. The changes made to a
 * {@link CommandCandidate} instance will be reflected in the registered command
 *
 * The command candidate uses the fact that the leading command argument must necessarily
 * be a {@link cloud.commandframework.arguments.StaticArgument} and the changed parameters
 * will be used to construct a new static argument, which will be new leading command
 * argument. This can be used to alter command names, aliases, etc
 *
 * @param <C> Command sender type
 * @since 1.2.0
 */
public final class CommandCandidate<C> {

    private String name;
    private String[] aliases;

    /**
     * Construct a new command candidate
     *
     * @param name    Command name
     * @param aliases Command aliases
     */
    public CommandCandidate(
            final @NonNull String name,
            final @NonNull String @NonNull [] aliases
    ) {
        this.name(name);
        this.aliases(aliases);
    }

    /**
     * Construct a new command candidate from a static argument
     *
     * @param argument Leading command argument
     */
    public CommandCandidate(
            final @NonNull StaticArgument<C> argument
    ) {
        this(
                argument.getName(),
                argument.getAlternativeAliases().toArray(new String[0])
        );
    }

    /**
     * Change the command name
     *
     * @param name New name. May not be null
     */
    public void name(final @NonNull String name) {
        this.name = Objects.requireNonNull(name, "Name may not be null");
    }

    /**
     * Get the command name
     *
     * @return Command name
     */
    public @NonNull String name() {
        return this.name;
    }

    /**
     * Get the command aliases
     *
     * @return Command aliases
     */
    public @NonNull String @NonNull [] aliases() {
        return this.aliases;
    }

    /**
     * Change the command aliases
     *
     * @param aliases New aliases. May not be null
     */
    public void aliases(final @NonNull String @NonNull [] aliases) {
        this.aliases = Objects.requireNonNull(aliases, "Aliases may not be null");
    }

    /**
     * Construct a new static argument from this candidate
     *
     * @return Constructed static argument
     */
    public @NonNull StaticArgument<C> toArgument() {
        return StaticArgument.of(this.name, this.aliases);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CommandCandidate<?> candidate = (CommandCandidate<?>) o;
        return Objects.equals(name, candidate.name) &&
                Arrays.equals(aliases, candidate.aliases);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(aliases);
        return result;
    }

}
