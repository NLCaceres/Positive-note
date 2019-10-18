package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;

public class UserProfileActivity extends AppCompatActivity {

  private Toolbar profileToolbar;
  private Fragment fragContainer;

  private UserInfo mUserInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_profile);

    FragmentManager fm = getSupportFragmentManager();
    fragContainer = fm.findFragmentById(R.id.fragment_container);

    if (fragContainer == null) {
      fragContainer = new ProfileFragment();
      FragmentTransaction ft = fm.beginTransaction();
      ft.add(R.id.fragment_container, fragContainer);
      ft.commit();
    }

    mUserInfo = getIntent().getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO);

    profileToolbar = (Toolbar) findViewById(R.id.home_toolbar);
    setSupportActionBar(profileToolbar);
    getSupportActionBar().setTitle(mUserInfo.getUserName());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.settings_button_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_settings) {
      Intent intent = new Intent(getApplicationContext(), UserSettingsActivity.class);
      intent.putExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO, mUserInfo);
      startActivityForResult(intent, 1); // Fragment has its own version so be careful which you use!
      Log.d("Settings", "Activity will come up now");
      return true;
    } else { return super.onOptionsItemSelected(item); }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data); // It MAY propagate to fragment
    if (requestCode == 0) { // Coming from createAccount I think
      if (resultCode == RESULT_OK) {
        ProfileFragment profileFragment = ProfileFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();

      }
    }
    if (requestCode == 1) { // Coming from SettingActivity
      if (resultCode == RESULT_OK) {
        Log.d("Settings Result", "Returning from settings activity");
      } else {
        Log.d("Settings Result", "Back button pressed from settings activity");
        fragContainer.onActivityResult(requestCode, resultCode, data);
      }
    }
  }
}
