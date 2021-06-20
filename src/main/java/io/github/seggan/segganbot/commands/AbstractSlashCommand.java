package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;


public abstract class AbstractSlashCommand {

    @Getter
    @NotNull
    private final String name;

    public AbstractSlashCommand(@NotNull String name, @NotNull String desc) {
        this.name = name;

        CommandListUpdateAction commands = Main.jda.getGuildById(809178621424041997L).updateCommands();
        CommandData data = new CommandData(name, desc);
        getData(data);
        commands.addCommands(data).queue();

        Main.slashCommands.add(this);
    }

    protected void getData(@NotNull CommandData data) {
    }

    public boolean canExecute(@NotNull Member member) {
        return true;
    }

    public abstract void execute(@NotNull SlashCommandEvent event, @NotNull InteractionHook hook);
}
