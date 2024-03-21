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
        setContentView(R.layout.main_activity);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnItemSelectedListener(this);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        int itemId = item.getItemId();
        if (itemId == R.id.profile_item) {
            fragment = new ProfileFragment();
        } else if (itemId == R.id.notification_item) {
            fragment = new NotificationsFragment();
        } else if (itemId == R.id.home_item) {
            fragment = new HomeFragment();
        } else {
            fragment = new CalendarFragment();
        }



        getSupportFragmentManager().beginTransaction()
                .replace(R.id.nav_fragment, fragment)
                .commit();
        return true;
    }
}
