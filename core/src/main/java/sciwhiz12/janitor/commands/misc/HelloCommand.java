package sciwhiz12.janitor.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import sciwhiz12.janitor.api.command.BaseCommand;
import sciwhiz12.janitor.api.command.CommandRegistry;
import sciwhiz12.janitor.api.command.arguments.GuildMemberArgument;
import sciwhiz12.janitor.utils.Util;

import java.util.List;

import static sciwhiz12.janitor.api.Logging.JANITOR;
import static sciwhiz12.janitor.api.command.arguments.GuildMemberArgument.getMembers;
import static sciwhiz12.janitor.api.utils.CommandHelper.argument;
import static sciwhiz12.janitor.api.utils.CommandHelper.literal;

public class HelloCommand extends BaseCommand {
    public HelloCommand(CommandRegistry registry) {
        super(registry);
    }

    public LiteralArgumentBuilder<MessageReceivedEvent> getNode() {
        return literal("greet")
            .then(argument("member", GuildMemberArgument.member())
                .executes(this::run)
            );
    }

    int run(final CommandContext<MessageReceivedEvent> ctx) throws CommandSyntaxException {
        if (ctx.getSource().isFromGuild()) {
            final List<Member> memberList = getMembers("member", ctx).fromGuild(ctx.getSource().getGuild());
            if (memberList.size() == 1) {
                final Member member = memberList.get(0);
                ctx.getSource().getChannel().sendMessage("Hello " + member.getAsMention() + "!").queue(
                    success -> {
                        JANITOR.debug("Sent greeting message to {}, on cmd of {}", Util.toString(member.getUser()),
                            Util.toString(ctx.getSource().getAuthor()));
                        getBot().getReactions().newMessage(success)
                            .add("\u274C", (msg, event) -> success.delete()
                                .flatMap(v -> event.getChannel()
                                    .deleteMessageById(ctx.getSource().getMessageIdLong()))
                                .queue()
                            )
                            .owner(ctx.getSource().getAuthor().getIdLong())
                            .create();
                    },
                    err -> JANITOR.error("Error while sending greeting message to {}, on cmd of {}",
                        Util.toString(member.getUser()), Util.toString(ctx.getSource().getAuthor()))
                );
            }
        }
        return 1;
    }
}
