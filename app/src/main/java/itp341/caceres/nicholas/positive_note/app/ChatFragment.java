package itp341.caceres.nicholas.positive_note.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import itp341.caceres.nicholas.positive_note.app.Model.UserProfile;
import itp341.caceres.nicholas.positive_note.app.Model.UserSingleton;

/**
 * Created by NLCaceres on 4/30/2016.
 */
public class ChatFragment extends Fragment {

    private ListView chatMessagesList;
    private ChatAdapter chatAdapter;

    private UserSingleton userBase;

    private Button findUsers;

    public static final String ARG_CHAT_TAB_NAME = "itp341.caceres.nicholas.finalproject.app.chat_frag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static ChatFragment newInstance(String name) {
        ChatFragment chatFrag = new ChatFragment();
        return chatFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        userBase = UserSingleton.getInstance();

        chatMessagesList = (ListView) v.findViewById(R.id.chatListView);
        chatAdapter = new ChatAdapter(getContext(), userBase.getUsers());
        chatMessagesList.setAdapter(chatAdapter);
        chatMessagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getContext(), InMessageActivity.class);
                TextView personTV = (TextView) view.findViewById(R.id.personUserName);
                String receiverUserName = personTV.getText().toString();
                i.putExtra(InMessageActivity.EXTRA_RECEIVER, receiverUserName);
                startActivity(i);
            }
        });

        findUsers = (Button) v.findViewById(R.id.findUsersButton);
        findUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), findUsers.class);
                startActivity(i);
            }
        });

        return v;
    }

    public class ChatAdapter extends ArrayAdapter<UserProfile> {
        public ChatAdapter(Context context, ArrayList<UserProfile> users) { super(context, 0, users);}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserProfile user = getItem(position);
            if (convertView == null) {
                if (user.getIsPrivate()) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_without, parent, false);
                }
                else {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_with_pic, parent, false);
                }
            }

            // If this happens to not work the way as intended, then will delete chat without pic and use
            // glide to load a unidentifiable picture from the googer

            if (user.getIsPrivate()) {
                TextView userName = (TextView) convertView.findViewById(R.id.personUserName);
                userName.setText(user.getUserName());
                String databaseIndex;
                /* if (position == 0) {
                    databaseIndex = "One";
                }
                else if (position == 1) {
                    databaseIndex = "Two";
                }
                Firebase userRef = new Firebase("https://positive-note.firebaseio.com/users/");
                userRef.child(user.getUserName()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                }); */
                TextView userRecentMessage = (TextView) convertView.findViewById(R.id.chatText);
            }
            else {
                //ImageView userPic = (ImageView) convertView.findViewById(R.id.personPic);
                // Glide.with(getContext())
                //     .load(getResources().getString(R.string.filler_profile_pic))
                //   .override(60, 45)
                // .into(userPic);
                TextView userName = (TextView) convertView.findViewById(R.id.personUserName);
                userName.setText(user.getUserName());
                TextView userRecentMessage = (TextView) convertView.findViewById(R.id.chatText);
            }
            return convertView;
        }
    }

}
