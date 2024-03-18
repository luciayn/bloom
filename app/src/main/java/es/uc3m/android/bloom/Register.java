package es.uc3m.android.bloom;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity implements View.OnClickListener {
    EditText name, surname, user, pass, pass2, email;
    Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        name = findViewById(R.id.editTextNombre);
        surname = findViewById(R.id.editTextApellidos);
        user = findViewById(R.id.editTextUsu);
        pass = findViewById(R.id.editTextPassword);
        pass2 = findViewById(R.id.editTextPassword2);
        email = findViewById(R.id.editTextEmail);
        register = findViewById(R.id.register);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                // Get the values from the EditText fields
                String emailStr = email.getText().toString();
                String passwordStr = pass.getText().toString();
                String password2Str = pass2.getText().toString();

                // Check if passwords match and are not empty
                if (!passwordStr.equals(password2Str)) {
                    // Show an error message
                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else if (passwordStr.isEmpty() || emailStr.isEmpty()) {
                    // Show an error message
                    Toast.makeText(Register.this, "Email or Password field is empty", Toast.LENGTH_SHORT).show();
                } else {
                    // Register the user in Firebase
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailStr, passwordStr)
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // User is successfully registered and logged in
                                        // Start your next activity here
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                break;
        }
    }

}
