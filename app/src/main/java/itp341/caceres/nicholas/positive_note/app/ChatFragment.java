package itp341.caceres.nicholas.positive_note.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import itp341.caceres.nicholas.positive_note.app.modelClasses.UserMessage;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserSingleton;

/**
 * Created by NLCaceres on 4/30/2016.
 */
public class ChatFragment extends Fragment {

  public static final String ARG_CHAT_TAB_NAME = "itp341.caceres.nicholas.positivenote.app.chat_frag";
  private ListView chatMessagesList;
  private ChatAdapter chatAdapter;
  private UserSingleton userBase;
  private List<Map<String, Object>> userChat;
  private List<UserInfo> userChats;
  private Location userLocation;
  private FirebaseUser mFirebaseUser;
  private UserInfo mUserInfo;
  private FusedLocationProviderClient fusedLocationProviderClient;
  private CollectionReference mChatsReference;
  private Button findUsers;

  public static ChatFragment newInstance(String name) {
    ChatFragment chatFrag = new ChatFragment();
    return chatFrag;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_chat, container, false);

    mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    userBase = UserSingleton.getInstance();
    mUserInfo = getActivity().getIntent().getParcelableExtra("UserInfo");

//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
//        fetchUserLocation();

//        chatMessagesList = (ListView) v.findViewById(R.id.chatListView);
//        userChats = new ArrayList<>();
//        chatAdapter = new ChatAdapter(getContext(), userChats);
//        chatMessagesList.setAdapter(chatAdapter);
//        chatMessagesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent i = new Intent(getContext(), InMessageActivity.class);
//                TextView personTV = (TextView) view.findViewById(R.id.chatUserNameTV);
//                String receiverUserName = personTV.getText().toString();
//                i.putExtra(InMessageActivity.EXTRA_RECEIVER, receiverUserName);
//                startActivity(i);
//            }
//        });

    findUsers = (Button) v.findViewById(R.id.findUsersButton);
    findUsers.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(getContext(), FindUsersActivity.class);
        UserInfo user = getActivity().getIntent().getParcelableExtra("UserInfo");
        i.putExtra("UserInfo", user);
        startActivity(i);
      }
    });

    mChatsReference = FirebaseFirestore.getInstance().collection("users").document(mUserInfo.getEmail()).collection("recentChats");
    fetchUserChats();

    return v;
  }

  private void fetchUserLocation() {
    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(getActivity(), new String[]
          {Manifest.permission.ACCESS_COARSE_LOCATION}, HomeActivity.LOCATION_REQUEST_CODE);
      return;
    }
    /* getLastLocation will only get last observed location (so in emulator googleMaps is only source of location coordinates
     * which is not a problem with a real phone where other apps are constantly asking for location updates)
     * Here if I want updates, go to Google Maps, get it, send the phone a new update via triple-dot extra controls, and go back to app to see update  */
    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
      @Override
      public void onSuccess(Location location) { // Save User Location up to 5 digits of geohash for security compare 3 digits to get a good radius
        if (location != null) {
          userLocation = location;
        }
      }
    });
  }

  private void fetchUserChats() {
    mChatsReference.get().addOnSuccessListener(recentChatCollection -> {
      for (DocumentSnapshot doc : recentChatCollection) {
        doc.getReference().collection("messages").orderBy("timeStamp").limit(1).get()
            .addOnSuccessListener(recentMessageDocs -> {
              UserMessage recentUserMessage;
              for (DocumentSnapshot recentMessageDoc : recentMessageDocs) {
                recentUserMessage = recentMessageDoc.toObject(UserMessage.class);
              }
            });
        String docID = doc.getId();
        FirebaseFirestore.getInstance().collection("users").document(docID).get()
            .addOnSuccessListener(documentSnapshot -> {
              UserInfo chatUser = documentSnapshot.toObject(UserInfo.class);
              userChats.add(chatUser);
            });
        Log.d("RecentChats", docID);
      }
      chatAdapter.notifyDataSetChanged();
    });
  }

  public class ChatAdapter extends ArrayAdapter<UserInfo> {
    public ChatAdapter(Context context, List<UserInfo> users) {
      super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      UserInfo user = getItem(position);
      if (convertView == null) {
        if (user.getPrivate()) {
          convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_without, parent, false);
        } else {
          convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_with_pic, parent, false);
        }
      }

      if (user.getPrivate()) {
        TextView userName = (TextView) convertView.findViewById(R.id.chatUserNameWithoutTV);
        userName.setText(user.getUserName());
        TextView userRecentMessage = (TextView) convertView.findViewById(R.id.chatRecentMessageWithoutTV);
      } else {
        ImageView userPic = (ImageView) convertView.findViewById(R.id.chatUserImageView);
        Glide.with(getContext()).load(getResources()
            .getString(R.string.filler_profile_pic))
            .override(60, 45).into(userPic);
        TextView userName = (TextView) convertView.findViewById(R.id.chatUserNameTV);
        userName.setText(user.getUserName());
        TextView userRecentMessage = (TextView) convertView.findViewById(R.id.chatRecentMessageTV);
      }
      return convertView;
    }
  }

}
