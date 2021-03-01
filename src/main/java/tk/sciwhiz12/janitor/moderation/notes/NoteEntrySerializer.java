package tk.sciwhiz12.janitor.moderation.notes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import tk.sciwhiz12.janitor.api.moderation.notes.NoteEntry;

import java.io.IOException;

public class NoteEntrySerializer extends StdSerializer<NoteEntry> {
    private static final long serialVersionUID = 1L;

    public NoteEntrySerializer() {
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
