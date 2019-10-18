package itp341.caceres.nicholas.positive_note.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.alpharogroup.jgeohash.GeoHashExtensions;
import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;

public class FindUsersActivity extends AppCompatActivity {


  private Double userLat;
  private Double userLong;
  private Location userLocation;

  private FirebaseUser mFirebaseUser;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private CollectionReference mUsersDBRef;

  private UserInfo fullUser;
  private String userName;
  private String userHash;
  private boolean isUserPrivate;
  private boolean isUserHelper;
  private boolean isBlue;
  private boolean isRelationship;
  private boolean isLooks;
  private boolean isEmotions;
  private boolean isSelf;
  // TODO: Delete the above bools

  private Toolbar homeToolbar;
  private ProgressBar mProgBar;
  private Button findNewUsersButton;

  private ListView findUsersListView;
  private FindUsersAdapter findAdapter;
  private List<UserInfo> foundUsers;
  //private ArrayList<UserProfile> foundUsers;

  public static double formatLocationData(double userLocationData) { // Used to round and format doubles to ###.# (x num of digits and 1st place decimal)
    DecimalFormat decFormat = new DecimalFormat("###.#");
    decFormat.setRoundingMode(RoundingMode.HALF_EVEN); // Round in whatever direction is closest 2.6 to 3, 1.1 to 1, etc.

    String formattedUserLocation = decFormat.format(userLocationData);
    return Double.parseDouble(formattedUserLocation);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_find_users);

    // Init Google/Firebase API vars
    mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    mUsersDBRef = FirebaseFirestore.getInstance().collection("users");

    // Init Views
    mProgBar = findViewById(R.id.app_progressbar);
    homeToolbar = findViewById(R.id.home_toolbar);
    setSupportActionBar(homeToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    fullUser = getIntent().getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO);

    findNewUsersButton = (Button) findViewById(R.id.addButton);
    findNewUsersButton.setOnClickListener(new FindUsersButtonListener());

    findUsersListView = (ListView) findViewById(R.id.helperListView);
    foundUsers = new ArrayList<>();
    findAdapter = new FindUsersAdapter(getApplicationContext(), foundUsers);
    findUsersListView.setAdapter(findAdapter);
    findUsersListView.setOnItemClickListener(new NewChatItemListener());

    //setViews();

