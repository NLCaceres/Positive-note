package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import itp341.caceres.nicholas.positive_note.app.constants.ConstantsKt;
import itp341.caceres.nicholas.positive_note.app.modelClasses.UserInfo;

public class TabsActivity extends AppCompatActivity {

  Toolbar homeToolbar;
  private ViewPager tabsViewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tabs);

    homeToolbar = (Toolbar) findViewById(R.id.home_toolbar);
    setSupportActionBar(homeToolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
    tabsViewPager = (ViewPager) findViewById(R.id.viewPager);

    TabsPagerAdapter adapter = new TabsPagerAdapter(getSupportFragmentManager());
    tabsViewPager.setAdapter(adapter);

    tabsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        FragmentManager fragManager = getSupportFragmentManager();
        if (fragManager.getBackStackEntryCount() > 0) {
          while (fragManager.getBackStackEntryCount() > 0) {
            fragManager.popBackStackImmediate();
          }
        }
      }
      @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
      @Override public void onPageScrollStateChanged(int state) { }
    });
    tabLayout.setupWithViewPager(tabsViewPager);
  }

  @Override
  public void onBackPressed() {
    if (tabsViewPager.getCurrentItem() == 0) {
//      ChatListFragment chatListFrag = (ChatListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + tabsViewPager.getCurrentItem());
//      Selection<String> selectedList = chatListFrag.getMSelectionTracker().getSelection();
//      if (selectedList.size() == 1) {
//        chatListFrag.getMSelectionTracker().clearSelection();
//      }
    } else if (tabsViewPager.getCurrentItem() == 1) {
      TherapyFragment webViewFrag = (TherapyFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + tabsViewPager.getCurrentItem());
      if (webViewFrag.getWebView().copyBackForwardList().getCurrentIndex() > 0) {
        webViewFrag.getWebView().goBack();
        return;
      }
    } else if (tabsViewPager.getCurrentItem() == 2) { // Works differently than normal due to "nested" fragments
      //RootFragment rootParentFrag = (RootFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + tabsViewPager.getCurrentItem());
      //TherapyFragment webViewFrag = (TherapyFragment) getSupportFragmentManager().findFragmentById(R.id.root_frame);
      if (getSupportFragmentManager().findFragmentById(R.id.root_frame) instanceof TherapyFragment) {
        TherapyFragment webViewFrag = (TherapyFragment) getSupportFragmentManager().findFragmentById(R.id.root_frame);
        if (webViewFrag.getWebView().copyBackForwardList().getCurrentIndex() > 0) {
          webViewFrag.getWebView().goBack();
          return;
        }
      }
    }
    super.onBackPressed();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (tabsViewPager.getCurrentItem() == 0) {
      ChatListFragment chatListFrag = (ChatListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + tabsViewPager.getCurrentItem());
      if (chatListFrag.getActionMode() != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
        if (chatListFrag.getMSelectionTracker().getSelection().size() > 1) {
          Log.d("Back Button ActionMode", "Selection tracker size is bigger than 1");
        }
        return true; // consumes the back key event - ActionMode is not finished
      }
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.profile_button_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_profile:
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        UserInfo userInfo = getIntent().getParcelableExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO);
        if (userInfo != null) {
          intent.putExtra(ConstantsKt.INTENT_EXTRAS_PARCEABLE_USER_INFO, userInfo);
        }
        startActivity(intent);
        return true;
      default: {
        return super.onOptionsItemSelected(item);
      }
    }
  }

  private class TabsPagerAdapter extends FragmentPagerAdapter {

    TabsPagerAdapter(FragmentManager fm) {
      super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public @NonNull
    Fragment getItem(int position) {
      if (position == 0) {
        return ChatListFragment.newInstance();
      } else if (position == 1) {
        return TherapyFragment.newInstance(null);
      } else {
        return RootFragment.newInstance();
      }
    }

    @Override
    public int getCount() {
      return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      if (position == 0) {
        return "Chat";
      } else if (position == 1) {
        return "Resources";
      } else {
        return "What's Mental Health";
      }
    }
  }
}
