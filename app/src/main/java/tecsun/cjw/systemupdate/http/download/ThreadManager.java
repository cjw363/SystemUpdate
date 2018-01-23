package tecsun.cjw.systemupdate.http.download;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

class ThreadManager {

	private static ThreadPool threadPool;

	static ThreadPool getInstance() {
		if (threadPool == null) {
			synchronized (ThreadManager.class) {
				if (threadPool == null) {
					int cpuCount = Runtime.getRuntime().availableProcessors();//cpu数量

					threadPool = new ThreadPool(cpuCount + 1, cpuCount * 2 + 1, 1);//AsyncTask的配置
				}
			}
		}
		return threadPool;
	}

	/**
	 * 自定义线程池
	 */
	public static class ThreadPool {
		private int corePoolSize;
		private int maximumPoolSize;
		private long keepAliveTime;
		private ThreadPoolExecutor executor;

		ThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
			this.corePoolSize = corePoolSize;
			this.maximumPoolSize = maximumPoolSize;
			this.keepAliveTime = keepAliveTime;
		}

		public void execute(Runnable r) {
			if (executor == null) {
				// 参1:核心线程数;参2:最大线程数;参3:线程休眠时间;参4:时间单位;参5:线程队列;参6:生产线程的工厂;参7:线程异常处理策略
				executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), Executors.defaultThreadFactory(), new AbortPolicy());
			}
			// 线程池执行一个Runnable对象, 具体运行时机线程池说了算
			executor.execute(r);
		}

		//从队列中取消任务
		public void cancel(Runnable r) {
			if (executor != null) {
				executor.getQueue().remove(r);
			}
		}

	}
}
