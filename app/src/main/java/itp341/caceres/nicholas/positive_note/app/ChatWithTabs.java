package itp341.caceres.nicholas.positive_note.app;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class ChatWithTabs extends AppCompatActivity {

    private Toolbar tabsToolbar;
    private ViewPager tabsViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_tabs);

        tabsToolbar = (Toolbar) findViewById(R.id.tabs_toolbar);
        setSupportActionBar(tabsToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabsViewPager = (ViewPager) findViewById(R.id.viewPager);

        TabsPagerAdapter adapter = new TabsPagerAdapter(getSupportFragmentManager());
        tabsViewPager.setAdapter(adapter);

        tabsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                FragmentManager fragManager = getSupportFragmentManager();
                if (fragManager.getBackStackEntryCount() > 0) {
                    while (fragManager.getBackStackEntryCount() > 0){
                        fragManager.popBackStackImmediate();
                    }
                }
            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });
        tabLayout.setupWithViewPager(tabsViewPager);
    }

    @Override
    public void onBackPressed() {
        if (tabsViewPager.getCurrentItem() == 1) {
            TherapyFragment webViewFrag = (TherapyFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + tabsViewPager.getCurrentItem());
            if (webViewFrag.getWebView().copyBackForwardList().getCurrentIndex() > 0) {
                webViewFrag.getWebView().goBack();
            } else {
                super.onBackPressed();
            }
        } else if (tabsViewPager.getCurrentItem() == 2) { // Works differently than normal due to "nested" fragments
            RootFragment rootParentFrag = (RootFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + tabsViewPager.getCurrentItem());
            TherapyFragment webViewFrag = (TherapyFragment) getSupportFragmentManager().findFragmentById(R.id.root_frame); // Really just a fragment view inside the tabView's current Fragment
            if (webViewFrag != null && webViewFrag.getWebView().copyBackForwardList().getCurrentIndex() > 0) {
                webViewFrag.getWebView().goBack();
            } else {
                super.onBackPressed();
            }
        } else { super.onBackPressed(); }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    private class TabsPagerAdapter extends FragmentPagerAdapter {

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return ChatFragment.newInstance(null);
            }
            else if (position == 1) {
                return TherapyFragment.newInstance(null);
            }
            else {
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
            }
            else if (position == 1) {
                return "Resources";
            }
            else {
                return "What's Mental Health";
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                Intent i = new Intent(getApplicationContext(), UserProfileWithSettings.class);
                startActivity(i);
                return true;
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
