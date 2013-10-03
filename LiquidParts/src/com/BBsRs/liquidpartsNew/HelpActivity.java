package com.BBsRs.liquidpartsNew;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.inappbilling.util.IabHelper;
import com.inappbilling.util.IabResult;
import com.inappbilling.util.Inventory;
import com.inappbilling.util.Purchase;

public class HelpActivity extends FragmentActivity {
	
	IabHelper mHelper;
	private static final String TAG = "<Liquid Parts>.inappbilling";
	static final String ITEM_SKU = "donate";
	boolean mIsPremium = false;
	
	int donateFrId;

	private PagerSlidingTabStrip tabs;
	private ViewPager pager;
	private MyPagerAdapter adapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);
	    getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	    getActionBar().setTitle(getResources().getString(R.string.app_name)+": "+getResources().getString(R.string.help));
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setTextColorResource(R.color.white);
		tabs.setIndicatorColorResource(R.color.blue);
		tabs.setDividerColorResource(R.color.divider);
		tabs.setUnderlineColorResource(R.color.blue);
		pager = (ViewPager) findViewById(R.id.pager);
		adapter = new MyPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);

		final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
				.getDisplayMetrics());
		pager.setPageMargin(pageMargin);

		tabs.setViewPager(pager);
	    // TODO Auto-generated method stub
		
    	SharedPreferences settingsTweaks = HelpActivity.this.getSharedPreferences("lpts", Context.MODE_PRIVATE);
		if (settingsTweaks.getBoolean("restartAct", false)){
			SharedPreferences.Editor prefEditor = settingsTweaks.edit();
			prefEditor.putBoolean("restartAct", false);
			prefEditor.commit();
			pager.setCurrentItem(2, false);
		}
		
		/*---------INIT IN APP BILLING------------*/
	 	String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuhk7nb6Z58h2JlOQYLrepaia72vigmdfKdV9UUM2hSStoQGqTdu7/NkSM2W7mzIBVCIshUYQk8Q25b7FZiEdOLohgJZkDVMSB4cdY6C74kHKe2tR/yGcUZKN9iZpoSMSJrKMMwiZJ8+gtaa5sahdZGYsHJ0AGPn1g9Duvsqv4mto1c9cfJOSyci2RqORGKFtMnJe8h5xrcI0OhvIXSKoVcp9GulHkWVoJ4n8AsgG0WI5jkAPBYk5TPiUGfQZmuVXV3eMXgyNfXYW6Bqkb7ZkLzmTIx1xOLDCV4s+RipypeOWy+dcOfwqHaIOEeNuwsRvz9M+wrkIBd0JaD9/GKgwFQIDAQAB";
    	mHelper = new IabHelper(HelpActivity.this, base64EncodedPublicKey);
    
    	mHelper.startSetup(new 
		IabHelper.OnIabSetupFinishedListener() {
    	   	  public void onIabSetupFinished(IabResult result) 
		  {
    	        if (!result.isSuccess()) {
    	           Log.d(TAG, "In-app Billing setup failed: " + 
				result);
    	      } else {             
    	      	    Log.d(TAG, "In-app Billing is set up OK");
    	      	    mHelper.queryInventoryAsync(mGotInventoryListener);			//check if we have premium
	      }
    	   }
    	});
    	/*---------INIT IN APP BILLING END------------*/
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	      switch (item.getItemId()) {
	          case android.R.id.home:
	        	  super. onBackPressed();
	        	  break;
	          default:
	              return super.onOptionsItemSelected(item);

	      }
		return true;
	  }
	
	public class MyPagerAdapter extends FragmentPagerAdapter {

		private final String[] TITLES = getResources().getStringArray(R.array.help);

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {
			if (position!=2)
				return new Help(getResources().getStringArray(R.array.info)[position]);
			else {
				Fragment fr = new Donate(getResources().getStringArray(R.array.info)[position]);
				donateFrId = fr.getId();
				return fr;
			}
			
		}

	}
	
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener 
	= new IabHelper.OnIabPurchaseFinishedListener() {
	public void onIabPurchaseFinished(IabResult result, 
                    Purchase purchase) 
	{
	   if (result.isFailure()) {
	      // Handle error
	      return;
	 }      
	 else if (purchase.getSku().equals(ITEM_SKU)) {
		 	mIsPremium = true;
		 	UpdateState();
	    	SharedPreferences settingsTweaks = HelpActivity.this.getSharedPreferences("lpts", Context.MODE_PRIVATE);
	    	SharedPreferences.Editor prefEditor = settingsTweaks.edit();
			prefEditor.putBoolean("restartAct", true);
			startActivity(new Intent(HelpActivity.this, HelpActivity.class));
			overridePendingTransition(R.anim.nulling_animation, R.anim.nulling_animation);
			prefEditor.commit();
			finish();
	 		}
	      
		}
	};
		
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener
	   = new IabHelper.QueryInventoryFinishedListener() {
		   public void onQueryInventoryFinished(IabResult result,
		      Inventory inventory) {
		      if (result.isFailure()) {
			  // Handle failure
		      } else {
		    	  Log.d(TAG, "Query inventory was successful.");
		          Purchase premiumPurchase = inventory.getPurchase(ITEM_SKU);
		          mIsPremium = (premiumPurchase != null);
		          Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
		          UpdateState();
		      }
	    }
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, 
	     Intent data) 
	{
	      if (!mHelper.handleActivityResult(requestCode, 
	              resultCode, data)) {     
	    	super.onActivityResult(requestCode, resultCode, data);
	      }
	}
	
	public void buyButton(){
		mHelper.launchPurchaseFlow(HelpActivity.this, ITEM_SKU, 10001,   
  			   mPurchaseFinishedListener, "");
	}
	
	public void UpdateState(){
		SharedPreferences settingsTweaks = HelpActivity.this.getSharedPreferences("lpts", Context.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = settingsTweaks.edit();
		prefEditor.putBoolean("paid", mIsPremium);
		prefEditor.commit();
	}
}
