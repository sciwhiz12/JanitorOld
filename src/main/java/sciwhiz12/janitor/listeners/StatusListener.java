package sciwhiz12.janitor.listeners;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import org.jetbrains.annotations.NotNull;
import sciwhiz12.janitor.JanitorBot;
import sciwhiz12.janitor.utils.Util;

import static sciwhiz12.janitor.Logging.JANITOR;
import static sciwhiz12.janitor.Logging.STATUS;

public class StatusListener extends BaseListener {
    public StatusListener(JanitorBot bot) {
        super(bot);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getPresence()
            .setPresence(OnlineStatus.ONLINE, Activity.playing("n' sweeping n' testing!"));
        JANITOR.info("Ready!");
        bot.getConfig().getOwnerID()
            .map(ownerId -> bot.getJDA().retrieveUserById(ownerId))
            .ifPresent(retrieveUser ->
                retrieveUser.submit()
                    .thenCompose(user -> user.openPrivateChannel().submit())
                    .thenCompose(channel -> channel.sendMessage("Started up and ready!").submit())
                    .whenComplete(Util.handle(
                        msg -> JANITOR.debug(STATUS, "Sent ready message to owner!"),
                        error -> JANITOR.error(STATUS, "Error while sending ready message to owner", error))
                    )
            );
    }
}
