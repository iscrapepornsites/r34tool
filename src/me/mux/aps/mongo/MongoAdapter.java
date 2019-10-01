package me.mux.aps.mongo;

import java.util.Arrays;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;

public class MongoAdapter {
	private static final String DBHOST = "10.0.0.212";
	private static final int DBPORT = 27017;
	private static final String DBUSER = "aps";
	private static final String DBPASS = "QBrEXbdGOFBHaPe1";
	private static final String DBNAME = "aps";

	private static CodecRegistry registry;

	private static MongoDatabase database;
	private static MongoClient client;
	private static GridFSBucket bucket;

	private static void initGridFS() {
		bucket = GridFSBuckets.create(database);
	}

	private static void initMongoDb() {
		MongoCredential creds = MongoCredential.createCredential(DBUSER, DBNAME, DBPASS.toCharArray());
		MongoClientSettings settings = MongoClientSettings.builder().credential(creds)
				.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(DBHOST, DBPORT))))
				.build();
		client = MongoClients.create(settings);
		database = client.getDatabase(DBNAME);
	}

	private static void initRegistry() {
		registry = CodecRegistries.fromRegistries(com.mongodb.MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		database = database.withCodecRegistry(registry);
	}

	public static void init() {
		initMongoDb();
		initRegistry();
		initGridFS();
	}

	public static MongoDatabase getDatabase() {
		return database;
	}

	public static MongoClient getClient() {
		return client;
	}

	public static GridFSBucket getBucket() {
		return bucket;
	}

	public static void dropAllCollections() {
		database.getCollection("posts").drop();
		database.getCollection("fs.chunks").drop();
		database.getCollection("fs.files").drop();
	}

}
