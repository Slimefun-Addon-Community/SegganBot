package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSlashCommand {

    @NotNull
    private final String name;

    public AbstractSlashCommand(@NotNull String name, @NotNull String desc, @NotNull OptionData... options) {
        this.name = name;

        CommandListUpdateAction commands = Main.jda.updateCommands();
        CommandData data = new CommandData(name, desc);
        data.addOptions(options);
        commands.addCommands(data).queue();
    }

    protected void execute(@NotNull SlashCommandEvent event) {

    }
}
