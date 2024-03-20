package es.uc3m.android.bloom;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigation extends AppCompatActivity implements BottomNavigationView.OnItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Replace 'your_layout_name' with the actual layout file name
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnItemSelectedListener(this);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        int itemId = item.getItemId();
        if (itemId == R.id.profile_item) {
            fragment = new ProfileFragment();
        } else if (itemId == R.id.settings_item) {
            fragment = new SettingsFragment();
        } else {
            fragment = new HomeFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment, fragment)
                .commit();
        return true;
    }
}
