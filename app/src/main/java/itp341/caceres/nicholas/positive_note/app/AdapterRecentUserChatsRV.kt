package itp341.caceres.nicholas.positive_note.app

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import itp341.caceres.nicholas.positive_note.app.constants.VIEWHOLDER_NO_PIC_LAYOUT
import itp341.caceres.nicholas.positive_note.app.constants.VIEWHOLDER_PIC_LAYOUT
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserChatItem
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_chat_with_pic.*
import kotlinx.android.synthetic.main.item_chat_without.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RecentMessagesAdapter(private val recentMessages: ArrayList<UserChatItem>, private val listener : UserChatItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  var selectionTracker: SelectionTracker<String>? = null

  class ChatViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer { // LayoutContainer uses IDs instead of private vals!
    var viewHolderID: String? = null

    fun bind(userChatItem: UserChatItem, listener: UserChatItemClickListener) { // With LayoutContainer, a bind func is even easier than onBindView!
      userChatItem.user?.apply {
        chatUserNameTV.text = userName
        chatUserLocationTV.text = currentLocation?.userCity
        viewHolderID = email
      }
      userChatItem.userRecentMessage?.apply {
        chatRecentMessageTV.text = message
        timeStamp?.let { timeTextView.text = setTimeTextView(it)}
      }
      Glide.with(itemView.context).load(itemView.context.resources.getString(R.string.filler_profile_pic)).override(60, 45).into(chatUserImageView)
      userChatWithPic.setOnClickListener { listener.onUserChatClick(it, userChatItem) }
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> =
        object : ItemDetailsLookup.ItemDetails<String>() {
          override fun getPosition(): Int = adapterPosition
          override fun getSelectionKey(): String = viewHolderID!!
        }
  }

  class WithoutChatViewHolder(view: View) : RecyclerView.ViewHolder(view) { // Private vals are another method to ensure views are cached!
    val userNameTV: TextView = view.chatUserNameWithoutTV
    val userMessageTV: TextView = view.chatRecentMessageWithoutTV
    val userMessageTimeTV: TextView = view.timeWithoutTV
    var viewHolderID: String? = null

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<String> = // Returns ItemDetailsLookup.ItemDetails object used by ItemDetailsLookup in selectionTracker
        object : ItemDetailsLookup.ItemDetails<String>() { // Functions below aid lookup/selection of viewHolders
          override fun getPosition(): Int = adapterPosition
          override fun getSelectionKey(): String = viewHolderID!!
        }
  }

  class UserChatItemKeyProvider(scope: Int, private val recentMessageList: ArrayList<UserChatItem>) : ItemKeyProvider<String>(scope) {
    val mKeyToPosition: HashMap<String, Int> = hashMapOf()
    init { // Init blocks are called after constructor so computed vals can work on params/member vars here
      setKeyToPositionMap(mKeyToPosition, recentMessageList)
    }
    override fun getKey(position: Int): String? = recentMessageList[position].user?.email
    override fun getPosition(key: String): Int = mKeyToPosition[key] ?: -1 // Return -1 if null
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = if (viewType == VIEWHOLDER_PIC_LAYOUT) {
    ChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_with_pic, parent, false))
  } else {
    WithoutChatViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat_without, parent, false))
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val chatHolder: RecyclerView.ViewHolder
    val userChatItem = UserChatItem(recentMessages[position].user, recentMessages[position].userRecentMessage)

    if (holder.itemViewType == VIEWHOLDER_PIC_LAYOUT) {
      chatHolder = holder as ChatViewHolder
      chatHolder.bind(userChatItem, listener)
      selectionTracker?.let { chatHolder.itemView.isActivated = it.isSelected(chatHolder.viewHolderID) }
    } else {
      chatHolder = holder as WithoutChatViewHolder
      chatHolder.itemView.setOnClickListener {
        listener.onUserChatClick(it, userChatItem)
      }
      selectionTracker?.let {
        chatHolder.apply {
          userNameTV.text = userChatItem.user?.userName
          userMessageTV.text = userChatItem.userRecentMessage?.message
          viewHolderID = userChatItem.user?.email
          userChatItem.userRecentMessage?.timeStamp?.let { userMessageTimeTV.text = setTimeTextView(it) }
          itemView.isActivated = it.isSelected(viewHolderID) // Set ChatHolder's Item view to activated (effectively selected) based on the tracker's info and email ID
        }
      }
    }
  }

  override fun getItemCount(): Int = recentMessages.size // Could do it with brackets but functions are just like swift!

  override fun getItemViewType(position: Int): Int {
    val user = recentMessages[position].user
    user?.apply {
      if (private) return VIEWHOLDER_NO_PIC_LAYOUT
    }
    return VIEWHOLDER_PIC_LAYOUT
  }

  class UserChatItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<String>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<String>? {
      val view = recyclerView.findChildViewUnder(event.x, event.y)
      if (view != null) {
        val viewHolder = (recyclerView.getChildViewHolder(view) as? ChatViewHolder)
            ?: (recyclerView.getChildViewHolder(view) as? WithoutChatViewHolder)
        return if (viewHolder is ChatViewHolder) viewHolder.getItemDetails() else (viewHolder as WithoutChatViewHolder).getItemDetails()
      }
      return null
    }
  }

  interface UserChatItemClickListener {
    fun onUserChatClick(view: View, userChatItem: UserChatItem)
  }
}

// Allows access across the project using RecentMessagesAdapterKt.setTimeTextView! (effectively creating a static helper func from a class named similarly!)
fun setTimeTextView(messageDate: Date): String {
  val currentDate = Date()
  // val timeElapsed = Duration.between(messageDate.toInstant(), currentDate.toInstant()) // Could be super useful! but API 26+
  val difference = currentDate.time - messageDate.time // Gives you milliseconds

  val differenceInMinutes = (difference / 1000) / 60 // timeElapsed and this are the same (since it returns seconds as well as nanoseconds)
  if (differenceInMinutes < 60) {
    return "$differenceInMinutes mins ago"
  }

  val differenceInHours = differenceInMinutes / 60

  if (differenceInHours < 24) {
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(messageDate)
  } // HH:MM(am/pm)
  val differenceInDays = differenceInHours / 24
  val dayOfTheWeek = SimpleDateFormat("E", Locale.getDefault()).format(messageDate) // DayName
  return if (differenceInDays < 7) dayOfTheWeek else SimpleDateFormat("MMM d", Locale.getDefault()).format(messageDate) // Month Day#
}

fun setKeyToPositionMap(keyPositionMap: HashMap<String, Int>, recentMessages: ArrayList<UserChatItem>) {
  for ((index, recentMessage) in recentMessages.withIndex()) { // Fill StringKey to Int Map
    val userEmail = recentMessage.user?.email
    if (userEmail != null) {
      keyPositionMap[userEmail] = index
    }
  }
}