package com.lester.phoneauth;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
        fbAuth = FirebaseAuth.getInstance();


        phoneText = findViewById(R.id.phoneText);
        codeText = findViewById(R.id.codeText);
        verifyButton = findViewById(R.id.verifyButton);
        sendButton = findViewById(R.id.sendButton);
        resendButton = findViewById(R.id.resendButton);
        country_code = "+263";
        FirebaseAuth.AuthStateListener mAuthListener;






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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
                            Toast.makeText(LoginActivity.this, "Limit Reached Try Again In a few Hours", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {

                        phoneVerificationId = verificationId;
                        resendToken = token;
                        verifyButton.setVisibility(View.VISIBLE);
                        codeText.setVisibility(View.VISIBLE);
                        sendButton.setEnabled(false);
                        resendButton.setVisibility(View.VISIBLE);

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
                            codeText.setText("");

                          goToHome();
                            FirebaseUser user = task.getResult().getUser();

                            uid = user.getUid();

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
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                .title("Sending code")
                .content("Please wait")
                .progress(true, 0);

        process_dialog = builder.build();
        process_dialog.show();
    }
    public boolean isConnected() throws InterruptedException, IOException
    {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }











}
