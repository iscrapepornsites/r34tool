package me.mux.aps.entry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

import me.mux.aps.models.Post;
import me.mux.aps.mongo.MongoAdapter;

public class SplitDumper extends Thread {

	private String folder;
	private int split;
	private static final int INFO_INTERVAL = 100;

	public SplitDumper(String folder, int splitInterval) {
		this.folder = folder;
		this.split = splitInterval;
	}

	@Override
	public void run() {
		int splits = 0;
		
		File outFolder = new File(folder + "_" + splits);
		if (!outFolder.exists()) {
			outFolder.mkdirs();
		}

		MongoCollection<Post> posts = MongoAdapter.getDatabase().getCollection("posts", Post.class);
		GridFSBucket bucket = MongoAdapter.getBucket();
		final Bson filter = Filters.exists("postMedia");
		System.out.println(filter.toString());
		long count = posts.countDocuments(filter);
		if (count == 0) {
			System.err.println("There are no posts");
			return;
		}
		System.out.printf("Dumping %d files%n", count);
		FindIterable<Post> result = posts.find(filter);
		int it = 0;
		for (Post p : result) {
			it++;
			if (it % INFO_INTERVAL == 0) {
				System.out.println();
			}
			if (it % split == 0) {
				System.out.printf("%nCurrent split: %d%n", splits);
				splits++;
				outFolder = new File(folder + "_" + splits);
				if (!outFolder.exists()) {
					outFolder.mkdirs();
				}
			}
			GridFSFile f = bucket.find(Filters.eq("_id", p.getPostMedia())).first();
			if (f != null) {
				Path outPath = Paths.get(outFolder.getAbsolutePath(), f.getFilename());
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(outPath.toFile());
					bucket.downloadToStream(f.getId(), fos);
					System.out.printf("+");
				} catch (FileNotFoundException e) {
					System.out.printf("!");
				} finally {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		System.out.println("Done!");
	}
}
