package com.mx.app.weibo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mx.app.weibo.RPCHelper.User;
import com.mx.app.weibo.tasks.LoginTask;
import com.mx.app.weibo.tasks.SearchContentByKeywordsAndAtMessage;
import com.mx.app.weibo.tasks.SearchContentByKeywordsAndFllow;
import com.mx.app.weibo.tasks.SearchUserByKeywordsAndAtMessage;
import com.mx.app.weibo.tasks.SearchUserByKeywordsAndFllow;
import com.mx.app.weibo.tasks.UnfllowUserTask;

public class WeiboToolsActivity extends Activity {
    protected static final int LOGIN_TASK = 0x1000;	//登陆
	protected static final int UNFLLOW_TASK = 0x1001;  //取消关注
	private static final int FLLOW_BY_USER_KEYWORDS_TASK = 0x1002; //根据用户关键字进行关注
	private static final int FLLOW_BY_CONTENT_KEYWORDS_TASK = 0x1003; //根据微薄内容进行关注
	private static final int POST_AND_AT_SEARCHE_CONTENT = 0x1004; //搜索内容，并且@消息给发布内容的用户
	private static final int POST_AND_AT_SEARCHE_USER = 0x1005; //搜索用户，并且@消息给发布内容的用户
	
	
	
	protected static final int SEARCH_USER_AND_FALLOW = 0;
	protected static final int SEARCH_CONTENT_FALLOW = 1;
	protected static final int SEARCH_USER_AT = 3;
	protected static final int SEARCH_CONTENT_AT = 4;
	
	/** Called when the activity is first created. */
    
    private User mCurUser = null;
    
    private Handler mHandler = new Handler (){
    	public void handleMessage(android.os.Message msg) {
    		if(msg.what == LOGIN_TASK) {
    			if(msg.arg1 == MxAsyncTaskRequest.TASK_STATUS_FINISHED) {
    				User user = (User) msg.obj;
    				if(user != null) {
						loginSuccess(user);
				    	final Button loginBtn = (Button) findViewById(R.id.btn_login);
				    	loginBtn.setText(R.string.logout);  
						notifyStopProcess(user.nick);
    				}
    			} 
    		} else if(msg.what == FLLOW_BY_USER_KEYWORDS_TASK) {
    			if(msg.arg1 == MxAsyncTaskRequest.TASK_STATUS_FINISHED) {
        			findViewById(R.id.btn_search_user).setEnabled(true);
        			notifyStopProcess(R.string.status_task_finished);
    			}

    		} else if(msg.what == FLLOW_BY_CONTENT_KEYWORDS_TASK) {
    			if(msg.arg1 == MxAsyncTaskRequest.TASK_STATUS_FINISHED) {
        			findViewById(R.id.btn_search_content).setEnabled(true);
        			notifyStopProcess(R.string.status_task_finished);
    			}

    		} else if(msg.what == UNFLLOW_TASK) {
    			if(msg.arg1 == MxAsyncTaskRequest.TASK_STATUS_FINISHED) {
    	  			findViewById(R.id.btn_unfllow_expire_user).setEnabled(true);
    	  			notifyStopProcess(R.string.status_task_finished);
    			}
    		} else if(msg.what == POST_AND_AT_SEARCHE_CONTENT) {
    			if(msg.arg1 == MxAsyncTaskRequest.TASK_STATUS_FINISHED) {
    	  			findViewById(R.id.btn_post_and_at_search_content).setEnabled(true);
    	  			notifyStopProcess(R.string.status_task_finished);
    			}
    		} else if(msg.what == POST_AND_AT_SEARCHE_USER) {
    			if(msg.arg1 == MxAsyncTaskRequest.TASK_STATUS_FINISHED) {
    	  			findViewById(R.id.btn_post_and_at_search_user).setEnabled(true);
        			notifyStopProcess(R.string.status_task_finished);
    			}
    		}
    	}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); 
        getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, R.layout.progress); 
        
        setContentView(R.layout.main);
        final Button loginBtn = (Button) findViewById(R.id.btn_login);
        final String loginStr = getResources().getString(R.string.login);
        final String logoutStr = getResources().getString(R.string.logout);
    	loginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		
				if(loginBtn.getText().equals(loginStr)) {
					showLoginDlg();  
				} else {
					logout();
				}
				
			}
		});
        if(hasLogin()) {
        	loginBtn.setText(logoutStr);
        	mCurUser = new User();
        	mCurUser.gsid = PreferenceManager
    				.getDefaultSharedPreferences(this).getString("user.gsid", "");
        	
        	mCurUser.nick = PreferenceManager
    				.getDefaultSharedPreferences(this).getString("user.nick", "");
        	
        	mCurUser.uid = PreferenceManager
    				.getDefaultSharedPreferences(this).getString("user.uid", "");
        	setTitle(mCurUser.nick);
        	Log.i("test", "user:" + mCurUser.toString());
        } else {
        	loginBtn.setText(loginStr);
        }
        findViewById(R.id.btn_search_content).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSearchDlg(SEARCH_CONTENT_FALLOW);
			}
		});
        
        findViewById(R.id.btn_search_user).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSearchDlg(SEARCH_USER_AND_FALLOW);
			}
		});
        
        findViewById(R.id.btn_post_and_at_search_content).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showSearchDlg(SEARCH_CONTENT_AT);
 			}
 		});
        
        findViewById(R.id.btn_post_and_at_search_user).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showSearchDlg(SEARCH_USER_AT);
 			}
 		});
        
        findViewById(R.id.btn_unfllow_expire_user).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
