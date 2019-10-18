package itp341.caceres.nicholas.positive_note.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;

import static android.app.Activity.RESULT_CANCELED;

/**
 * Created by NLCaceres on 5/9/2016.
 */
public class ProfileFragment extends Fragment {

//  public static final String GOOGLE_TAG = "itp341.google_client";
//  public static final String KEY_USERNAME = "itp341.userName";
  private ProgressBar mProgBar;
  private ImageView userPic;
  private TextView userFullNameTV;
  private TextView userNameTV;
  private TextView userEmailTV;
  private TextView userLocationTV;
  private TextView userBioTV;
  private RadioButton publicRB;
  private RadioButton privateRB;
  private RadioButtonListener RBListener;
  private Button logoutButton;
  private FirebaseUser mUser;
  private UserInfo mUserInfo;
  private DocumentReference mUserDocRef;
  private boolean isPrivate;
  private String userName;
  private String userBio;

  public static ProfileFragment newInstance() {
    ProfileFragment profFrag = new ProfileFragment();

    // Bundle is used to trade data across activities vs SharedPreferences for data persistence
//        Bundle args = new Bundle();
//        args.putString("tag", "value");
//        profFrag.setArguments(args);

    return profFrag;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) { // Init views typically
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

    mProgBar = v.findViewById(R.id.app_progressbar);
    mProgBar.setVisibility(View.INVISIBLE);

    mUser = FirebaseAuth.getInstance().getCurrentUser();
    mUserInfo = getActivity().getIntent().getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO);
    mUserDocRef = FirebaseFirestore.getInstance().collection("users").document(mUserInfo.getEmail());
    //userName = mUser.getEmail().substring(0, mUser.getEmail().indexOf("@"));
    Uri userPhotoUrl = mUser.getPhotoUrl();

    userFullNameTV = (TextView) v.findViewById(R.id.userProfileNameTextView);
    userNameTV = v.findViewById(R.id.userNameTextView);
    userEmailTV = v.findViewById(R.id.userEmailTextView);
    userLocationTV = v.findViewById(R.id.userLocationTextView);
    userBioTV = (TextView) v.findViewById(R.id.userBioTextView);
    userPic = (ImageView) v.findViewById(R.id.userProfileImageView);

    CircularProgressDrawable progressCircle = new CircularProgressDrawable(getContext());
    progressCircle.setStrokeWidth(5f);
    progressCircle.setCenterRadius(30f);
    progressCircle.start();

    String userPicURL;
    if (userPhotoUrl != null) {
      userPicURL = userPhotoUrl.toString().replace("96", "400");
    } else {
      userPicURL = getResources().getString(R.string.filler_profile_pic);
    } // If needed swap the below glide for an override (no signature or transform)
    Glide.with(getContext())
        .load(userPicURL)
        .placeholder(progressCircle)
        .signature(new ObjectKey(mUser.getMetadata()))
        .transform(new CenterCrop(), new RoundedCorners(30)) // Unchecked variable arg array issue
        .into(userPic);

    publicRB = (RadioButton) v.findViewById(R.id.publicRadioButton);
    privateRB = (RadioButton) v.findViewById(R.id.privateRadioButton);
    RBListener = new RadioButtonListener();
    publicRB.setOnClickListener(RBListener);
    privateRB.setOnClickListener(RBListener);

    if (mUserInfo != null) {
      userFullNameTV.setText(mUserInfo.getFullName());

      if (mUserInfo.getPrivate()) {
        privateRB.setChecked(true);
      } else {
        publicRB.setChecked(true);
      }

      userNameTV.setText(mUserInfo.getUserName());
      userEmailTV.setText(mUserInfo.getEmail());
      userLocationTV.setText(mUserInfo.getCurrentLocation().getUserCity());
      userBioTV.setText(mUserInfo.getBiography());
    } else {
      mProgBar.setVisibility(View.VISIBLE);
      fetchUserInfo();
    }

    logoutButton = (Button) v.findViewById(R.id.logOutButton);
    logoutButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().getSharedPreferences(ConstantsKt.PREFERENCE_FILE, Context.MODE_PRIVATE).edit().clear().apply();

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
  public void onActivityResult(int requestCode, int resultCode, Intent data) { // SharedPreferences are not ready yet
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1) { // Propagated code from Profile Activity
      if (resultCode == RESULT_CANCELED) {
        Log.d("Prof Frag Result", "Activity propagated this up to fragment");

        mUserInfo = data.getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO);
        if (mUserInfo != null && mUserInfo.getBiography().length() > 0 && mUserInfo.getFullName().length() > 0) {
          userBioTV.setText(mUserInfo.getBiography());
          userFullNameTV.setText(mUserInfo.getFullName());
        }
//        userBio = data.getStringExtra("user-bio"); // Should always have something, but other extras may be null
//        userBioTV.setText(userBio);
//        String userScreenName = data.getStringExtra("user-name");
//        userFullNameTV.setText(userScreenName);
      }
    }
  }

  private void grabPreferences() {
    SharedPreferences prefs = getActivity().getSharedPreferences(ConstantsKt.PREFERENCE_FILE, Context.MODE_PRIVATE);
    mUserInfo = new Gson().fromJson(prefs.getString(ConstantsKt.PREFERENCE_FULL_USER_INFO, ""), UserInfo.class);
    isPrivate = prefs.getBoolean(ConstantsKt.PREFERENCE_IS_PRIVATE, false); // Do null checks to check if DB request is needed!
    userName = prefs.getString(ConstantsKt.PREFERENCE_USERNAME, null);
    userBio = prefs.getString(ConstantsKt.PREFERENCE_USER_BIO, null);
  }

  private void fetchUserInfo() {
    mUserDocRef.get().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        DocumentSnapshot userDoc = task.getResult();
        if (userDoc.exists()) {
          if (userDoc.getString("fullName") != null) {
            userFullNameTV.setText(userDoc.getString("fullName"));
          }
          if (userDoc.getString("userName") != null) {
            userNameTV.setText(userDoc.getString("userName"));
          }
          if (userDoc.getString("email") != null) {
            userEmailTV.setText(userDoc.getString("email"));
          }
          if (userDoc.getString("currentLocation.userCity") != null) {
            userLocationTV.setText(userDoc.getString("currentLocation.userCity"));
          }
          if (userDoc.getString("biography") != null && userDoc.getString("biography").length() > 0) {
            userBioTV.setText(userDoc.getString("biography"));
          } else {
            userBioTV.setText(getResources().getString(R.string.user_bio_default));
          }

          if (userDoc.getBoolean("private") != null) {
            isPrivate = userDoc.getBoolean("private");
            if (isPrivate) {
              privateRB.setChecked(true);
            } else {
              publicRB.setChecked(true);
            }
          } else {
            isPrivate = true;
            privateRB.setChecked(true);
          }
        }
      }
      mProgBar.setVisibility(View.GONE);
    });
  }

  public class RadioButtonListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      boolean checked = ((RadioButton) view).isChecked();

      if (view.getId() == R.id.privateRadioButton && checked) {
        isPrivate = true;
      } else {
        isPrivate = false;
      }
      getActivity().getSharedPreferences(ConstantsKt.PREFERENCE_FILE, Context.MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_PRIVATE, isPrivate).apply();

      mUserDocRef.update("private", isPrivate).addOnCompleteListener(new OnCompleteListener<Void>() {
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
