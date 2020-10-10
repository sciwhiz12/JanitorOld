package sciwhiz12.janitor.msg.json;

import net.dv8tion.jda.api.EmbedBuilder;
import sciwhiz12.janitor.msg.substitution.ISubstitutor;
import sciwhiz12.janitor.msg.TranslationMap;

public interface IMessage {
    EmbedBuilder create(TranslationMap translations, ISubstitutor substitutions);
}
