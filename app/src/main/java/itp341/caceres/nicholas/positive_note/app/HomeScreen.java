package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeScreen extends AppCompatActivity {

    private TextView welcomeTextView;
    private TextView receivedPositiveNoteTV;
    private ArrayList<String> receivedPositiveNotes;

    private Button startChatButton;

    private Toolbar homeToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String userDisplayName;

    public static final int RC_SIGN_IN = 0;
    public static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        // Init Views
        welcomeTextView = (TextView) findViewById(R.id.welcomeMessageTextView);
        receivedPositiveNoteTV = (TextView) findViewById(R.id.receivedPositiveNotes);
        homeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        startChatButton = (Button) findViewById(R.id.startChatButton);

        // Init Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Setup Views
        if (mFirebaseUser != null) {
            if (userDisplayName == null) {
                userDisplayName = mFirebaseUser.getDisplayName();
            } // TODO: Considering alternate/randomized welcome messages, but currently drawing a blank as to what other messages could appear
            welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_init_user_message)).append(" ").append(userDisplayName).append("!"));
        }

        // Toolbar Setup
        setSupportActionBar(homeToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(); // No need to specify Firebase URL anymore, google-services.json handles it.

        // TODO: Set up a way for users to send each other positive notes, then arrange into list (listview? or random single note that appears?)
        receivedPositiveNotes = new ArrayList<>();
//        rootRef.child("receivedNotes").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                receivedPositiveNoteTV.setText(dataSnapshot.getValue(String.class));
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError firebaseError) {
//                Log.d("CANCEL TAG", "The data exchange was cancelled somehow");
//            }
//        });
//        rootRef.child("welcomeMessage").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                welcomeTextView.setText(dataSnapshot.getValue(String.class));
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError firebaseError) {
//                Log.d("CANCEL TAG", "The data exchange was cancelled somehow");
//            }
//        });

        // Button Listener setup
        startChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ChatWithTabs.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) { // Not signed in, launch the Sign In activity

            ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setAndroidPackageName(PACKAGE_NAME, true, null) // Package Name, Install redirect, min version of App
                    .setHandleCodeInApp(true) // Must be true ALWAYS
                    .setUrl("https://positiveNote.page.link").build(); // Whitelist the link here

            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().enableEmailLinkSignIn().setActionCodeSettings(actionCodeSettings).build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setTheme(R.style.AppTheme)
                            .build(),
                    RC_SIGN_IN);

            // With Firebase Dynamic Links (deep link), you could send User to different part of app, but must be configured properly
            /* if (AuthUI.canHandleIntent(getIntent())) {
                if (getIntent().getExtras() == null) {
                    return;
                }
                String link = getIntent().getExtras().getString(ExtraConstants.EMAIL_LINK_SIGN_IN);
                if (link != null) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setEmailLink(link)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                    // Firebase's Login UI above uses AppCompat for themeing (and even accepts your logo)
                    // with appropriately named helper methods (setTheme and setLogo) to grab from R.style/R.drawable
                }
            } */
        } else {
            if (userDisplayName == null) { // This is actually called before onActivityResult! so update welcomeTextView!
                userDisplayName = mFirebaseUser.getDisplayName();
                welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_back_user_message)).append(" ").append(userDisplayName).append("!"));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_back_user_message)).append(" ").append(userDisplayName).append("!"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==  R.id.action_profile) {
            Intent i = new Intent(getApplicationContext(), UserProfileActivity.class);
            startActivity(i);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) { // Successfully signed in
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                userDisplayName = mFirebaseUser.getDisplayName(); // Usually User's actual name
                welcomeTextView.setText(new StringBuilder().append(getResources().getString(R.string.welcome_init_user_message)).append(" ").append(userDisplayName).append("!"));
                final String userEmail = mFirebaseUser.getEmail();
                final String userName = userEmail.substring(0,1).toUpperCase() + userEmail.substring(1, userEmail.indexOf("@")); // Usually user email without the address

                final SharedPreferences.Editor prefEditor = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE).edit();
                prefEditor.putString(createAccount.PREFERENCE_USER_DISPLAY_NAME, userDisplayName);
                prefEditor.putString(createAccount.PREFERENCE_USER_EMAIL, userEmail);
                prefEditor.putString(createAccount.PREFERENCE_USERNAME, userName);
                prefEditor.apply();

                // TODO: Add additional login screen to catch duplicates (JohnDoe@Gmail would match a JohnDoe@Aol.com Username so no good)

                final DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(userEmail);
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("Login UserDoc Check", "User Doc Found. Don't update");
                                prefEditor.putBoolean(createAccount.PREFERENCE_IS_PRIVATE, document.getBoolean("isPrivate"));
                                prefEditor.putBoolean(createAccount.PREFERENCE_IS_HELPER, document.getBoolean("isHelper"));
                                prefEditor.putBoolean(createAccount.PREFERENCE_IS_BLUE, document.getBoolean("currentFeelings.isBlue"));
                                prefEditor.putBoolean(createAccount.PREFERENCE_IS_RELATIONSHIP, document.getBoolean("currentFeelings.isRelationship"));
                                prefEditor.putBoolean(createAccount.PREFERENCE_IS_EMOTIONAL, document.getBoolean("currentFeelings.isEmotional"));
                                prefEditor.putBoolean(createAccount.PREFERENCE_IS_LOOK, document.getBoolean("currentFeelings.isLooks"));
                                prefEditor.putBoolean(createAccount.PREFERENCE_IS_SELF, document.getBoolean("currentFeelings.isSelf"));
                                prefEditor.apply(); //Apply all
                            } else {
                                Map<String, Object> newUser = new HashMap<>();
                                newUser.put("fullName", userDisplayName);
                                newUser.put("email", userEmail);
                                newUser.put("username", userName);
                                newUser.put("isVerified", mFirebaseUser.isEmailVerified());
                                setDBandPrefDefaults(newUser);

                                userDocRef.set(newUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("User DB Upload Success","Database upload of new user successful");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("User DB Upload Failed: ", "DB Upload failed with error: ", e);
                                            }
                                        });
                            }
                        } else {
                            Log.w("Error UserDoc Check", task.getException());
                        }
                    }
                });
            } else { // Sign in failed/cancelled (probably)
                // Handle error if there is one
                Log.w("Sign In Issue: ", "Issue logging, but returned from activity");
            }
        }
    }

    private void setDBandPrefDefaults(Map<String, Object> newUser) {
        newUser.put("isPrivate", true);
        newUser.put("isHelper", false);
        newUser.put("accountCreationDate", new Timestamp(new Date()));

        Map<String, Object> currentFeelings = new HashMap<>(); // Nested Data entry for set of user's feelings / feelings they can help with
        currentFeelings.put("isBlue", false);
        currentFeelings.put("isRelationship", false);
        currentFeelings.put("isEmotional", false);
        currentFeelings.put("isLooks", false);
        currentFeelings.put("isSelf", false);
        newUser.put("currentFeelings", currentFeelings);

        SharedPreferences.Editor prefEditor = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE).edit();
        prefEditor.putBoolean(createAccount.PREFERENCE_IS_PRIVATE, true);
        prefEditor.putBoolean(createAccount.PREFERENCE_IS_HELPER, false);
        prefEditor.putBoolean(createAccount.PREFERENCE_IS_BLUE, false);
        prefEditor.putBoolean(createAccount.PREFERENCE_IS_RELATIONSHIP, false);
        prefEditor.putBoolean(createAccount.PREFERENCE_IS_EMOTIONAL, false);
        prefEditor.putBoolean(createAccount.PREFERENCE_IS_LOOK, false);
        prefEditor.putBoolean(createAccount.PREFERENCE_IS_SELF, false);
        prefEditor.apply();
    }
}
