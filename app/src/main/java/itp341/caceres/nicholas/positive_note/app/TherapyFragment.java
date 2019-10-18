package itp341.caceres.nicholas.positive_note.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;


/**
 * Created by NLCaceres on 4/30/2016.
 */
public class TherapyFragment extends Fragment {

  private WebView therapyWebView;
  private String startingURL;

  public static TherapyFragment newInstance(String startingLink) {
    TherapyFragment therapyFrag = new TherapyFragment();
    if (startingLink != null) {
      therapyFrag.setStartingURL(startingLink);
    } else {
      therapyFrag.setStartingURL(null);
    }

    return therapyFrag;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_therapy, container, false);

    therapyWebView = (WebView) v.findViewById(R.id.therapyWebView);
    therapyWebView.setWebViewClient(new MyWebBrowser());
    therapyWebView.getSettings().setLoadsImagesAutomatically(true);
    therapyWebView.getSettings().setJavaScriptEnabled(true);
    therapyWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

    if (startingURL != null) {
      therapyWebView.loadUrl(startingURL);
    } else {
      therapyWebView.loadUrl(getResources().getString(R.string.therapy_page));
    }

    return v;
  }

  public void setStartingURL(String startingURL) {
    this.startingURL = startingURL;
  }

  public WebView getWebView() {
    return this.therapyWebView;
  }

  private class MyWebBrowser extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
      view.loadUrl(request.getUrl().toString());
      //return super.shouldOverrideUrlLoading(view, request);
      return true;
    }
  }
}
