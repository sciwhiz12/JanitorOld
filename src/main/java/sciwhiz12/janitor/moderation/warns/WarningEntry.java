package sciwhiz12.janitor.moderation.warns;

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

    public static class Serializer implements JsonDeserializer<WarningEntry>, JsonSerializer<WarningEntry> {
        private final JanitorBot bot;

        public Serializer(JanitorBot bot) {
            this.bot = bot;
        }

        @Override
        public WarningEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            final JsonObject obj = json.getAsJsonObject();
            final User warned = bot.getDiscord().retrieveUserById(obj.get("warned").getAsLong()).complete();
            final User performer = bot.getDiscord().retrieveUserById(obj.get("performer").getAsLong()).complete();
            final OffsetDateTime dateTime = OffsetDateTime.parse(obj.get("dateTime").getAsString());
            @Nullable
            final String reason = obj.has("reason") ? obj.get("reason").getAsString() : null;
            return new WarningEntry(warned, performer, dateTime, reason);
        }

        @Override
        public JsonElement serialize(WarningEntry src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject obj = new JsonObject();
            obj.addProperty("warned", src.getWarned().getId());
            obj.addProperty("performer", src.getPerformer().getId());
            obj.addProperty("dateTime", src.getDateTime().toString());
            if (src.getReason() != null) {
                obj.addProperty("reason", src.getReason());
            }
            return obj;
        }
    }
}
