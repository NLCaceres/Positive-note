package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
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
    private ArrayList<String> welcomeMessages;
    private TextView receivedPositiveNoteTV;
    private ArrayList<String> receivedPositiveNotes;

    private Button startChatButton;

    private Toolbar homeToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String userName;

    public static final int RC_SIGN_IN = 0;
    public static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        homeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(homeToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(); // No need to specify Firebase URL anymore, google-services.json handles it.

        welcomeTextView = (TextView) findViewById(R.id.welcomeMessageTextView);
        welcomeMessages = new ArrayList<>();
        receivedPositiveNoteTV = (TextView) findViewById(R.id.receivedPositiveNotes);
        receivedPositiveNotes = new ArrayList<>();
        rootRef.child("receivedNotes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                receivedPositiveNoteTV.setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError firebaseError) {
                Log.d("CANCEL TAG", "The data exchange was cancelled somehow");
            }
        });
        rootRef.child("welcomeMessage").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                welcomeTextView.setText(dataSnapshot.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError firebaseError) {
                Log.d("CANCEL TAG", "The data exchange was cancelled somehow");
            }
        });

        startChatButton = (Button) findViewById(R.id.startChatButton);
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

            //startActivity(new Intent(this, UserProfileWithSettings.class));

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
            userName = mFirebaseUser.getDisplayName();
            // if (mFirebaseUser.getPhotoUrl() != null) {
            // mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            // }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==  R.id.action_profile) {
            Intent i = new Intent(getApplicationContext(), UserProfileWithSettings.class);
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
                userName = mFirebaseUser.getDisplayName();
                final String userEmail = mFirebaseUser.getEmail();
                final String userDocName = userEmail.substring(0, userEmail.indexOf("@"));

                FirebaseFirestore dbRef = FirebaseFirestore.getInstance();
                final DocumentReference userDocRef = dbRef.collection("users").document(userDocName);
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("Checking UserDoc", "User Doc Found. Don't update");
                            } else {
                                Map<String, Object> newUser = new HashMap<>();
                                newUser.put("fullName", userName);
                                newUser.put("isVerified", mFirebaseUser.isEmailVerified());
                                newUser.put("email", userEmail);
                                newUser.put("accountCreationDate", new Timestamp(new Date()));
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
                                                Log.d("User DB Upload Failed: ","Database upload of new user data didn't work!");
                                                Log.w("User DB Upload Failed: ", "Failed with error: ", e);
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
}
