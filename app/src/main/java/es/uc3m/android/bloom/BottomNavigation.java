package es.uc3m.android.bloom;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigation extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {
    Button login, register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);

//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
//        bottomNavigationView.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        int itemId = item.getItemId();
        if (itemId == R.id.profile_item) {
            fragment = new ProfileFragment();
        }
        else if (itemId == R.id.settings_item) {
            fragment = new SettingsFragment();
        }
        else {
            fragment = new HomeFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment, fragment)
                .commit();
        return true;
    }
}
