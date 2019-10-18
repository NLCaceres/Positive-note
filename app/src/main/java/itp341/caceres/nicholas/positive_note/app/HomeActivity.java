package itp341.caceres.nicholas.positive_note.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.alpharogroup.jgeohash.GeoHashExtensions;
import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserMessage;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;

public class HomeActivity extends AppCompatActivity {

  public static final int RC_SIGN_IN = 0;
  public static final int LOCATION_REQUEST_CODE = 101;
  public static final String BUNDLE_FILE = "itp341.caceres.nicholas.positivenote.bundle";
  public static final String BUNDLE_USER_NAME = BUNDLE_FILE + ".userName";
  public static final String BUNDLE_USER_LAT = BUNDLE_FILE + ".userLat";
  public static final String BUNDLE_USER_LONG = BUNDLE_FILE + ".userLong";
  public static final String BUNDLE_USER_HASH = BUNDLE_FILE + ".userHash";
  public static String PACKAGE_NAME;
  private Toolbar homeToolbar;
  private TextView welcomeTextView;
  private ViewPager2 positiveNotePager;
  private FragmentStateAdapter pagerAdapter;
  private ArrayList<UserMessage> positiveNotesList;
  private int currentPage;
  private ProgressBar mProgBar;
  private Button startChatButton;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private FirebaseAuth mFirebaseAuth;
  private FirebaseUser mFirebaseUser;
  private CollectionReference mUsersCollection;
  private UserInfo mUserInfo;
  private String userDisplayName;
  private Location userLocation;
  private Handler handler;
  Runnable update = () -> {
    if (currentPage == positiveNotesList.size()) {
      currentPage = 0;
    }
    positiveNotePager.setCurrentItem(currentPage++, true);
    handler.postDelayed(this.update, 3000);
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_screen);

    PACKAGE_NAME = getApplicationContext().getPackageName();

    // Init Views
    homeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
    setSupportActionBar(homeToolbar);
    welcomeTextView = (TextView) findViewById(R.id.welcomeMessageTextView);
    positiveNotePager = findViewById(R.id.positiveNoteViewPager);
    startChatButton = (Button) findViewById(R.id.startChatButton);
    mProgBar = findViewById(R.id.app_progressbar);

    // Init Firebase Auth
    mFirebaseAuth = FirebaseAuth.getInstance();
    mFirebaseUser = mFirebaseAuth.getCurrentUser();
    mUsersCollection = FirebaseFirestore.getInstance().collection("users");

