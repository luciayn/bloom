package es.uc3m.android.bloom;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity implements View.OnClickListener, View.OnHoverListener {

    EditText user, password;
    TextView signin;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        user = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        login = findViewById(R.id.login);
        login.setOnClickListener(this);
        signin = findViewById(R.id.signInText);
        signin.setOnClickListener(this);
        signin.setOnHoverListener(this);
        user.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        password.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                login();
                break;
            case R.id.signInText:
                signIn();
                break;
        }
    }

    private void login() {
        String email = user.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        user.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        password.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));

        if (email.isEmpty() || passwordStr.isEmpty()) {
            Toast.makeText(Login.this, "Please enter both email and password.", Toast.LENGTH_LONG).show();
            user.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            password.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, passwordStr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            Intent intent = new Intent(this, BottomNavigation.class);
                            startActivity(intent);
                        }
                    } else {
                        user.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                        password.setBackgroundTintList(ColorStateList.valueOf(Color.RED));


                        Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signIn() {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }


    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                signin.setTextColor(getResources().getColor(R.color.light_gray));
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                signin.setTextColor(getResources().getColor(R.color.purple));
                break;
        }
        return true;
    }
}
