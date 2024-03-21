package es.uc3m.android.bloom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {
    Button login, register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_activity);
        login = findViewById(R.id.init_login);
        register = findViewById(R.id.init_register);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.init_register:
                Intent intent_reg = new Intent(this, Register.class);
                startActivity(intent_reg);
                break;
            case R.id.init_login:
                Intent intent_log = new Intent(this, Login.class);
                startActivity(intent_log);
                break;
        }


    }
}