package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;

public class UserSettingsActivity extends AppCompatActivity {

  private Toolbar settingsToolbar;

  private UserSettingsETFocusListener focusListener;
  private UpdateButtonsListener updateListener;

  private EditText updateUserEmailET;
  private Button updateUserEmailButton;
  private String userEmail;
  private EditText updateUserNameET;
  private Button updateUserNameButton;
  private String userName;
  private EditText updatePasswordET;
  private Button updatePasswordButton;
  private EditText updateUserBioET;
  private Button updateUserBioButton;
  private String userBio;

  private TextView helpOrHelpedTV;
  private RadioButton giveHelpRB;
  private RadioButton receiveHelpRB;
  //private boolean isHelper;
  private CheckBox blueCB;
  private CheckBox relationshipCB;
  private CheckBox looksCB;
  private CheckBox emotionalCB;
  private CheckBox selfCB;

  private FirebaseAuth mFirebaseAuth;
  private FirebaseUser mUser;
  private DocumentReference mUserDocRef;
  private UserInfo mUserInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_settings);

    // In the event of a configuration change or other system related activity death
//    if (savedInstanceState != null) {
//        userEmail = savedInstanceState.getString(USER_EMAIL);
//        userName = savedInstanceState.getString(USER_NAME);
//        userPassword = savedInstanceState.getString(USER_PASSWORD);
//        userBio = savedInstanceState.getString(USER_BIO);
//    } // Worth considering an else statement or early return because the above will get overwritten

    // Toolbar setup
    settingsToolbar = (Toolbar) findViewById(R.id.home_toolbar);
    setSupportActionBar(settingsToolbar);

    mFirebaseAuth = FirebaseAuth.getInstance();
    mUserInfo = getIntent().getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO);
    mUser = mFirebaseAuth.getCurrentUser();

    if (mUser != null) {
      userEmail = mUser.getEmail();
      userName = userEmail.substring(0, userEmail.indexOf("@"));
      mUserDocRef = FirebaseFirestore.getInstance().collection("users").document(userEmail);
    }

    setUpUpdateViews(); // Update Email, Pass, and username related views

    setUpHelpRadioGroup(); // Helper Textview, RadioGroup and init from prefs

    setUpCheckBoxes(); // CheckBox Group plus init from prefs
  }

  private void setUpUpdateViews() {
    focusListener = new UserSettingsETFocusListener();
    updateListener = new UpdateButtonsListener();

    updateUserEmailET = findViewById(R.id.updateUserEmailET);
    updateUserEmailButton = findViewById(R.id.updateUserEmailButton);

    updateUserNameET = (EditText) findViewById(R.id.updateUserNameET);
    updateUserNameButton = findViewById(R.id.updateUserNameButton);

    updatePasswordET = (EditText) findViewById(R.id.updatePasswordET);
    updatePasswordButton = findViewById(R.id.updatePasswordButton);

    updateUserBioET = (EditText) findViewById(R.id.updateUserBioET);
    updateUserBioButton = findViewById(R.id.updateUserBioButton);

    updateUserEmailET.setOnFocusChangeListener(focusListener);
    updateUserEmailButton.setOnClickListener(updateListener);

    updateUserNameET.setOnFocusChangeListener(focusListener);
    updateUserNameButton.setOnClickListener(updateListener);

    updatePasswordET.setOnFocusChangeListener(focusListener);
    updatePasswordButton.setOnClickListener(updateListener);

    updateUserBioET.setOnFocusChangeListener(focusListener);
    updateUserBioButton.setOnClickListener(updateListener);

    if (mUserInfo != null) {
      String userEmail;
      if (mUserInfo.getEmail().length() > 0) { userEmail = mUserInfo.getEmail(); }
      else { userEmail = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).getString(ConstantsKt.PREFERENCE_USER_EMAIL, ""); }
      updateUserEmailET.setText(userEmail);

      String userName;
      if (mUserInfo.getUserName().length() > 0) { userName = mUserInfo.getUserName(); }
      else { userName = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).getString(ConstantsKt.PREFERENCE_USERNAME, ""); }
      updateUserEmailET.setText(userName);

      String userBio;
      if (mUserInfo.getBiography().length() > 0) { userBio = mUserInfo.getBiography(); }
      else { userBio = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).getString(ConstantsKt.PREFERENCE_USER_BIO, ""); }
      updateUserEmailET.setText(userBio);
    }
  }

  private void setUpHelpRadioGroup() { // RadioGroup is connected via xml to onRadioButtonClicked func
    helpOrHelpedTV = (TextView) findViewById(R.id.helpOrHelpedTextView);
    giveHelpRB = (RadioButton) findViewById(R.id.giveHelpRadioButton);
    receiveHelpRB = (RadioButton) findViewById(R.id.receiveHelpRadioButton);

    boolean isHelper;
    if (mUserInfo == null) {
      isHelper = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).getBoolean(ConstantsKt.PREFERENCE_IS_HELPER, false);
    } else { isHelper = mUserInfo.getHelper(); }

    if (isHelper) {
      giveHelpRB.setChecked(true);
      helpOrHelpedTV.setText(getResources().getString(R.string.yes_help_text));
    } else {
      receiveHelpRB.setChecked(true);
      helpOrHelpedTV.setText(getResources().getString(R.string.no_help_text));
    }
  }

  public void onRadioButtonClicked(View view) { // View param - Checked RadioButton - Always true
    boolean isHelper;

    if (view.getId() == R.id.giveHelpRadioButton) {
      isHelper = true;
      if (mUserInfo != null) { mUserInfo.setHelper(true); }
      helpOrHelpedTV.setText(getResources().getString(R.string.yes_help_text));
    } else {
      isHelper = false;
      if (mUserInfo != null) { mUserInfo.setHelper(false); }
      helpOrHelpedTV.setText(getResources().getString(R.string.no_help_text));
    }

    // Easy one-liner! getPreferences, set editMode, putting in a boolean, apply changes!
    getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_HELPER, isHelper).apply();
    mUserDocRef.update("helper", isHelper).addOnCompleteListener(task -> {
      if (task.isSuccessful()) { Log.d("IsHelper Switch", "Changing helper mode successful"); }
    });
  }

  private void setUpCheckBoxes() {
    SharedPreferences prefs = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE);

    blueCB = (CheckBox) findViewById(R.id.feelingBlueCheck);
    relationshipCB = (CheckBox) findViewById(R.id.relationshipCheck);
    looksCB = (CheckBox) findViewById(R.id.looksCheck);
    emotionalCB = (CheckBox) findViewById(R.id.emotionalCheck);
    selfCB = (CheckBox) findViewById(R.id.selfCheck);

    if (mUserInfo != null && mUserInfo.getCurrentFeelings() != null) {
      blueCB.setChecked(mUserInfo.getCurrentFeelings().getBlue());
      relationshipCB.setChecked(mUserInfo.getCurrentFeelings().getRelationship());
      looksCB.setChecked(mUserInfo.getCurrentFeelings().getLooks());
      emotionalCB.setChecked(mUserInfo.getCurrentFeelings().getEmotions());
      selfCB.setChecked(mUserInfo.getCurrentFeelings().getSelf());
    } else {
      blueCB.setChecked(prefs.getBoolean(ConstantsKt.PREFERENCE_IS_BLUE, false));
      relationshipCB.setChecked(prefs.getBoolean(ConstantsKt.PREFERENCE_IS_RELATIONSHIP, false));
      looksCB.setChecked(prefs.getBoolean(ConstantsKt.PREFERENCE_IS_LOOK, false));
      emotionalCB.setChecked(prefs.getBoolean(ConstantsKt.PREFERENCE_IS_EMOTIONAL, false));
      selfCB.setChecked(prefs.getBoolean(ConstantsKt.PREFERENCE_IS_SELF, false));
    }
  }

  public void onCheckBoxClicked(View view) {
    boolean checked = ((CheckBox) view).isChecked();

    switch (view.getId()) {
      case R.id.feelingBlueCheck: {
        checkBoxHandler(ConstantsKt.PREFERENCE_IS_BLUE, checked, "currentFeelings.blue");
        break;
      }
      case R.id.relationshipCheck: {
        checkBoxHandler(ConstantsKt.PREFERENCE_IS_RELATIONSHIP, checked, "currentFeelings.relationship");
        break;
      }
      case R.id.looksCheck: {
        checkBoxHandler(ConstantsKt.PREFERENCE_IS_LOOK, checked, "currentFeelings.looks");
        break;
      }
      case R.id.emotionalCheck: {
        checkBoxHandler(ConstantsKt.PREFERENCE_IS_EMOTIONAL, checked, "currentFeelings.emotions");
        break;
      }
      case R.id.selfCheck: {
        checkBoxHandler(ConstantsKt.PREFERENCE_IS_SELF, checked, "currentFeelings.self");
        break;
      }
    }
  }

  private void checkBoxHandler(String prefToChange, boolean checked, final String docFieldToChange) {
    // Done in one liner - Grabs Preferences File, sets up in editMode, changes Preference in ?, and applies the changes
    getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(prefToChange, checked).apply();
    mUserDocRef.update(docFieldToChange, checked).addOnCompleteListener(task -> {
      if (task.isSuccessful()) { Log.d(docFieldToChange, "Successfully Changed"); }
    });
  }

  @Override // TODO: Consider whether to send parcelable or individual fields (may need to send whole parcelable back since it'll affect other views)
  public void onBackPressed() {
    Intent backButtonIntent = new Intent();

    if (mUserInfo != null) { // Current thought process - Send only what's necessary. May need to still send out the entire UserInfo object due to other fields
      if (mUserInfo.getUserName().length() > 0) { backButtonIntent.putExtra(ConstantsKt.INTENT_EXTRAS_USERNAME, mUserInfo.getUserName()); }
      if (mUserInfo.getEmail().length() > 0) { backButtonIntent.putExtra(ConstantsKt.INTENT_EXTRAS_USER_EMAIL, mUserInfo.getEmail()); }
      if (mUserInfo.getBiography().length() > 0) { backButtonIntent.putExtra(ConstantsKt.INTENT_EXTRAS_USER_BIO, mUserInfo.getBiography()); }
    } else { // if for some reason the userInfo that should have been passed isn't there
      SharedPreferences prefs = getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE);
      backButtonIntent.putExtra(ConstantsKt.INTENT_EXTRAS_USERNAME, prefs.getString(ConstantsKt.PREFERENCE_USERNAME, ""));
      backButtonIntent.putExtra(ConstantsKt.INTENT_EXTRAS_USER_EMAIL, prefs.getString(ConstantsKt.PREFERENCE_USERNAME, ""));
      backButtonIntent.putExtra(ConstantsKt.INTENT_EXTRAS_USER_BIO, prefs.getString(ConstantsKt.PREFERENCE_USER_BIO, getResources().getString(R.string.user_bio_default)));
    }

    setResult(RESULT_CANCELED, backButtonIntent);
    super.onBackPressed();
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) { // May not be necessary for the EditText (now auto-save)

    savedInstanceState.putParcelable(ConstantsKt.BUNDLE_USER_INFO, mUserInfo);

    savedInstanceState.putString(ConstantsKt.BUNDLE_USER_NAME, mUserInfo.getUserName());
    savedInstanceState.putString(ConstantsKt.BUNDLE_USER_EMAIL, mUserInfo.getEmail());
    savedInstanceState.putString(ConstantsKt.BUNDLE_USER_BIO, mUserInfo.getBiography());

    super.onSaveInstanceState(savedInstanceState); // ALWAYS call super to ensure viewHierachy saved too
  }

  @Override // Called after onStart and only if savedInstanceState exists
  public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) { // Restore activity state (properties) from saved instance
    super.onRestoreInstanceState(savedInstanceState); // ALWAYS call the superclass so it can restore the view hierarchy

    mUserInfo = savedInstanceState.getParcelable(ConstantsKt.BUNDLE_USER_INFO);
    userEmail = savedInstanceState.getString(ConstantsKt.BUNDLE_USER_EMAIL);
    userName = savedInstanceState.getString(ConstantsKt.BUNDLE_USER_NAME);
    userBio = savedInstanceState.getString(ConstantsKt.BUNDLE_USER_BIO);
  }

  private void hideUpdateButtons() { // Used below to handle visibility change on focus change
    updateUserEmailButton.setVisibility(View.INVISIBLE);
    updateUserNameButton.setVisibility(View.INVISIBLE);
    updatePasswordButton.setVisibility(View.INVISIBLE);
    updateUserBioButton.setVisibility(View.INVISIBLE);
  }

  class UserSettingsETFocusListener implements View.OnFocusChangeListener { // Will not interact with UI if not private class var
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
      if (hasFocus) {
        switch (view.getId()) {
          case R.id.updateUserEmailET: {
            updateUserEmailButton.setVisibility(View.VISIBLE);
            break;
          }
          case R.id.updateUserNameET: {
            updateUserNameButton.setVisibility(View.VISIBLE);
            break;
          }
          case R.id.updatePasswordET: {
            updatePasswordButton.setVisibility(View.VISIBLE);
            break;
          }
          case R.id.updateUserBioET: {
            updateUserBioButton.setVisibility(View.VISIBLE);
            break;
          }
          default: {
            hideUpdateButtons();
            break;
          }
        }
      } else { hideUpdateButtons(); }
    }
  }

  class UpdateButtonsListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      switch (view.getId()) {
        case R.id.updateUserEmailButton: { // TODO: Special consideration here - Firestore doc is based on email - Recreate entire doc?
          String userEmail = updateUserEmailET.getText().toString();
          if (userEmail.length() == 0) { Toast.makeText(getApplicationContext(), "Email can't be blank", Toast.LENGTH_SHORT).show(); }
          else { Toast.makeText(getApplicationContext(), "User Email updated!", Toast.LENGTH_SHORT).show(); }
          //mUserInfo.setEmail(userEmail);
//          mUserDocRef.update("email", userEmail).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) { Log.d("Updated Email", "Email update successful"); }
//          });
          //getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putString(ConstantsKt.PREFERENCE_USER_EMAIL, userEmail).apply();
          break;
        }
        case R.id.updateUserNameButton: { // TODO: Double check what this might affect - Likely not much
          String userName = updateUserNameET.getText().toString();
          if (userName.length() == 0) { Toast.makeText(getApplicationContext(), "Username can't be blank", Toast.LENGTH_SHORT).show(); }
          else { Toast.makeText(getApplicationContext(), "Username updated!", Toast.LENGTH_SHORT).show(); }
              //mUserInfo.setUserName(userName);
//          mUserDocRef.update("userName", userName).addOnCompleteListener(task -> {
//            if (task.isSuccessful()) { Log.d("Updated Username", "Username update successful"); }
//          });
          //getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putString(ConstantsKt.PREFERENCE_USERNAME, userName).apply();
          break;
        }
        case R.id.updatePasswordButton: { // TODO: Password not implemented yet but need to consider how user logged in
          String userPassword = updatePasswordET.getText().toString();
          if (userPassword.length() == 0) { Toast.makeText(getApplicationContext(), "User password can't be blank", Toast.LENGTH_SHORT).show(); }
          else { Toast.makeText(getApplicationContext(), "User password updated!", Toast.LENGTH_SHORT).show(); }

          break;
        }
        case R.id.updateUserBioButton: {
          String userBio = updateUserBioET.getText().toString();
          if (userBio.length() == 0) { Toast.makeText(getApplicationContext(), "User Bio reset!", Toast.LENGTH_SHORT).show(); }
          else { Toast.makeText(getApplicationContext(), "User Bio updated!", Toast.LENGTH_SHORT).show(); }

          mUserInfo.setBiography(userBio);
          mUserDocRef.update("biography", userBio).addOnCompleteListener(task -> {
            if (task.isSuccessful()) { Log.d("Updated Bio", "Userbio update successful"); }
          });
          getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putString(ConstantsKt.PREFERENCE_USER_BIO, updateUserBioET.getText().toString()).apply();
          updateUserBioET.getText().clear();
          break;
        }
        default: {
          break;
        }
      }
    }
  }
}
