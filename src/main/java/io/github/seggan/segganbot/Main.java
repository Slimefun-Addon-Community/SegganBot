package io.github.seggan.segganbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.github.seggan.segganbot.commands.AbstractSlashCommand;
import io.github.seggan.segganbot.commands.impls.BanCommand;
import io.github.seggan.segganbot.commands.impls.KickCommand;
import io.github.seggan.segganbot.commands.impls.MuteCommand;
import io.github.seggan.segganbot.commands.impls.PingCommand;
import io.github.seggan.segganbot.commands.impls.SetTagCommand;
import io.github.seggan.segganbot.commands.impls.WarnCommand;
import io.github.seggan.segganbot.constants.Roles;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bson.Document;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.security.auth.login.LoginException;

public class Main {
    public static MongoCollection<Document> warningDb;
    public static MongoCollection<Document> commandsDb;
    public static final Map<String, String> tags = new HashMap<>();
    public static final Set<Warning> warnings = new HashSet<>();
    public static final Set<AbstractSlashCommand> slashCommands = new HashSet<>();

    public static JDA jda = null;
    public static final JsonObject config = JsonParser.parseString(Util.getFileAsString(new File("config.json"))).getAsJsonObject();

    public static void main(String[] args) throws LoginException, InterruptedException {
        setup();
        JDABuilder jdaBuilder = JDABuilder.createDefault(config.get("discord").getAsString())
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setChunkingFilter(ChunkingFilter.ALL)
            .enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdaBuilder.addEventListeners(new Listener());
        jdaBuilder.setEnabledIntents(GatewayIntent.GUILD_MEMBERS, EnumSet.allOf(GatewayIntent.class).toArray(new GatewayIntent[0]));
        jda = jdaBuilder.build().awaitReady();
        setupCommands();
        // setPerms();
        // setSlowMode();
    }

    private static void setup() {
        MongoClient mongoClient = MongoClients.create(
            "mongodb+srv://SegganBot:" + Main.config.get("mongo").getAsString() + "@cluster0.9lcjl.mongodb.net/<dbname>?retryWrites=true&w=majority");
        MongoDatabase database = mongoClient.getDatabase("segganbot");

        warningDb = database.getCollection("warnings");

        for (Document document : warningDb.find()) {
            warnings.add(MongoUtil.deserializeWarning(document));
        }

        commandsDb = database.getCollection("commands");
        for (Document document : commandsDb.find()) {
            tags.put(document.getString("_id"), document.getString("message"));
        }
    }

    private static void setupCommands() {
        // admin
        new PingCommand();
        new MuteCommand();
        new KickCommand();
        new BanCommand();
        new WarnCommand();
        new SetTagCommand();

        // slash
        //new ReleaseSlash();
        //new SaySlash();
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
        for (Category category : jda.getCategories()) {
            ChannelManager manager = category.getManager();

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
