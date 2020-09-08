package sciwhiz12.janitor.utils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class CommandHelper {
    public static LiteralArgumentBuilder<MessageCreateEvent> literal(String command) {
        return LiteralArgumentBuilder.literal(command);
    }
}
