package itp341.caceres.nicholas.positive_note.app;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * Created by NLCaceres on 4/30/2016.
 */
public class TherapyFragment extends Fragment {

    private WebView therapyWebView;
    private String startingURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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

    private class MyWebBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView v, String URL) {
            v.loadUrl(URL);
            return true;
        }
    }

    public void setStartingURL(String startingURL) {
        this.startingURL = startingURL;
    }
    public WebView getWebView() { return this.therapyWebView; }
}
