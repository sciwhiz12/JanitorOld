package sciwhiz12.janitor.utils;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelper {
    public static LiteralArgumentBuilder<MessageReceivedEvent> literal(String command) {
        return LiteralArgumentBuilder.literal(command);
    }
}
