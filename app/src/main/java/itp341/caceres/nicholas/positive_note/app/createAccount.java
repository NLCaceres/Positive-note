package itp341.caceres.nicholas.positive_note.app;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

import itp341.caceres.nicholas.positive_note.app.Model.UserProfile;

public class createAccount extends AppCompatActivity {

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

    private Button createNewAccount;
    private long childrenCount;

    private Toolbar createAccountToolbar;

    private FirebaseAuth mAuth;

    public static final String PREFERENCE_FILE = "itp341.caceres.nicholas.positivenote.preferences";
    public static final String PREFERENCE_USER_DISPLAY_NAME = PREFERENCE_FILE + ".display_name";
    public static final String PREFERENCE_USER_EMAIL = PREFERENCE_FILE + ".user_email";
    public static final String PREFERENCE_USERNAME = PREFERENCE_FILE + ".username";
    public static final String PREFERENCE_USERBIO = PREFERENCE_FILE + ".userbio";
    public static final String PREFERENCE_IS_PRIVATE = PREFERENCE_FILE + ".private";
    public static final String PREFERENCE_IS_HELPER = PREFERENCE_FILE + ".helper";
    public static final String PREFERENCE_IS_BLUE = PREFERENCE_FILE + ".blue";
    public static final String PREFERENCE_IS_LOOK = PREFERENCE_FILE + ".look";
    public static final String PREFERENCE_IS_EMOTIONAL = PREFERENCE_FILE + ".emotional";
    public static final String PREFERENCE_IS_RELATIONSHIP = PREFERENCE_FILE + ".relationship";
    public static final String PREFERENCE_IS_SELF = PREFERENCE_FILE + ".self";
    public static final String PREFERENCE_LONGITUDE =  PREFERENCE_FILE + ".longitude";
    public static final String PREFERENCE_LATITUDE = PREFERENCE_FILE + ".latitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        Log.i("Im working!", "Seriously I just ran");

        createAccountToolbar = (Toolbar) findViewById(R.id.create_account_toolbar);
        setSupportActionBar(createAccountToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        SharedPreferences prefs = getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();

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
                    SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = prefs.edit();
                    prefEditor.putString(PREFERENCE_USERNAME, newUserName);
                    prefEditor.commit();
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
        newUserBioET = (EditText) findViewById(R.id.newUserBioEditText);
        newUserBioET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    newUserBio = newUserBioET.getText().toString();
                    SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
                    SharedPreferences.Editor prefEditor = prefs.edit();
                    prefEditor.putString(PREFERENCE_USERBIO, newUserBio);
                    prefEditor.commit();
                    return true;
                }
            });

        newHORHTV = (TextView) findViewById(R.id.newHelpOrHelpedTextView);
        newRBListener = new newRadioButtonListener();
        helperRB = (RadioButton) findViewById(R.id.newHelperRadioButton);
        helpedRB = (RadioButton) findViewById(R.id.newHelpedRadioButton);
        helperRB.setOnClickListener(newRBListener);
        helpedRB.setOnClickListener(newRBListener);

        blueCB = (CheckBox) findViewById(R.id.newFeelingBlueCheck);
        relationshipCB = (CheckBox) findViewById(R.id.newRelationshipCheck);
        looksCB = (CheckBox) findViewById(R.id.newLooksCheck);
        emotionalCB = (CheckBox) findViewById(R.id.newEmotionalCheck);
        selfCB = (CheckBox) findViewById(R.id.newSelfCheck);

        createNewAccount = (Button) findViewById(R.id.createButton);
        createNewAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserProfile newUser;
                    if (newUserBio == null || newUserBio == "") {
                        newUser = new UserProfile(newUserName, isHelper, isBlueCheck, isRelationshipCheck, isLooksCheck, isEmotionalCheck, isSelfCheck);
                    }
                    else {
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
                                        Log.d(ProfileFragment.GOOGLE_TAG, "Successfully created user account with uid: " + mAuth.getCurrentUser().getUid());
                                    } else {
                                        Log.e(ProfileFragment.GOOGLE_TAG, "Error creating a new user");
                                    }
                                }
                            });
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }

    public class newRadioButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton) view).isChecked();

            SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = prefs.edit();

            switch (view.getId()) {
                case R.id.newHelperRadioButton: {
                    if (checked) {
                        isHelper = true;
                        newHORHTV.setText(getResources().getString(R.string.yes_help_text));
                        prefEditor.putBoolean(createAccount.PREFERENCE_IS_HELPER, isHelper);
                        prefEditor.commit();
                    }
                    break;
                }
                case R.id.newHelpedRadioButton: {
                    if (checked) {
                        isHelper = false;
                        newHORHTV.setText(getResources().getString(R.string.no_help_text));
                        prefEditor.putBoolean(createAccount.PREFERENCE_IS_HELPER, isHelper);
                        prefEditor.commit();
                    }
                    break;
                }
            }
        }
    }

        public void onCheckBoxClicked(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            SharedPreferences prefs = getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = prefs.edit();

            switch (view.getId()) {
                case R.id.newFeelingBlueCheck: {
                    if (checked) {
                        isBlueCheck = true;
                        prefEditor.putBoolean(PREFERENCE_IS_BLUE, isBlueCheck);
                        prefEditor.commit();

                    }
                    else {
                        isBlueCheck = false;
                        prefEditor.putBoolean(PREFERENCE_IS_BLUE, isBlueCheck);
                        prefEditor.commit();
                    }
                }
                case R.id.newRelationshipCheck: {
                    if (checked) {
                        isRelationshipCheck = true;
                        prefEditor.putBoolean(PREFERENCE_IS_RELATIONSHIP, isRelationshipCheck);
                        prefEditor.commit();
                    }
                    else {
                        isRelationshipCheck = false;
                        prefEditor.putBoolean(PREFERENCE_IS_RELATIONSHIP, isRelationshipCheck);
                        prefEditor.commit();
                    }

                }
                case R.id.newLooksCheck: {
                    if (checked) {
                        isLooksCheck = true;
                        prefEditor.putBoolean(PREFERENCE_IS_LOOK, isLooksCheck);
                        prefEditor.commit();
                    }
                    else {
                        isLooksCheck = false;
                        prefEditor.putBoolean(PREFERENCE_IS_LOOK, isLooksCheck);
                        prefEditor.commit();
                    }

                }
                case R.id.newEmotionalCheck: {
                    if (checked) {
                        isEmotionalCheck = true;
                        prefEditor.putBoolean(PREFERENCE_IS_EMOTIONAL, isEmotionalCheck);
                        prefEditor.commit();
                    }
                    else {
                        isEmotionalCheck = false;
                        prefEditor.putBoolean(PREFERENCE_IS_EMOTIONAL, isEmotionalCheck);
                        prefEditor.commit();
                    }

                }
                case R.id.newSelfCheck: {
                    if (checked) {
                        isSelfCheck = true;
                        prefEditor.putBoolean(PREFERENCE_IS_SELF, isSelfCheck);
                        prefEditor.commit();
                    }
                    else {
                        isSelfCheck = false;
                        prefEditor.putBoolean(PREFERENCE_IS_SELF, isSelfCheck);
                        prefEditor.commit();
                    }

                }
            }
        }
}
