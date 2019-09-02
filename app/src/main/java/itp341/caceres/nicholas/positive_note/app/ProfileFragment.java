package itp341.caceres.nicholas.positive_note.app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * Created by NLCaceres on 5/9/2016.
 */
public class ProfileFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private ProgressBar mProgBar;

    private ImageView userPic;
    private TextView userNameTV;
    private String UserName;
    private TextView userBioTV;

    private RadioButton publicRB;
    private RadioButton privateRB;
    private boolean isPrivate;
    private RadioButtonListener RBListener;

    private Button logoutButton;

    private GoogleApiClient mGoogleClient;
    private Location userLocation;
    private double userLongitude;
    private double userLatitude;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUser;
    private DocumentReference mUserDocRef;

    public static final String GOOGLE_TAG = "itp341.google_client";
    public static final String KEY_USERNAME = "itp341.userName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mProgBar = v.findViewById(R.id.app_progressbar);
        mProgBar.setVisibility(View.VISIBLE);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();
        mUserDocRef = FirebaseFirestore.getInstance().collection("users").document(mUser.getEmail());
        UserName = mUser.getEmail().substring(0, mUser.getEmail().indexOf("@"));
        Uri userPhotoUrl = mUser.getPhotoUrl();

        userNameTV = (TextView) v.findViewById(R.id.userProfileNameTextView);
        userBioTV = (TextView) v.findViewById(R.id.userBioTextView);
        userPic = (ImageView) v.findViewById(R.id.userProfileImageView);

        CircularProgressDrawable progressCircle = new CircularProgressDrawable(getContext());
        progressCircle.setStrokeWidth(5f);
        progressCircle.setCenterRadius(30f);
        progressCircle.start();

        String userPicURL;
        if (userPhotoUrl != null) { userPicURL = userPhotoUrl.toString().replace("96", "400"); }
        else { userPicURL = getResources().getString(R.string.filler_profile_pic); } // If needed swap the below glide for an override (no signature or transform)
        Glide.with(getContext())
                .load(userPicURL)
                .placeholder(progressCircle)
                .signature(new ObjectKey(mUser.getMetadata()))
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(userPic);

        publicRB = (RadioButton) v.findViewById(R.id.publicRadioButton);
        privateRB = (RadioButton) v.findViewById(R.id.privateRadioButton);
        RBListener = new RadioButtonListener();
        publicRB.setOnClickListener(RBListener);
        privateRB.setOnClickListener(RBListener);

        SharedPreferences prefs = getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE);
        UserName = prefs.getString(createAccount.PREFERENCE_USERNAME, "");
        isPrivate = prefs.getBoolean(createAccount.PREFERENCE_IS_PRIVATE, true);

        userNameTV.setText(UserName);
        if (isPrivate) { privateRB.setChecked(true); }
        else { publicRB.setChecked(true); }

        mUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot userDoc = task.getResult();
                            if (userDoc.exists()) {
                                if (userDoc.getString("biography") != null) {
                                    userBioTV.setText(userDoc.getString("biography"));
                                } else { userBioTV.setText(getResources().getString(R.string.user_bio_default)); }

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
                        mProgBar.setVisibility(View.GONE);
                    }
                });

        logoutButton = (Button) v.findViewById(R.id.logOutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE).edit().clear().apply();

                AuthUI.getInstance().signOut(getContext()) // mFirebaseAuth.signOut(); is also an option for all providers!
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) { // Logged out, sent them back to login screen
                                getActivity().getSupportFragmentManager().beginTransaction().remove(ProfileFragment.this).commit();
                                getActivity().finish();
                            }
                        });
                //Auth.GoogleSignInApi.signOut(mGoogleClient); - Useful for Google Sign-in. NOT RELATED TO FIREBASE
            }
        });

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
        SharedPreferences.Editor prefEditor = getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE).edit();
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

            if (view.getId() == R.id.privateRadioButton && checked) { isPrivate = true; }
            else {
                isPrivate = false;
                // Save UserLat and UserLong or access them in sharedPreferences from onConnected func above
                // TODO: Grab userLocation with Geofire
                /* if (mGoogleClient == null) {
                            mGoogleClient = new GoogleApiClient.Builder(getContext())
                                    .addConnectionCallbacks(ProfileFragment.this)
                                    .addOnConnectionFailedListener(ProfileFragment.this)
                                    .addApi(LocationServices.API)
                                    .build();
                        }
                        mGoogleClient.connect(); */

                //GeoFire geoFire = new GeoFire(rootRef.child("users").child(UserName)); - GOAL HERE: USE LOCATIONS SAVED ON FIREBASE FOR USER DOC
//                        geoFire.setLocation("coordinates", new GeoLocation(userLatitude, userLongitude), new GeoFire.CompletionListener() {
//                            @Override
//                            public void onComplete(String key, DatabaseError error) {
//                                if (error != null) {
//                                    Log.d("itp341.coordinates", "There was an error saving the location to GeoFire: " + error);
//                                } else {
//                                    Log.d("itp341.coordinates", "Location saved on server successfully!");
//                                }
//                            }
//                        });
            }
            mUserDocRef.update("isPrivate", isPrivate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("Update isPrivate", "isPrivate update successful");
                    }
                }
            });
        }
    }

}
