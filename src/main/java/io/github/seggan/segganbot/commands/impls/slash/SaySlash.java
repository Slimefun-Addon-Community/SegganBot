package io.github.seggan.segganbot.commands.impls.slash;

import io.github.seggan.segganbot.commands.AbstractSlashCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public final class SaySlash extends AbstractSlashCommand {

    public SaySlash() {
        super("say", "make the bot say something");
    }

    @Override
    protected void getData(@NotNull CommandData data) {
        data.addOption(OptionType.STRING, "message", "the message to say", true);
    }

    @Override
    public void execute(@NotNull SlashCommandEvent event, @NotNull InteractionHook hook) {
        event.deferReply(true).queue();
        hook.setEphemeral(true);
        hook.editOriginal("Done").queue();
        event.getChannel().sendMessage(event.getOption("message").getAsString()).queue();
    }
}
