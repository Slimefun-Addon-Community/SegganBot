package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.Listener;
import io.github.seggan.segganbot.constants.Patterns;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class AbstractAdminCommand {

    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("(<\\w+:\\w+>( )*)+");


    private final String name;

    private final String argumentString;

    private final ListOrderedMap<String, String> argumentTypes = new ListOrderedMap<>();

    public AbstractAdminCommand(@NotNull String name, @Nullable String arguments) {
        this.name = name;

        Listener.adminCommands.add(this);

        if (arguments == null) {
            this.argumentString = "";
            return;
        }

        assert ARGUMENT_PATTERN.matcher(arguments).matches() : arguments;
        this.argumentString = arguments;

        for (String arg : Patterns.SPACE.split(arguments)) {
            arg = arg.substring(1, arg.length() - 1);

            String[] split = Patterns.COLON.split(arg);
            argumentTypes.put(split[0], split[1]);
        }
    }

    public void startExecuting(Message message) {
        String[] split = Patterns.SPACE.split(message.getContentRaw());
        String[] args = Arrays.copyOfRange(
            split,
            1,
            split.length
        );

        Map<String, String> pass = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            String s = args[i];

            String req;
            try {
                req = argumentTypes.getValue(i);
            } catch (IndexOutOfBoundsException e) {
                req = "\0";
            }

            if (!getType(s).equals(req)) {
                message.getChannel().sendMessage("Format: " + argumentString).queue();
                return;
            }

            pass.put(argumentTypes.get(i), s);
        }

        execute(message, pass, message.getContentRaw(), Objects.requireNonNull(message.getMember()));
    }

    @NotNull
    private String getType(String s) {
        if (Patterns.INTEGER.matcher(s).matches()) {
            return "int";
        } else {
            return "string";
        }
    }

    protected abstract void execute(@NotNull Message message, @NotNull Map<String, String> args, @NotNull String content, @NotNull Member member);

    public String getName() {
        return name;
    }
}
