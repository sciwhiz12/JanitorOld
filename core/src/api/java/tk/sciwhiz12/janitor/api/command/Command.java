package tk.sciwhiz12.janitor.api.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tk.sciwhiz12.janitor.api.JanitorBot;
import tk.sciwhiz12.janitor.api.config.GuildConfig;
import tk.sciwhiz12.janitor.api.messages.Messages;

public interface Command {
    LiteralArgumentBuilder<MessageReceivedEvent> getNode();

    JanitorBot getBot();

    default Messages messages() {
        return getBot().getMessages();
    }

    default GuildConfig config(MessageReceivedEvent event) {
        return config(event.getGuild().getIdLong());
    }

    default GuildConfig config(Guild guild) {
        return config(guild.getIdLong());
    }

    default GuildConfig config(long guildID) {
        return getBot().getConfigs().get(guildID);
    }
}
