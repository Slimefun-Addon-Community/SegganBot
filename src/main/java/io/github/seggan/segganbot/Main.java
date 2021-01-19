package io.github.seggan.segganbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;

public class Main {
    public static final JsonObject config = JsonParser.parseString(Util.getFileAsString(new File("config.json"))).getAsJsonObject();

    public static void main(String[] args) throws LoginException, InterruptedException {
        JDABuilder jdaBuilder = JDABuilder.createDefault(config.get("discord").getAsString());
        jdaBuilder.addEventListeners(new Listener());
        jdaBuilder.build().awaitReady();
    }
}
