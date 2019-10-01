package me.mux.aps.request;

public class Throttler {
	private long last;
	private long delay;
	public Throttler(long delay) {
		this.last = System.currentTimeMillis();
		this.delay = delay;
	}
	
	public void await() {
		while(System.currentTimeMillis() < (last + delay))
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		last = System.currentTimeMillis();		
	}
	
	public void await(Runnable r) {
		while(System.currentTimeMillis() < (last + delay))
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {}
		last = System.currentTimeMillis();		
		r.run();
	}
}
