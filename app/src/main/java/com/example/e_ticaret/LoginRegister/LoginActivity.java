package com.example.e_ticaret.LoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_ticaret.DAO.ActiveUserDao;
import com.example.e_ticaret.DAO.UserDao;
import com.example.e_ticaret.MainActivity;
import com.example.e_ticaret.R;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private MaterialButton materialButtonSkipLogin;

    // Her iki DAO sınıfını da tanımlıyoruz
    private UserDao userDao;
    private ActiveUserDao activeUserDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDao = new UserDao(this);
        activeUserDao = new ActiveUserDao(this);

        initializeView();
        setListeners();
    }

    private void initializeView() {
        editTextUsername = findViewById(R.id.activity_login_username_editText);
        editTextPassword = findViewById(R.id.activity_login_password_editText);
        btnLogin = findViewById(R.id.activity_login_btnLogin);
        tvRegister = findViewById(R.id.activity_login_btnTvRegister);
        materialButtonSkipLogin = findViewById(R.id.activity_login_btnSkipLogin);
    }

    private void setListeners() {
        IntentForBtnRegister();
        IntentAndCheckUserForBtnLogin();
        IntentForBtnSkipLogin();
    }

    private void IntentForBtnRegister() {
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intentRegister);
            }
        });
    }

    private void IntentAndCheckUserForBtnLogin() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1. UserDao'yu aç ve username/password eşleşen ID'yi al
                userDao.open();
                long userId = userDao.checkUserLogin(username, password);
                userDao.close();

                if (userId != -1) {

                    activeUserDao.open();
                    activeUserDao.setActiveUser(userId);
                    activeUserDao.close();

                    Toast.makeText(LoginActivity.this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();

                    Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intentMain);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Kullanıcı adı veya şifre hatalı!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void IntentForBtnSkipLogin() {
        materialButtonSkipLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intentMain);
                finish();
            }
        });
    }
}