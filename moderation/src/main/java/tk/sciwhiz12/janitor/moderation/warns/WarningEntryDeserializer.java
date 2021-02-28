package tk.sciwhiz12.janitor.moderation.warns;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import net.dv8tion.jda.api.entities.User;
import tk.sciwhiz12.janitor.api.JanitorBot;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

public class WarningEntryDeserializer extends StdDeserializer<WarningEntry> {
    private static final long serialVersionUID = 1L;

    private final Supplier<JanitorBot> bot;

    public WarningEntryDeserializer(Supplier<JanitorBot> bot) {
        super(WarningEntry.class);
        this.bot = bot;
    }

    @Override
    public WarningEntry deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        final JsonNode obj = ctx.readTree(p);
        User performer = bot.get().getDiscord().retrieveUserById(obj.get("performer").asLong()).complete();
        User warned = bot.get().getDiscord().retrieveUserById(obj.get("warned").asLong()).complete();
        OffsetDateTime dateTime = OffsetDateTime.parse(obj.get("dateTime").asText());
        String contents = obj.get("reason").asText();
        return new WarningEntry(performer, warned, dateTime, contents);
    }
}
