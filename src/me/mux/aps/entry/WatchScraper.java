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

public class WatchScraper extends Thread {
	private long delayInbetweenCrawls;

	public WatchScraper(long interval) {
		this.delayInbetweenCrawls = interval;
	}

	private static final int INFO_INTERVAL = 100;
	private MongoCollection<Post> postCollection = MongoAdapter.getDatabase().getCollection("posts", Post.class);
	private MongoCollection<org.bson.Document> gridFsCollection = MongoAdapter.getDatabase().getCollection("fs.files");
	private long total = 0;
	private long failed = 0;
	private long mediaFailed = 0;
	private long postsLeft = 0;
	private long iteration = 0;
	private int lastId = 0;

	@Override
	public void run() {
		while (true) {
			iteration++;
			lastId = Util.getLastLocalId();
			int start = lastId + 1;
			final TimerUtil tu = new TimerUtil();
			int end = Util.getLastRemoteId();
			System.out.printf("Starting from %d%n", start);
			long totalPostEstimate = end - start;
			System.out.printf("Estimated Post Count: %d%n", totalPostEstimate);
			if (totalPostEstimate < 1) {
				System.out.printf("No posts, waiting...%n");
				try {
					Thread.sleep(delayInbetweenCrawls);
					continue;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
			for (int i = start; i <= end; i++) {
				postsLeft = totalPostEstimate - total;
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
						// System.err.printf("Error download media for %d%n", i);
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
			System.out.println("Update complete, waiting...");
			try {
				Thread.sleep(delayInbetweenCrawls);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
	}

	public void printInfo(TimerUtil tu) {
		System.out.println("===============================================================================");
		System.out.printf("Iteration: %d, LastId: %d%n", iteration, lastId);
		System.out.printf("Average Post Time: %4.2fms%n", tu.getAverage());
		if (postsLeft > 0) {
			System.out.printf("Estimated time left: %s%n",
					DurationFormatUtils.formatDurationWords((long) (tu.getAverage() * postsLeft), true, true));
		}
		System.out.printf("GFS Docs: %d, Post Docs: %d%n", gridFsCollection.estimatedDocumentCount(),
				postCollection.estimatedDocumentCount());
		System.out.printf("Total Count: %d, Failed: %d, Media Failed: %d%n", total, failed, mediaFailed);
		System.out.println("===============================================================================");
	}

}
