package com.lacantera.lacantera;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.mongojack.JacksonMongoCollection;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.mongodb.client.model.Filters.eq;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    protected final MongoCollection<Greeting> collection = getCollection("LaCantera", "products", Greeting.class);

    @GetMapping("/all")
    public List<Greeting> getAll() {
        List<Greeting> greetings = new ArrayList<>();
        this.collection.find().forEach(greetings::add);
        return greetings;
    }

    @PostMapping("/create")
    public void create(@RequestParam(value="content", defaultValue="Pepe Juan") String content) {
        Greeting greeting = new Greeting();
        greeting.setId(generateId());
        greeting.setContent(content);
        this.collection.insertOne(greeting);
    }

    @PutMapping("/update")
    public Greeting update(@RequestBody Greeting updatedGreeting) {
        return this.collection.findOneAndReplace(
            eq("_id", updatedGreeting.getId()), updatedGreeting, new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER));
    }


    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
            this.collection.findOneAndDelete(eq("_id", id));
    }

    public static <T> JacksonMongoCollection<T> getCollection(String database, String collection, Class<T> valueType) {
        MongoClient mongoClient = getMongoClient();
        return JacksonMongoCollection.builder()
            .build(mongoClient, database, collection, valueType, UuidRepresentation.STANDARD);
    }

    private static MongoClient getMongoClient() {
        String mongoHost = "localhost";
        Integer mongoPort = 27017;
        ConnectionString connString = new ConnectionString(String.format("mongodb://%s:%s", mongoHost, mongoPort));
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
            PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(connString)
            .codecRegistry(codecRegistry)
            .retryWrites(true)
            .build();
        return MongoClients.create(settings);
    }

    public String generateId() {
        return new ObjectId().toHexString();
    }

}
