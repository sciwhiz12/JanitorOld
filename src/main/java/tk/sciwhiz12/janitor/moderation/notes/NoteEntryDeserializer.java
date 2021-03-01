package tk.sciwhiz12.janitor.moderation.notes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.dv8tion.jda.api.entities.User;
import tk.sciwhiz12.janitor.api.JanitorBot;
import tk.sciwhiz12.janitor.api.moderation.notes.NoteEntry;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

public class NoteEntryDeserializer extends StdDeserializer<NoteEntry> {
    private static final long serialVersionUID = 1L;

    private final Supplier<JanitorBot> bot;

    public NoteEntryDeserializer(Supplier<JanitorBot> bot) {
        super(NoteEntry.class);
        this.bot = bot;
    }

    @Override
    public NoteEntry deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final JsonNode obj = ctx.readTree(p);
        User performer = bot.get().getDiscord().retrieveUserById(obj.get("performer").asLong()).complete();
        User target = bot.get().getDiscord().retrieveUserById(obj.get("target").asLong()).complete();
        OffsetDateTime dateTime = OffsetDateTime.parse(obj.get("dateTime").asText());
        String contents = obj.get("contents").asText();
        return new NoteEntry(performer, target, dateTime, contents);
    }
}
