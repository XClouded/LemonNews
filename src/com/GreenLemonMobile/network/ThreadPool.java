package com.GreenLemonMobile.network;

import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Vector;

import com.GreenLemonMobile.config.Configuration;
import com.GreenLemonMobile.network.HttpGroup.HttpGroupaAsynPool.HttpRequestRunnable;
import com.GreenLemonMobile.network.HttpGroup.HttpRequest;
import com.GreenLemonMobile.util.IPriority;
import com.GreenLemonMobile.util.PriorityCollection;

public class ThreadPool {
	
	private static ThreadPool sPool;

	static {
		sPool = new ThreadPool(Integer.parseInt(Configuration.getProperty(Configuration.MAX_POOL_SIZE)), Integer.parseInt(Configuration.getProperty(Configuration.INIT_POOL_SIZE)));
		sPool.init();
	}

	public static ThreadPool getThreadPool() {
		return sPool;
	}

	protected int maxPoolSize = 2;
	protected int initPoolSize = 2;
	
	@SuppressWarnings("rawtypes")
	protected Vector threads = new Vector<PooledThread>();
	
	protected boolean isInitialized = false;
	protected boolean hasIdleThread = false;
	
	protected PriorityQueue<IPriority> queue = new PriorityQueue<IPriority>();
	
	protected ThreadPool(int maxPoolSize, int initPoolSize) {
		super();
		this.maxPoolSize = maxPoolSize;
		this.initPoolSize = initPoolSize;
	}
	
	protected int getPoolSize() {
		return threads.size();
	}
	
	protected void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
		if (maxPoolSize < getPoolSize())
			setPoolSize(maxPoolSize);
	}
	
	@SuppressWarnings("unchecked")
	protected void setPoolSize(int poolSize) {
		if (isInitialized && poolSize > 0) {
			if (poolSize > getPoolSize()) {
				for (int i = getPoolSize(); i < poolSize && i < maxPoolSize; ++i) {
					PooledThread thread = new PooledThread(this);
					thread.start();
					
					threads.add(thread);
				}
			} else if (poolSize < getPoolSize()) {
				while (getPoolSize() > poolSize)
					((PooledThread)threads.remove(threads.size() - 1)).kill();
			}
		}
	}
	
	protected synchronized void notifyForIdleThread() {
		hasIdleThread = true;
		notify();
	}
	
	protected synchronized boolean waitForIdleThread() {
		hasIdleThread = false;
		
		while (!hasIdleThread && getPoolSize() >= maxPoolSize) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected PooledThread getIdleThread() {
		while (true) {
			for (Iterator iter = threads.iterator(); iter.hasNext();) {
				PooledThread thread = (PooledThread) iter.next();
				if (!thread.isRunning())
					return thread;
			}
			
			if (getPoolSize() < maxPoolSize) {
				PooledThread thread = new PooledThread(this);
				thread.start();
				
				threads.add(thread);
			}
			
			if (!waitForIdleThread())
				return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected void init() {
		if (!isInitialized) {
			isInitialized = true;
			
			for (int i = 0; i < initPoolSize; ++i) {
				PooledThread thread = new PooledThread(this);
				thread.start();
				
				threads.add(thread);
			}
			
			new Thread() {

				@Override
				public void run() {
					super.run();
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
					while (true) {
						PooledThread idleThread = getIdleThread();
						if (idleThread != null) {
							Collection<Runnable> pollTasks = (Collection<Runnable>) pollTasks();
							if (null != pollTasks) {
								idleThread.putTasks(pollTasks);
								idleThread.startTasks();
							} else {
								synchronized (queue) {
									try {
										queue.wait();
									} catch (InterruptedException e) {
									}
								}
							}
						}
					}					
				}
				
			}.start();
		}
	}
	
	public synchronized void offerTask(Runnable runnable, int priority) {
		PriorityCollection<Runnable> list = new PriorityCollection<Runnable>(priority);
		list.add(runnable);
		offerTasks(list);
	}
	
	public synchronized void offerTasks(IPriority list) {
		queue.offer(list);
		synchronized (queue) {
			queue.notify();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public synchronized void removeTask(final HttpRequest httpRequest) {
		Vector<Object> list = new Vector<Object>();
		Iterator iter = queue.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof PriorityCollection) {
				@SuppressWarnings("unchecked")
				HttpRequestRunnable runable = (HttpRequestRunnable)(((PriorityCollection<Runnable>)obj).get(0));
				if (runable.getHttpRequest() != httpRequest) {
					list.add(obj);
				}
			}
		}
		queue.clear();
		for (Object obj : list) {
			queue.offer((IPriority) obj);
		}
	}	
	
	private synchronized IPriority pollTasks() {
		return queue.poll();
	}	
}
