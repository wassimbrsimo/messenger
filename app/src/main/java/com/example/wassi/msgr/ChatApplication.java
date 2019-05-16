package com.example.wassi.msgr;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ChatApplication {
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.1.5:6969");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}