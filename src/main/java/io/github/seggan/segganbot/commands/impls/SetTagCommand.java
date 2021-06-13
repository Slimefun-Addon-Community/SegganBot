package io.github.seggan.segganbot.commands.impls;

import io.github.seggan.segganbot.Main;
import io.github.seggan.segganbot.Util;
import io.github.seggan.segganbot.commands.AbstractAdminCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public class SetTagCommand extends AbstractAdminCommand {

    public SetTagCommand() {
        super("settag", "<name:string> <content:string...>");
    }

    @Override
    protected void execute(@NotNull Message message, @NotNull ListOrderedMap<String, String> args, @NotNull Member member) {
        String content = args.get("content");
        loop:
        {
            Document d = new Document("_id", args.get("name"));
            for (Document document : Main.commandsDb.find()) {
                if (document.get("_id").equals(args.get("name"))) {
                    Main.commandsDb.replaceOne(document, d.append("message", content));
                    break loop;
                }
            }
            Main.commandsDb.insertOne(d.append("message", content));
        }

        Main.tags.put(args.get("name"), content);

        message.getChannel().sendMessage(Util.parseMessage(null, content).build()).queue();
    }
}
