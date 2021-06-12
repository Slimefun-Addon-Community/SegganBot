package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.constants.Patterns;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public record Command(String command, String[] arguments, TextChannel channel, Message message) {
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^[!?]\\w+");

    @Nullable
    public static Command parse(MessageReceivedEvent e) {
        Message message = e.getMessage();
        String string = message.getContentRaw();
        if (!COMMAND_PATTERN.matcher(string).find()) {
            return null;
        }

        String[] args = Patterns.SPACE_PATTERN.split(string);

        return new Command(
            args[0],
            Arrays.copyOfRange(args, 1, args.length),
            e.getTextChannel(),
            message
        );
    }
}
