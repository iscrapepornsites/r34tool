package me.mux.aps.entry;

import java.io.IOException;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jsoup.nodes.Document;

import com.mongodb.client.MongoCollection;

import me.mux.aps.models.Post;
import me.mux.aps.mongo.MongoAdapter;
import me.mux.aps.request.Getter;
import me.mux.aps.util.TimerUtil;
import me.mux.aps.util.Util;

public class FullScraper extends Thread {
	private static final int INFO_INTERVAL = 100;
	private MongoCollection<Post> postCollection = MongoAdapter.getDatabase().getCollection("posts", Post.class);
	private MongoCollection<org.bson.Document> gridFsCollection = MongoAdapter.getDatabase()
			.getCollection("fs.files");
	private long total = 0;
	private long failed = 0;
	private long mediaFailed = 0;
	private long postsLeft = 0;
	
	private TimerUtil tu = new TimerUtil();
	
	@Override
	public void run() {		
		int end = Util.getLastRemoteId();
		System.out.printf("Estimated Post Count: %d%n", end);
		for (int i = 1; i <= end; i++) {
			postsLeft = end - total;
			// Print Info
			if (i % INFO_INTERVAL == 0 && i != 0) {
				System.out.println();
				printInfo(tu);
			}
			tu.start(i);
			Document d = Getter.getPost(i);
			if (d != null) {
				Post p = Util.extractAll(d);
				try {
					Util.downloadMedia(p, MongoAdapter.getBucket());
				} catch (IOException e) {
					//System.err.printf("Error download media for %d%n", i);
					mediaFailed++;
					p.setPostMedia(null);
				} finally {
					postCollection.insertOne(p);
					long t = tu.stop(i);
					System.out.print(t > tu.getAverage() ? "+" : "-");
				}
			} else {
				failed++;
				tu.cancel(i);
			}
			total++;
		}
		
		printInfo(tu);
		System.out.println("Done.");
	}

	public void printInfo(TimerUtil tu) {
		System.out.println("===============================================================================");
		System.out.printf("Average Post Time: %4.2fms%n", tu.getAverage());
		System.out.printf("Estimated time left: %s%n",
				DurationFormatUtils.formatDurationWords((long) (tu.getAverage() * postsLeft), true, true));
		System.out.printf("GFS Docs: %d, Post Docs: %d%n", gridFsCollection.estimatedDocumentCount(),
				postCollection.estimatedDocumentCount());
		System.out.printf("Total Count: %d, Failed: %d, Media Failed: %d%n", total, failed, mediaFailed);
		System.out.println("===============================================================================");	
	}

}
