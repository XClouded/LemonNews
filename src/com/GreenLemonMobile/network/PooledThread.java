package com.GreenLemonMobile.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PooledThread extends Thread{
	
	protected boolean isRunning = false;
	protected boolean isStopped = false;
	protected boolean isPaused = false;
	protected boolean isKilled = false;
	
	protected int SYNC_TIMEOUT = 200;
	
	private ThreadPool pool;
	
	private List<Runnable> tasks = new ArrayList<Runnable>();
	
	protected PooledThread(ThreadPool pool) {
		this.pool = pool;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void putTask(Runnable task) {
		tasks.add(task);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void putTasks(Collection tasks) {
		this.tasks.addAll(tasks);
	}
	
	public Runnable popTask() {
		if (tasks.size() > 0) {
			return tasks.remove(0);
		}
		return null;
	}
	
	public void stopTasks() {
		isStopped = true;
	}
	
	public void stopTasksSync() {
		stopTasks();
		while (isRunning()) {
			try {
				sleep(SYNC_TIMEOUT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void pauseTasks() {
		isPaused = true;
	}
	
	public void pauseTasksSync() {
		pauseTasks();
		while (isRunning()) {
			try {
				sleep(SYNC_TIMEOUT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void kill() {
		if (isRunning())
			interrupt();
		
		isKilled = true;
	}
	
	public void killSync() {
		kill();
		
		while (isAlive()) {
			try {
				sleep(SYNC_TIMEOUT);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void startTasks() {
		isRunning = true;
		this.notify();
	}

	@Override
	public synchronized void run() {
		super.run();
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		try {
			while (true) {
				if (!isRunning || tasks.size() == 0) {
					pool.notifyForIdleThread();
					wait();
				} else {
					Runnable task = null;
					while ((task = popTask()) != null) {
	                    if (isStopped){
	                    	isStopped = false;
                            if(tasks.size() > 0){
                                tasks.clear();
                                System.out.println(Thread.currentThread().getId() + ": Tasks are stopped");
                                break;
                            }
                        }
                        if (isPaused){
                        	isPaused = false;
                            if(tasks.size() > 0){
                                System.out.println(Thread.currentThread().getId() + ": Tasks are paused");
                                break;
                            }
                        }
                        
                        task.run();
					}
					isRunning = false;
				}
				if (isKilled) {
					isKilled = false;
					break;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			isRunning = false;
		}		
	}

}
