package io.github.seggan.segganbot.commands.impls;

import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PingCommand extends AbstractAdminCommand {

    public PingCommand() {
        super("ping", null);
    }

    @Override
    protected void execute(@NotNull Message message, @NotNull Map<String, String> args, @NotNull String content, @NotNull Member member) {
        message.getChannel().sendMessage("Pong!").queue();
    }
}
