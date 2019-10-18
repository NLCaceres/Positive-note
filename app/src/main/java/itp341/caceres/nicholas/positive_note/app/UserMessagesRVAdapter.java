package itp341.caceres.nicholas.positive_note.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserMessage;

// RecyclerView Selector instructions at the bottom

public class UserMessagesRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private ArrayList<UserMessage> usersMessages;
  private String currentUserFullName;
  private SelectionTracker<String> selectionTracker; // Always include what type of key it uses (otherwise unchecked call is unsafe)

  UserMessagesRVAdapter(ArrayList<UserMessage> usersMessages, String userFullName) {
    this.usersMessages = usersMessages;
    currentUserFullName = userFullName;
  }

  void setSelectionTracker(SelectionTracker<String> selectionTracker) {
    this.selectionTracker = selectionTracker;
  }

  // Static inner classes only valid if in top-level class (error in Activity since twice-nested)
  static class MainUserMessageViewHolder extends RecyclerView.ViewHolder {
    String viewHolderID; // timeStamp from UserMessage
    TextView messageTextView;
    TextView timeTextView;

    MainUserMessageViewHolder(View view) {
      super(view);
      messageTextView = view.findViewById(R.id.mainUserMessageTextView);
      timeTextView = view.findViewById(R.id.mainUserTimeTextView);
    }

    final void bind(UserMessage message) { // Add listener and even active for itemView
      viewHolderID = message.getTimeStamp().toString();
      messageTextView.setText(message.getMessage());
      timeTextView.setText(RecentUserChatsRVAdapterKt.setTimeTextView(message.getTimeStamp()));
      messageTextView.setOnClickListener(view -> {
        if (timeTextView.getVisibility() == View.INVISIBLE) {
          timeTextView.setAlpha(0f);
          timeTextView.setVisibility(View.VISIBLE);
          timeTextView.animate().alpha(1f).setDuration(400).setListener(null); // NEEDED otherwise weird visibility bug
        } else {
          timeTextView.animate().alpha(0f).setDuration(400).setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) { timeTextView.setVisibility(View.INVISIBLE); }
          });
        }
      });
    }

    ItemDetailsLookup.ItemDetails<String> getItemDetails() {
      return new ItemDetailsLookup.ItemDetails<String>() {
        @Override public int getPosition() { return getAdapterPosition(); }
        @Nullable @Override public String getSelectionKey() { return viewHolderID; }
      };
    }
  }

  static class ChatPartnerMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    String viewHolderID; // timeStamp from userMessage
    ImageView userImageView;
    TextView messageTextView;
    TextView timeTextView;
    ImageView heartImageView; // ImageView that serves as an indicator for liked messages - add to positive note list

    ChatPartnerMessageViewHolder(View view) {
      super(view);
      userImageView = view.findViewById(R.id.partnerImageView);
      messageTextView = view.findViewById(R.id.partnerMessageTextView);
      timeTextView = view.findViewById(R.id.partnerTimeTextView);
      heartImageView = view.findViewById(R.id.heartedMessageImageView);
    }

    final void bind(UserMessage message) {
      viewHolderID = message.getTimeStamp().toString(); // Java Date toString returns Day Month Day# HH:MM:SS TimeZone YYYY
      messageTextView.setText(message.getMessage());
      messageTextView.setOnClickListener(this);
      timeTextView.setText(RecentUserChatsRVAdapterKt.setTimeTextView(message.getTimeStamp()));
      heartImageView.setVisibility(!message.getFavorited() ? View.INVISIBLE : View.VISIBLE);
      heartImageView.setOnClickListener(this);
    }

    ItemDetailsLookup.ItemDetails<String> getItemDetails() {
      return new ItemDetailsLookup.ItemDetails<String>() {
        @Override public int getPosition() { return getAdapterPosition(); }
        @Nullable @Override public String getSelectionKey() { return viewHolderID; }
      };
    }

    @Override
    public void onClick(View view) { // Sets up animation to display time elapsed when message was sent
      if (view.getId() == R.id.partnerMessageTextView) {
        if (timeTextView.getVisibility() == View.INVISIBLE) { // Alpha 0 then make visible and increase alpha until actually visible
          timeTextView.setAlpha(0f);
          timeTextView.setVisibility(View.VISIBLE);
          timeTextView.animate().alpha(1f).setDuration(400).setListener(null);
        } else { // Alpha down to 0, and on completion make it truly invisible
          timeTextView.animate().alpha(0f).setDuration(400).setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) { timeTextView.setVisibility(View.INVISIBLE); }
          });
        }
      } else if (view.getId() == R.id.heartedMessageImageView && heartImageView.getVisibility() == View.VISIBLE) {
        Log.d("Hearted image", "OnClick fired");
      }
    }
  }

  @Override
  public int getItemViewType(int position) {
    String userFullName = usersMessages.get(position).getUser();
    return userFullName.equalsIgnoreCase(currentUserFullName) ? ConstantsKt.VIEWHOLDER_MAIN_USER_LAYOUT : ConstantsKt.VIEWHOLDER_CHAT_PARTNER_LAYOUT;
  }


  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return viewType == ConstantsKt.VIEWHOLDER_MAIN_USER_LAYOUT
        ? new MainUserMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_user_message, parent, false))
        : new ChatPartnerMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partner_message, parent, false));
  }

  // Also a version that has a payload included to check what views need updating (e.g. update heartedImage visibility)
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder.getItemViewType() == ConstantsKt.VIEWHOLDER_MAIN_USER_LAYOUT) {
      MainUserMessageViewHolder viewHolder = (MainUserMessageViewHolder) holder;
      viewHolder.itemView.setActivated(selectionTracker.isSelected(viewHolder.viewHolderID));
      viewHolder.bind(usersMessages.get(position));
    } else {
      ChatPartnerMessageViewHolder viewHolder = (ChatPartnerMessageViewHolder) holder;
      viewHolder.itemView.setActivated(selectionTracker.isSelected(viewHolder.viewHolderID));
      viewHolder.bind(usersMessages.get(position));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (!payloads.isEmpty()) { // Empty on init, otherwise contains Selection-Changed on long-press
      if (holder.getItemViewType() == ConstantsKt.VIEWHOLDER_CHAT_PARTNER_LAYOUT) { // Ensure ChatPartnerLayout
        ChatPartnerMessageViewHolder viewHolder = (ChatPartnerMessageViewHolder) holder;
        String checkedPayload = (String) payloads.stream().filter(payload -> payload instanceof String && payload.equals("Selection-Changed")).findFirst().orElse(null);
        if (checkedPayload != null) { // Null Check && see if selection changed
          viewHolder.itemView.setActivated(selectionTracker.isSelected(viewHolder.viewHolderID));
        }
        String favoritePayload = (String) payloads.stream().filter(payload -> payload instanceof String && payload.equals("Favorites-Changed")).findFirst().orElse(null);
        if (favoritePayload != null) { // Avoid NullException && check if true
          viewHolder.heartImageView.setVisibility(!usersMessages.get(position).getFavorited() ? View.INVISIBLE : View.VISIBLE);
        }
      } else {
        String checkedPayload = (String) payloads.stream().filter(payload -> payload instanceof String && payload.equals("Selection-Changed")).findFirst().orElse(null);
        if (checkedPayload != null) {
          MainUserMessageViewHolder viewHolder = (MainUserMessageViewHolder) holder;
          viewHolder.itemView.setActivated(selectionTracker.isSelected(viewHolder.viewHolderID));
        }
      }
      return; // Prevent normal binder from firing as well
    }
    this.onBindViewHolder(holder, position);
  }

  @Override
  public int getItemCount() {
    return usersMessages.size();
  }
}

/* 1. Create static class that extends ItemKeyProvider (in Activity in the case of Java file)
*     - Can be long, string or even parcelable. Long is easiest to implement due to RView's stableIDs prop
*     - but string makes a lot of sense for objects with IDs (or things that can act as IDs)
*  2. Create static class that extends ItemDetailsLookup (in Activity in case of Java file)
*     - Factory object for ItemDetails objects that describe items in RView
*  3. Enable selection (via activated property of itemViews in RView)
*     - Make Item Layout have background with selector (for activated and not activated state - changing colors)
*  4. Create ActionMode class to launch toolbar's actionmode and handle what's needed
*     - Init'd in observer for selectionTracker (onSelectionChanged)
*  5. Assemble with SelectionTracker.Builder<> in Activity once array is full
*     - Whenever you add objects, be aware selection tracker has to know too. Deletes, too!
*     - Selection Tracker prop in activity AND adapter
*     - Add observer to activity version
*  6. Preserve selection across lifecycle events
*     - onSaveInstanceState for activities/frags. onRestoreInstanceState for activities and onViewStateRestored for frags
* */