package itp341.caceres.nicholas.positive_note.app;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
    private String UserPassword;
    private EditText updateUserBioET;
    private Button updateUserBioButton;
    private String userBio;
    private TextView helpOrHelpedTV;

    private RadioButton giveHelpRB;
    private RadioButton receiveHelpRB;
    private boolean isHelper;
    //private SettingsRBListener helpRBListener;

    private CheckBox blueCB;
    private CheckBox relationshipCB;
    private CheckBox looksCB;
    private CheckBox emotionalCB;
    private CheckBox selfCB;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUser;
    private DocumentReference mUserDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        // Toolbar setup
        settingsToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUser = mFirebaseAuth.getCurrentUser();

        if (mUser != null) {
            userEmail = mUser.getEmail();
            userName = userEmail.substring(0, userEmail.indexOf("@"));
            mUserDocRef = FirebaseFirestore.getInstance().collection("users").document(userEmail);
        }

        setUpUpdateViews(); // Update Email, Pass, and username related views

        setUpHelpViews(); // Helper Textview, RadioGroup and init from prefs

        setUpCheckBoxes(); // CheckBox Group plus init from prefs
    }

    private void setUpUpdateViews() {
        focusListener = new UserSettingsETFocusListener();
        updateListener = new UpdateButtonsListener();

        updateUserEmailET = findViewById(R.id.updateUserEmailET);
        updateUserEmailButton = findViewById(R.id.updateUserEmailButton);
        updateUserEmailET.setText(userEmail);
        updateUserEmailET.setOnFocusChangeListener(focusListener);
        updateUserEmailButton.setOnClickListener(updateListener);

        updateUserNameET = (EditText) findViewById(R.id.updateUserNameET);
        updateUserNameButton = findViewById(R.id.updateUserNameButton);
        updateUserNameET.setText(userName);
        updateUserNameET.setOnFocusChangeListener(focusListener);
        updateUserNameButton.setOnClickListener(updateListener);

        updatePasswordET = (EditText) findViewById(R.id.updatePasswordET);
        updatePasswordButton = findViewById(R.id.updatePasswordButton);
        updatePasswordET.setOnFocusChangeListener(focusListener);
        updatePasswordButton.setOnClickListener(updateListener);

        updateUserBioET = (EditText) findViewById(R.id.updateUserBioET);
        updateUserBioButton = findViewById(R.id.updateUserBioButton);
        updateUserBioET.setOnFocusChangeListener(focusListener);
        updateUserBioButton.setOnClickListener(updateListener);
    }

    private void setUpHelpViews() {
        SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);

        helpOrHelpedTV = (TextView) findViewById(R.id.helpOrHelpedTextView);

        giveHelpRB = (RadioButton) findViewById(R.id.getHelpRadioButton);
        receiveHelpRB = (RadioButton) findViewById(R.id.receiveHelpRadioButton);
        isHelper = prefs.getBoolean(createAccount.PREFERENCE_IS_HELPER, false);
        if (isHelper) {
            giveHelpRB.setChecked(true);
            helpOrHelpedTV.setText(getResources().getString(R.string.yes_help_text));
        }
        else {
            receiveHelpRB.setChecked(true);
            helpOrHelpedTV.setText(getResources().getString(R.string.no_help_text));
        }
    }

    private void setUpCheckBoxes() {
        SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);

        blueCB = (CheckBox) findViewById(R.id.feelingBlueCheck);
        blueCB.setChecked(prefs.getBoolean(createAccount.PREFERENCE_IS_BLUE, false));

        relationshipCB = (CheckBox) findViewById(R.id.relationshipCheck);
        relationshipCB.setChecked(prefs.getBoolean(createAccount.PREFERENCE_IS_RELATIONSHIP, false));

        looksCB = (CheckBox) findViewById(R.id.looksCheck);
        looksCB.setChecked(prefs.getBoolean(createAccount.PREFERENCE_IS_LOOK, false));

        emotionalCB = (CheckBox) findViewById(R.id.emotionalCheck);
        emotionalCB.setChecked(prefs.getBoolean(createAccount.PREFERENCE_IS_EMOTIONAL, false));

        selfCB = (CheckBox) findViewById(R.id.selfCheck);
        selfCB.setChecked(prefs.getBoolean(createAccount.PREFERENCE_IS_SELF, false));
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
            } else {
                hideUpdateButtons();
            }

        }
    }

    private void hideUpdateButtons() {
        updateUserEmailButton.setVisibility(View.INVISIBLE);
        updateUserNameButton.setVisibility(View.INVISIBLE);
        updatePasswordButton.setVisibility(View.INVISIBLE);
        updateUserBioButton.setVisibility(View.INVISIBLE);
    }

    class UpdateButtonsListener implements View.OnClickListener { // TODO: Setup to update DB Data
        @Override
        public void onClick(View view) {
            // use mUserDocRef to make updates
            switch (view.getId()) {
                case R.id.updateUserEmailButton: {
                    Toast.makeText(getApplicationContext(), "User Email updated!", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.updateUserNameButton: {
                    Toast.makeText(getApplicationContext(), "Username updated!", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.updatePasswordButton: {
                    Toast.makeText(getApplicationContext(), "User password updated!", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.updateUserBioButton: {
                    Toast.makeText(getApplicationContext(), "User Bio updated!", Toast.LENGTH_SHORT).show();
                    mUserDocRef.update("UserBio", updateUserBioET.getText()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Update Bio", "Updating User bio was successful");
                            }
                        }
                    });
                    break;
                }
                default: {
                    break;
                }
            }
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        if (view.getId() == R.id.getHelpRadioButton && checked) {
            isHelper = true;
            helpOrHelpedTV.setText(getResources().getString(R.string.yes_help_text));
        } else {
            isHelper = false;
            helpOrHelpedTV.setText(getResources().getString(R.string.no_help_text));
        }

        // Easy one-liner! getPreferences, set editMode, putting in a boolean, apply changes!
        getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(createAccount.PREFERENCE_IS_HELPER, isHelper).apply();

        mUserDocRef.update("isHelper", isHelper).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("IsHelper Switch", "Changing helper mode successful");
                }
            }
        });
    }

    public void onCheckBoxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.feelingBlueCheck: {
                checkBoxHandler(createAccount.PREFERENCE_IS_BLUE, checked, "currentFeelings.isBlue");
                break;
            }
            case R.id.relationshipCheck: {
                checkBoxHandler(createAccount.PREFERENCE_IS_RELATIONSHIP, checked, "currentFeelings.isRelationship");
                break;
            }
            case R.id.looksCheck: {
                checkBoxHandler(createAccount.PREFERENCE_IS_LOOK, checked, "currentFeelings.isLooks");
                break;
            }
            case R.id.emotionalCheck: {
                checkBoxHandler(createAccount.PREFERENCE_IS_EMOTIONAL, checked, "currentFeelings.isEmotional");
                break;
            }
            case R.id.selfCheck: {
                checkBoxHandler(createAccount.PREFERENCE_IS_SELF, checked, "currentFeelings.isSelf");
                break;
            }
        }
    }

    private void checkBoxHandler(String prefToChange, boolean checked, final String docFieldToChange) {
        // Done in one liner - Grabs Preferences File, sets up in editMode, changes Preference in ?, and applies the changes
        getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE).edit().putBoolean(prefToChange, checked).apply();

        mUserDocRef.update(docFieldToChange, checked).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(docFieldToChange, " Successfully changed!");
                }
            }
        });
    }
}
