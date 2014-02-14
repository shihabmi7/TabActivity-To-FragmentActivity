package app.tab.fragment;
 
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.util.HashMap;

/**
 * @author Adil Soomro
 *
 */
public class TabSample extends FragmentActivity /*TabActivity*/ {
	/** Called when the activity is first created. */
	
    TabHost mTabHost;
    TabManager mTabManager;
    TabWidget tabWidget;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        setContentView(R.layout.main);
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        tabWidget=(TabWidget) findViewById(android.R.id.tabs);
        
        mTabHost.setup();
 
        mTabManager = new TabManager(this, mTabHost,android.R.id.tabcontent);
 
        mTabManager.addTab(mTabHost.newTabSpec("simple").
                setIndicator(getTabIndicator("Home", R.drawable.tab_home)),
                ArrowsActivity.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("throttle").
                setIndicator(getTabIndicator("Search", R.drawable.tab_search)),
                OptionsActivity.class, null);
 
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }
    }
 
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
    }
    
   /* TabHost tabHost;
    TabWidget tabWidget;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tabHost=(TabHost) findViewById(android.R.id.tabhost);
		tabWidget=(TabWidget) findViewById(android.R.id.tabs);
		
		setTabs() ;
		
		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            
            @Override
            public void onTabChanged(String arg0) {
                // TODO Auto-generated method stub
                
            }
        });
	}
	private void setTabs()
	{
		addTab("Home", R.drawable.tab_home);
		addTab("Search", R.drawable.tab_search);
		
		addTab("Home", R.drawable.tab_home);
		addTab("Search", R.drawable.tab_search);
	}
	
	private void addTab(String labelId, int drawableId)
	{  
		TabHost.TabSpec spec = tabHost.newTabSpec("tab" + labelId);			
		
		//Customizing tab indicator usnig image and text....
		View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabWidget, false);
		TextView title = (TextView) tabIndicator.findViewById(R.id.title);
		title.setText(labelId);
		ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
		icon.setImageResource(drawableId);
		
		//Adding indicator to spec;
		spec.setIndicator(tabIndicator); 
		tabHost.addTab(spec);
	}*/
	
    public View getTabIndicator(String labelId, int drawableId) {

        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator,tabWidget ,false);
        TextView title = (TextView) tabIndicator.findViewById(R.id.title);
        title.setText(labelId);
        ImageView icon = (ImageView) tabIndicator.findViewById(R.id.icon);
        icon.setImageResource(drawableId);

        return tabIndicator;

    }

/**
 * This is a helper class that implements a generic mechanism for
 * associating fragments with the tabs in a tab host.  It relies on a
 * trick.  Normally a tab host has a simple API for supplying a View or
 * Intent that each tab will show.  This is not sufficient for switching
 * between fragments.  So instead we make the content part of the tab host
 * 0dp high (it is not shown) and the TabManager supplies its own dummy
 * view to show as the tab content.  It listens to changes in tabs, and takes
 * care of switch to the correct fragment shown in a separate content area
 * whenever the selected tab changes.
 */
	
public static class TabManager implements TabHost.OnTabChangeListener {
    private final FragmentActivity mActivity;
    private final TabHost mTabHost;
    private final int mContainerId;
    private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
    TabInfo mLastTab;

    static final class TabInfo {
        private final String tag;
        private final Class<?> clss;
        private final Bundle args;
        private Fragment fragment;

        TabInfo(String _tag, Class<?> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }

    public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
        mActivity = activity;
        mTabHost = tabHost;
        mContainerId = containerId;
        mTabHost.setOnTabChangedListener(this);
    }

    public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
        tabSpec.setContent(new DummyTabFactory(mActivity));
        String tag = tabSpec.getTag();

        TabInfo info = new TabInfo(tag, clss, args);

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tag);
        if (info.fragment != null && !info.fragment.isDetached()) {
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            ft.detach(info.fragment);
            ft.commit();
        }

        mTabs.put(tag, info);
        mTabHost.addTab(tabSpec);
    }

    @Override
    public void onTabChanged(String tabId) {
        TabInfo newTab = mTabs.get(tabId);
        if (mLastTab != newTab) {
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
                    ft.detach(mLastTab.fragment);
                }
            }
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(mActivity,
                            newTab.clss.getName(), newTab.args);
                    ft.add(mContainerId, newTab.fragment, newTab.tag);
                } else {
                    ft.attach(newTab.fragment);
                }
            }

            mLastTab = newTab;
            ft.commit();
            mActivity.getSupportFragmentManager().executePendingTransactions();
        }
    }
}
}

