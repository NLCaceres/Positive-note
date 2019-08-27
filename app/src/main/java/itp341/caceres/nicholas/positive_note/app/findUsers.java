package itp341.caceres.nicholas.positive_note.app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import itp341.caceres.nicholas.positive_note.app.Model.UserProfile;

public class findUsers extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Button updateUsersButton;
    private Double userLat;
    private Double userLon;
    private Location userLocation;
    private GoogleApiClient mGoogleClient;
    private String userName;
    private boolean isUserPrivate;

    private ListView findUsersList;
    private findUsersAdapter findAdapter;
    private ArrayList<UserProfile> foundUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_users);

        SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
        userLat = Double.longBitsToDouble(prefs.getLong(createAccount.PREFERENCE_LATITUDE, 0));
        userLon = Double.longBitsToDouble(prefs.getLong(createAccount.PREFERENCE_LONGITUDE, 0));
        userName = prefs.getString(createAccount.PREFERENCE_USERNAME, "");
        foundUsers = new ArrayList<>();

        //Firebase isPrivateRef = new Firebase("https://positive-note.firebaseio.com/users/" + userName + "/isPrivate");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("isPrivate");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isUserPrivate = dataSnapshot.getValue(boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
        });

        updateUsersButton = (Button) findViewById(R.id.addButton);
        updateUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserPrivate) {
                    //Firebase usersRef = new Firebase("https://positive-note.firebaseio.com/users/");
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users");
                    rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            foundUsers.clear();
                            for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                                foundUsers.add(dataSnapshot.getValue(UserProfile.class));
                            }
                            findAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {

                        }
                    });
                }
                else {
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(userName);
                    GeoFire geofire = new GeoFire(rootRef);
                    //GeoFire geofire = new GeoFire(new Firebase("https://positive-note.firebaseio.com/users/" + userName + "/"));
                    GeoQuery nearbyUsersQuery = geofire.queryAtLocation(new GeoLocation(userLat, userLon), 16.09);
                    nearbyUsersQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {

                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    });
                    DatabaseReference otherUserRef = FirebaseDatabase.getInstance().getReference().child("users").child("cgsazon");
                    GeoFire otherUserGeo = new GeoFire(otherUserRef);
                    //GeoFire otherUserGeo = new GeoFire(new Firebase("https://positive-note.firebaseio.com/users/cgsazon/"));
                    otherUserGeo.setLocation("coordinates", new GeoLocation(33.640687, -117.832642), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Log.d("itp341.coordinates", "There was an error saving the location to GeoFire: " + error);
                            } else {
                                Log.d("itp341.coordinates", "Location saved on server successfully!");
                            }
                        }
                    });
                    /* if (mGoogleClient == null) {
                        mGoogleClient = new GoogleApiClient.Builder(getApplicationContext())
                                .addConnectionCallbacks(findUsers.this)
                                .addOnConnectionFailedListener(findUsers.this)
                                .addApi(LocationServices.API)
                                .build();
                    }
                    mGoogleClient.connect(); */

                    geofire.setLocation("coordinates", new GeoLocation(userLat, userLon), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Log.d("itp341.coordinates", "There was an error saving the location to GeoFire: " + error);
                            } else {
                                Log.d("itp341.coordinates", "Location saved on server successfully!");
                            }
                        }
                    });
                }
            }
        });

        findUsersList = (ListView) findViewById(R.id.helperListView);

        findAdapter = new findUsersAdapter(getApplicationContext(), foundUsers);
        findUsersList.setAdapter(findAdapter);

    }

    public class findUsersAdapter extends ArrayAdapter<UserProfile> {
        public findUsersAdapter(Context context, ArrayList<UserProfile> FoundUsers) {
            super(context, 0, FoundUsers);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserProfile user = getItem(position);
            if (convertView == null) {
                if (user.getIsPrivate()) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_without, parent, false);
                } else {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_with_pic, parent, false);
                }
            }

            if (user.getIsPrivate()) {
                TextView userName = (TextView) convertView.findViewById(R.id.personUserName);
                userName.setText(user.getUserName());
                TextView userRecentMessage = (TextView) convertView.findViewById(R.id.chatText);
            }
            else {
                //ImageView userPic = (ImageView) convertView.findViewById(R.id.personPic);
                //Glide.with(getContext())
                  //      .load(getResources().getString(R.string.filler_profile_pic))
                    //    .override(60, 45)
                      //  .into(userPic);
                TextView userName = (TextView) convertView.findViewById(R.id.personUserName);
                userName.setText(user.getUserName());
                TextView userRecentMessage = (TextView) convertView.findViewById(R.id.chatText);
                TextView userLocationTV = (TextView) convertView.findViewById(R.id.userLocationTV);
                userLocationTV.setText(user.getUserLocation());
            }
            return convertView;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
        if (userLocation != null) {
            userLat = userLocation.getLatitude();
            Log.i(ProfileFragment.GOOGLE_TAG, Double.toString(userLat));
            userLon = userLocation.getLongitude();
            Log.i(ProfileFragment.GOOGLE_TAG, Double.toString(userLon));
        }
        SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putLong(createAccount.PREFERENCE_LATITUDE, Double.doubleToLongBits(userLat));
        prefEditor.putLong(createAccount.PREFERENCE_LONGITUDE, Double.doubleToLongBits(userLon));
        prefEditor.commit();
        Log.d(ProfileFragment.GOOGLE_TAG, "Location found");
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(ProfileFragment.GOOGLE_TAG, "Location services suspended. Please reconnect");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(ProfileFragment.GOOGLE_TAG, "Location services connection failed with code " + connectionResult.getErrorCode());

    }
}
