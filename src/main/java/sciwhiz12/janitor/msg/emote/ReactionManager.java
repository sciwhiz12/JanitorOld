package sciwhiz12.janitor.msg.emote;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sciwhiz12.janitor.JanitorBot;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class ReactionManager extends ListenerAdapter {
    private final JanitorBot bot;
    private final Map<Long, ReactionMessage> messageMap = new HashMap<>();

    public ReactionManager(JanitorBot bot) {
        this.bot = bot;
    }

    public ReactionMessage newMessage(Message message) {
        if (messageMap.containsKey(message.getIdLong())) {
            throw new IllegalArgumentException("Reaction message already exists for message with id " + message.getIdLong());
        }
        final ReactionMessage msg = new ReactionMessage(bot, message);
        messageMap.put(message.getIdLong(), msg);
        return msg;
    }

    public void removeMessage(long messageID) {
        bot.getDiscord().removeEventListener(messageMap.remove(messageID));
    }

    public Map<Long, ReactionMessage> getRegisteredMessages() {
        return messageMap;
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        if (messageMap.containsKey(event.getMessageIdLong())) {
            bot.getDiscord().removeEventListener(messageMap.get(event.getMessageIdLong()));
        }
    }
}
