package itp341.caceres.nicholas.positive_note.app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * Created by NLCaceres on 5/9/2016.
 */
public class ProfileFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private ImageView userPic;
    private TextView userNameTV;
    private String UserName;
    private TextView userBioTV;

    private RadioButton publicRB;
    private RadioButton privateRB;
    private boolean isPrivate;
    private RadioButtonListener RBListener;

    private GoogleApiClient mGoogleClient;
    private Location userLocation;
    private double userLongitude;
    private double userLatitude;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mDBRef;

    private Button logoutButton;

    public static final String GOOGLE_TAG = "itp341.google_client";
    public static final String KEY_USERNAME = "itp341.userName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // TODO: Clean up old unused code

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();
        Uri userPhotoUrl = mUser.getPhotoUrl();
        UserName = mUser.getEmail().substring(0, mUser.getEmail().indexOf("@"));
        mDBRef = FirebaseFirestore.getInstance();

        userPic = (ImageView) v.findViewById(R.id.userProfileImageView);
        CircularProgressDrawable progressCircle = new CircularProgressDrawable(getContext());
        progressCircle.setStrokeWidth(5f);
        progressCircle.setCenterRadius(30f);
        progressCircle.start();

        String userPicURL;
        if (userPhotoUrl != null) {
            userPicURL = userPhotoUrl.toString();
        } else {
            userPicURL = getResources().getString(R.string.filler_profile_pic);
        }
        Glide.with(getContext())
                .load(userPicURL)
                .placeholder(progressCircle)
                .override(120, 120)
                .into(userPic);

        userNameTV = (TextView) v.findViewById(R.id.userProfileNameTextView);

//        SharedPreferences prefs = getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE);
//        UserName = prefs.getString(createAccount.PREFERENCE_USERNAME, "");
//        userNameTV.setText(UserName);

        if (mUser != null) {
            userNameTV.setText(UserName);
        }
        userBioTV = (TextView) v.findViewById(R.id.userBioTextView);

        DocumentReference userDocRef = mDBRef.collection("users").document(UserName);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDoc = task.getResult();
                            if (userDoc.exists()) {
                                userBioTV.setText(userDoc.getString("biography"));
                                if (userDoc.getBoolean("isPrivate") != null) {
                                    isPrivate = userDoc.getBoolean("isPrivate");
                                    if (isPrivate) {
                                        privateRB.setChecked(true);
                                    } else { publicRB.setChecked(true); }
                                } else {
                                    isPrivate = true;
                                    privateRB.setChecked(true);
                                }
                            }
                        }
                    }
                });

        logoutButton = (Button) v.findViewById(R.id.logOutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(createAccount.PREFERENCE_USERNAME, "default");
                prefEditor.apply();
                AuthUI.getInstance().signOut(getContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // Logged out, sent them back to login screen
                                getActivity().getSupportFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
                                getActivity().finish();
                            }
                        });
                // mFirebaseAuth.signOut();
                //Auth.GoogleSignInApi.signOut(mGoogleClient);
                // UserName = ANONYMOUS
            }
        });

        publicRB = (RadioButton) v.findViewById(R.id.publicRadioButton);
        privateRB = (RadioButton) v.findViewById(R.id.privateRadioButton);
        RBListener = new RadioButtonListener();
        publicRB.setOnClickListener(RBListener);
        privateRB.setOnClickListener(RBListener);
        //isPrivate = false;
        /*rootRef.child("isPrivate").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(boolean.class)) {
                    isPrivate = dataSnapshot.getValue(boolean.class);
                }

                if (isPrivate) {
                    privateRB.setChecked(isPrivate);
                }
                else {
                    publicRB.setChecked(isPrivate);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError firebaseError) {

            }
        }); */

        return v;
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleClient);
        if (userLocation != null) {
            userLatitude = userLocation.getLatitude();
            Log.i(GOOGLE_TAG, Double.toString(userLatitude));
            userLongitude = userLocation.getLongitude();
            Log.i(GOOGLE_TAG, Double.toString(userLongitude));
        }
        SharedPreferences prefs = getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putLong(createAccount.PREFERENCE_LATITUDE, Double.doubleToLongBits(userLatitude));
        prefEditor.putLong(createAccount.PREFERENCE_LONGITUDE, Double.doubleToLongBits(userLongitude));
        prefEditor.apply(); // using .commit() makes it a priority and can take up memory
        Log.d(GOOGLE_TAG, "Location found");
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(GOOGLE_TAG, "Location services suspended. Please reconnect");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(GOOGLE_TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
    }

    public class RadioButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton) view).isChecked();

            //Firebase isPrivateRef = new Firebase("https://positive-note.firebaseio.com/users/" + UserName + "/isPrivate");
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users").child(UserName).child("isPrivate");
            DocumentReference userRef = mDBRef.collection("users").document(UserName);

            switch (view.getId()) {
                case R.id.privateRadioButton: {
                    if (checked) {
                        isPrivate = true;
                        rootRef.setValue(isPrivate);
                    }
                    break;
                }
                case R.id.publicRadioButton: {
                    if (checked) {
                        /* if (mGoogleClient == null) {
                            mGoogleClient = new GoogleApiClient.Builder(getContext())
                                    .addConnectionCallbacks(ProfileFragment.this)
                                    .addOnConnectionFailedListener(ProfileFragment.this)
                                    .addApi(LocationServices.API)
                                    .build();
                        }
                        mGoogleClient.connect(); */
                        isPrivate = false;
                        rootRef.setValue(isPrivate);

                        SharedPreferences prefs = getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor prefEditor = prefs.edit();
                        prefEditor.putLong(createAccount.PREFERENCE_LATITUDE, Double.doubleToLongBits(userLatitude));
                        prefEditor.putLong(createAccount.PREFERENCE_LONGITUDE, Double.doubleToLongBits(userLongitude));
                        prefEditor.apply();

                        //GeoFire geofire = new GeoFire(new Firebase("https://positive-note.firebaseio.com/users/" + UserName));
                        GeoFire geoFire = new GeoFire(rootRef.child("users").child(UserName));
                        geoFire.setLocation("coordinates", new GeoLocation(userLatitude, userLongitude), new GeoFire.CompletionListener() {
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
                    break;
                }
            }
            userRef.update("isPrivate", isPrivate);
        }
    }

}
