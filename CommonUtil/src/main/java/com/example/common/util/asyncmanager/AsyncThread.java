package com.example.common.util.asyncmanager;

/**
 * Created by hpplay on 2018/3/23.
 */

public class AsyncThread extends Thread{

    public static volatile int mThreadCount = 0;
    public AsyncThread() {
        super();
        mThreadCount++;
    }

    public AsyncThread(Runnable target) {
        super(target);
        mThreadCount++;
    }

    public AsyncThread(ThreadGroup group, Runnable target) {
        super(group,target);
        mThreadCount++;
    }

    public AsyncThread(String name) {
        super(name);
        mThreadCount++;
    }

    public AsyncThread(ThreadGroup group, String name) {
        super(group,name);
        mThreadCount++;
    }

    public AsyncThread(Runnable target, String name) {
        super(target,name);
        mThreadCount++;
    }

    public AsyncThread(ThreadGroup group, Runnable target, String name) {
        super(group,target,name);
        mThreadCount++;
    }

    public AsyncThread(ThreadGroup group, Runnable target, String name,
                  long stackSize) {
        super(group,target,name,stackSize);
        mThreadCount++;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mThreadCount--;
    }
}
