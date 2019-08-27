package itp341.caceres.nicholas.positive_note.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import itp341.caceres.nicholas.positive_note.app.Model.Message;

public class InMessageActivity extends AppCompatActivity {

    private DatabaseReference myFireBaseRef;

    private EditText messageInputET;
    private String newMessage;
    private Map<String, String> newMessageData;
    private Map<String, Object> newMessageMap;
    private String userName;

    private ImageView sendMessageButton;

    private ListView messageList;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messageArray;

    private Toolbar messageToolbar;

    public static final String EXTRA_RECEIVER = "itp341.caceres.nicholas.positivenote.receiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_message);

        messageToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(messageToolbar);
        Intent i = getIntent();
        final String userNameTitle = i.getStringExtra(EXTRA_RECEIVER);
        getSupportActionBar().setTitle(userNameTitle);

        SharedPreferences prefs = getSharedPreferences(createAccount.PREFERENCE_FILE, MODE_PRIVATE);
        userName = prefs.getString(createAccount.PREFERENCE_USERNAME, "");

        //myFireBaseRef = new Firebase("https://positive-note.firebaseio.com/messages/");
        myFireBaseRef = FirebaseDatabase.getInstance().getReference();
        newMessageData = new HashMap<>();
        newMessageMap = new HashMap<>();

        messageInputET = (EditText) findViewById(R.id.messageInputEditText);
        sendMessageButton = (ImageView) findViewById(R.id.sendMessageButton);
        Glide.with(getApplicationContext())
                .load(getResources().getString(R.string.send_button_url))
                .override(50,40)
                .into(sendMessageButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMessage = messageInputET.getText().toString();
                if (newMessage != null && newMessage.isEmpty()) {

                } else {
                    int newIndex = messageArray.size() + 1;
                    newMessageData.put("sender", userName);
                    newMessageData.put("message", newMessage);
                    newMessageMap.put("one/m" + newIndex, newMessageData);
                    myFireBaseRef.updateChildren(newMessageMap);
                    messageInputET.setText("");
                }
            }
        });
        messageInputET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                newMessage = messageInputET.getText().toString();
                if (newMessage != null && newMessage.isEmpty()) {
                    return true;
                } else {
                    int newIndex = messageArray.size() + 1;
                    newMessageData.put("sender", userName);
                    newMessageData.put("message", newMessage);
                    newMessageMap.put("one/m" + newIndex, newMessageData);
                    myFireBaseRef.updateChildren(newMessageMap);
                    messageInputET.setText("");
                    return true;
                }
            }
        });

        messageList = (ListView) findViewById(R.id.messageListView);
        //Firebase messagesRef = new Firebase("https://positive-note.firebaseio.com/messages/one/");
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference().child("messages").child("one");
        messageArray = new ArrayList<>();
        messageAdapter = new MessageAdapter(getApplicationContext(), messageArray);
        messageList.setAdapter(messageAdapter);
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageArray.clear();
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messageArray.add(message);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                // Error in finding any messages
            }
        });
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ImageView starredMessage = (ImageView) view.findViewById(R.id.starredMessage);
                TextView userMessageTV = (TextView) view.findViewById(R.id.UMTextView);
                String userMessage = userMessageTV.getText().toString();
                //Firebase myPositiveNotes = new Firebase("https://positive-note.firebaseio.com/" + userName + "/myPositiveNotes/" /*+ */);
                if (starredMessage.getVisibility() == View.VISIBLE) {
                    starredMessage.setVisibility(View.INVISIBLE);
                }
                else {

                }
            }
        });
    }

    public class MessageAdapter extends ArrayAdapter<Message> {
        public MessageAdapter(Context context, ArrayList<Message> messages) {super(context, 0, messages);}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
            }
            TextView userNameTV = (TextView) convertView.findViewById(R.id.UNTextView);
            userNameTV.setText(message.getSender());
            TextView userMessageTV = (TextView) convertView.findViewById(R.id.UMTextView);
            userMessageTV.setText(message.getMessage());
            ImageView starredMessage = (ImageView) convertView.findViewById(R.id.starredMessage);
            Glide.with(getContext())
                    .load(getResources().getString(R.string.starred_message_url))
                    .override(30,30)
                    .into(starredMessage);
            return convertView;
        }
    }
}
