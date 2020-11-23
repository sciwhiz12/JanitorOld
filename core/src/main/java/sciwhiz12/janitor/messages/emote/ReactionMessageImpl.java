package sciwhiz12.janitor.messages.emote;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import sciwhiz12.janitor.JanitorBotImpl;
import sciwhiz12.janitor.api.messages.emote.ReactionMessage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import static net.dv8tion.jda.api.Permission.MESSAGE_MANAGE;

public class ReactionMessageImpl extends ListenerAdapter implements ReactionMessage {
    private final JanitorBotImpl bot;
    private final Message message;
    private final Map<ReactionEmote, ReactionListener> emotes = new LinkedHashMap<>();
    private boolean removeEmotes = true;
    private long ownerID;
    private boolean onlyOwner;

    public ReactionMessageImpl(JanitorBotImpl bot, Message message, boolean onlyOwner, long ownerID) {
        this.bot = bot;
        this.message = message;
        this.ownerID = ownerID;
        this.onlyOwner = onlyOwner;
    }

    public ReactionMessageImpl(JanitorBotImpl bot, Message message) {
        this(bot, message, false, 0);
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

    public void create() {
        for (ReactionEmote reaction : emotes.keySet()) {
            if (reaction.isEmote()) {
                message.addReaction(reaction.getEmote()).queue();
            } else {
                message.addReaction(reaction.getEmoji()).queue();
            }
        }
        bot.getDiscord().addEventListener(this);
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getMessageIdLong() != message.getIdLong()) return;
        if (event.getUserIdLong() == bot.getDiscord().getSelfUser().getIdLong()) return;
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
