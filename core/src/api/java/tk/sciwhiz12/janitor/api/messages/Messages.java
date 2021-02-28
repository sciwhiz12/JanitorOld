package tk.sciwhiz12.janitor.api.messages;

import net.dv8tion.jda.api.entities.MessageEmbed;
import tk.sciwhiz12.janitor.api.JanitorBot;

import java.util.Collections;

public interface Messages {
    JanitorBot getBot();

    void loadMessages();

    RegularMessage.Builder<?> getRegularMessage(String messageKey);

    <T> ListingMessage.Builder<T> getListingMessage(String messageKey);

    RegularMessage UNKNOWN_REGULAR_MESSAGE = new RegularMessage(
        "UNKNOWN MESSAGE!",
        null,
        "A regular message was tried to be looked up, but was not found. " +
            "Please report this to your bot maintainer/administrator.",
        String.valueOf(0xFF0000),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Collections.singletonList(new MessageEmbed.Field("Message Key", "${key}", false))
    );

    ListingMessage UNKNOWN_LISTING_MESSAGE = new ListingMessage(
        "UNKNOWN MESSAGE!",
        null,
        "A listing message was tried to be looked up, but was not found. " +
            "Please report this to your bot maintainer/administrator.",
        String.valueOf(0xFF0000),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        new ListingMessage.DescriptionEntry(null, ""),
        Collections.singletonList(new MessageEmbed.Field("Message Key", "${key}", false)),
        Collections.emptyList()
    );
}
