package me.mux.aps.mongo;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;

public class GridFSHelper {
	private static final int CHUNKSIZE = 131072;

	public static ObjectId uploadFromUrl(String url, String fileName, String type, GridFSBucket bucket)
			throws MalformedURLException, IOException {
		if (url.contains("index.php") || fileName.contains("index.php"))
			return null;
		InputStream fStream = new URL(url).openStream();
		GridFSUploadOptions opts = new GridFSUploadOptions().chunkSizeBytes(CHUNKSIZE)
				.metadata(new Document("type", type));
		return bucket.uploadFromStream(fileName, fStream, opts);
	}
	
}
