package io.github.seggan.segganbot.commands.impls.slash;

import io.github.seggan.segganbot.Main;
import io.github.seggan.segganbot.commands.AbstractSlashCommand;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public final class ReleaseSlash extends AbstractSlashCommand {

    public ReleaseSlash() {
        super("release", "moves an addon from in development to released");
    }

    @Override
    protected void getData(@NotNull CommandData data) {
        data.addOption(OptionType.CHANNEL, "channel", "the channel to move", true);
    }

    @Override
    public boolean canExecute(@NotNull Member member) {
        return member.getRoles().contains(Roles.ADDON_CREATORS.getRole());
    }

    @Override
    public void execute(@NotNull SlashCommandEvent event, @NotNull InteractionHook hook) {
        GuildChannel channel = event.getOption("channel").getAsGuildChannel();
        if (channel.getParent().getIdLong() != 809185692688187452L) {
            event.reply("Addon is not in development!").queue();
            return;
        }

        Category newCat = Main.jda.getCategoryById(809185621746122832L);
        channel.getManager().setParent(newCat).queue(v ->
            Main.jda.getGuildChannelById(channel.getId()).getManager().sync().queue()
        );
        channel.getManager().clearOverridesAdded().queue();
        channel.getManager().clearOverridesRemoved().queue();

        event.reply("Successfully moved " + channel.getAsMention() + " to released addons").queue();
    }
}
