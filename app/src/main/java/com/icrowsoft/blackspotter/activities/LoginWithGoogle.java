package com.icrowsoft.blackspotter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.icrowsoft.blackspotter.R;

/**
 * Created by teardrops on 10/11/16.
 */

public class LoginWithGoogle extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 20;
    private static final int AUTO_MANAGE_ID = 1;
    private static final String TAG = "Kibet";
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions
                .DEFAULT_SIGN_IN)
                .requestEmail();
//                .requestIdToken(getString(R.string.my_client_id))

        // Add additional scopes
        String[] extraScopes = getResources().getStringArray(com.firebase.ui.auth.R.array.google_permissions);
        for (String scopeString : extraScopes) {
            builder.requestScopes(new Scope(scopeString));
        }

//        if (!TextUtils.isEmpty(email)) {
//            builder.setAccountName(email);
//        }
        GoogleSignInOptions googleSignInOptions = builder.build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, AUTO_MANAGE_ID, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        getLoggedInUser();

        SignInButton google_sign_in = (SignInButton) findViewById(R.id.google_sign_in);
        google_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start login
                doLogin();
            }
        });
    }

    private void getLoggedInUser() {
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
    }

    private void doLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null) {
                if (result.isSuccess()) {
                    // get google account
                    GoogleSignInAccount account = result.getSignInAccount();
                    Log.e("Kibet", "Success: ");
                    Log.e("Kibet", "Name: " + account.getDisplayName());
                    Log.e("Kibet", "Email: " + account.getEmail());
                    Log.e("Kibet", "TokenID: " + account.getIdToken());
//                    mIDPCallback.onSuccess(createIDPResponse(result.getSignInAccount()));
                } else {
//                    Log.e("Kibet", "Error occurred: " + result.getStatus().getStatusMessage());
                    Log.e("Kibet", "Error occurred: " + result.toString());
                }
            } else {
                Log.e("Kibet", "No result found in intent");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Kibet", "Error occurred: " + connectionResult.getErrorMessage());
    }
}
