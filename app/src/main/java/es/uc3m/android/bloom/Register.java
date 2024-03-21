package es.uc3m.android.bloom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
        name = findViewById(R.id.editTextNombre);
        surname = findViewById(R.id.editTextApellidos);
        user = findViewById(R.id.editTextUsu);
        pass = findViewById(R.id.editTextPassword);
        pass2 = findViewById(R.id.editTextPassword2);
        email = findViewById(R.id.editTextEmail);
        register = findViewById(R.id.register);

        register.setOnClickListener(this);
        login = findViewById(R.id.textViewLogIn);

        login.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                register();
                break;
            case R.id.textViewLogIn:
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                break;
        }


    }

    private void register() {
        String emailStr = email.getText().toString().trim();
        String passwordStr = pass.getText().toString().trim();
        String password2Str = pass2.getText().toString().trim();
        String nameStr = name.getText().toString().trim();
        String surnameStr = surname.getText().toString().trim();
        String userStr = user.getText().toString().trim();

        if (emailStr.isEmpty() || passwordStr.isEmpty() || password2Str.isEmpty() || nameStr.isEmpty() || surnameStr.isEmpty() || userStr.isEmpty()) {
            Toast.makeText(Register.this, "All the fields must be informed", Toast.LENGTH_SHORT).show();
        } else if (!passwordStr.equals(password2Str)) {
            Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser != null) {
                                    String userId = firebaseUser.getUid();

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", nameStr);
                                    user.put("surname", surnameStr);
                                    user.put("username", userStr);

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                                    databaseReference.child("Users").child(userId).setValue(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(Register.this, HomeFragment.class);
                                                        startActivity(intent);
                                                    } else {

                                                        Toast.makeText(getApplicationContext(), "Failed to save user information.", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                }
                            } else {
                                Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


}
