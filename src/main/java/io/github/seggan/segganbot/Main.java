package io.github.seggan.segganbot;

import com.besaba.revonline.pastebinapi.Pastebin;
import com.besaba.revonline.pastebinapi.impl.factory.PastebinFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDABuilder;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Main {
    public static final PastebinFactory factory = new PastebinFactory();
    public static final Pastebin pastebin = setupPastebin();

    private static Pastebin setupPastebin() {
        try {
            return factory.createPastebin(getResourceAsString("pastebin.txt"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public static void main(String[] args) throws IOException, LoginException, InterruptedException {
        JDABuilder jdaBuilder = JDABuilder.createDefault(getResourceAsString("token.txt"));
        jdaBuilder.addEventListeners(new Listener());
        jdaBuilder.build().awaitReady();

        String json;
        File file = new File("commands.json");
        if (!file.exists()) {
            throw new AssertionError("File commands.json does not exist!");
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            json = new String(data, StandardCharsets.UTF_8);
        }

        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Listener.tags.put(entry.getKey(), entry.getValue().getAsString());
        }

        File warningFile = new File("warnings.json");
    }

    @Nonnull
    private static String getResourceAsString(@Nonnull String resource) throws IOException {
        InputStream stream = Main.class.getResourceAsStream("/" + resource);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }
}
