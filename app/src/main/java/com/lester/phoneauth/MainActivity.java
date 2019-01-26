package com.lester.phoneauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    private String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView text = findViewById(R.id.txtView);
        Button button = findViewById(R.id.signOut);
        firebaseAuth = FirebaseAuth.getInstance();


        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            text.setText(firebaseAuth.getCurrentUser().getUid());

            Log.e("Inn","inn");
        } else {

                       Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);

             startActivity(intent);
            Log.e("not","OUT");
        }

//    Log.e("uid",this.uid);








        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signout();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);            }
        });


      //  String phone =  firebaseAuth.getCurrentUser().getPhoneNumber();




    }



    private void signout() {
        firebaseAuth.signOut();
        Log.e("OUT","OUT");
    }

//    private void subscribeToAll() {
//        FirebaseMessaging.getInstance().subscribeToTopic("test").addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                String msg = "subscribed";
//                if (!task.isSuccessful()) {
//                    msg = "failed";
//                }
//            }
//        });
//
//        FirebaseMessaging.getInstance().subscribeToTopic("notices").addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                String msg = "subscribed";
//                if (!task.isSuccessful()) {
//                    msg = "failed";
//                }
//            }
//        });
//
//
//    }
}
