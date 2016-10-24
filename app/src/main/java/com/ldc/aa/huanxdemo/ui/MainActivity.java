package com.ldc.aa.huanxdemo.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.easemob.EMCallBack;
import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.EMEventListener;
import com.easemob.EMNotifierEvent;
import com.easemob.chat.EMMessage;
import com.easemob.chat.KefuChatManager;
import com.easemob.easeui.controller.EaseUI;
import com.ldc.aa.huanxdemo.Constant;
import com.ldc.aa.huanxdemo.R;

import java.util.List;

public class MainActivity extends AppCompatActivity implements EMEventListener {
    private TextView tv_login;
    private MyConnectionListener connectionListener=null;
    private EaseUI easeUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_login = (TextView) findViewById(R.id.tv_login);

        init();

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class).putExtra(Constant.MESSAGE_TO_INTENT_EXTRA,
                        Constant.MESSAGE_TO_PRE_SALES));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //register the event listener when enter the foreground
        KefuChatManager.getInstance().registerEventListener(this,
                new EMNotifierEvent.Event[]{EMNotifierEvent.Event.EventNewMessage,
                        EMNotifierEvent.Event.EventOfflineMessage});
    }

    @Override
    protected void onStop() {
        super.onStop();
        KefuChatManager.getInstance().unregisterEventListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectionListener != null) {
            KefuChatManager.getInstance().removeConnectionListener(connectionListener);
        }
    }

    private void init(){
        easeUI = EaseUI.getInstance();

        //注册一个监听连接状态的listener
        connectionListener = new MyConnectionListener();
        KefuChatManager.getInstance().addConnectionListener(connectionListener);
    }

    //region EMEventListener监听事件
    @Override
    public void onEvent(EMNotifierEvent event) {
        switch (event.getEvent()) {
            case EventNewMessage:
                EMMessage message = (EMMessage) event.getData();
                //提示新消息
                easeUI.getNotifier().onNewMsg(message);
                break;
            case EventOfflineMessage:
                //处理离线消息
                List<EMMessage> messages = (List<EMMessage>) event.getData();
                //消息提醒或只刷新UI
                easeUI.getNotifier().onNewMesg(messages);
                break;
            default:
                break;
        }
    }
    //endregion

    //region MyConnectionListener
    public class MyConnectionListener implements EMConnectionListener {

        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (error == EMError.USER_REMOVED) {
                        //账号被移除
                        logout(true, null);
                        if (ChatActivity.activityInstance != null) {
                            ChatActivity.activityInstance.finish();
                        }
                    } else if (error == EMError.CONNECTION_CONFLICT) {
                        //账号在其他地方登录
                        logout(true, null);
                        if (ChatActivity.activityInstance != null) {
                            ChatActivity.activityInstance.finish();
                        }
                    } else {
                        //连接不到服务器


                    }

                }
            });
        }

    }

    /**
     * 退出登陆
     * @param unbindDeviceToken
     * @param callback
     */
    private void logout(boolean unbindDeviceToken, final EMCallBack callback) {
        // 如果需要解绑token,需要调用 KefuChatManager.getInstance.logout(unbindDeviceToken, new EMCallBack(){});
        KefuChatManager.getInstance().logout(new EMCallBack() {

            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onSuccess();
                    Log.e("logout","退出登陆");
                }

            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

            @Override
            public void onError(int code, String error) {
                if (callback != null) {
                    callback.onError(code, error);
                }
            }
        });
    }

    //endregion
}
