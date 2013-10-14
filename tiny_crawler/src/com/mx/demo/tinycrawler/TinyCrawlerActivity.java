package com.mx.demo.tinycrawler;

import java.io.File;
import java.net.MalformedURLException;

import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class TinyCrawlerActivity extends Activity {

	public final static int MENU_CMD_ID_RETURN_HOME = Menu.FIRST + 1;
	public final static int MENU_CMD_ID_CRAWL_CURRENT_PAGE = Menu.FIRST + 2;
	public final static int MENU_CMD_ID_CRAWL_SETING = Menu.FIRST + 3;
	public final static int MENU_CMD_ID_CLEAN_DATA = Menu.FIRST + 4;
	public final static int MENU_CMD_ID_QUIT = Menu.FIRST + 5;

	public static final String ACTION_CRAWL_FINISHED = "tinycrawl_FINISHED";
	private String mHomePageUrl = "file:///android_asset/home/home.html";

	private WebView mWebView = null;
	private BroadcastReceiver mReceiver;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		String loadUrl = null;
		if (getIntent() != null) {
			loadUrl = getIntent().getDataString();
		} else {
			loadUrl = mHomePageUrl;
		}

		if (TextUtils.isEmpty(loadUrl)) {
			loadUrl = mHomePageUrl;
		}
		mReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(ACTION_CRAWL_FINISHED)){
					if(mWebView!=null && mWebView.getUrl()!=null && mWebView.getUrl().startsWith(mHomePageUrl)
							&&mWebView.getUrl().endsWith("home.html")){
						mWebView.loadUrl(mHomePageUrl);
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter(ACTION_CRAWL_FINISHED);
		registerReceiver(mReceiver, filter);
		setupWebView();
		setContentView(mWebView);
		mWebView.loadUrl(loadUrl);

	}

	@Override
	protected void onResume() {
		if (mWebView != null && mWebView.getUrl() != null
				&& mWebView.getUrl().startsWith(mHomePageUrl)) {
			Log.i("crawl", this.getClass().getSimpleName() + " onResume,url="
					+ mWebView.getUrl());
			mWebView.loadUrl(mHomePageUrl);
		}
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		// 判断是否是搜索请求
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// 获取搜索的查询内容（关键字）
			String query = intent.getStringExtra(SearchManager.QUERY);
			// 执行相应的查询动作
			String search = "http://m.baidu.com/s?word=" + query;
			mWebView.loadUrl(search);
		} else {
			String url = intent.getDataString();
			mWebView.loadUrl(url);
		}
	}

	private void setupWebView() {
		mWebView = new WebView(this);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new CrawlerJsObj(this), "crawler");

		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
				setProgress(newProgress);
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				// TODO Auto-generated method stub
				super.onReceivedTitle(view, title);
				if (view.getUrl().startsWith("file://")) {
					setTitle("[" + getString(R.string.page_status_offline)
							+ "]" + title);
				} else {
					setTitle(title);
				}
			}

		});

		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				setProgressBarVisibility(true);
				setProgressBarIndeterminateVisibility(true);

				// view.loadUrl("javascript:document.body.innerHTML+='<h1>hello</h1>'");

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				Log.i("crawl", "should override url:"+url);
				if (TinyCrawler.getInstance().hasOfflineFile(url)) {
					try {
						String offlineuri = TinyCrawler.getInstance()
								.urlToLocalFileUri(url);
						// return
						// super.shouldOverrideUrlLoading(view,offlineuri);
						view.loadUrl(offlineuri);
						return true;
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return super.shouldOverrideUrlLoading(view, url);
					}
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}

			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				setProgressBarVisibility(false);
				setProgressBarIndeterminateVisibility(false);

				// view.loadUrl("javascript:document.body.innerHTML+= 'abc'");
			}
		});
		// mWebView.setEmbeddedTitleBar(mTitleBar);
		// mWebView.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_CMD_ID_RETURN_HOME, 1, R.string.menu_home);
		menu.add(Menu.NONE, MENU_CMD_ID_CRAWL_CURRENT_PAGE, 2,
				R.string.menu_do_crawl);
		// menu.add(Menu.NONE, MENU_CMD_ID_CRAWL_SETING,3,
		// R.string.menu_setting);
		// menu.add(Menu.NONE, MENU_CMD_ID_CLEAN_DATA,4,
		// R.string.menu_clean_data);
		menu.add(Menu.NONE, MENU_CMD_ID_QUIT, 5, R.string.menu_quit);
		// menu.add(Menu.NONE, Menu.FIRST + 3, 5, "stop");
		// menu.add(Menu.NONE, Menu.FIRST + 4, 5, "seting");
		// menu.add(Menu.NONE, Menu.FIRST + 3, 5, "crawl");
		// menu.add(Menu.NONE, Menu.FIRST + 4, 5, "crawl");
		return true;
	}

	// @Override
	// public boolean onSearchRequested() {
	// Bundle appDataBundle = new Bundle();
	// appDataBundle.putString("search", "开始搜索");
	// startSearch("搜索", false, appDataBundle, false);
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case MENU_CMD_ID_RETURN_HOME:
			mWebView.loadUrl(mHomePageUrl);
			break;
		case MENU_CMD_ID_CRAWL_CURRENT_PAGE:

			try {
				String url = mWebView.getUrl();
				Log.i("crawl", "url = " + url);
				if (TextUtils.isEmpty(url) || url.startsWith(mHomePageUrl)) {
					Toast.makeText(this, R.string.toast_unsupported_url,
							Toast.LENGTH_LONG).show();
					return true;
				}
				ConnectivityManager manager = (ConnectivityManager) this
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkinfo = manager.getActiveNetworkInfo();
				if (networkinfo != null) {
					if (CrawlSettings.getInstance().mJustCrawlInWifiType
							&& networkinfo.getType() != ConnectivityManager.TYPE_WIFI) {
						Toast.makeText(this, R.string.toast_crawl_in_wifi,
								Toast.LENGTH_LONG).show();
						return true;
					}
				}

				TinyCrawler.getInstance().crawl(mWebView.getUrl(),
						CrawlSettings.getInstance().mCrawlLevel,
						mWebView.getTitle());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case MENU_CMD_ID_CLEAN_DATA:

			// deleteDir(TinyCrawler.getInstance().mBaseDir);
			break;
		case MENU_CMD_ID_CRAWL_SETING:
			Intent i = new Intent();
			i.setClass(this, CrawlPreferenceActivity.class);
			startActivity(i);
			break;
		case MENU_CMD_ID_QUIT:
			finish();
		default:
			return false;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mWebView.getUrl().startsWith(mHomePageUrl)){
				Log.i("crawl", "url="+mWebView.getUrl());
				finish();
				return true;
			}
			if (mWebView.canGoBack()) {
				WebBackForwardList list = mWebView.copyBackForwardList();
				int cur = list.getCurrentIndex();
				int back = cur-1;
				WebHistoryItem item = list.getItemAtIndex(back);
				if(item.getUrl().startsWith(mHomePageUrl)){
					mWebView.loadUrl(mHomePageUrl);
					return true;
				}
				mWebView.goBack();
				return true;
			}
		}
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService(new Intent(this, CrawlerService.class));
		unregisterReceiver(mReceiver);
		// kill my self
		int pid = android.os.Process.myPid();
		android.os.Process.killProcess(pid);
	}
}