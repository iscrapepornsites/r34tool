package me.mux.aps.entry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import me.mux.aps.mongo.MongoAdapter;
import me.mux.aps.request.Getter;

public class Entry {

	public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
		if (args.length == 0) {
			System.err.println("Requires at least one argument");
			System.err.println("Usage:");
			System.err.println("drop\t\t\t\t\t\tDrops all collections");
			System.err.println("<delay> initial\t\t\t\t\tStarts the initial crawling (use watch instead)");
			System.err.println(
					"<delay> watch <interval>\t\t\tWatch the site every <interval> milliseconds (or full scrape)");
			System.err.println("dumpall <folder>\t\t\t\tDump ALL files to the folder");
			System.err.println("dumpallzip <zipfile>\t\t\t\tDump ALL files to the zipfile");
			System.err.println("dump <folder> [tags...]\t\t\t\tDump the selected tags to the folder");
			System.err.println("dumpzip <zipfile> [tags...]\t\t\tDump the selected tags to the zipfile");
			System.err.println("dumpsplit <folder> <interval>\t\t\tDump everything into multiple folders");
			System.err.println("");
			System.exit(1);
		}
		MongoAdapter.init();

		if (args.length == 1 && args[0].equals("drop")) {
			MongoAdapter.dropAllCollections();
			System.out.println("Dropped collections");
			System.exit(0);
		} else if (args.length == 2 && args[1].equals("initial")) {
			// Initial Build (Full Scrape)
			int scraperDelay = Integer.parseInt(args[0]);
			Getter.DELAY = scraperDelay;
			System.out.println("Starting initial Crawling");
			FullScraper fs = new FullScraper();
			fs.start();
			Thread.sleep(1000);
			System.out.println("Main Thread exited");
		} else if (args.length == 3 && args[1].equals("watch")) {
			// Watch Mode (Incremental Scrape)
			int scraperDelay = Integer.parseInt(args[0]);
			Getter.DELAY = scraperDelay;
			int delay = Integer.parseInt(args[2]);
			if (delay < 1000) {
				System.err.println("Delay must be > 1000ms");
				System.exit(1);
			}
			WatchScraper us = new WatchScraper(delay);
			us.start();
			Thread.sleep(1000);
			System.out.println("Main Thread exited");
		} else if (args.length == 2 && args[0].equals("dumpall")) {
			String folder = args[1];
			FullDumper d = new FullDumper(folder);
			d.start();
			Thread.sleep(1000);
			System.out.println("Main Thread exited");
		} else if (args.length == 3 && args[0].equals("dumpsplit")) {
			String folder = args[1];
			int interval = Integer.parseInt(args[2]);
			SplitDumper d = new SplitDumper(folder, interval);
			d.start();
			Thread.sleep(1000);
			System.out.println("Main Thread exited");
		} else if (args.length == 2 && args[0].equals("dumpallzip")) {
			String file = args[1];
			FullZipDumper d = new FullZipDumper(file);
			d.start();
			Thread.sleep(1000);
			System.out.println("Main Thread exited");
		} else if (args.length >= 3 && args[0].equals("dump")) {
			String folder = args[1];
			List<String> tags = new ArrayList<>();
			for (int i = 2; i < args.length; i++) {
				tags.add(args[i]);
			}
			Dumper d = new Dumper(folder, tags);
			d.start();
			Thread.sleep(1000);
			System.out.println("Main Thread exited");
		} else if (args.length >= 3 && args[0].equals("dumpzip")) {
			String file = args[1];
			List<String> tags = new ArrayList<>();
			for (int i = 2; i < args.length; i++) {
				tags.add(args[i]);
			}
			ZipDumper d = new ZipDumper(file, tags);
			d.start();
			Thread.sleep(1000);
			System.out.println("Main Thread exited");
		} else if (args.length == 2 && args[0].equals("check")) {
			System.err.println("Checker is Broken");
			return;
//			int scraperDelay = Integer.parseInt(args[1]);
//			Getter.DELAY = scraperDelay;
//			Checker c = new Checker();
//			c.start();
//			Thread.sleep(1000);
//			System.out.println("Main Thread exited");
		}

	}

}
