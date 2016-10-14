package com.icrowsoft.blackspotter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.icrowsoft.blackspotter.R;

import net.ralphpina.permissionsmanager.PermissionsManager;

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
    private EditText txt_confirm_password;
    private Button btn_signup;
    private TextInputLayout cnt_confirm_password;
    private ActivityLogin _context;
    private SharedPreferences prefs;
    private MaterialDialog.Builder dialog_builder;
    private MaterialDialog progress_dialog;

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public final static boolean isValidPassword(CharSequence target) {
        return target.length() > 5;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get reference to this activity
        _context = this;

        // get reference to shared preferences
        prefs = getSharedPreferences("LoggedInUsersPrefs", 0);

        try {
            // initialize permission manager
            PermissionsManager.init(this);
        } catch (Exception e) {
            // initialization already done
        }

        // prepare dialog builder
        dialog_builder = new MaterialDialog.Builder(this)
                .content("Please wait...")
                .progress(true, 0);

        // set up onAuthentication change
        setup_on_Authentication_change();
    }

    private void toggle_loading_dialog(boolean display) {
        try {
            if (display) {
                progress_dialog = dialog_builder.show();
            } else {
                progress_dialog.dismiss();
            }
        } catch (Exception e) {
            // seems dialog is not showing
        }
    }

    private void setup_on_Authentication_change() {

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // request for permissions
                    request_for_permissions();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    // setup layout
                    setupLayout();
                }
            }
        };
    }

    private void setupLayout() {
        // set layout
        setContentView(R.layout.activity_login);

        // set flag for  login/register
        existingUser = true;

        lbl_login_title = (TextView) findViewById(R.id.lbl_login_title);
        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_confirm_password = (EditText) findViewById(R.id.txt_confirm_password);
        txt_password = (EditText) findViewById(R.id.txt_password);

        cnt_confirm_password = (TextInputLayout) findViewById(R.id.cnt_confirm_password);

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_signup = (Button) findViewById(R.id.btn_signup);

        btn_login.setOnClickListener(_context);
        btn_signup.setOnClickListener(_context);

        // request focus
        btn_signup.requestFocus();
    }

    private void request_for_permissions() {
        // request for camera
        if (!PermissionsManager.get().isCameraGranted()) {
            PermissionsManager.get().requestCameraPermission(this);
        } else {
            // request for location
            if (!PermissionsManager.get().isLocationGranted()) {
                PermissionsManager.get().requestLocationPermission(this);
            } else {
                // close activity
                finish();

                // start home
                startActivity(new Intent(getBaseContext(), ActivityHome.class));
            }
        }
    }

    public void log_in_user(String email, String password) {
        // show dialog
        toggle_loading_dialog(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Sign in: " + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            // get error message
                            String error = task.getException().getMessage();

                            Log.e(TAG, "Error msg: " + task.getException().getMessage());

                            String err_message = "Unknown error";
                            if (error.startsWith("The password is invalid or the user does not have a password")) {
                                err_message = "Wrong password";
                            }

                            if (error.startsWith("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                err_message = "Unknown user";
                            }

                            if (error.startsWith("A network error")) {
                                err_message = "Network failed";
                            }

                            Toast myToast = Toast.makeText(getBaseContext(), err_message, Toast.LENGTH_LONG);
                            myToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            myToast.show();
//
//
                        } else {
                            // get user
                            FirebaseUser user = task.getResult().getUser();
                            // Name, email address, and profile photo Url
                            String name = user.getDisplayName();
                            String email = user.getEmail();
                            Uri photoUrl = user.getPhotoUrl();

                            String ui = user.getUid();

                            Log.i("Kibet", "+++++++++++++++++++++++++++++++++");
                            Log.i("Kibet", "UID: " + ui);
                            Log.i("Kibet", "Name: " + name);
                            Log.i("Kibet", "Email: " + email);
                            Log.i("Kibet", "Photo: " + photoUrl);
                            Log.i("Kibet", "+++++++++++++++++++++++++++++++++");

                            SharedPreferences prefs = getSharedPreferences("LoggedInUsersPrefs", 0);

                            //prefs = PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", email);
                            editor.commit();

                            // get user_id to test if session exists
                            String logged_in_user_email = prefs.getString("email", "");

                            Log.e("Kibet", "logged_in_user_email: " + logged_in_user_email);
                        }

                        //close dialog
                        toggle_loading_dialog(false);
                    }
                });
    }

    public void create_new_user(String email, String password) {
        // show dialog
        toggle_loading_dialog(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Sign up: " + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            // get error message
                            String error = task.getException().getMessage();

                            Log.e(TAG, "Error msg: " + task.getException().getMessage());

                            String err_message = "Unknown error";
                            if (error.startsWith("The email address is already in use by another account")) {
                                err_message = "Email already in use";
                            }

                            if (error.startsWith("A network error")) {
                                err_message = "Network failed";
                            }

                            Toast myToast = Toast.makeText(getBaseContext(), err_message, Toast.LENGTH_LONG);
                            myToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            myToast.show();
                        } else {
                            // get user
                            FirebaseUser user = task.getResult().getUser();
                            // Name, email address, and profile photo Url
                            String name = user.getDisplayName();
                            String email = user.getEmail();
                            Uri photoUrl = user.getPhotoUrl();

                            String ui = user.getUid();

                            Log.i("Kibet", "+++++++++++++++++++++++++++++++++");
                            Log.i("Kibet", "UID: " + ui);
                            Log.i("Kibet", "Name: " + name);
                            Log.i("Kibet", "Email: " + email);
                            Log.i("Kibet", "Photo: " + photoUrl);
                            Log.i("Kibet", "+++++++++++++++++++++++++++++++++");

                            SharedPreferences prefs = getSharedPreferences("LoggedInUsersPrefs", 0);

                            //prefs = PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", email);
                            editor.commit();
                        }

                        //close dialog
                        toggle_loading_dialog(false);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                // get credentials
                String email = txt_email.getText().toString();
                String confirm_password = txt_confirm_password.getText().toString();
                String password = txt_password.getText().toString();

                if (existingUser) {
                    // validate credentials
                    if (!validate_sign_in_credentials(email, password)) {
                        return;
                    }

                    // try user login
                    log_in_user(email, password);
                } else {
                    // validate credentials
                    if (!validate_sign_up_credentials(email, confirm_password, password)) {
                        return;
                    }
                    // try user login
                    create_new_user(email, password);
                }

                break;

            case R.id.btn_signup:

                if (existingUser) {
                    // unhide confirm password
                    cnt_confirm_password.setVisibility(View.VISIBLE);
                    // change title
                    lbl_login_title.setText("Create Account");

                    // change button text
                    btn_login.setText("Sign up");
                    btn_signup.setText("Have an account? Login");
                } else {
                    // unhide confirm password
                    cnt_confirm_password.setVisibility(View.GONE);
                    // change title
                    lbl_login_title.setText("Login");

                    // change button text
                    btn_login.setText("Sign in");
                    btn_signup.setText("New? Create account...");
                }

                // change new user flag
                existingUser = !existingUser;

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
        // check if valid password
        if(!isValidPassword(password)){
            txt_password.setError("Invalid");
            Snackbar.make(txt_password, "Password less than 6 characters", Snackbar.LENGTH_SHORT).show();
            txt_password.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validate_sign_up_credentials(String email, String confirm_password, String password) {
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
        // check if valid password
        if (!isValidPassword(password)) {
            txt_password.setError("Invalid");
            Snackbar.make(txt_password, "Password less than 6 characters", Snackbar.LENGTH_SHORT).show();
            txt_password.requestFocus();
            return false;
        }

        // check for blanks
        if (TextUtils.isEmpty(confirm_password)) {
            txt_confirm_password.setError("Required");
            Snackbar.make(txt_confirm_password, "Confirm password", Snackbar.LENGTH_SHORT).show();
            txt_confirm_password.requestFocus();
            return false;
        }
        // check if valid password
        if (!isValidPassword(confirm_password)) {
            txt_confirm_password.setError("Invalid");
            Snackbar.make(txt_confirm_password, "Password less than 6 characters", Snackbar.LENGTH_SHORT).show();
            txt_confirm_password.requestFocus();
            return false;
        }

        // check for mail match
        if (!confirm_password.equals(password)) {
            txt_confirm_password.setError("Mismatch");
            Snackbar.make(txt_confirm_password, "Passwords do not match", Snackbar.LENGTH_SHORT).show();
            txt_confirm_password.setText("");
            txt_confirm_password.requestFocus();
            return false;
        }

        return true;
    }
}
