package io.github.seggan.segganbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.EnumSet;

public class Main {
    public static JDA jda = null;
    public static final JsonObject config = JsonParser.parseString(Util.getFileAsString(new File("config.json"))).getAsJsonObject();

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDABuilder jdaBuilder = JDABuilder.createDefault(config.get("discord").getAsString());
        jdaBuilder.addEventListeners(new Listener());
        jdaBuilder.setEnabledIntents(GatewayIntent.GUILD_MEMBERS, EnumSet.allOf(GatewayIntent.class).toArray(new GatewayIntent[0]));
        jda = jdaBuilder.build().awaitReady();
        // setPerms();
        // setSlowMode();
    }

    private static void setPerms() {
        Role role = jda.getRoleById(Roles.MUTED.getId());
        for (TextChannel channel : jda.getTextChannels()) {
            ChannelManager manager = channel.getManager();

            manager.putPermissionOverride(role, null, EnumSet.of(
                Permission.MESSAGE_WRITE,
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_TTS,
                Permission.MESSAGE_ATTACH_FILES
            )).queue();
        }
    }

    private static void setSlowMode() {
        for (TextChannel channel : jda.getTextChannels()) {
            ChannelManager manager = channel.getManager();

            manager.setSlowmode(5).queue();
        }
    }
}
