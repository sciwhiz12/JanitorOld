package sciwhiz12.janitor.messages.emote;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import sciwhiz12.janitor.JanitorBotImpl;
import sciwhiz12.janitor.api.messages.emote.ReactionMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.dv8tion.jda.api.Permission.MESSAGE_MANAGE;

public class ReactionMessageImpl implements ReactionMessage {
    private final JanitorBotImpl bot;
    private long messageID;
    private final Map<ReactionEmote, ReactionListener> emotes = new LinkedHashMap<>();
    private boolean removeEmotes = true;
    private long ownerID;
    private boolean onlyOwner;

    public ReactionMessageImpl(JanitorBotImpl bot, boolean onlyOwner, long ownerID) {
        this.bot = bot;
        this.ownerID = ownerID;
        this.onlyOwner = onlyOwner;
    }

    public ReactionMessageImpl(JanitorBotImpl bot) {
        this(bot, false, 0);
    }

    public ReactionMessageImpl add(ReactionEmote emote, ReactionListener listener) {
        emotes.put(emote, listener);
        return this;
    }

    public ReactionMessageImpl add(String emote, ReactionListener listener) {
        return add(ReactionEmote.fromUnicode(emote, bot.getDiscord()), listener);
    }

    public ReactionMessageImpl add(Emote emote, ReactionListener listener) {
        return add(ReactionEmote.fromCustom(emote), listener);
    }

    public ReactionMessageImpl removeEmotes(boolean remove) {
        this.removeEmotes = remove;
        return this;
    }

    public ReactionMessageImpl owner(long ownerID) {
        this.ownerID = ownerID;
        this.onlyOwner = true;
        return this;
    }

    public RestAction<Message> create(Message message) {
        List<RestAction<Void>> reactionList = new ArrayList<>();
        for (ReactionEmote reaction : emotes.keySet()) {
            if (reaction.isEmote()) {
                reactionList.add(message.addReaction(reaction.getEmote()));
            } else {
                reactionList.add(message.addReaction(reaction.getEmoji()));
            }
        }
        messageID = message.getIdLong();
        return RestAction.allOf(reactionList)
            .map($ -> message);
    }

    void acceptReaction(MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != messageID) return;
        if (onlyOwner && event.getUserIdLong() != ownerID) return;

        emotes.keySet().stream()
            .filter(emote -> event.getReactionEmote().equals(emote))
            .forEach(emote -> emotes.get(emote).accept(this, event));

        if (removeEmotes && (!event.isFromGuild()
            || event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), MESSAGE_MANAGE))) {
            event.retrieveUser()
                .flatMap(user -> event.getReaction().removeReaction(user))
                .queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
        }
    }

    public JanitorBotImpl getBot() {
        return bot;
    }

    public long getOwnerID() {
        return ownerID;
    }

    public boolean isOwnerOnly() {
        return onlyOwner;
    }

    public Map<ReactionEmote, ReactionListener> getListeners() {
        return Collections.unmodifiableMap(emotes);
    }
}
