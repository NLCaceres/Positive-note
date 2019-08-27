package itp341.caceres.nicholas.positive_note.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by NLCaceres on 5/9/2016.
 */
public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;

    private EditText userNameET;
    private String UsersNameEmail;
    private String UsersName;
    private EditText passwordET;
    private String UserPass;


    private Button loginButton;
    private Button createNewButton;
    private Button forgotPassButton;
    private userLoginButtonListener buttonListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        userNameET = (EditText) v.findViewById(R.id.userNameEditText);
        userNameET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    UsersNameEmail = userNameET.getText().toString();
                    Log.d("username", "username changed");
                }
            }
        });
        passwordET = (EditText) v.findViewById(R.id.passwordEditText);
        passwordET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    UserPass = passwordET.getText().toString();
                    Log.d("pass", "pass changed");
                }
            }
        });

        loginButton = (Button) v.findViewById(R.id.loginButton);
        createNewButton = (Button) v.findViewById(R.id.createNewButton);
        forgotPassButton = (Button) v.findViewById(R.id.forgotPassButton);
        buttonListener = new userLoginButtonListener();
        loginButton.setOnClickListener(buttonListener);
        createNewButton.setOnClickListener(buttonListener);
        forgotPassButton.setOnClickListener(buttonListener);

        return v;
    }

    private class userLoginButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            switch (v.getId()) {
                case R.id.loginButton: {
                    Log.d("TAG", UsersNameEmail);
                    Log.d("TAG", UserPass);
                    mAuth.signInWithEmailAndPassword(UsersNameEmail, UserPass)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("Login Task", "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Log.d("User Information", "Unique ID: " + user.getUid() + ", Provider ID: " + user.getProviderId());
                                        SharedPreferences prefs = getActivity().getSharedPreferences(createAccount.PREFERENCE_FILE, Context.MODE_PRIVATE);
                                        SharedPreferences.Editor prefEditor = prefs.edit();
                                        UsersName = user.getEmail();
                                        prefEditor.putString(createAccount.PREFERENCE_USERNAME, UsersName);
                                        prefEditor.commit();
                                        FragmentManager fm = getFragmentManager();
                                        Fragment fragContainer = fm.findFragmentById(R.id.fragment_container);
                                        if (fragContainer == null) {
                                            fragContainer = new ProfileFragment();
                                            FragmentTransaction ft = fm.beginTransaction();
                                            ft.add(R.id.fragment_container, fragContainer);
                                            ft.commit();
                                        }
                                        else {
                                            fragContainer = new ProfileFragment();
                                            fm.beginTransaction().replace(R.id.fragment_container,fragContainer).commit();
                                        }

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("Login Task", "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(getContext(), getResources().getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                                        /*switch (firebaseError.getCode()) {
                                            case FirebaseError.INVALID_EMAIL: {
                                                Log.d("LOGIN", "Invalid email");
                                            }
                                            case FirebaseError.INVALID_PASSWORD: {
                                                Log.d("LOGIN", "Invalid password");
                                            }
                                            case FirebaseError.NETWORK_ERROR: {
                                                Log.d("LOGIN", "Network issue");
                                            }
                                            case FirebaseError.PROVIDER_ERROR: {
                                                Log.d("LOGIN", "Provider issue");
                                            }
                                            case FirebaseError.USER_DOES_NOT_EXIST: {
                                                Log.d("LOGIN", "Non-existent user");
                                            }
                                            case FirebaseError.UNKNOWN_ERROR: {
                                                Log.d("LOGIN", "Unknown issue");
                                            }
                                        }*/
                                    }
                                }
                            });
                    break;
                }
                case R.id.createNewButton: {
                    Intent i = new Intent(getContext(), createAccount.class);
                    startActivityForResult(i, 0);
                    break;
                }
                case R.id.forgotPassButton: {
                    mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Password Reset Task", "Forgotten password email reset");
                                    } else {
                                        Log.d("Password Reset Task", "Password reset failure sending email");
                                        // The below assumes there was no email in the EditText so check for that before showing this toast
                                        Toast.makeText(getContext(), getResources().getString(R.string.no_username), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    break;

                }
            }
        }

    }
}
