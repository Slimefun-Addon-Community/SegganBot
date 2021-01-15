package io.github.seggan.segganbot;

import net.dv8tion.jda.api.JDABuilder;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException, LoginException, InterruptedException {
        JDABuilder jdaBuilder = JDABuilder.createDefault(getResourceAsString("token.txt"));
        jdaBuilder.addEventListeners(new Listener());
        jdaBuilder.build().awaitReady();
        // JDA jda = JDABuilder.createDefault(getResourceAsString("testing_token.txt")).build().awaitReady();
        // jda.getTextChannelById(799455256793710622L).sendMessage("test").queue();
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
