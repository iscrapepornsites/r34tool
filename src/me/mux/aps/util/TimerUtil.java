package me.mux.aps.util;

import java.util.concurrent.ConcurrentHashMap;

public class TimerUtil {
	private ConcurrentHashMap<Integer, Long> timerMap = new ConcurrentHashMap<>();
	private long count = 0;
	private double average;

	public void start(int id) {
		timerMap.put(id, System.currentTimeMillis());
	}

	public long stop(int id) {
		long time = getTime(id);
		updateAvg(time);
		timerMap.remove(id);
		return time;
	}

	public void cancel(int id) {
		timerMap.remove(id);
	}

	private synchronized void updateAvg(long time) {
		if (count == 0)
			average = time;
		else
			average = (average * count + time) / (count + 1d);
		count++;
	}

	public void reset() {
		count = 0;
		average = 0;
		timerMap = new ConcurrentHashMap<Integer, Long>();
	}

	private long getTime(int id) {
		long time = System.currentTimeMillis() - timerMap.get(id);
		return time > 0 ? time : 1;
	}

	public double getAverage() {
		return average;
	}
}
