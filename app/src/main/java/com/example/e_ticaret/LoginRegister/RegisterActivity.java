package com.example.e_ticaret.LoginRegister;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_ticaret.DAO.UserDao;
import com.example.e_ticaret.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etSurname, etUsername, etEmail, etPhone, etAge, etPassword;
    private AutoCompleteTextView actvGender;
    private Button btnRegister;
    private TextView tvBackLogin;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeView();
        clickListeners();
    }

    private void initializeView() {
        etName = findViewById(R.id.activity_register_etRegisterName);
        etSurname = findViewById(R.id.activity_register_etRegisterSurname);
        etUsername = findViewById(R.id.activity_register_etRegisterUsername);
        etEmail = findViewById(R.id.activity_register_etRegisterEmail);
        etPhone = findViewById(R.id.activity_register_etRegisterPhone);
        etAge = findViewById(R.id.activity_register_etRegisterAge);
        etPassword = findViewById(R.id.activity_register_etRegisterPassword);
        btnRegister = findViewById(R.id.activity_register_btnRegisterSubmit);
        tvBackLogin = findViewById(R.id.activity_register_tvBackToLogin);

        actvGender = findViewById(R.id.activity_register_actvGender);

        //  Cinsiyet menüsü için adapter kurulumu
        String[] genderOptions = new String[]{"Erkek", "Kadın", "Belirtmek İstemiyorum"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );
        actvGender.setAdapter(adapter);
    }

    private void clickListeners(){
        setTvBackLoginIntent();
        setBtnRegister();
    }

    private void setTvBackLoginIntent(){
        tvBackLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backLogin= new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(backLogin);
            }
        });
    }

    private void setBtnRegister() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String surname = etSurname.getText().toString().trim();
                String username = etUsername.getText().toString().trim();
                String email= etEmail.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String strAge = etAge.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String gender = actvGender.getText().toString().trim();

                if (name.isEmpty() || surname.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty() || strAge.isEmpty() ||password.isEmpty() || gender.isEmpty() ){
                    Toast.makeText(RegisterActivity.this, "Lütfen Tüm Alanları Doldurunuz", Toast.LENGTH_SHORT).show();
                    return;
                }

                int age;
                try {
                    age = Integer.parseInt(strAge);
                }catch (NumberFormatException e){
                    Toast.makeText(RegisterActivity.this, "Lütfen Geçerli Bir Yaş Giriniz", Toast.LENGTH_SHORT).show();
                    return;
                }

                userDao = new UserDao(RegisterActivity.this);
                userDao.open();

                long result = userDao.addUser(name, surname, username, email,phone,age , gender, password);

                userDao.close();

                if(result != -1){
                    Toast.makeText(RegisterActivity.this, "Harika! Kayıt Başarılı.", Toast.LENGTH_SHORT).show();
                    finish();
                }else
                    Toast.makeText(RegisterActivity.this, "Bir Hata Oluştu Oluştu. Kullanıcı Adı veya email zaten kayıtlı olabilir.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    }
