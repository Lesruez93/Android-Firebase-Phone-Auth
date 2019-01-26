package com.lester.phoneauth;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.ybs.countrypicker.CountryPicker;
import com.ybs.countrypicker.CountryPickerListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "PhoneAuth";

    MaterialDialog process_dialog;

    private EditText phoneText;
    private EditText codeText;
    private Button verifyButton;
    private Button sendButton;
    private TextView resendButton;
    // private Button signoutButton;
    // private TextView statusText;
    private String uid;
    private String country_code;

    private String phoneVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    private FirebaseAuth fbAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        verifyButton = findViewById(R.id.verifyButton);
        sendButton = findViewById(R.id.sendButton);
        resendButton = findViewById(R.id.resendButton);
        Button login = findViewById(R.id.btnProceed);
        country_code = "+263";

        login.setOnClickListener(view -> {
            //  Toast.makeText(LoginActivity.this, "Please wait.....", Toast.LENGTH_SHORT).show();
            try {
                if (!isConnected()) {
                    //   Toast.makeText(LoginActivity.this, "Network problem", Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, "Sorry there was a problem with you network", Snackbar.LENGTH_LONG)
                            .setAction("Check Internet", null).show();
                }
                else {
       goToHome();

                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        sendButton.setOnClickListener((View view) -> {
            try {
                if (!isConnected()) {
                    //  Toast.makeText(LoginActivity.this, "Network problem", Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, "Sorry there was a problem with your network", Snackbar.LENGTH_LONG)
                            .setAction("Check Internet", null).show();
                }
                else {

                    sendCode(view);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // signoutButton = (Button) findViewById(R.id.signoutButton);

        verifyButton.setEnabled(false);
        resendButton.setEnabled(false);
        //  signoutButton.setEnabled(false);
        // statusText.setText("Signed Out");



        // fbAuth = FirebaseAuth.getInstance();


        final EditText codeText = findViewById(R.id.text_countrycode);

        codeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final CountryPicker picker = CountryPicker.newInstance("Select Country");  // dialog title
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        // Implement your code here
                        codeText.setText(dialCode);
                        country_code = dialCode;
                        picker.dismiss();
                    }
                });
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");

            }
        });

    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public void sendCode(View view) {

        String phoneNumber = country_code + phoneText.getText().toString();
        if (phoneNumber.length() > 7) {

            setUpVerificatonCallbacks();


            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    verificationCallbacks);

            //Set processing indicators
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                    .title("Sending code")
                    .content("Please wait")
                    .progress(true, 0);

            process_dialog = builder.build();
            process_dialog.show();
        }else {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpVerificatonCallbacks() {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {

                        //   signoutButton.setEnabled(true);
                        //   statusText.setText("Signed In");
                        resendButton.setEnabled(false);
                        verifyButton.setEnabled(false);
                        codeText.setText("");
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Toast.makeText(LoginActivity.this, "Invalid credential", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                        phoneVerificationId = verificationId;
                        resendToken = token;
                        verifyButton.setEnabled(true);
                        sendButton.setEnabled(false);
                        resendButton.setEnabled(true);

                        process_dialog.dismiss();
                    }
                };
    }

    public void verifyCode(View view) {

        String code = codeText.getText().toString();

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(phoneVerificationId, code);
        signInWithPhoneAuthCredential(credential);

        //Set processing indicators
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Verifying code")
                .content("Please wait")
                .progress(true, 0);

        process_dialog = builder.build();
        process_dialog.show();
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //  signoutButton.setEnabled(true);
                            codeText.setText("");
                            // statusText.setText("Signed In");
                            resendButton.setEnabled(false);
                            verifyButton.setEnabled(false);
                            FirebaseUser user = task.getResult().getUser();
                            //   TextView mtext = (TextView) findViewById(R.id.text_creds);
                            uid = user.getUid();
                            //  mtext.setText(uid);
                            process_dialog.dismiss();

                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void resendCode(View view) {

        String phoneNumber = phoneText.getText().toString();

        setUpVerificatonCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
    }
    public boolean isConnected() throws InterruptedException, IOException
    {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }











}
