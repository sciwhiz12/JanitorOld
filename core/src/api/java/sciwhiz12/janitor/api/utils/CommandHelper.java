package sciwhiz12.janitor.api.utils;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public final class CommandHelper {
    private CommandHelper() {}

    public static LiteralArgumentBuilder<MessageReceivedEvent> literal(String command) {
        return LiteralArgumentBuilder.literal(command);
    }

    public static <Arg> RequiredArgumentBuilder<MessageReceivedEvent, Arg> argument(String command,
        ArgumentType<Arg> argument) {
        return RequiredArgumentBuilder.argument(command, argument);
    }
}
