package com.skagit.feynman;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FeynmanThreadPool {
	private int _nWorkers;

	final private static int _PriorityAdjustment = -3;
	final private ThreadPoolExecutor _threadPool;
	private int _nextThreadId = 0;

//	public FeynmanThreadPool(final int nWorkerThreads, final int maxNWorkerThreadsInOneCall,
	// final int priorityAdjustment, final String coreName) {
	public FeynmanThreadPool() {
		_nWorkers = Runtime.getRuntime().availableProcessors() - 1;
		if (_nWorkers > 0) {
			_threadPool = new ThreadPoolExecutor(_nWorkers, _nWorkers, 0L, TimeUnit.MILLISECONDS,
					new LinkedBlockingQueue<Runnable>()) {
				@Override
				protected <T> RunnableFuture<T> newTaskFor(final Runnable runnable, final T value) {
					return new FutureTask<>(runnable, value);
				}
			};
			_threadPool.setThreadFactory(new ThreadFactory() {
				@Override
				public Thread newThread(final Runnable r) {
					final String threadName = String.format("%s.%02d", "ParFeynmanF", _nextThreadId++);
					final Thread thread = new Thread(r, threadName);
					adjustPriority(thread, Thread.NORM_PRIORITY + _PriorityAdjustment);
					return thread;
				}
			});
		} else {
			_threadPool = null;
			_nWorkers = 0;
		}
	}

	/** Use the following 3 routines to submit tasks to the workers. */
	public Object getLockOnWorkersThreadPool() {
		return this;
	}

	public int getNFreeWorkerThreads(final String whereFrom) {
		final int maxPoolSize = _threadPool.getMaximumPoolSize();
		final int activeCount = _threadPool.getActiveCount();
		return Math.min(_nWorkers, maxPoolSize - activeCount);
	}

	public Future<?> submitToWorkers(final Runnable runnable) {
		try {
			return _threadPool.submit(runnable);
		} catch (final Exception e) {
		}
		return null;
	}

	public interface WorkerRunnableFactory {
		Runnable getRunnable(int k, int n);
	}

	private static void adjustPriority(final Thread t, final int priorityAdjustment) {
		final int priority = Thread.NORM_PRIORITY + priorityAdjustment;
		final int newPriority = Math.max(Thread.MIN_PRIORITY, Math.min(priority, Thread.MAX_PRIORITY));
		t.setPriority(newPriority);
	}

	public void shutDown() {
		synchronized (_threadPool) {
			if (!_threadPool.isShutdown()) {
				_threadPool.shutdownNow();
			}
		}
	}

}