    findOtherUsers();
  }

  private void setViews() {
    // Set vars and view values
    setSupportActionBar(homeToolbar);

    Bundle bundle = getIntent().getExtras();

    userLat = bundle.getDouble(HomeActivity.BUNDLE_USER_LAT);
    userLong = bundle.getDouble(HomeActivity.BUNDLE_USER_LONG);
    userHash = bundle.getString(HomeActivity.BUNDLE_USER_HASH);

    SharedPreferences prefs = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE);
    if (userHash == null) {
      userHash = prefs.getString("userHash", null);
      if (userHash == null) {
        userLat = Double.longBitsToDouble(prefs.getLong(ConstantsKt.PREFERENCE_LATITUDE, 0));
        userLong = Double.longBitsToDouble(prefs.getLong(ConstantsKt.PREFERENCE_LONGITUDE, 0));
        if (userLat == 0 || userLong == 0) {
          fetchUserLocation();
        }
      }
    }

    userName = bundle.getString(HomeActivity.BUNDLE_USER_NAME);
    if (userName == null) {
      userName = prefs.getString(ConstantsKt.PREFERENCE_USERNAME, mFirebaseUser.getEmail().substring(0, mFirebaseUser.getEmail().indexOf("@")));
    }
    try {
      String result = new PreferencesAsyncTask().execute(getApplicationContext()).get();
    } catch (Exception execError) {
      Log.d("PrefsAsyncTask Issue", "Problem while retrieving preferences in the background");
    }
    isUserPrivate = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_PRIVATE, true);
    isUserHelper = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_HELPER, false);
    isBlue = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_BLUE, false);
    isRelationship = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_RELATIONSHIP, false);
    isLooks = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_LOOK, false);
    isEmotions = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_EMOTIONAL, false);
    isSelf = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_SELF, false);
  }

  private void fetchUserLocation() {
    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, new String[]
          {Manifest.permission.ACCESS_COARSE_LOCATION}, HomeActivity.LOCATION_REQUEST_CODE);
      return;
    }
    /* getLastLocation will only get last observed location (so in emulator googleMaps is only source of location coordinates
     * which is not a problem with a real phone where other apps are constantly asking for location updates)
     * Here if I want updates, go to Google Maps, get it, send the phone a new update via triple-dot extra controls, and go back to app to see update  */
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
      if (location != null) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
          fullUser.getCurrentLocation().setUserLatitude(location.getLatitude());
          fullUser.getCurrentLocation().setUserLongitude(location.getLongitude());
          fullUser.getCurrentLocation().setUserHash(GeoHashExtensions.encode(location.getLatitude(), location.getLongitude()));
          fullUser.getCurrentLocation().setUserCity(geocoder
              .getFromLocation(fullUser.getCurrentLocation().getUserLatitude(), fullUser.getCurrentLocation().getUserLongitude(), 1).get(0).getLocality());
        } catch (Exception e) {
          Log.w("Location Failure", "Issue with finding the device location");
        }
        userLat = location.getLatitude();
        userLong = location.getLongitude();
        userHash = GeoHashExtensions.encode(userLat, userLong);
      }
    });
  }

  private void findOtherUsers() {
    mProgBar.setVisibility(View.VISIBLE);

    Query userPrivacyHelperLocationQuery = mUsersDBRef.whereEqualTo("private", false)
        .whereEqualTo("helper", !fullUser.getHelper()).whereEqualTo("currentLocation.userHash", fullUser.getCurrentLocation().getUserHash())
        .whereEqualTo("verified", true);

    userPrivacyHelperLocationQuery.get().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        for (QueryDocumentSnapshot doc : task.getResult()) {
          Log.d("Fuller Query", doc.getId() + " -> " + doc.getData());
          UserInfo userFound = doc.toObject(UserInfo.class);
          if (checkUser(userFound)) {
            foundUsers.add(userFound);
          }
        }
      }
      findAdapter.notifyDataSetChanged();
      mProgBar.setVisibility(View.INVISIBLE);
    });
  }

  private boolean checkUser(UserInfo otherUser) {
    boolean blue = fullUser.getCurrentFeelings().getBlue();
    boolean blueCheck = blue && otherUser.getCurrentFeelings().getBlue() == blue;

    boolean emotions = fullUser.getCurrentFeelings().getEmotions();
    boolean emotionsCheck = emotions && otherUser.getCurrentFeelings().getEmotions() == emotions;

    boolean looks = fullUser.getCurrentFeelings().getLooks();
    boolean looksCheck = looks && otherUser.getCurrentFeelings().getLooks() == looks;

    boolean relationship = fullUser.getCurrentFeelings().getRelationship();
    boolean relationshipCheck = relationship && otherUser.getCurrentFeelings().getRelationship() == relationship;

    boolean self = fullUser.getCurrentFeelings().getSelf();
    boolean selfCheck = self && otherUser.getCurrentFeelings().getRelationship() == self;

    if (blueCheck || emotionsCheck || looksCheck || relationshipCheck || selfCheck) {
      return true;
    }
    return false;
  }

  // Need to work around not being able to actively manipulate activity
  static class PreferencesAsyncTask extends AsyncTask<Context, Void, String> {
    @Override
    protected String doInBackground(Context... params) {
      Context context = params[0];
      SharedPreferences prefs = context.getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE);
      // May just pass into a userInfo object to return
      return null;
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
    }

  }

  class FindUsersButtonListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      if (fullUser.getPrivate()) { // Originally grabbed all users and added into list
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.change_privacy_settings_hint), Toast.LENGTH_SHORT).show();
      } // Originally set query to 16 mile radius, set location of another user and set current User location with Geofire (now deprecated/dead)
      findOtherUsers();
    }
  }

  class NewChatItemListener implements AdapterView.OnItemClickListener {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      UserInfo user = (UserInfo) adapterView.getItemAtPosition(position);
      Intent i = new Intent(getApplicationContext(), InMessageActivity.class);
      i.putExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO, fullUser);
      i.putExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_CHAT_PARTNER_INFO, user);
      startActivity(i);
    }
  }

  public class FindUsersAdapter extends ArrayAdapter<UserInfo> {
    FindUsersAdapter(Context context, List<UserInfo> FoundUsers) {
      super(context, 0, FoundUsers);
    }

    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull ViewGroup parent) {
      UserInfo user = getItem(position);
      if (convertView == null) {
        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_with_pic, parent, false);
      }

      // TODO: Cloud Storage for image support (DON'T convert images to 64bit String)
      ImageView userPic = (ImageView) convertView.findViewById(R.id.chatUserImageView);
      Glide.with(getContext())
          .load(getResources().getString(R.string.filler_profile_pic))
          .override(60, 45)
          .into(userPic);
      TextView userNameTV = (TextView) convertView.findViewById(R.id.chatUserNameTV);
      userNameTV.setText(user.getUserName());
      TextView userLocationTV = (TextView) convertView.findViewById(R.id.chatUserLocationTV);
      userLocationTV.setText(user.getCurrentLocation().getUserCity());
      // TODO: May change to list of feelings in common
      TextView userBioTV = (TextView) convertView.findViewById(R.id.chatRecentMessageTV);
      userBioTV.setText(user.getBiography());

      return convertView;
    }
  }
}
