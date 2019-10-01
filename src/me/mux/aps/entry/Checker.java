package me.mux.aps.entry;

import java.util.regex.Pattern;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import me.mux.aps.models.Post;
import me.mux.aps.mongo.MongoAdapter;
import me.mux.aps.request.Getter;
import me.mux.aps.request.Throttler;
import me.mux.aps.util.Util;

public class Checker extends Thread {

	private static final int INFO_INTERVAL = 250;
	private static Throttler throttle;

	public Checker() {
		throttle = new Throttler(Getter.DELAY);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		MongoCollection<Post> posts = MongoAdapter.getDatabase().getCollection("posts", Post.class);
		final Bson filter = Filters.and(
				Filters.or(Filters.not(Filters.exists("postMedia")), Filters.eq("postMedia", null)),
				Filters.not(Filters.regex("postMedia", Pattern.compile(".*index\\.php.*"))));
		System.out.printf("%s%n", filter.toString());
		long iteration = 0;
		GridFSBucket bucket = MongoAdapter.getBucket();
		FindIterable<Post> it = posts.find(filter);
		for (Post p : it) {

			iteration++;
			if (iteration % INFO_INTERVAL == 0) {
				System.out.printf("%n");
				System.out.printf("Completed: %7d%n", iteration);
			}
			try {
				final ObjectId id = Util.downloadMediaExternalSet(p, bucket);
				if (id == null) {
					System.out.printf("-");
					continue;
				} else {
					throttle.await();
				}
				final Bson postFilter = Filters.eq("_id", p.getId());
				final Bson update = Updates.set("postMedia", id);
				posts.updateOne(postFilter, update);
				System.out.printf(".");
			} catch (Exception e) {
				System.out.printf("#");
			}
			return;
		}
		System.out.printf("Done %d checks%n", iteration);
	}

}
