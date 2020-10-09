package sciwhiz12.janitor.moderation.warns;

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
import javax.annotation.Nullable;

public class WarningEntry {
    private final User performer;
    private final User warned;
    private final OffsetDateTime dateTime;
    @Nullable
    private final String reason;

    public WarningEntry(User warned, User performer, OffsetDateTime dateTime, @Nullable String reason) {
        this.performer = performer;
        this.warned = warned;
        this.dateTime = dateTime;
        this.reason = reason;
    }

    public User getPerformer() {
        return performer;
    }

    public User getWarned() {
        return warned;
    }

    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    @Nullable
    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WarningEntry that = (WarningEntry) o;
        return getPerformer().equals(that.getPerformer()) &&
            getWarned().equals(that.getWarned()) &&
            getDateTime().equals(that.getDateTime()) &&
            Objects.equals(getReason(), that.getReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPerformer(), getWarned(), getDateTime(), getReason());
    }

    public static class Serializer extends StdSerializer<WarningEntry> {
        private static final long serialVersionUID = 1L;

        public Serializer() {
            super(WarningEntry.class);
        }

        @Override
        public void serialize(WarningEntry value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeNumberField("performer", value.getPerformer().getIdLong());
            gen.writeNumberField("warned", value.getWarned().getIdLong());
            gen.writeStringField("dateTime", value.getDateTime().toString());
            gen.writeStringField("reason", value.getReason());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends StdDeserializer<WarningEntry> {
        private static final long serialVersionUID = 1L;

        private final Supplier<JanitorBot> bot;

        public Deserializer(Supplier<JanitorBot> bot) {
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
}
