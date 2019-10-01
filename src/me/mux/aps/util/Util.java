package me.mux.aps.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mongodb.BasicDBObject;
import com.mongodb.client.gridfs.GridFSBucket;

import me.mux.aps.models.Post;
import me.mux.aps.mongo.GridFSHelper;
import me.mux.aps.mongo.MongoAdapter;
import me.mux.aps.request.Getter;

public class Util {
	private static final String QUERY_ISPOST = "#post-view > div.sidebar > div:nth-child(7) > ul > li > a > img";
	private static final String QUERY_TAG = "li.tag > a";
	private static final String QUERY_UPLOADER = "#stats > ul > li:nth-child(2) > a";
	private static final String QUERY_MEDIA = "#post-view > div.sidebar > div:nth-child(9) > ul > li:nth-child(2) > a";
	private static final BasicDBObject BY_ID_DESC = new BasicDBObject("postId", -1);

	public static boolean isPostPage(Document d) {
		return !d.select(QUERY_ISPOST).isEmpty();
	}

	public static List<String> extractTags(Document d) {
		ArrayList<String> tags = new ArrayList<>();
		for (Element e : d.select(QUERY_TAG)) {
			tags.add(e.text().replace(' ', '_'));
		}
		return tags;
	}

	public static int extractPostId(Document d) {
		return Integer.parseInt(d.baseUri().substring(d.baseUri().lastIndexOf('=') + 1));
	}

	public static String extractMediaUrl(Document d) {
		return d.selectFirst(QUERY_MEDIA).absUrl("href");
	}

	public static String extractUploader(Document d) {
		return d.selectFirst(QUERY_UPLOADER).text();
	}

	public static String extractPostUrl(Document d) {
		return d.baseUri();
	}

	public static Post extractAll(Document d) {
		Post p = new Post();
		p.setPostId(extractPostId(d));
		p.setPostUrl(extractPostUrl(d));
		p.setTags(extractTags(d));
		p.setUploader(extractUploader(d));
		p.setMediaUrl(extractMediaUrl(d));
		return p;
	}

	public static String getFileName(String mediaUrl) {
		return FilenameUtils.getName(mediaUrl);
	}

	public static String getType(String mediaUrl) {
		switch (FilenameUtils.getExtension(mediaUrl).toLowerCase()) {
		case "jpg":
		case "png":
		case "bmp":
		case "tiff":
		case "jpeg":
		case "webp":
			return "image";
		case "webm":
		case "mp4":
		case "gif":
			return "animated";
		default:
			return "unknown";
		}
	}

	public static int getLastRemoteId() {
		String url = Getter.get("https://rule34.xxx/index.php?page=post&s=list").selectFirst(".thumb > a")
				.absUrl("href");
		return Integer.parseInt(url.substring(url.lastIndexOf('=') + 1));
	}

	public static int getLastLocalId() {
		Post p = MongoAdapter.getDatabase().getCollection("posts", Post.class).find().limit(1).sort(BY_ID_DESC).first();
		if (p != null)
			return p.getPostId();
		return 0;
	}

	public static void downloadMedia(Post p, GridFSBucket bucket) throws MalformedURLException, IOException {

		ObjectId id = GridFSHelper.uploadFromUrl(p.getMediaUrl(), getFileName(p.getMediaUrl()),
				getType(p.getMediaUrl()), bucket);
		p.setPostMedia(id);
	}
	public static ObjectId downloadMediaExternalSet(Post p, GridFSBucket bucket) throws MalformedURLException, IOException {

		ObjectId id = GridFSHelper.uploadFromUrl(p.getMediaUrl(), getFileName(p.getMediaUrl()),
				getType(p.getMediaUrl()), bucket);
		return id;
	}

}
