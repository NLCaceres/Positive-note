package itp341.caceres.nicholas.positive_note.app

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserMessage
import kotlinx.android.synthetic.main.fragment_auto_slider_page.view.*
import kotlin.random.Random

// FragmentInteractionListener may be used to handle interaction events
class AutoSlideFragment : Fragment() {
  private var pagerMessageTextView: TextView? = null // Must be init-ed on creation!
  private var pagerUserTextView: TextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_auto_slider_page, container, false)

    val backgroundColor = when (Random.nextInt(0,5)) {
      0 -> Color.parseColor("cyan") // Delivers int instead of Color object
      1 -> Color.parseColor("lightgrey")
      2 -> Color.parseColor("olive")
      3 -> Color.parseColor("fuchsia")
      4 -> Color.parseColor("yellow")
      else -> Color.parseColor("red")
    }
    view.backgroundView.background.setTint(backgroundColor) // Will complain not init'd if no else branch in when statement

    val message = arguments?.getParcelable<UserMessage>("message")

    val userMessage = "${message?.message}"
    pagerMessageTextView = view.pagerMessageTextView.apply {
      text = userMessage
    }

    val userText = "From: ${message?.user}"
    pagerUserTextView = view.pagerUserTextView.apply {
      text = userText
    }

    return view
  }

  companion object {
    @JvmStatic
    fun newInstance(messageInside: UserMessage): AutoSlideFragment {
      val autoSlideFrag = AutoSlideFragment()
      val args = Bundle()
      args.putParcelable("message", messageInside)
      autoSlideFrag.arguments = args
      return autoSlideFrag
    }
  }
}
