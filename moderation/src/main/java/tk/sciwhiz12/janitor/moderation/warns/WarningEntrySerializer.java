package tk.sciwhiz12.janitor.moderation.warns;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class WarningEntrySerializer extends StdSerializer<WarningEntry> {
    private static final long serialVersionUID = 1L;

    public WarningEntrySerializer() {
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
