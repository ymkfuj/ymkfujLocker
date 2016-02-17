package com.anguanjia.framework.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    public static ExecutorService mService = Executors.newCachedThreadPool();
}
