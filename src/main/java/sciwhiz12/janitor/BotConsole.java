package sciwhiz12.janitor;

import com.google.common.base.Strings;

import java.io.InputStream;
import java.util.Scanner;

import static sciwhiz12.janitor.Logging.CONSOLE;

public class BotConsole {
    private final JanitorBot bot;
    private final Thread thread;
    private volatile boolean running = true;

    public BotConsole(JanitorBot bot, InputStream input) {
        this.bot = bot;
        this.thread = new Thread(this.new ConsoleThread(input));
        this.thread.setName("janitor_console");
        this.thread.setDaemon(true);
    }

    public Thread getConsoleThread() {
        return this.thread;
    }

    public void start() {
        this.thread.start();
    }

    public void stop() {
        running = false;
        this.thread.interrupt();
    }

    public void parseCommand(String input) {
        String[] parts = input.split(" ");
        outer:
        switch (parts[0]) {
            case "shutdown": {
                running = false;
                bot.shutdown();
                break;
            }
            case "reload": {
                if (parts.length >= 2)
                    switch (parts[1]) {
                        case "messages": {
                            CONSOLE.info("Reloading messages");
                            bot.getMessages().loadMessages();
                            break outer;
                        }
                    }
            }
            default:
                CONSOLE.warn("Unknown command: {}", input);
        }
    }

    public class ConsoleThread implements Runnable {
        private final Scanner scanner;

        public ConsoleThread(InputStream consoleInput) {
            scanner = new Scanner(consoleInput);
        }

        @Override
        public void run() {
            CONSOLE.info("Console thread is now running");
            outer:
            while (BotConsole.this.running) {
                while (!scanner.hasNextLine()) {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                        CONSOLE.warn("Console thread is interrupted");
                        continue outer;
                    }
                }
                try {
                    String input = scanner.nextLine();
                    if (Strings.isNullOrEmpty(input)) {
                        continue;
                    }
                    CONSOLE.debug("Received command: {}", input);
                    BotConsole.this.parseCommand(input);
                } catch (Exception e) {
                    CONSOLE.error("Error while running console thread", e);
                }
            }
            CONSOLE.info("Console thread is now closed");
        }
    }
}
