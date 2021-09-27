package io.github.seggan.segganbot.commands;

import io.github.seggan.segganbot.Listener;
import io.github.seggan.segganbot.Util;
import io.github.seggan.segganbot.constants.Patterns;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Arrays;
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

    public final void startExecution(@NotNull Message message) {
        Member member = message.getMember();
        assert member != null;

        if (!Util.isAdmin(member)) {
            message.getChannel().sendMessage("You must be staff to execute this command").queue();
            return;
        }

        String[] split = Patterns.SPACE.split(message.getContentRaw()); // get arguments
        String[] args = Arrays.copyOfRange( // remove 1st item (the command itself)
            split,
            1,
            split.length
        );

        ListOrderedMap<String, String> pass = new ListOrderedMap<>();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String s = args[i];

            String req;
            try {
                req = argumentTypes.getValue(i); // get the corresponding required type
            } catch (IndexOutOfBoundsException e) {
                req = "\0"; // if not found set to null string
            }

            if (!builder.isEmpty() || req.endsWith("...")) { // if were constructing a vararg or the type is a vararg
                builder.append(s); // add the string to the vararg
                builder.append(' ');
            }

            if (builder.isEmpty()) { // if not constructing vararg
                if (!getType(s).equals(req)) { // if wrong type (null string will return false so it will fail)
                    message.getChannel().sendMessage("Format: !" + name + ' ' + argumentString).queue();
                    return;
                }

                pass.put(argumentTypes.get(i), s); // otherwise add to parameters
            }
        }

        if (!builder.isEmpty()) { // if we were constructing a vararg
            builder.deleteCharAt(builder.lastIndexOf(" "));
            pass.put(argumentTypes.lastKey(), builder.toString()); // add the vararg parameter
        }

        if (pass.size() != argumentTypes.size()) {
            message.getChannel().sendMessage("Format: !" + name + ' ' + argumentString).queue();
            return;
        }

        try {
            execute(message, pass, member);
        } catch (Exception e) {
            String trace;
            try (StringWriter writer = new StringWriter();
                 PrintWriter printWriter = new PrintWriter(writer)) {
                e.printStackTrace(printWriter);
                trace = writer.toString();
            } catch (IOException ioException) {
                throw new UncheckedIOException(ioException);
            }

            message.getChannel().sendMessage(String.format("""
                    ```
                    %s
                    ```""",
                trace.substring(0, Math.min(trace.length(), 1990))
            )).queue();
        }
    }

    @NotNull
    private String getType(String s) {
        if (Patterns.INTEGER.matcher(s).matches()) {
            return "int";
        } else {
            return "string";
        }
    }

    protected abstract void execute(@NotNull Message message, @NotNull ListOrderedMap<String, String> args, @NotNull Member member);

    public String getName() {
        return name;
    }
}
