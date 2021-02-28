package tk.sciwhiz12.janitor.api.messages.emote;

import net.dv8tion.jda.api.entities.Message;
import tk.sciwhiz12.janitor.api.JanitorBot;

import java.util.Map;

public interface ReactionManager {
    ReactionMessage newMessage(Message message);

    void removeMessage(long messageID);

    Map<Long, ReactionMessage> getRegisteredMessages();

    JanitorBot getBot();
}
