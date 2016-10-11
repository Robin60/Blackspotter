package com.icrowsoft.blackspotter.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.icrowsoft.blackspotter.R;

/**
 * Created by kibet-GDTL on 11/10/2016.
 */

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    private EditText txt_email, txt_password;
    private Button btn_login;
    private TextView lbl_login_title;
    private boolean existingUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
//            Firebase.setAndroid;
        }

        // set flag for  login/register
        existingUser = true;

        lbl_login_title = (TextView) findViewById(R.id.lbl_login_title);
        txt_email = (EditText) findViewById(R.id.txt_email);
        txt_password = (EditText) findViewById(R.id.txt_password);
        btn_login = (Button) findViewById(R.id.btn_login);

        // request focus
        btn_login.requestFocus();


    }

    public void checkIfLoggedIn() {
//        Firebase firebase = new Firebase(xxxx);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (existingUser) {

                } else {

                }
                break;
        }
    }
}
