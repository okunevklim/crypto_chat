package com.example.android.skillboxcryptochat;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Server {
    Map<Long, String> names = new ConcurrentHashMap<>();
    WebSocketClient client;
    private Consumer<Pair<String, String>> onMessageReceived;
    private Consumer<Integer> onUserStatusChanged;

    public Server(Consumer<Pair<String, String>> onMessageReceived, Consumer<Integer> onUserStatusChanged ) {
        this.onMessageReceived = onMessageReceived;
        this.onUserStatusChanged = onUserStatusChanged;
    }

    public void connect() {
        URI address;
        try {
            address = new URI("ws://192.168.31.197:8881");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        client = new WebSocketClient(address) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.i("SERVER", "The server is opened");


                // 1 - статус юзера, 2 - текст месседж, 3 - имя юзера
            }

            @Override
            public void onMessage(String message) {
                Log.i("SERVER", "Got message from server" + message);
                int type = Protocol.getType(message);
                if (type == Protocol.USER_STATUS) {
                    //обработать факт подключения
                    userStatusChanged(message);
                }

                if (type == Protocol.MESSAGE) {
                    //покаать на экране
                    displayIncomingMessage(message);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("SERVER", "The server is closed");

            }

            @Override
            public void onError(Exception ex) {
                Log.i("SERVER", "Error is occured " + ex.getMessage());
            }
        };
        client.connect();
    }

    private void displayIncomingMessage(String json) {
        Protocol.Message m = Protocol.unpackMessage(json);
        String name = names.get(m.getSender());
        if (name == null) {
            name = "НЕНАЗВАННЫЙ";
        }
        String text = m.getEncodedText();
        try {
            text = Crypto.decrypt(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onMessageReceived.accept(
                new Pair<String, String>(name, text )
        );
    }

    private void userStatusChanged(String json) {
        Protocol.UserStatus s = Protocol.unpackStatus(json);
        Protocol.User user = s.getUser();
        if (s.isConnected()) {
            names.put(user.getId(), user.getName());
        } else {
            names.remove(user.getId());
        }
        onMessageReceived.accept(new Pair<>(user.getName(), String.valueOf(s.isConnected())));
        onUserStatusChanged.accept(names.size());
    }

    public void sendMessage(String message) {
        if (client == null || !client.isOpen()) {
            return;
        }
        try {
            message = Crypto.encrypt(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Protocol.Message m = new Protocol.Message("Всем привет!");
        m.setReceiver(Protocol.GROUP_CHAT);
        String packedMessage = Protocol.packMessage(m);
        Log.i("SERVER", "Sending message:" + packedMessage);
        client.send(packedMessage);
    }
    public void sendUserName(String name){
        String myName = Protocol.packName(new Protocol.UserName(name));
        Log.i("Server", "Sending my name to server:" + myName);
        client.send(myName);
    }

}
