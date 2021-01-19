package io.github.seggan.segganbot;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.regex.Pattern;

@Data
public final class Command {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^[!?]\\w+");

    private final String command;
    private final String[] arguments;
    private final TextChannel channel;
    private final Message message;

    @Nullable
    public static Command parse(MessageReceivedEvent e) {
        Message message = e.getMessage();
        String string = message.getContentRaw();
        if (!COMMAND_PATTERN.matcher(string).find()) {
            return null;
        }

        String[] args = string.split(" ");

        return new Command(
            args[0],
            Arrays.copyOfRange(args, 1, args.length),
            e.getTextChannel(),
            message
        );
    }
}
