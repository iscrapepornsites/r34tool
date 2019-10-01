package me.mux.aps.models;

import java.util.List;

import org.bson.types.ObjectId;

public class Post {
	private ObjectId id;
	private ObjectId postMedia;
	private int postId;
	private String uploader;
	private List<String> tags;
	private String postUrl;
	private String mediaUrl;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getPostMedia() {
		return postMedia;
	}

	public void setPostMedia(ObjectId postMedia) {
		this.postMedia = postMedia;
	}

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}
	public String getUploader() {
		return uploader;
	}

	public void setUploader(String uploader) {
		this.uploader = uploader;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getPostUrl() {
		return postUrl;
	}

	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

}
