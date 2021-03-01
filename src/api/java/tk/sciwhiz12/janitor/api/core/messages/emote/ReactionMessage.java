package tk.sciwhiz12.janitor.api.core.messages.emote;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import tk.sciwhiz12.janitor.api.JanitorBot;

import java.util.Map;
import java.util.function.BiConsumer;

public interface ReactionMessage {
    ReactionMessage add(ReactionEmote emote, ReactionListener listener);

    ReactionMessage add(String emote, ReactionListener listener);

    ReactionMessage add(Emote emote, ReactionListener listener);

    ReactionMessage removeEmotes(boolean remove);

    ReactionMessage owner(long ownerID);

    RestAction<Message> create(Message message);

    long getOwnerID();

    boolean isOwnerOnly();

    Map<ReactionEmote, ReactionListener> getListeners();

    JanitorBot getBot();

    @FunctionalInterface
    interface ReactionListener extends BiConsumer<ReactionMessage, MessageReactionAddEvent> {
        void accept(ReactionMessage message, MessageReactionAddEvent event);
    }
}
