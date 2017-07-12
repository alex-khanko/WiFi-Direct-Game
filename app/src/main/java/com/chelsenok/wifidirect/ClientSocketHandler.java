package com.chelsenok.wifidirect;

import android.os.Handler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import static com.chelsenok.wifidirect.Constants.SERVER_PORT;

class ClientSocketHandler extends Thread {

    private static final int TIMEOUT = 5000;
    private final Handler handler;
    private final InetAddress mInetAddress;

    public ClientSocketHandler(final Handler handler, final InetAddress inetAddress) {
        this.handler = handler;
        this.mInetAddress = inetAddress;
    }

    @Override
    public void run() {
        final Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mInetAddress.getHostAddress(), SERVER_PORT), TIMEOUT);
            new Thread(new ChatNeedle(socket, handler)).start();
        } catch (final IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (final IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
