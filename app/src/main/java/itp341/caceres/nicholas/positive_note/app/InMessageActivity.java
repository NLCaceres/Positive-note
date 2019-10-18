package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.Selection;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.data.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserMessage;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;

public class InMessageActivity extends AppCompatActivity {

  private Toolbar messageToolbar;
  private ActionMode actionMode = null;
  private ProgressBar mProgBar;

  private EditText messageInputET;
  private String newMessage;
  private ImageView sendMessageButton;

  private UserInfo mUserInfo;
  private UserInfo mChatPartner;

  private RecyclerView messageRecyclerView;
  private UserMessagesRVAdapter messagesAdapter;
  private ArrayList<UserMessage> userMessageArray = new ArrayList<>();
  private SelectionTracker<String> mSelectionTracker;

  private DocumentReference messageDBRef;
  private boolean messageCollectionExists;
  private UserMessageItemKeyProvider mItemKeyProvider;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_in_message);

    mUserInfo = getIntent().getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO);
    mChatPartner = getIntent().getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_CHAT_PARTNER_INFO);

    messageToolbar = (Toolbar) findViewById(R.id.home_toolbar);
    setSupportActionBar(messageToolbar);
    getSupportActionBar().setTitle(mChatPartner.getUserName());
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mProgBar = findViewById(R.id.app_progressbar);
    mProgBar.setVisibility(View.VISIBLE);

    messageInputET = (EditText) findViewById(R.id.messageInputEditText);
    messageInputET.addTextChangedListener(new MessageInputTextWatcher()); // Handles new chars entered into editText

    messageRecyclerView = findViewById(R.id.messageRecyclerView);
    //messageRecyclerView.setHasFixedSize(true); // If children are all the same size height, then setFixed for optimization - wrap_content does not count
    messagesAdapter = new UserMessagesRVAdapter(userMessageArray, mUserInfo.getFullName());
    messageRecyclerView.setAdapter(messagesAdapter);

    messageDBRef = FirebaseFirestore.getInstance().collection("users").document(mUserInfo.getEmail()).collection("recentChats").document(mChatPartner.getEmail());
    messageDBRef.collection("messages").orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener(new NewMessageEventListener());

    sendMessageButton = (ImageView) findViewById(R.id.sendMessageButton);
    sendMessageButton.setOnClickListener(new SendMessageButtonListener()); // Handles firestore call and add message into array
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    mSelectionTracker.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mSelectionTracker.onRestoreInstanceState(savedInstanceState);
  }

  private class NewMessageEventListener implements EventListener<QuerySnapshot> {
    @Override // EventListener that returns array (immediately and updates later)
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
      if (e != null) { Log.w("Snapshot Listener Error", "Issue listening in for new messages"); }

      if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) { // All changes will fire this event (in app changes will immediately fire it)
        if (userMessageArray.size() > 0) { return; }
        for (DocumentSnapshot queryDocRef : queryDocumentSnapshots) {
          UserMessage newMessage = queryDocRef.toObject(UserMessage.class);
          userMessageArray.add(newMessage);
        }

        mItemKeyProvider = new UserMessageItemKeyProvider(ItemKeyProvider.SCOPE_CACHED, userMessageArray);
        mSelectionTracker = new SelectionTracker.Builder<String>("SelectedChatMessage", messageRecyclerView,
            mItemKeyProvider, new UserMessageItemDetailsLookup(messageRecyclerView),
            StorageStrategy.createStringStorage()).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build();
        messagesAdapter.setSelectionTracker(mSelectionTracker);
        mSelectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
          @Override
          public void onSelectionChanged() {
            if (mSelectionTracker.hasSelection() && actionMode == null) { actionMode = startSupportActionMode(new ActionModeCallback()); }
            else if (!mSelectionTracker.hasSelection() && actionMode != null) { actionMode.finish(); }
            else if (actionMode != null){ actionMode.setTitle(mSelectionTracker.getSelection().size() + " selected"); }
          }
        });

        messagesAdapter.notifyDataSetChanged();
        mProgBar.setVisibility(View.INVISIBLE);

        Log.d("Docs returned", "Messages found");
      } else { Log.w("MessageDoc null", "Message event listener doc null / does not exist"); }
    }
  }

  static class UserMessageItemKeyProvider extends ItemKeyProvider<String> { // Key will have to be timeStamp val
    private HashMap<String, Integer> userMessageTimeStampMap;
    private ArrayList<UserMessage> userMessageList;

    UserMessageItemKeyProvider(int scope, ArrayList<UserMessage> userMessageTimeStampList) {
      super(scope);
      userMessageList = userMessageTimeStampList;
      userMessageTimeStampMap = setTimeStampMap(userMessageTimeStampList);
    }

    HashMap<String, Integer> setTimeStampMap(ArrayList<UserMessage> userMessages) {
      HashMap<String, Integer> timeStampMap = new HashMap<>();
      for (int i = 0; i < userMessages.size(); i++) {
        String userMessageTimeStamp = userMessages.get(i).getTimeStamp().toString();
        timeStampMap.put(userMessageTimeStamp, i);
      }
      return timeStampMap;
    }

    void updateTimeStampMap(ArrayList<UserMessage> userMessageTimeStampList) {
      int position = userMessageTimeStampList.size() - 1;
      UserMessage newMessage = userMessageTimeStampList.get(position);
      userMessageTimeStampMap.put(newMessage.getTimeStamp().toString(), position);
    }

    @Nullable @Override public String getKey(int position) { return userMessageList.get(position).getTimeStamp().toString(); }
    @Override public int getPosition(@NonNull String key) { return userMessageTimeStampMap.get(key) != null ? userMessageTimeStampMap.get(key) : -1; }
  }

  static class UserMessageItemDetailsLookup extends ItemDetailsLookup<String> {
    private RecyclerView recyclerView;

    UserMessageItemDetailsLookup(RecyclerView recyclerV) {
      super();
      recyclerView = recyclerV;
    }

    @Nullable
    @Override
    public ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
      View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
      if (view != null) {
        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
        return viewHolder instanceof UserMessagesRVAdapter.MainUserMessageViewHolder ?
            ((UserMessagesRVAdapter.MainUserMessageViewHolder) viewHolder).getItemDetails() :
            ((UserMessagesRVAdapter.ChatPartnerMessageViewHolder) viewHolder).getItemDetails();
      }
      return null;
    }
  }

  private class MessageInputTextWatcher implements TextWatcher {
    @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
    @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
    @Override public void afterTextChanged(Editable editable) { newMessage = messageInputET.getText().toString(); }
  }

  private class SendMessageButtonListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
      newMessage = messageInputET.getText().toString();
      if (newMessage.isEmpty()) {
        return;
      }
      UserMessage newUserMessage = new UserMessage(mUserInfo.getFullName(), newMessage, new Date(), false);
      userMessageArray.add(newUserMessage);
      messagesAdapter.notifyDataSetChanged();
      mItemKeyProvider.updateTimeStampMap(userMessageArray);

      messageRecyclerView.scrollToPosition(messagesAdapter.getItemCount() - 1);

      messageDBRef.update("mostRecentMessage", newUserMessage);
      messageDBRef.collection("messages").add(newUserMessage);

      messageInputET.setText(""); // Reset text after sending
    }
  }

  private class ActionModeCallback implements ActionMode.Callback {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      mode.getMenuInflater().inflate(R.menu.chat_messages_action_menu, menu);
      // menu.findItem() // Grabs item by ID so you can modify things if necessary
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; } // May be useful to decide on certain drawable or layout!

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      switch (item.getItemId()) {
        case R.id.action_delete_chat: // TODO: Delete Functionality
          Log.d("Delete Message", "Delete Message tapped");
          if (mSelectionTracker.hasSelection()) {
            for (String selectedItem : mSelectionTracker.getSelection()) {
              int position = mItemKeyProvider.getPosition(selectedItem);
              UserMessage selectedMessage = userMessageArray.get(position);
              //UserMessage selectedMessage = userMessageArray.stream() // Java 8 handling of higher order functions
                  //.filter(userMessage -> selectedItem.equals(userMessage.getTimeStamp().toString())).findFirst().orElse(null); // orElse handles Optional return of FindFirst
              if (selectedMessage != null) {
                handleDeleteMessages(selectedMessage, position);
              }
            }
          }
          mode.finish();
          break;
        case R.id.action_favorite_chat: // TODO: Add favorited message to PositiveNotes Collection
          Log.d("Favorite Message", "Favorite Message tapped");
          if (mSelectionTracker.hasSelection()) {
            for (String selectedItem : mSelectionTracker.getSelection()) {
              int position = mItemKeyProvider.getPosition(selectedItem);
              UserMessage favoriteMessage = userMessageArray.get(position);

              if (!favoriteMessage.getUser().equals(mUserInfo.getFullName())) {
                favoriteMessage.setFavorited(!favoriteMessage.getFavorited());
                handleNewFavoritedMessages(favoriteMessage, position);
              }
            }
          }
          mode.finish();
          break;
        default:
          return false;
      }
      return false;
    }

    private void handleDeleteMessages(UserMessage messageToDelete, int position) {
      if (position == userMessageArray.size() - 1) { // Happens before delete in array
        messageDBRef.update("mostRecentMessage", userMessageArray.get(position - 1));
      }

      messageDBRef.collection("messages").whereEqualTo("timeStamp", messageToDelete.getTimeStamp()).limit(1).get()
        .addOnSuccessListener(queryDocumentSnapshots -> {
          for (DocumentSnapshot docSnapshot : queryDocumentSnapshots.getDocuments()) {
            docSnapshot.getReference().delete();
          }
        });

      userMessageArray.remove(messageToDelete);
      messagesAdapter.notifyItemRemoved(position);
      mItemKeyProvider.setTimeStampMap(userMessageArray);
    }

    // This entire function will run before onBindViewHolder w/ payload runs
    private void handleNewFavoritedMessages(UserMessage favoriteMessage, int position) { // If Visible then true so it must hide it
      userMessageArray.set(position, favoriteMessage);
      messagesAdapter.notifyItemChanged(position, "Favorites-Changed"); // Payload used to ensure only heartImage is changed and updated

      if (position == userMessageArray.size() - 1) {
        messageDBRef.update("mostRecentMessage.favorited", favoriteMessage.getFavorited());
      }

      messageDBRef.collection("messages").whereEqualTo("timeStamp", favoriteMessage.getTimeStamp()).limit(1).get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot docSnapshot : queryDocumentSnapshots.getDocuments()) {
              UserMessage messageToChange = docSnapshot.toObject(UserMessage.class);
              docSnapshot.getReference().update("favorited", !messageToChange.getFavorited()); // Must be set this way to ensure right
            }
          });

      CollectionReference positiveNotesRef = FirebaseFirestore.getInstance().collection("users").document(mUserInfo.getEmail()).collection("positiveNotes");
      if (favoriteMessage.getFavorited()) { // Now fave so add
        positiveNotesRef.add(favoriteMessage); // Add makes a random ID as opposed to set where an ID is required
      } else {
        positiveNotesRef.whereEqualTo("timeStamp", favoriteMessage.getTimeStamp()).limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
          for (DocumentSnapshot docSnapshot : queryDocumentSnapshots.getDocuments()) {
            docSnapshot.getReference().delete();
          }
        });
      }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      mSelectionTracker.clearSelection();
    }
  }
}
