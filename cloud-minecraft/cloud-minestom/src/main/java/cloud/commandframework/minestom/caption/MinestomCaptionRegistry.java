package cloud.commandframework.minestom.caption;

import cloud.commandframework.captions.SimpleCaptionRegistry;

public class MinestomCaptionRegistry<C> extends SimpleCaptionRegistry<C> {
    public MinestomCaptionRegistry() {
        this.registerMessageFactory(
                MinestomCaptionKeys.ARGUMENT_PARSE_FAILURE_PLAYER,
                (caption, sender) -> ARGUMENT_PARSE_FAILURE_PLAYER
        );
    }

    /**
     * Default caption for {@link MinestomCaptionKeys#ARGUMENT_PARSE_FAILURE_PLAYER}
     */
    public static final String ARGUMENT_PARSE_FAILURE_PLAYER = "No player found for input '{input}'";
}
