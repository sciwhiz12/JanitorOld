package sciwhiz12.janitor.moderation.notes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.dv8tion.jda.api.entities.User;
import sciwhiz12.janitor.JanitorBot;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Supplier;

public class NoteEntry {
    private final User performer;
    private final User target;
    private final OffsetDateTime dateTime;
    private final String contents;

    public NoteEntry(User performer, User target, OffsetDateTime dateTime, String contents) {
        this.performer = performer;
        this.target = target;
        this.dateTime = dateTime;
        this.contents = contents;
    }

    public User getPerformer() {
        return performer;
    }

    public User getTarget() {
        return target;
    }

    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    public String getContents() {
        return contents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteEntry noteEntry = (NoteEntry) o;
        return getPerformer().equals(noteEntry.getPerformer()) &&
            getTarget().equals(noteEntry.getTarget()) &&
            getDateTime().equals(noteEntry.getDateTime()) &&
            getContents().equals(noteEntry.getContents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPerformer(), getTarget(), getDateTime(), getContents());
    }

    public static class Serializer extends StdSerializer<NoteEntry> {
        private static final long serialVersionUID = 1L;

        public Serializer() {
            super(NoteEntry.class);
        }

        @Override
        public void serialize(NoteEntry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("performer", value.getPerformer().getIdLong());
            gen.writeNumberField("target", value.getTarget().getIdLong());
            gen.writeStringField("dateTime", value.getDateTime().toString());
            gen.writeStringField("contents", value.getContents());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<NoteEntry> {
        private static final long serialVersionUID = 1L;

        private final Supplier<JanitorBot> bot;

        public Deserializer(Supplier<JanitorBot> bot) {
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
}
