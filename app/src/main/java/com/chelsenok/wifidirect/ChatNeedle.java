package com.chelsenok.wifidirect;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static com.chelsenok.wifidirect.Constants.CAPACITY;
import static com.chelsenok.wifidirect.Constants.CHAT_NEEDLE;

class ChatNeedle implements Runnable {

    private final Socket socket;
    private final Handler handler;

    public ChatNeedle(final Socket socket, final Handler handler) {
        this.socket = socket;
        this.handler = handler;
    }

    private OutputStream oStream;

    @Override
    public void run() {
        try {
            final InputStream iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            final byte[] buffer = new byte[CAPACITY];
            int bytes;
            handler.obtainMessage(CHAT_NEEDLE, this)
                    .sendToTarget();
            while (true) {
                try {
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    final Message message = new Message();
                    message.what = 0;
                    message.obj = buffer;
                    handler.obtainMessage(message.what, message.obj).sendToTarget();
                } catch (final IOException ignored) {
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(final byte[] buffer) {
        try {
            oStream.write(buffer);
        } catch (final IOException ignored) {
        }
    }

}
