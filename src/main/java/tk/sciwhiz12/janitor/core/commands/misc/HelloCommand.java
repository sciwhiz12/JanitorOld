package tk.sciwhiz12.janitor.core.commands.misc;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import tk.sciwhiz12.janitor.api.core.command.BaseCommand;
import tk.sciwhiz12.janitor.api.core.command.CommandRegistry;
import tk.sciwhiz12.janitor.api.core.command.arguments.GuildMemberArgument;
import tk.sciwhiz12.janitor.core.utils.Util;

import java.util.Collections;
import java.util.List;

import static tk.sciwhiz12.janitor.api.Logging.JANITOR;
import static tk.sciwhiz12.janitor.api.core.utils.CommandHelper.argument;
import static tk.sciwhiz12.janitor.api.core.utils.CommandHelper.literal;

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
            final List<Member> memberList = GuildMemberArgument.getMembers("member", ctx).fromGuild(ctx.getSource().getGuild());
            if (memberList.size() == 1) {
                final Member member = memberList.get(0);
                ctx.getSource().getChannel().sendMessage("Hello " + member.getAsMention() + "!")
                    .allowedMentions(Collections.emptyList())
                    .reference(ctx.getSource().getMessage())
                    .flatMap(message ->
                        getBot().getReactions().newMessage(message)
                            .add("\u274C", (msg, event) -> message.delete()
                                .flatMap(v -> event.getChannel()
                                    .deleteMessageById(ctx.getSource().getMessageIdLong()))
                                .queue()
                            )
                            .owner(ctx.getSource().getAuthor().getIdLong())
                            .create(message)
                    )
                    .queue(
                        success -> JANITOR.debug("Sent greeting message to {}, on cmd of {}", Util.toString(member.getUser()),
                            Util.toString(ctx.getSource().getAuthor())),
                        err -> JANITOR.error("Error while sending greeting message to {}, on cmd of {}",
                            Util.toString(member.getUser()), Util.toString(ctx.getSource().getAuthor()))
                    );
            }
        }
        return 1;
    }
}
