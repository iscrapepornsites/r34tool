package me.mux.aps.request;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.mux.aps.util.Util;

public class Getter {
	public static String USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X x.y; rv:10.0) Gecko/20100101 Firefox/10.0";
	public static long DELAY = 250;
	public static long TIMEOUT = 60000;
	private static Throttler throttler;

	public static Document getPost(String url) {
		if (throttler == null)
			throttler = new Throttler(DELAY);
		try {
			throttler.await();
			Document d = Jsoup.connect(url).timeout(30000).userAgent(USERAGENT).get();
			if (Util.isPostPage(d))
				return d;
			else
				return null;
		} catch (IOException e) {
			System.err.printf("%nGot error, awaiting cooldown (60s)%n");
			try {
				Thread.sleep(TIMEOUT);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return null;
		}
	}

	public static Document get(String url) {
		if (throttler == null)
			throttler = new Throttler(DELAY);
		try {
			throttler.await();
			return Jsoup.connect(url).timeout(30000).userAgent(USERAGENT).get();
		} catch (IOException e) {
			System.err.printf("%nGot error, awaiting cooldown (60s)%n");
			try {
				Thread.sleep(TIMEOUT);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return null;
		}
	}

	public static Document getPost(int id) {
		return getPost(String.format("https://rule34.xxx/index.php?page=post&s=view&id=%d", id));
	}
}
