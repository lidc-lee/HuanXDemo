package com.ldc.aa.huanxdemo.ui;

import android.content.Intent;
import android.os.Bundle;

import com.easemob.chat.KefuChatManager;
import com.easemob.easeui.ui.EaseBaseActivity;
import com.ldc.aa.huanxdemo.Constant;
import com.ldc.aa.huanxdemo.R;
import com.ldc.aa.huanxdemo.util.HelpDeskPreferenceUtils;

/**
 * Created by AA on 2016/10/18.
 */

public class ChatActivity extends EaseBaseActivity {
    public static ChatActivity activityInstance;
    private ChatFragment chatFragment;
    String toChatUsername;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.em_activity_chat);
        activityInstance = this;
        // 聊天人或群id
        toChatUsername = HelpDeskPreferenceUtils.getInstance(this).getSettingCustomerAccount();
        // 可以直接new EaseChatFratFragment使用
        chatFragment = new ChatFragment();
        Intent intent = getIntent();
        intent.putExtra(Constant.EXTRA_USER_ID, toChatUsername);
        intent.putExtra(Constant.EXTRA_SHOW_USERNICK, true);
        // 传入参数
        chatFragment.setArguments(intent.getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();
        KefuChatManager.getInstance().bindChatUI(toChatUsername);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityInstance = null;
        KefuChatManager.getInstance().unBind();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // 点击notification bar进入聊天页面，保证只有一个聊天页面
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        chatFragment.onBackPressed();
    }

    public void sendRobotMessage(String txtContent, String menuId) {
        chatFragment.sendRobotMessage(txtContent, menuId);
    }

}
