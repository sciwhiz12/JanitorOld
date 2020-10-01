package sciwhiz12.janitor.moderation.notes;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.dv8tion.jda.api.entities.User;
import sciwhiz12.janitor.JanitorBot;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Objects;

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

    public static class Serializer implements JsonDeserializer<NoteEntry>, JsonSerializer<NoteEntry> {
        private final JanitorBot bot;

        public Serializer(JanitorBot bot) {
            this.bot = bot;
        }

        @Override
        public NoteEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            final User performer = bot.getDiscord().retrieveUserById(obj.get("performer").getAsLong()).complete();
            final User target = bot.getDiscord().retrieveUserById(obj.get("target").getAsLong()).complete();
            final OffsetDateTime dateTime = OffsetDateTime.parse(obj.get("dateTime").getAsString());
            final String reason = obj.get("contents").getAsString();
            return new NoteEntry(performer, target, dateTime, reason);
        }

        @Override
        public JsonElement serialize(NoteEntry src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject obj = new JsonObject();
            obj.addProperty("performer", src.getPerformer().getId());
            obj.addProperty("target", src.getTarget().getId());
            obj.addProperty("dateTime", src.getDateTime().toString());
            obj.addProperty("contents", src.getContents());
            return obj;
        }
    }
}