//				final int pageNum =  PreferenceManager.getDefaultSharedPreferences(WeiboToolsActivity.this).getInt(mCurUser.nick + ".pn", 1);
				final int pageNum = 1;
				Toast.makeText(WeiboToolsActivity.this, "unfllow user....", Toast.LENGTH_LONG).show();
				notifyProcess("unfllow user page [" + pageNum + "]");
				final Button unfllowUserbtn = (Button) findViewById(R.id.btn_unfllow_expire_user);
				unfllowUserbtn.setEnabled(false);
				UnfllowUserTask unfllowTask = new UnfllowUserTask(WeiboToolsActivity.this,mCurUser,mHandler,UNFLLOW_TASK);
				MxTaskManager.getInstance().executeTask(unfllowTask);
			}
		});

    }
    
    private void logout() {

    	SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
    	p.edit().putString("user.gsid", "").putString("user.nick", "").putString("user.uid", "").commit();
    	final Button loginBtn = (Button) findViewById(R.id.btn_login);
    	loginBtn.setText(R.string.login);  
    	setTitle(getApplicationInfo().name);
    	Toast.makeText(WeiboToolsActivity.this, "user has logout", Toast.LENGTH_LONG).show();
    	
    }
    
    private void notifyProcess(String text) {
        setProgressBarIndeterminateVisibility(true); 
        setTitle(text);
    }
    
    private void notifyStopProcess(int resid) {
    	notifyStopProcess(getResources().getString(resid));
    }
    
    private void notifyStopProcess(String text) {
    	if(TextUtils.isEmpty(text)) {
        	setTitle(getApplication().getApplicationInfo().name);
    	} else {
    		setTitle(text);
    	}
    	setProgressBarIndeterminateVisibility(false);
    }
    //保存登陆成功信息
    private void loginSuccess(User user) {
    	Log.i("test", "user:" + user);
    	SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
    	p.edit().putString("user.gsid", user.gsid).putString("user.nick", user.nick).putString("user.uid", user.uid).commit();
    	
    }

    private boolean hasLogin() {
    	SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
    	return !TextUtils.isEmpty(p.getString("user.gsid", ""));
    }
    
    private void showSearchDlg(final int type) {
    	final View searchView = View.inflate(WeiboToolsActivity.this, R.layout.search_dlg, null);
		AlertDialog dlg = new AlertDialog.Builder(WeiboToolsActivity.this).setTitle("search")
				.setView(searchView).setPositiveButton(getResources().getString(R.string.search_btn), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
						final EditText keywords = (EditText) searchView.findViewById(R.id.keywords);
						String searchkeywords = keywords.getText().toString();
						MxAsyncTaskRequest searchTask = null;
						if(type == SEARCH_USER_AND_FALLOW) {
							WeiboToolsActivity.this.findViewById(R.id.btn_search_user).setEnabled(false);
							Toast.makeText(WeiboToolsActivity.this, "do search user", Toast.LENGTH_LONG).show();
							notifyProcess("正在搜索用户自动关注...");
							searchTask = new SearchUserByKeywordsAndFllow(WeiboToolsActivity.this, mCurUser, searchkeywords, mHandler, FLLOW_BY_USER_KEYWORDS_TASK);
						} else if(type == SEARCH_CONTENT_FALLOW){
							WeiboToolsActivity.this.findViewById(R.id.btn_search_content).setEnabled(false);
							notifyProcess("正在搜索内容自动关注...");
							Toast.makeText(WeiboToolsActivity.this, "do search content", Toast.LENGTH_LONG).show();
							searchTask = new SearchContentByKeywordsAndFllow(WeiboToolsActivity.this, mCurUser, searchkeywords, mHandler, FLLOW_BY_CONTENT_KEYWORDS_TASK);
						} else if(type == SEARCH_CONTENT_AT) {
							WeiboToolsActivity.this.findViewById(R.id.btn_post_and_at_search_content).setEnabled(false);
							notifyProcess("正在搜索内容并发送消息@用户...");
							searchTask = new  SearchContentByKeywordsAndAtMessage(WeiboToolsActivity.this, mCurUser, searchkeywords, mHandler, POST_AND_AT_SEARCHE_CONTENT);
						} else if(type == SEARCH_USER_AT) {
							WeiboToolsActivity.this.findViewById(R.id.btn_post_and_at_search_user).setEnabled(false);
							searchTask = new  SearchUserByKeywordsAndAtMessage(WeiboToolsActivity.this, mCurUser, searchkeywords, mHandler, POST_AND_AT_SEARCHE_USER);
							notifyProcess("正在搜索用户并发送消息@用户...");
						}
						
						MxTaskManager.getInstance().executeTask(searchTask);
					}
					
				}).create();
		dlg.show();
    }
    
    
    
    private void showLoginDlg() {
    	final View loginView = View.inflate(WeiboToolsActivity.this, R.layout.login_dlg, null); 
		AlertDialog dlg = new AlertDialog.Builder(WeiboToolsActivity.this)  
		                  .setTitle(getResources().getString(R.string.login_dlg_title))  
		                  .setView(loginView)
		                  .setPositiveButton(getResources().getString(R.string.login_btn_ok),new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								Toast.makeText(WeiboToolsActivity.this, "do login", Toast.LENGTH_LONG).show();
								notifyProcess("logining...");
								EditText userName = (EditText) loginView.findViewById(R.id.username);
								EditText passwd = (EditText) loginView.findViewById(R.id.password);
								String name = userName.getText().toString();
								String pass = passwd.getText().toString();
								MxAsyncTaskRequest loginTask = new LoginTask(WeiboToolsActivity.this,mHandler, name, pass, LOGIN_TASK);
								MxTaskManager.getInstance().executeTask(loginTask);
								
							}
						}).create();
		dlg.show();
		                 
					
    }
}