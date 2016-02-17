package com.anguanjia.framework.net;

import android.content.Context;

import com.anguanjia.framework.thread.ThreadPool;

class RequestQueue {
    static void commitRequest(final Context context, final Request req){
        ThreadPool.mService.execute(new Runnable() {
            @Override
            public void run() {
                req.submitRequest(context);
            }
        });
    }
}
