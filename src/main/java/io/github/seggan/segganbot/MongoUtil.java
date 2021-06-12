package io.github.seggan.segganbot;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MongoUtil {

    public static void addWarning(MongoCollection<Document> collection, Warning warning) {
        Document document = new Document();
        document.append("player", warning.playerId());
        document.append("time", warning.time());
        document.append("reason", warning.reason());

        collection.insertOne(document);
    }

    public static Warning deserializeWarning(Document document) {
        return new Warning(
            document.getLong("player"),
            document.getDate("time").toInstant(),
            document.getString("reason")
        );
    }
}
