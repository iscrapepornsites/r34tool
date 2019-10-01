package me.mux.aps.entry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;

import me.mux.aps.models.Post;
import me.mux.aps.mongo.MongoAdapter;

public class ZipDumper extends Thread {

	private List<String> tags;
	private String outFile;
	private static final int INFO_INTERVAL = 100;

	public ZipDumper(String file, List<String> tags) {
		this.outFile = file;
		this.tags = tags;
	}

	@Override
	public void run() {
		File outFile = new File(this.outFile);

		MongoCollection<Post> posts = MongoAdapter.getDatabase().getCollection("posts", Post.class);
		GridFSBucket bucket = MongoAdapter.getBucket();
		final Bson filter = Filters.and(Filters.all("tags", tags), Filters.exists("postMedia"));
		System.out.println(filter.toString());
		long count = posts.countDocuments(filter);
		if (count == 0) {
			System.err.println("There are no posts matching those tags");
			return;
		}
		System.out.printf("Dumping %d files%n", count);
		FindIterable<Post> result = posts.find(filter);
		int it = 0;
		FileOutputStream zFile = null;
		ZipOutputStream zip = null;
		try {
			zFile = new FileOutputStream(outFile);
			zip = new ZipOutputStream(zFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Post p : result) {
			it++;
			if (it % INFO_INTERVAL == 0) {
				System.out.println();
			}
			GridFSFile f = bucket.find(Filters.eq("_id", p.getPostMedia())).first();
			if (f != null) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ZipEntry ze = new ZipEntry(f.getFilename());
					zip.putNextEntry(ze);
					bucket.downloadToStream(f.getId(), baos);
					zip.write(baos.toByteArray());
					zip.closeEntry();
					baos.close();
					System.out.printf("+");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			zip.close();
			System.out.println("Done!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