    // Setup Views
    if (mFirebaseUser != null) {
      if (userDisplayName == null) {
        userDisplayName = mFirebaseUser.getDisplayName();
      } // Considering alternate/randomized welcome messages, but currently drawing a blank as to what other messages could appear
      welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_init_user_message)).append(" ").append(userDisplayName).append("!"));
    }

    positiveNotesList = new ArrayList<>();
    positiveNotePager.setPageTransformer(new DepthPageTransformer());
    pagerAdapter = new AutoSliderPagerAdapter(this); // AppCompat descends from FragmentActivity which descends from Activity
    positiveNotePager.setAdapter(pagerAdapter);
    handler = new Handler();

    // Button Listener setup
    startChatButton.setOnClickListener(v -> {
      Intent i = new Intent(getApplicationContext(), TabsActivity.class);
      if (mUserInfo != null) {
        i.putExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO, mUserInfo);
      }
      startActivity(i);
    });

    mUserInfo = new Gson().fromJson(getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).getString(ConstantsKt.PREFERENCE_FULL_USER_INFO, ""), UserInfo.class);

    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    fetchUserLocation();
  }

  @Override
  public void onStart() {
    super.onStart();

    mFirebaseUser = mFirebaseAuth.getCurrentUser();
    if (mFirebaseUser == null) { // Not signed in, launch the Sign In activity
      setUpFirebaseLogin();
    } else {
      if (userDisplayName == null) { // This is actually called before onActivityResult! so update welcomeTextView!
        userDisplayName = mFirebaseUser.getDisplayName();
        welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_back_user_message)).append(" ").append(userDisplayName).append("!"));
        fetchUserLocation();
      }
      setupPositiveNotes();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    handler.postDelayed(update, 3000); // First call to handler and runnable
  }

  @Override
  protected void onPause() {
    super.onPause();
    handler.removeCallbacks(update);
  }

  @Override
  protected void onStop() {
    super.onStop();
    welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_back_user_message)).append(" ").append(userDisplayName).append("!"));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.profile_button_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.action_profile) {
      Intent i = new Intent(getApplicationContext(), UserProfileActivity.class);
      if (mUserInfo != null) {
        i.putExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO, mUserInfo);
      }
      startActivity(i);
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      IdpResponse response = IdpResponse.fromResultIntent(data);

      if (resultCode == RESULT_OK) { // Successfully signed in
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        userDisplayName = mFirebaseUser.getDisplayName(); // Usually User's actual name
        welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_init_user_message)).append(" ").append(userDisplayName).append("!"));
        final String userEmail = mFirebaseUser.getEmail();
        final String userName = userEmail.substring(0, 1).toUpperCase() + userEmail.substring(1, userEmail.indexOf("@")); // Usually user email without the address

        // TODO: Code clean up
        final SharedPreferences.Editor prefEditor = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit();
        prefEditor.putString(ConstantsKt.PREFERENCE_USER_DISPLAY_NAME, userDisplayName);
        prefEditor.putString(ConstantsKt.PREFERENCE_USER_EMAIL, userEmail);
        prefEditor.putString(ConstantsKt.PREFERENCE_USERNAME, userName);
        prefEditor.apply();

        // TODO: Add additional login screen to catch duplicates (JohnDoe@Gmail would match a JohnDoe@Aol.com Username so no good)

        final DocumentReference userDocRef = mUsersCollection.document(userEmail);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
          @Override
          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
              DocumentSnapshot document = task.getResult();
              if (document.exists()) {
                Log.d("Login UserDoc Check", "User Doc Found. Don't update");
                mUserInfo = document.toObject(UserInfo.class);
                prefEditor.putString(ConstantsKt.PREFERENCE_FULL_USER_INFO, new Gson().toJson(mUserInfo));
                prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_PRIVATE, mUserInfo.getPrivate());
                prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_HELPER, mUserInfo.getHelper());
                prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_BLUE, mUserInfo.getCurrentFeelings().getBlue());
                prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_RELATIONSHIP, mUserInfo.getCurrentFeelings().getRelationship());
                prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_EMOTIONAL, mUserInfo.getCurrentFeelings().getEmotions());
                prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_LOOK, mUserInfo.getCurrentFeelings().getLooks());
                prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_SELF, mUserInfo.getCurrentFeelings().getSelf());
                prefEditor.apply(); //Apply all
              } else {
                Map<String, Object> newUser = new HashMap<>();
                newUser.put("fullName", userDisplayName);
                newUser.put("email", userEmail);
                newUser.put("username", userName);
                newUser.put("verified", mFirebaseUser.isEmailVerified());
                setDBandPrefDefaults(newUser);

                userDocRef.set(newUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {
                        Log.d("User DB Upload Success", "Database upload of new user successful");
                      }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                        Log.w("User DB Upload Failed: ", "DB Upload failed with error: ", e);
                      }
                    });
              }
            } else {
              Log.w("Error UserDoc Check", task.getException());
            }
          }
        });
      } else { // Sign in failed/cancelled (probably) - handle error
        Log.w("Sign In Issue: ", "Issue logging, but returned from activity");
      }
    }
  }

  private void setUpFirebaseLogin() {
    ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
        .setAndroidPackageName(PACKAGE_NAME, true, null) // Package Name, Install redirect, min version of App
        .setHandleCodeInApp(true) // Must be true ALWAYS
        .setUrl("https://positiveNote.page.link").build(); // Whitelist the link here

    List<AuthUI.IdpConfig> providers = Arrays.asList( // Choose authentication providers
        new AuthUI.IdpConfig.EmailBuilder().enableEmailLinkSignIn().setActionCodeSettings(actionCodeSettings).build(),
        new AuthUI.IdpConfig.GoogleBuilder().build());

    startActivityForResult(
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.AppTheme)
            .build(),
        RC_SIGN_IN);

    // With Firebase Dynamic Links (deep link), you could send user to different part of app if configured
        /* if (AuthUI.canHandleIntent(getIntent())) {
                if (getIntent().getExtras() == null) {
                    return;
                }
                String link = getIntent().getExtras().getString(ExtraConstants.EMAIL_LINK_SIGN_IN);
                if (link != null) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setEmailLink(link)
                                    .setAvailableProviders(providers)
                                    .build(), // Firebase also allows setTheme and setLogo with drawables
                            RC_SIGN_IN);
                }
        } */
  }

  private void setupPositiveNotes() {
    if (mFirebaseUser != null) {
      positiveNotesList.clear();
      mUsersCollection.document(mFirebaseUser.getEmail()).collection("positiveNotes").whereLessThanOrEqualTo("timeStamp", new Date()).limit(5)
          .get().addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
          if (task.getResult() != null) {
            for (DocumentSnapshot doc : task.getResult()) {
              positiveNotesList.add(doc.toObject(UserMessage.class));
            }
          }
          pagerAdapter.notifyDataSetChanged(); // Will change all BG colors due to changes in list
        } else {
          positiveNotePager.setVisibility(View.INVISIBLE);
          Toast.makeText(getApplicationContext(), "Issue grabbing your latest Positive Notes!", Toast.LENGTH_SHORT).show();
        }
        mProgBar.setVisibility(View.INVISIBLE);
      });
    }
  }

  private void setDBandPrefDefaults(Map<String, Object> newUser) {
    newUser.put("private", true);
    newUser.put("helper", false);
    newUser.put("accountCreationDate", new Timestamp(new Date()));

    Map<String, Object> currentFeelings = new HashMap<>(); // Nested Data entry for set of user's feelings / feelings they can help with
    currentFeelings.put("blue", false);
    currentFeelings.put("relationship", false);
    currentFeelings.put("emotions", false);
    currentFeelings.put("looks", false);
    currentFeelings.put("self", false);
    newUser.put("currentFeelings", currentFeelings);

    SharedPreferences.Editor prefEditor = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit();
    prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_PRIVATE, true);
    prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_HELPER, false);
    prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_BLUE, false);
    prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_RELATIONSHIP, false);
    prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_EMOTIONAL, false);
    prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_LOOK, false);
    prefEditor.putBoolean(ConstantsKt.PREFERENCE_IS_SELF, false);
    prefEditor.apply();
  }

  private void fetchUserLocation() {
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]
          {Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
      return;
    }
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
      @Override
      public void onSuccess(Location location) {
        if (location != null) {
          userLocation = location;
          setUserDBLocation();
        }
      }
    });
  }

  private void setUserDBLocation() {
    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
    try {
      String userCity = geocoder.getFromLocation(userLocation.getLatitude(), userLocation.getLongitude(), 1).get(0).getLocality();
      String locationHash = GeoHashExtensions.encode(userLocation.getLatitude(), userLocation.getLongitude()).substring(0, 3);
      double userLat = FindUsersActivity.formatLocationData(userLocation.getLatitude());
      double userLong = FindUsersActivity.formatLocationData(userLocation.getLongitude());

      SharedPreferences.Editor prefEditor = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit();
      prefEditor.putString("userCity", userCity);
      prefEditor.putString("userHash", locationHash);
      prefEditor.putLong("userLatitude", Double.doubleToLongBits(userLat));
      prefEditor.putLong("userLongitude", Double.doubleToLongBits(userLong));
      prefEditor.apply();

      Map<String, Object> currentLocation = new HashMap<>();
      currentLocation.put("userCity", userCity);
      currentLocation.put("userHash", locationHash);
      currentLocation.put("userLatitude", userLat);
      currentLocation.put("userLongitude", userLong);
      FirebaseFirestore.getInstance().collection("users").document(mFirebaseUser.getEmail()).update("currentLocation", currentLocation)
          .addOnSuccessListener(aVoid -> Log.d("Location Update", "Successfully upload user location settings to DB"));

    } catch (IOException ioException) {
      Log.d("Geocoder Error", "Issue with ioException");
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) { // Useful if multiple permissions are needed and must be requested
      case LOCATION_REQUEST_CODE: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // If request cancelled, grantResults arr is empty.
          fetchUserLocation(); // Permission granted - Find the Location
        } else {
          // Permission denied - Disable function (or do nothing, same thing effectively)
        }
        return;
      }
    }
  }

  private class AutoSliderPagerAdapter extends FragmentStateAdapter {
    AutoSliderPagerAdapter(@NonNull FragmentActivity fragmentActivity) { super(fragmentActivity); }

    @NonNull @Override public Fragment createFragment(int position) { return AutoSlideFragment.newInstance(positiveNotesList.get(position)); }

    @Override public int getItemCount() { return positiveNotesList.size(); }
  }

  private class DepthPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
      int pageWidth = view.getWidth();

      if (position < -1) { // [-Infinity,-1)
        // This page is way off-screen to the left.
        view.setAlpha(0f);

      } else if (position <= 0) { // [-1,0]
        // Use the default slide transition when moving to the left page
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);

      } else if (position <= 1) { // (0,1]
        // Fade the page out.
        view.setAlpha(1 - position);

        // Counteract the default slide transition
        view.setTranslationX(pageWidth * -position);

        // Scale the page down (between MIN_SCALE and 1)
        float scaleFactor = MIN_SCALE
            + (1 - MIN_SCALE) * (1 - Math.abs(position));
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);

      } else { // (1,+Infinity]
        // This page is way off-screen to the right.
        view.setAlpha(0f);
      }
    }
  }
}
