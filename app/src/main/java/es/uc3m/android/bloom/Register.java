package es.uc3m.android.bloom;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener {
    EditText name, surname, user, pass, pass2, email;
    Button register;
    TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        initializeFields();
        setupListeners();
    }

    private void initializeFields() {
        name = findViewById(R.id.editTextNombre);
        surname = findViewById(R.id.editTextApellidos);
        user = findViewById(R.id.editTextUsu);
        pass = findViewById(R.id.editTextPassword);
        pass2 = findViewById(R.id.editTextPassword2);
        email = findViewById(R.id.editTextEmail);
        register = findViewById(R.id.register);
        login = findViewById(R.id.textViewLogIn);
    }

    private void setupListeners() {
        register.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                register();
                break;
            case R.id.textViewLogIn:
                startActivity(new Intent(this, Login.class));
                break;
        }
    }

    private void register() {
        if (!validateFields()) {
            return;
        }

        String emailStr = email.getText().toString().trim();
        String passwordStr = pass.getText().toString().trim();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        onAuthSuccess(task.getResult().getUser());
                    } else {
                        Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateFields() {
        boolean valid = true;
        resetFieldTints(); // Reset tints before validation

        if (isEmptyField(email)) {
            email.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            Toast.makeText(this, "Email field cannot be empty.", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (isEmptyField(pass) || isEmptyField(pass2)) {
            pass.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            pass2.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            Toast.makeText(this, "Password fields cannot be empty.", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (!pass.getText().toString().trim().equals(pass2.getText().toString().trim())) {
            pass.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            pass2.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (pass.getText().toString().trim().length() < 6) {
            pass.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }


    private void resetFieldTints() {
        email.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        pass.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        pass2.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        name.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        surname.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        user.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
    }

    private boolean isEmptyField(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }

    private void onAuthSuccess(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("name", name.getText().toString().trim());
        user.put("surname", surname.getText().toString().trim());
        user.put("username", this.user.getText().toString().trim());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("Users").child(userId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(Register.this, HomeFragment.class));
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed to save user information.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
