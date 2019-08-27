package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileWithSettings extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private Toolbar profileToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_with_settings);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragContainer = fm.findFragmentById(R.id.fragment_container);

        /* SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        String s = prefs.getString(createAccount.PREFERENCE_USERNAME, "default");
        Log.d("prefs", s);
        if (s.equals("default")) {
            if (fragContainer == null) {
                fragContainer = new LoginFragment();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.fragment_container, fragContainer);
                ft.commit();
            }
        }
        else {
            if (fragContainer == null) {
                fragContainer = new ProfileFragment();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.fragment_container, fragContainer);
                ft.commit();
            }
        } */

        if (mUser != null) {
            if (fragContainer == null) {
                fragContainer = new ProfileFragment();
                FragmentTransaction ft = fm.beginTransaction();
                ft.add(R.id.fragment_container, fragContainer);
                ft.commit();
            }
        }

        profileToolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(profileToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Intent i = new Intent(getApplicationContext(), settingsActivity.class);
                startActivityForResult(i, 1);
                Log.d("Settings", "Activity will come up now");
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //
            }
        }
    }
}
