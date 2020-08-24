package com.example.android.skillboxcryptochat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    private Button sendButton;
    private EditText userInput;
    private RecyclerView chatWindow;
    private MessageController controller;
    private Server server;
    private TextView online;
    String userName;

    public void getUserName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your name: ");
        final EditText nameInput = new EditText(this);
        builder.setView(nameInput);
        builder.setCancelable(false);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (nameInput.getText().length() != 0) {
                    userName = nameInput.getText().toString();
                    server.sendUserName(userName);
                } else {
                    Toast.makeText(MainActivity.this, "Your name is too short",
                            Toast.LENGTH_SHORT).show();
                }

                //отослать на сервер
            }
        });
        builder.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.sendButton);
        userInput = findViewById(R.id.userInput);
        chatWindow = findViewById(R.id.chatWindow);
        online = findViewById(R.id.online);



        controller = new MessageController();
        controller.setIncomingLayout(R.layout.incoming_message);
        controller.setOutgoingLayout(R.layout.outgoing_message);
        controller.setMessageTextId(R.id.userInput);
        controller.setMessageTimeId(R.id.messageDate);
        controller.setUserNameId(R.id.userName);
        controller.appendTo(chatWindow, this);
        controller.addMessage(new MessageController.Message("Всем привет! Добро пожаловать в чат!", "Administrator", false));
        
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userInput.getText().length() != 0) {
                    String text = userInput.getText().toString();
                    controller.addMessage(new MessageController.Message(text, userName, true));
                    server.sendMessage(text);
                    userInput.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "Your message is too short",
                            Toast.LENGTH_SHORT).show();
                }
                            }
        });

        TextWatcher inputUser = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(userInput.getText().length() != 0){
                    sendButton.setEnabled(true);
                }
                else {
                    sendButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        userInput.addTextChangedListener(inputUser);

        server = new Server(new Consumer<Pair<String, String>>() {
            @Override
            public void accept(Pair<String, String> p) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //                controller.addMessage(new MessageController.Message(p.second, p.first, false));
                        Toast.makeText(MainActivity.this, p.first + " joined the server",
                                Toast.LENGTH_SHORT).show();
                        Log.i("SERVER", "message");
                    }

                });
            }
        }, new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        online.setText("On: " + integer);
                    }
                });
            }
        });
        server.connect();
        getUserName();

    }


}
