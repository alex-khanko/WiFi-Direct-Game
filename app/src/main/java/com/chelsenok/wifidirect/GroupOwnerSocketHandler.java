package com.chelsenok.wifidirect;

import android.os.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.chelsenok.wifidirect.Constants.SERVER_PORT;

class GroupOwnerSocketHandler extends Thread {

    private final ServerSocket serverSocket;
    private final int THREAD_COUNT = 10;
    private final Handler handler;

    public GroupOwnerSocketHandler(final Handler handler) throws IOException {
        try {
            this.serverSocket = new ServerSocket(SERVER_PORT);
            this.handler = handler;
        } catch (final IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }
    }

    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, THREAD_COUNT, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
    );

    @Override
    public void run() {
        while (true) {
            try {
                pool.execute(new ChatNeedle(serverSocket.accept(), handler));
            } catch (final IOException e) {
                try {
                    if (!serverSocket.isClosed()) {
                        serverSocket.close();
                    }
                } catch (final IOException ignored) {
                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }

}
