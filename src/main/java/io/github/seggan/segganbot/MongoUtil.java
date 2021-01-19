package io.github.seggan.segganbot;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public final class MongoUtil {
    private MongoUtil() {
    }

    public static void addWarning(MongoCollection<Document> collection, Warning warning) {
        Document document = new Document();
        document.append("player", warning.getPlayerId());
        document.append("time", warning.getTime());
        document.append("reason", warning.getReason());

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
