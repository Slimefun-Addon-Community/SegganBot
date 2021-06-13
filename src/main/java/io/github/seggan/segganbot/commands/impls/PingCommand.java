package io.github.seggan.segganbot.commands.impls;

import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;

public class PingCommand extends AbstractAdminCommand {

    public PingCommand() {
        super("ping", null);
    }

    @Override
    protected void execute(@NotNull Message message, @NotNull ListOrderedMap<String, String> args, @NotNull Member member) {
        message.getChannel().sendMessage("Pong!").queue();
    }
}
