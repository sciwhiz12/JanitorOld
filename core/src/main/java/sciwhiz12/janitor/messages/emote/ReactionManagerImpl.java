package sciwhiz12.janitor.messages.emote;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import sciwhiz12.janitor.JanitorBotImpl;
import sciwhiz12.janitor.api.messages.emote.ReactionManager;
import sciwhiz12.janitor.api.messages.emote.ReactionMessage;
import sciwhiz12.janitor.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class ReactionManagerImpl extends ListenerAdapter implements ReactionManager {
    private final JanitorBotImpl bot;
    private final Map<Long, ReactionMessageImpl> messageMap = new HashMap<>();

    public ReactionManagerImpl(JanitorBotImpl bot) {
        this.bot = bot;
    }

    public ReactionMessageImpl newMessage(Message message) {
        if (messageMap.containsKey(message.getIdLong())) {
            throw new IllegalArgumentException("Reaction message already exists for message with id " + message.getIdLong());
        }
        final ReactionMessageImpl msg = new ReactionMessageImpl(bot);
        messageMap.put(message.getIdLong(), msg);
        return msg;
    }

    public void removeMessage(long messageID) {
        bot.getDiscord().removeEventListener(messageMap.remove(messageID));
    }

    public Map<Long, ReactionMessage> getRegisteredMessages() {
        return messageMap.entrySet().stream()
            .map(entry -> Pair.<Long, ReactionMessage>of(entry.getKey(), entry.getValue()))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public JanitorBotImpl getBot() {
        return bot;
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getUserIdLong() == bot.getDiscord().getSelfUser().getIdLong()) return;
        if (messageMap.containsKey(event.getMessageIdLong())) {
            messageMap.get(event.getMessageIdLong()).acceptReaction(event);
        }
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        if (messageMap.containsKey(event.getMessageIdLong())) {
            bot.getDiscord().removeEventListener(messageMap.get(event.getMessageIdLong()));
        }
    }
}
