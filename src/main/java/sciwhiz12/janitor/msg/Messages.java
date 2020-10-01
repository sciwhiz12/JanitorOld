package sciwhiz12.janitor.msg;

import net.dv8tion.jda.api.EmbedBuilder;
import sciwhiz12.janitor.JanitorBot;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class Messages {
    public static final int FAILURE_COLOR = 0xF73132;

    private final JanitorBot bot;
    public final General GENERAL;
    public final Moderation MODERATION;

    public Messages(JanitorBot bot) {
        this.bot = bot;
        this.GENERAL = new General(this);
        this.MODERATION = new Moderation(this);
    }

    public String translate(String key, Object... args) {
        return bot.getTranslations().translate(key, args);
    }

    public EmbedBuilder failureEmbed(String title) {
        return new EmbedBuilder()
            .setTitle(title)
            .setColor(FAILURE_COLOR)
            .setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
