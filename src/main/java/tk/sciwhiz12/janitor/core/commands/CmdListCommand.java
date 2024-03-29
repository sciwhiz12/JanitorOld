package tk.sciwhiz12.janitor.core.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tk.sciwhiz12.janitor.api.core.command.BaseCommand;

import static tk.sciwhiz12.janitor.api.core.config.CoreConfigs.COMMAND_PREFIX;
import static tk.sciwhiz12.janitor.api.core.utils.CommandHelper.literal;

public class CmdListCommand extends BaseCommand {
    public CmdListCommand(CommandRegistryImpl registry) {
        super(registry);
    }

    @Override
    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("commands")
            .executes(ctx -> {
                messages().<String>getListingMessage("general/commands_listing")
                    .amountPerPage(12)
                    .with("commands_prefix", () -> config(ctx.getSource()).forGuild(COMMAND_PREFIX))
                    .setEntryApplier((command, subs) -> subs.with("command", () -> command))
                    .build(ctx.getSource().getChannel(), getBot(), ctx.getSource().getMessage(),
                        Lists.newArrayList(getRegistry().registry.keySet()))
                    .queue();
                return 1;
            });
    }

    @Override
    public CommandRegistryImpl getRegistry() {
        return (CommandRegistryImpl) super.getRegistry();
    }
}
