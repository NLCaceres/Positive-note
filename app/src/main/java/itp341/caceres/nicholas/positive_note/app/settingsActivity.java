package itp341.caceres.nicholas.positive_note.app;

import android.app.Activity;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class settingsActivity extends AppCompatActivity {

    private EditText userNameET;
    private String UserName;
    private EditText passwordET;
    private String UserPassword;
    private EditText userBioET;
    private String userBio;

    private TextView HorHTV;

    private RadioButton helperRB;
    private RadioButton helpedRB;
    private boolean isHelper;
    private settingsRBListener helpRBListener;

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

    private Button updateNewAccount;

    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();

        userNameET = (EditText) findViewById(R.id.updateUserNameET);
        UserName = prefs.getString(createAccount.PREFERENCE_USERNAME, "");
        userNameET.setText(UserName);
        userNameET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                UserName = userNameET.getText().toString();
                SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(createAccount.PREFERENCE_USERNAME, UserName);
                prefEditor.apply();
                Map<String, Object> userNameMap = new HashMap<String, Object>();
                userNameMap.put("userName", UserName);
                //Firebase userRef = new Firebase("https://positive-note.firebaseio.com/users/");
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users");
                FirebaseFirestore dbRef = FirebaseFirestore.getInstance();
                rootRef.child(UserName).updateChildren(userNameMap);
                return true;
            }
        });
        passwordET = (EditText) findViewById(R.id.updatePasswordET);
        passwordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                UserPassword = passwordET.getText().toString();
                return true;
            }
        });
        userBioET = (EditText) findViewById(R.id.userBioEditText);
        userBioET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                userBio = userBioET.getText().toString();
                SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(createAccount.PREFERENCE_USERBIO, userBio);
                prefEditor.apply();
                Map<String, Object> userBioMap = new HashMap<String, Object>();
                userBioMap.put("userBio", userBio);
                //Firebase userRef = new Firebase("https://positive-note.firebaseio.com/users/");
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users");
                rootRef.child(UserName).updateChildren(userBioMap);
                return true;
            }
        });

        HorHTV = (TextView) findViewById(R.id.helpOrHelpedTextView);

        helpRBListener = new settingsRBListener();
        helperRB = (RadioButton) findViewById(R.id.helperRadioButton);
        helpedRB = (RadioButton) findViewById(R.id.helpedRadioButton);
        isHelper = prefs.getBoolean(createAccount.PREFERENCE_IS_HELPER, false);
        if (isHelper) {
            helperRB.setChecked(true);
            HorHTV.setText(getResources().getString(R.string.yes_help_text));
        }
        else {
            helpedRB.setChecked(true);
            HorHTV.setText(getResources().getString(R.string.no_help_text));
        }
        helperRB.setOnClickListener(helpRBListener);
        helpedRB.setOnClickListener(helpRBListener);

        blueCB = (CheckBox) findViewById(R.id.feelingBlueCheck);
        isBlueCheck = prefs.getBoolean(createAccount.PREFERENCE_IS_BLUE, false);
        blueCB.setChecked(isBlueCheck);
        relationshipCB = (CheckBox) findViewById(R.id.relationshipCheck);
        isRelationshipCheck = prefs.getBoolean(createAccount.PREFERENCE_IS_RELATIONSHIP, false);
        relationshipCB.setChecked(isRelationshipCheck);
        looksCB = (CheckBox) findViewById(R.id.looksCheck);
        isLooksCheck = prefs.getBoolean(createAccount.PREFERENCE_IS_LOOK, false);
        looksCB.setChecked(isLooksCheck);
        emotionalCB = (CheckBox) findViewById(R.id.emotionalCheck);
        isEmotionalCheck = prefs.getBoolean(createAccount.PREFERENCE_IS_EMOTIONAL, false);
        emotionalCB.setChecked(isEmotionalCheck);
        selfCB = (CheckBox) findViewById(R.id.selfCheck);
        isSelfCheck = prefs.getBoolean(createAccount.PREFERENCE_IS_SELF, false);
        selfCB.setChecked(isSelfCheck);


        updateNewAccount = (Button) findViewById(R.id.updateButton);
        updateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    public class settingsRBListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton) view).isChecked();

            SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = prefs.edit();

            switch (view.getId()) {
                case R.id.helperRadioButton: {
                    if (checked) {
                        isHelper = true;
                        HorHTV.setText(getResources().getString(R.string.yes_help_text));
                        prefEditor.putBoolean(createAccount.PREFERENCE_IS_HELPER, isHelper);
                        prefEditor.apply();
                        Map<String, Object> isHelperMap = new HashMap<String, Object>();
                        isHelperMap.put("isHelper", isHelper);
                        //Firebase userRef = new Firebase("https://positive-note.firebaseio.com/users/");
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users");
                        rootRef.child(UserName).updateChildren(isHelperMap);
                    }
                    break;
                }
                case R.id.helpedRadioButton: {
                    if (checked) {
                        isHelper = false;
                        HorHTV.setText(getResources().getString(R.string.no_help_text));
                        prefEditor.putBoolean(createAccount.PREFERENCE_IS_HELPER, isHelper);
                        prefEditor.apply();
                        Map<String, Object> isHelperMap = new HashMap<String, Object>();
                        isHelperMap.put("isHelper", isHelper);
                        //Firebase userRef = new Firebase("https://positive-note.firebaseio.com/users/");
                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users");
                        rootRef.child(UserName).updateChildren(isHelperMap);
                    }
                    break;
                }
            }
        }
    }

    public void onCheckBoxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = prefs.edit();
        //Firebase userRef = new Firebase("https://positive-note.firebaseio.com/users/");
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users");

        switch (view.getId()) {
            case R.id.feelingBlueCheck: {
                if (checked) {
                    isBlueCheck = true;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_BLUE, isBlueCheck);
                    prefEditor.apply();
                    Map<String, Object> isBlueMap = new HashMap<String, Object>();
                    isBlueMap.put("isBlue", isBlueCheck);
                    rootRef.child(UserName).updateChildren(isBlueMap);

                }
                else {
                    isBlueCheck = false;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_BLUE, isBlueCheck);
                    prefEditor.apply();
                    Map<String, Object> isBlueMap = new HashMap<String, Object>();
                    isBlueMap.put("isBlue", isBlueCheck);
                    rootRef.child(UserName).updateChildren(isBlueMap);
                }
            }
            case R.id.relationshipCheck: {
                if (checked) {
                    isRelationshipCheck = true;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_RELATIONSHIP, isRelationshipCheck);
                    prefEditor.apply();
                    Map<String, Object> isRelationshipMap = new HashMap<String, Object>();
                    isRelationshipMap.put("isRelationship", isRelationshipCheck);
                    rootRef.child(UserName).updateChildren(isRelationshipMap);
                }
                else {
                    isRelationshipCheck = false;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_RELATIONSHIP, isRelationshipCheck);
                    prefEditor.apply();
                    Map<String, Object> isRelationshipMap = new HashMap<String, Object>();
                    isRelationshipMap.put("isRelationship", isRelationshipCheck);
                    rootRef.child(UserName).updateChildren(isRelationshipMap);
                }

            }
            case R.id.looksCheck: {
                if (checked) {
                    isLooksCheck = true;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_LOOK, isLooksCheck);
                    prefEditor.apply();
                    Map<String, Object> isLooksMap = new HashMap<String, Object>();
                    isLooksMap.put("isLooks", isLooksCheck);
                    rootRef.child(UserName).updateChildren(isLooksMap);
                }
                else {
                    isLooksCheck = false;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_LOOK, isLooksCheck);
                    prefEditor.apply();
                    Map<String, Object> isLooksMap = new HashMap<String, Object>();
                    isLooksMap.put("isLooks", isLooksCheck);
                    rootRef.child(UserName).updateChildren(isLooksMap);
                }

            }
            case R.id.emotionalCheck: {
                if (checked) {
                    isEmotionalCheck = true;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_EMOTIONAL, isEmotionalCheck);
                    prefEditor.apply();
                    Map<String, Object> isEmotionalMap = new HashMap<String, Object>();
                    isEmotionalMap.put("isEmotional", isEmotionalCheck);
                    rootRef.child(UserName).updateChildren(isEmotionalMap);
                }
                else {
                    isEmotionalCheck = false;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_EMOTIONAL, isEmotionalCheck);
                    prefEditor.apply();
                    Map<String, Object> isEmotionalMap = new HashMap<String, Object>();
                    isEmotionalMap.put("isEmotional", isEmotionalCheck);
                    rootRef.child(UserName).updateChildren(isEmotionalMap);
                }

            }
            case R.id.selfCheck: {
                if (checked) {
                    isSelfCheck = true;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_SELF, isSelfCheck);
                    prefEditor.apply();
                    Map<String, Object> isSelfMap = new HashMap<String, Object>();
                    isSelfMap.put("isSelf", isSelfCheck);
                    rootRef.child(UserName).updateChildren(isSelfMap);
                }
                else {
                    isSelfCheck = false;
                    prefEditor.putBoolean(createAccount.PREFERENCE_IS_SELF, isSelfCheck);
                    prefEditor.apply();
                    Map<String, Object> isSelfMap = new HashMap<String, Object>();
                    isSelfMap.put("isSelf", isSelfCheck);
                    rootRef.child(UserName).updateChildren(isSelfMap);
                }

            }
        }
    }
}
