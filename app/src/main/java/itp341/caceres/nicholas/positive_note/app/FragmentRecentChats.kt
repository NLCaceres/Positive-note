package itp341.caceres.nicholas.positive_note.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.view.ActionMode
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import itp341.caceres.nicholas.positive_note.app.constants.*
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserChatItem
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserMessage
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo
import kotlinx.android.synthetic.main.app_progressbar.view.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class FragmentRecentChats : Fragment(), CoroutineScope by MainScope() { // Delegation recommended for CoroutineScope

//  override val coroutineContext: CoroutineContext
//    get() = Dispatchers.IO + job

  //private lateinit var job: Job
  //private val scope = MainScope()

  private lateinit var chatMessagesRV: RecyclerView
  private lateinit var mProgBar: ProgressBar
  private lateinit var findUsersButton: Button
  var actionMode: ActionMode? = null

  private lateinit var recentChatMessagesAdapter: RecentMessagesAdapter
  private var mUserInfo: UserInfo? = null
  private var userChatItems: ArrayList<UserChatItem> = arrayListOf()
  var mSelectionTracker: SelectionTracker<String>? = null
  private lateinit var mUserChatItemKeyProvider : RecentMessagesAdapter.UserChatItemKeyProvider

  private var mUsersReference: CollectionReference? = null

  override fun onCreate(savedInstanceState: Bundle?) { // Only called by ViewPager when frag not retained
    super.onCreate(savedInstanceState)
    // Logger().info("Called onCreate again!") // Kotlin takes logging to a whole new level!
    Log.d("ChatListFrag onCreate", "Calling onCreate from ChatListFragment again!")
  }

  override fun onSaveInstanceState(outState: Bundle) { // Handle orientation changes
    mSelectionTracker?.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) { // Handle orientation changes - Fragment version (Activity is onRestoreInstanceState)
    super.onViewStateRestored(savedInstanceState)
    mSelectionTracker?.onRestoreInstanceState(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_chat, container, false)

    //job = Job()

    mProgBar = view.app_progressbar.apply { visibility = View.VISIBLE }

    findUsersButton = view.findUsersButton.apply {
      setOnClickListener {
        val intent = Intent(context, ActivityFindUsers::class.java)
        val user = activity?.intent?.getParcelableExtra<UserInfo>(INTENT_EXTRAS_PARCEABLE_USER_INFO)
        intent.putExtra(INTENT_EXTRAS_PARCEABLE_USER_INFO, user)
        startActivity(intent)
      }
    }

    setUpRecyclerView(view)

    fetchRecyclerViewData()

    return view
  }

  private fun setUpRecyclerView(view: View) {
    if (userChatItems.size > 0) { userChatItems.clear() }
    recentChatMessagesAdapter = RecentMessagesAdapter(userChatItems, object : RecentMessagesAdapter.UserChatItemClickListener {
      // Passing in the view enables smooth fancy transition if using AppCompatActivity.makeSceneTransitionAnimation - Otherwise unnecessary
      override fun onUserChatClick(view: View, userChatItem: UserChatItem) {
        val intent = Intent(context, ActivityChatMessages::class.java)
        intent.putExtra(INTENT_EXTRAS_PARCEABLE_CHAT_PARTNER_INFO, userChatItem.user)
        intent.putExtra(INTENT_EXTRAS_PARCEABLE_USER_INFO, mUserInfo)
        intent.putExtra(INTENT_EXTRAS_CHAT_EXISTS, true)
        startActivity(intent)
      }
    })
    mUserInfo = activity?.intent?.getParcelableExtra(INTENT_EXTRAS_PARCEABLE_USER_INFO)
    mUsersReference = FirebaseFirestore.getInstance().collection("users")

    chatMessagesRV = view.chatRecyclerView.apply {
      setHasFixedSize(true)
      adapter = recentChatMessagesAdapter
      addItemDecoration(DividerItemDecoration(context, (layoutManager as LinearLayoutManager).orientation))
    }
  }

  private fun fetchRecyclerViewData() {
    launch {
      val chatListSnapshot = withContext(Dispatchers.IO) {fetchUserChatMessages()}
      if (chatListSnapshot != null) {
        for (userChatSnapshot in chatListSnapshot) { // TODO: Figure out Async calls better for concurrent calls
          val recentMessage = convertToUserMessage(userChatSnapshot["mostRecentMessage"] as HashMap<*, *>)
          val chatUser = withContext(Dispatchers.IO) { fetchUserChat(userChatSnapshot.id) }
          userChatItems.add(UserChatItem(chatUser, recentMessage))
        }
      }

      mUserChatItemKeyProvider = RecentMessagesAdapter.UserChatItemKeyProvider(1, userChatItems) // Scope Cached = 1 / Mapped = 0
      mSelectionTracker = SelectionTracker.Builder<String>("SelectedChatItems", chatMessagesRV,
          mUserChatItemKeyProvider, RecentMessagesAdapter.UserChatItemDetailsLookup(chatMessagesRV),
          StorageStrategy.createStringStorage()).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
      recentChatMessagesAdapter.selectionTracker = mSelectionTracker
      mSelectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<String>() {
        // onSelectionRestored - called after orientation changes
        override fun onSelectionChanged() { // Called whenever new selections are made
          super.onSelectionChanged()
          mSelectionTracker?.let {
            if (it.hasSelection() && actionMode == null) {
              actionMode = (activity as AppCompatActivity).startSupportActionMode(actionModeCallback)
            } else if (!it.hasSelection() && actionMode != null) {
              actionMode?.finish()
            } else {
              actionMode?.title = "${mSelectionTracker?.selection?.size()} selected"
            }
          }
        }
      })

      recentChatMessagesAdapter.notifyDataSetChanged()
      mProgBar.visibility = View.INVISIBLE
    }
  }

  private suspend fun fetchUserChatMessages(): QuerySnapshot? {
    val recentChatsReference = mUserInfo?.let { mUsersReference?.document(it.email)?.
        collection("recentChats")?.orderBy("mostRecentMessage.timeStamp", Query.Direction.DESCENDING) } // Most recent message first
    return try {
      recentChatsReference?.get()?.await()
    } catch (e: FirebaseFirestoreException) {
      // Handle exception
      return null
    }
  }

  private fun convertToUserMessage(recentMessageMap: Map<*,*>) : UserMessage {
    val timeStamp = recentMessageMap["timeStamp"] as Timestamp
    val timeAsDate = timeStamp.toDate()
    val user = recentMessageMap["user"] as String
    val message = recentMessageMap["message"] as String
    return UserMessage(user, message, timeAsDate)
  }

  private suspend fun fetchUserChat(docID: String): UserInfo? {
    return try {
      mUsersReference?.document(docID)?.get()?.await()?.toObject(UserInfo::class.java)
    } catch (e: FirebaseFirestoreException) {
      return null
    }
  }

  override fun onDestroy() { // Different than onDestroyView - Called ONLY when fragment is no longer retained
    super.onDestroy()
    cancel() // MainScope will be cancelled including all jobs and children! - best when MainScope() is delegate
    //job.cancel() // No longer the best way to do it
    //scope.cancel() // With a val version of MainScope()
  }

  val actionModeCallback = object : ActionMode.Callback {

    // Called when the action mode is created; startActionMode() was called
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
      mode.menuInflater.inflate(R.menu.chat_list_action_menu, menu)
      return true
    }
    // Called after onCreate every time (possibly multiple times)
    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false // Return false if nothing is needed to be done

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
      return when (item.itemId) {
        R.id.action_delete_chat -> {
          mSelectionTracker?.let {
            for (selectedItem in it.selection) {
              Log.d("SelectedItem ID", selectedItem)
              val userChatToDelete = userChatItems.single {userChat -> selectedItem == (userChat.user?.email)}
              val currentUserEmail = mUserInfo?.email
              if (currentUserEmail != null && userChatToDelete.user?.email != null) {
                val userToDeleteRef = mUsersReference?.document(currentUserEmail)?.collection("recentChats")?.document(userChatToDelete.user.email)
                userChatItems.remove(userChatToDelete)
                recentChatMessagesAdapter.notifyDataSetChanged()
                setKeyToPositionMap(mUserChatItemKeyProvider.mKeyToPosition, userChatItems)
              }
            }
          }
          mode.finish() // Close Context Menu
          true
        }
        else -> false
      }
    }
    override fun onDestroyActionMode(mode: ActionMode) {
      actionMode = null
      mSelectionTracker?.clearSelection()
    }
  }

  companion object {
    @JvmStatic
    fun newInstance(): FragmentRecentChats {
      return FragmentRecentChats()
    }
  }
}