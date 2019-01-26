package com.lester.phoneauth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    private void subscribeToPrayers() {
        FirebaseMessaging.getInstance().subscribeToTopic("-prayers").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "subscribed";
                if (!task.isSuccessful()) {
                    msg = "failed";
                }
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("-notices").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "subscribed";
                if (!task.isSuccessful()) {
                    msg = "failed";
                }
            }
        });


    }
}
