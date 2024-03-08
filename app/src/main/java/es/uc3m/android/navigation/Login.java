package es.uc3m.android.navigation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    EditText ;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        login = findViewById(R.id.editTextNombre);
        apellidos = findViewById(R.id.editTextApellidos);
        usuario = findViewById(R.id.editTextUsu);
        pass = findViewById(R.id.editTextPassword);
        pass2 = findViewById(R.id.editTextPassword2);
        email = findViewById(R.id.editTextEmail);
        registro = findViewById(R.id.registrarse);

    }


}
