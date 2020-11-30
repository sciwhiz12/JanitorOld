package sciwhiz12.janitor.commands.bot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.JanitorBotImpl;
import sciwhiz12.janitor.api.command.BaseCommand;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.api.module.ModuleKey;
import sciwhiz12.janitor.api.utils.MessageHelper;

import java.util.Objects;
import java.util.stream.Collectors;

import static sciwhiz12.janitor.api.config.CoreConfigs.COMMAND_PREFIX;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;

public class AboutCommand extends BaseCommand {
    public AboutCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("about")
            .executes(ctx -> {
                Package pkg = JanitorBotImpl.class.getPackage();
                messages().getRegularMessage("general/about")
                    .with("bot.name", () -> Objects.requireNonNullElse(pkg.getImplementationTitle(), "Janitor"))
                    .with("bot.version", () -> Objects.requireNonNullElse(pkg.getImplementationVersion(), "in-dev"))
                    .with("bot.guild_count", () -> String.valueOf(getBot().getDiscord().getGuilds().size()))
                    .with("bot.modules.count", () -> String.valueOf(getBot().getModuleManager().getActiveModules().size()))
                    .with("bot.modules.names",
                        () -> getBot().getModuleManager().getActiveModules().stream()
                            .map(ModuleKey::getModuleName)
                            .collect(Collectors.joining(", "))
                    )
                    .apply(MessageHelper.member("bot.member", ctx.getSource().getGuild().getSelfMember()))
                    .with("guild.command_prefix", () -> config(ctx.getSource()).forGuild(COMMAND_PREFIX))
                    .send(getBot(), ctx.getSource().getChannel())
                    .reference(ctx.getSource().getMessage()).queue();
                return 1;
            });
    }
}
