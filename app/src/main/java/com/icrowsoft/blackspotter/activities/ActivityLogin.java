package com.icrowsoft.blackspotter.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.icrowsoft.blackspotter.R;

/**
 * Created by kibet-GDTL on 11/10/2016.
 */

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Kibet";
    private EditText txt_email, txt_password;
    private Button btn_login;
    private TextView lbl_login_title;
    private boolean existingUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText txt_confirm_email;
    private Button btn_signup;
    private EditText cnt_confirm_password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        // set layout
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
//            Firebase.setAndroid;
        }

        // set flag for  login/register
        existingUser = true;

        lbl_login_title = (TextView) findViewById(R.id.lbl_login_title);
        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_confirm_email = (EditText) findViewById(R.id.txt_confirm_email);
        txt_password = (EditText) findViewById(R.id.txt_password);

        cnt_confirm_password = (EditText) findViewById(R.id.cnt_confirm_password);

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_signup = (Button) findViewById(R.id.btn_signup);

        // request focus
        btn_login.requestFocus();
    }

    public void log_in_user(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Sign in: " + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(getBaseContext(), "Login failed!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // get user
                            FirebaseUser user = task.getResult().getUser();
                            // get email
                            String user_email = user.getEmail();
                            String user_name = user.getDisplayName();
                            String user_id = user.getUid();

                            Log.i("Gdane", "Email: " + user_email);
                            Log.i("Gdane", "Name: " + user_name);
                            Log.i("Gdane", "ID: " + user_id);

                            SharedPreferences prefs = getSharedPreferences("LoggedInUsersPrefs", 0);

                            //prefs = PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", user_email);
                        }
                    }
                });
    }

    public void create_new_user(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Sign up: " + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(getBaseContext(), "Sign up failed!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // // TODO: 11/10/2016  save user to preferences
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                // get credentials
                String email = txt_email.getText().toString();
                String confirm_email = txt_confirm_email.getText().toString();
                String password = txt_password.getText().toString();

                if (existingUser) {
                    // validate credentials
                    validate_sign_in_credentials(email, password);
                    // try user login
                    log_in_user(email, password);
                } else {
                    // validate credentials
                    validate_sign_up_credentials(email, confirm_email, password);
                    // try user login
                    create_new_user(email, password);
                }

                // hide confirm password
                cnt_confirm_password.setVisibility(View.GONE);
                // change title
                lbl_login_title.setText("Login");
                // change button text
                btn_login.setText("Sign in");

                break;

            case R.id.btn_signup:

                // unhide confirm password
                cnt_confirm_password.setVisibility(View.VISIBLE);
                // change title
                lbl_login_title.setText("Create Account");
                // change button text
                btn_login.setText("Sign up");
                btn_signup.setText("Have an account? Login");
                // change new user flag
                if (existingUser) {
                    existingUser = false;
                } else {
                    existingUser = true;
                }

                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private boolean validate_sign_in_credentials(String email, String password) {
        // check for blanks
        if (TextUtils.isEmpty(email)) {
            txt_email.setError("Required");
            Snackbar.make(txt_email, "Enter email", Snackbar.LENGTH_SHORT).show();
            txt_email.requestFocus();
            return false;
        }
        // check if valid mail
        if (!isValidEmail(email)) {
            txt_email.setError("Invalid");
            Snackbar.make(txt_email, "Enter a valid email", Snackbar.LENGTH_SHORT).show();
            txt_email.requestFocus();
            return false;
        }

        // check for blanks
        if (TextUtils.isEmpty(password)) {
            txt_password.setError("Required");
            Snackbar.make(txt_password, "Type a password", Snackbar.LENGTH_SHORT).show();
            txt_password.requestFocus();
            return false;
        }
//        // check if valid mail
//        if(!isValidPassword(password)){
//            txt_password.setError("Invalid");
//            Snackbar.make(txt_password, "Password should be 5 characters", Snackbar.LENGTH_SHORT).show();
//            txt_password.requestFocus();
//            return false;
//        }
        return true;
    }

    private boolean validate_sign_up_credentials(String email, String confirm_email, String password) {
        // check for blanks
        if (TextUtils.isEmpty(email)) {
            txt_email.setError("Required");
            Snackbar.make(txt_email, "Enter email", Snackbar.LENGTH_SHORT).show();
            txt_email.requestFocus();
            return false;
        }
        // check if valid mail
        if (!isValidEmail(email)) {
            txt_email.setError("Invalid");
            Snackbar.make(txt_email, "Enter a valid email", Snackbar.LENGTH_SHORT).show();
            txt_email.requestFocus();
            return false;
        }

        // check for blanks
        if (TextUtils.isEmpty(confirm_email)) {
            txt_confirm_email.setError("Required");
            Snackbar.make(txt_confirm_email, "Confirm email", Snackbar.LENGTH_SHORT).show();
            txt_confirm_email.requestFocus();
            return false;
        }
        // check if valid mail
        if (!isValidEmail(confirm_email)) {
            txt_confirm_email.setError("Invalid");
            Snackbar.make(txt_confirm_email, "Enter a valid email", Snackbar.LENGTH_SHORT).show();
            txt_confirm_email.requestFocus();
            return false;
        }

        // check for mail match
        if (!confirm_email.equals(email)) {
            txt_confirm_email.setError("Mismatch");
            Snackbar.make(txt_confirm_email, "Emails do not match", Snackbar.LENGTH_SHORT).show();
            txt_confirm_email.requestFocus();
            return false;
        }

        // check for blanks
        if (TextUtils.isEmpty(password)) {
            txt_password.setError("Required");
            Snackbar.make(txt_password, "Type a password", Snackbar.LENGTH_SHORT).show();
            txt_password.requestFocus();
            return false;
        }
        // check if valid mail
        if (!isValidPassword(password)) {
            txt_password.setError("Invalid");
            Snackbar.make(txt_password, "Password should be 5 characters", Snackbar.LENGTH_SHORT).show();
            txt_password.requestFocus();
            return false;
        }
        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public final static boolean isValidPassword(CharSequence target) {
        return target.length() == 5;
    }
}
