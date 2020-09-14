package sciwhiz12.janitor.utils;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class CommandHelper {
    public static LiteralArgumentBuilder<MessageCreateEvent> literal(String command) {
        return LiteralArgumentBuilder.literal(command);
    }

    public static <Arg> RequiredArgumentBuilder<MessageCreateEvent, Arg> argument(String command, ArgumentType<Arg> argument) {
        return RequiredArgumentBuilder.argument(command, argument);
    }
}
