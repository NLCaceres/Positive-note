package itp341.caceres.nicholas.positive_note.app;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserProfile;

public class CreateAccountActivity extends AppCompatActivity {

  private Toolbar createAccountToolbar;

  private EditText userNameET;
  private String newUserNameEmail;
  private String newUserName;
  private EditText passwordET;
  private String newPassword;
  private EditText newUserBioET;
  private String newUserBio;

  private TextView newHORHTV;
  private RadioButton helperRB;
  private RadioButton helpedRB;
  private boolean isHelper;
  private newRadioButtonListener newRBListener;

  private CheckBox blueCB;
  private boolean isBlueCheck;
  private CheckBox relationshipCB;
  private boolean isRelationshipCheck;
  private CheckBox looksCB;
  private boolean isLooksCheck;
  private CheckBox emotionalCB;
  private boolean isEmotionalCheck;
  private CheckBox selfCB;
  private boolean isSelfCheck;

  private Button finalizeNewAccountButton;

  private FirebaseAuth mAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_account);

    mAuth = FirebaseAuth.getInstance();

    createAccountToolbar = (Toolbar) findViewById(R.id.home_toolbar);
    setSupportActionBar(createAccountToolbar);
    getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

    userNameET = (EditText) findViewById(R.id.newUserNameET);
    userNameET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        newUserNameEmail = userNameET.getText().toString();
        newUserName = "";
        for (char c : newUserNameEmail.toCharArray()) {
          if (c == '@') {
            break;
          }
          newUserName = newUserName + c;
        }
        getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putString(ConstantsKt.PREFERENCE_USERNAME, newUserName).apply();
        return true;
      }
    });
    passwordET = (EditText) findViewById(R.id.newPasswordET);
    passwordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        newPassword = passwordET.getText().toString();
        return true;
      }
    });
    newUserBioET = (EditText) findViewById(R.id.newUserBioET);
    newUserBioET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        newUserBio = newUserBioET.getText().toString();
        getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putString(ConstantsKt.PREFERENCE_USER_BIO, newUserBio).apply();
        return true;
      }
    });

    newHORHTV = (TextView) findViewById(R.id.newHelpOrHelpedTextView);
    newRBListener = new newRadioButtonListener();
    helperRB = (RadioButton) findViewById(R.id.newGiveHelpRadioButton);
    helpedRB = (RadioButton) findViewById(R.id.newReceiveHelpRadioButton);
    helperRB.setOnClickListener(newRBListener);
    helpedRB.setOnClickListener(newRBListener);

    blueCB = (CheckBox) findViewById(R.id.newFeelingBlueCheck);
    relationshipCB = (CheckBox) findViewById(R.id.newRelationshipCheck);
    looksCB = (CheckBox) findViewById(R.id.newLooksCheck);
    emotionalCB = (CheckBox) findViewById(R.id.newEmotionalCheck);
    selfCB = (CheckBox) findViewById(R.id.newSelfCheck);

    finalizeNewAccountButton = (Button) findViewById(R.id.finalizeNewAccountButton);
    finalizeNewAccountButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        UserProfile newUser;
        if (newUserBio == null || newUserBio == "") {
          newUser = new UserProfile(newUserName, isHelper, isBlueCheck, isRelationshipCheck, isLooksCheck, isEmotionalCheck, isSelfCheck);
        } else {
          newUser = new UserProfile(newUserName, newUserBio, isHelper, isBlueCheck, isRelationshipCheck, isLooksCheck, isEmotionalCheck, isSelfCheck);
        }
        // Firebase newUserRef = new Firebase("https://positive-note.firebaseio.com/users/");
        DatabaseReference newUserRef = FirebaseDatabase.getInstance().getReference().child("users");
        newUserRef.child(newUserName).setValue(newUser);

        Log.i("TAG", newUserNameEmail);
        Log.i("TAG", newPassword);

        mAuth.createUserWithEmailAndPassword(newUserNameEmail, newPassword)
            .addOnCompleteListener(getParent(), new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  Log.d("Google Create Account", "Successfully created user account with uid: " + mAuth.getCurrentUser().getUid());
                } else {
                  Log.e("Google Create Account", "Error creating a new user");
                }
              }
            });
        setResult(RESULT_OK);
        finish();
      }
    });
  }

  public void onCheckBoxClicked(View view) {
    boolean checked = ((CheckBox) view).isChecked();

    switch (view.getId()) {
      case R.id.newFeelingBlueCheck: {
        getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_BLUE, checked).apply();
        isBlueCheck = checked;
      }
      case R.id.newRelationshipCheck: {
        getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_RELATIONSHIP, checked).apply();
        isRelationshipCheck = checked;
      }
      case R.id.newLooksCheck: {
        getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_LOOK, checked).apply();
        isLooksCheck = checked;
      }
      case R.id.newEmotionalCheck: {
        getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_EMOTIONAL, checked).apply();
        isEmotionalCheck = checked;
      }
      case R.id.newSelfCheck: {
        getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_SELF, checked).apply();
        isSelfCheck = checked;
      }
    }
  }

  public class newRadioButtonListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      boolean checked = ((RadioButton) view).isChecked();

      switch (view.getId()) {
        case R.id.giveHelpRadioButton: {
          if (checked) {
            isHelper = true;
            newHORHTV.setText(getResources().getString(R.string.yes_help_text));
            getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_HELPER, isHelper).apply();
          }
          break;
        }
        case R.id.receiveHelpRadioButton: {
          if (checked) {
            isHelper = false;
            newHORHTV.setText(getResources().getString(R.string.no_help_text));
            getSharedPreferences(ConstantsKt.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(ConstantsKt.PREFERENCE_IS_HELPER, isHelper).apply();
          }
          break;
        }
      }
    }
  }
}
