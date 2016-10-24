package com.ldc.aa.huanxdemo.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.easemob.chat.EMMessage;
import com.easemob.chat.KefuChatManager;
import com.easemob.chat.TextMessageBody;
import com.easemob.easeui.ui.EaseChatFragment;
import com.easemob.easeui.widget.chatrow.EaseChatRow;
import com.easemob.easeui.widget.chatrow.EaseCustomChatRowProvider;
import com.ldc.aa.huanxdemo.Constant;
import com.ldc.aa.huanxdemo.R;
import com.ldc.aa.huanxdemo.chatrow.ChatRowEvaluation;
import com.ldc.aa.huanxdemo.chatrow.ChatRowPictureText;
import com.ldc.aa.huanxdemo.chatrow.ChatRowRobotMenu;
import com.ldc.aa.huanxdemo.chatrow.ChatRowTransferToKefu;
import com.ldc.aa.huanxdemo.util.DemoHelper;
import com.ldc.aa.huanxdemo.util.HelpDeskPreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class ChatFragment extends EaseChatFragment implements EaseChatFragment.EaseChatFragmentListener {

	// 避免和基类定义的常量可能发生的冲突，常量从11开始定义
	private static final int ITEM_FILE = 11;
	private static final int ITEM_SHORT_CUT_MESSAGE = 12;
	public static final int REQUEST_CODE_CONTEXT_MENU = 14;

	private static final int MESSAGE_TYPE_SENT_PICTURE_TXT = 1;
	private static final int MESSAGE_TYPE_RECV_PICTURE_TXT = 2;
	private static final int MESSAGE_TYPE_SENT_ROBOT_MENU = 3;
	private static final int MESSAGE_TYPE_RECV_ROBOT_MENU = 4;

	// evaluation
	private static final int MESSAGE_TYPE_SENT_EVAL = 5;
	private static final int MESSAGE_TYPE_RECV_EVAL = 6;

	// transfer to kefu message
	private static final int MESSAGE_TYPE_SENT_TRANSFER_TO_KEFU = 7;
	private static final int MESSAGE_TYPE_RECV_TRANSFER_TO_KEFU = 8;


	private static final int REQUEST_CODE_SELECT_FILE = 11;
	//EVALUATION
	public static final int REQUEST_CODE_EVAL = 26;
	//SHORT CUT MESSAGES
	public static final int REQUEST_CODE_SHORTCUT = 27;

	//从详情进来的，发送轨迹跟踪
	private int imgSelectedIndex = Constant.INTENT_CODE_IMG_SELECTED_DEFAULT;
	protected int messageToIndex = Constant.MESSAGE_TO_DEFAULT;

	protected String currentUserNick;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//在父类中调用了initView和setUpView两个方法
		super.onActivityCreated(savedInstanceState);
		//判断是默认，还是用技能组（售前、售后）
		messageToIndex = fragmentArgs.getInt(Constant.MESSAGE_TO_INTENT_EXTRA, Constant.MESSAGE_TO_DEFAULT);
		currentUserNick = HelpDeskPreferenceUtils.getInstance(getActivity()).getSettingCurrentNick();

		messageList.setShowUserNick(true);
	}

	@Override
	protected void setUpView() {
		setChatFragmentListener(this);
		super.setUpView();
		titleBar.setTitle("我的客服");
		titleBar.setRightLayoutVisibility(View.GONE);
		titleBar.setLeftImageResource(R.mipmap.em_btn_back);

	}

	@Override
	protected void registerExtendMenuItem() {
		// demo这里不覆盖基类已经注册的item,item点击listener沿用基类的
//		super.registerExtendMenuItem();
		for(int i = 0; i < itemStrings.length-1; i++){
			inputMenu.registerExtendMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], extendMenuItemClickListener);
		}
	}
	@SuppressLint("NewApi")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_CONTEXT_MENU) {
			switch (resultCode) {
				case ContextMenuActivity.RESULT_CODE_COPY: // 复制消息
					clipboard.setText(((TextMessageBody) contextMenuMessage.getBody()).getMessage());
					break;
				case ContextMenuActivity.RESULT_CODE_DELETE: // 删除消息
					conversation.removeMessage(contextMenuMessage.getMsgId());
					messageList.refresh();
					break;
				default:
					break;
			}
		}
	}

	// 设置消息扩展属性
	@Override
	public void onSetMessageAttributes(EMMessage message) {

		//设置用户信息（昵称，qq等）
		setUserInfoAttribute(message);
		switch (messageToIndex) {
		case Constant.MESSAGE_TO_PRE_SALES:
			pointToSkillGroup(message, "shouqian");
			break;
		case Constant.MESSAGE_TO_AFTER_SALES:
			pointToSkillGroup(message, "shouhou");
			break;
		default:
			break;
		}
	}

	@Override
	public void onEnterToChatDetails() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAvatarClick(String username) {

	}

	@Override
	public boolean onMessageBubbleClick(EMMessage message) {
		// 消息框点击事件，demo这里不做覆盖，如需覆盖，return true
		return false;
	}

	@Override
	public void onMessageBubbleLongClick(EMMessage message) {
		// 消息框长按
		startActivityForResult((new Intent(getActivity(), ContextMenuActivity.class)).putExtra("message", message),
				REQUEST_CODE_CONTEXT_MENU);
	}

	@Override
	public boolean onExtendMenuItemClick(int itemId, View view) {

		return false;
	}

	@Override
	public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
		// 设置自定义listview item提供者
		return new CustomChatRowProvider();
	}

	/**
	 * chat row provider
	 * 
	 */
	private final class CustomChatRowProvider implements EaseCustomChatRowProvider {
		@Override
		public int getCustomChatRowTypeCount() {
			//此处返回的数目为getCustomChatRowType 中的布局的个数
			return 8;
		}

		@Override
		public int getCustomChatRowType(EMMessage message) {
			if (message.getType() == EMMessage.Type.TXT) {
				if (DemoHelper.getInstance().isRobotMenuMessage(message)) {
					// 机器人 列表菜单
					return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_ROBOT_MENU
							: MESSAGE_TYPE_SENT_ROBOT_MENU;
				} else if (DemoHelper.getInstance().isEvalMessage(message)) {
					// 满意度评价
					return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_EVAL : MESSAGE_TYPE_SENT_EVAL;
				} else if (DemoHelper.getInstance().isPictureTxtMessage(message)) {
					// 订单图文组合
					return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_PICTURE_TXT
							: MESSAGE_TYPE_SENT_PICTURE_TXT;
				} else if(DemoHelper.getInstance().isTransferToKefuMsg(message)){
					//转人工消息
					return message.direct == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_RECV_TRANSFER_TO_KEFU
							: MESSAGE_TYPE_SENT_TRANSFER_TO_KEFU;
				}
			}
			return 0;
		}

		@Override
		public EaseChatRow getCustomChatRow(EMMessage message, int position, BaseAdapter adapter) {
			if (message.getType() == EMMessage.Type.TXT) {
				if (DemoHelper.getInstance().isRobotMenuMessage(message)) {
					return new ChatRowRobotMenu(getActivity(), message, position, adapter);
				} else if (DemoHelper.getInstance().isEvalMessage(message)) {
					return new ChatRowEvaluation(getActivity(), message, position, adapter);
				} else if (DemoHelper.getInstance().isPictureTxtMessage(message)) {
					return new ChatRowPictureText(getActivity(), message, position, adapter);
				}else if (DemoHelper.getInstance().isTransferToKefuMsg(message)){
					return new ChatRowTransferToKefu(getActivity(), message, position, adapter);
				}
			}
			return null;
		}
	}


	/**
	 * 设置用户的属性，
	 * 通过消息的扩展，传递客服系统用户的属性信息
	 * @param message
	 */
	private void setUserInfoAttribute(EMMessage message) {
		if (TextUtils.isEmpty(currentUserNick)) {
			currentUserNick = KefuChatManager.getInstance().getCurrentUser();
		}
		JSONObject weichatJson = getWeichatJSONObject(message);
		try {
			JSONObject visitorJson = new JSONObject();
			visitorJson.put("userNickname", currentUserNick);
			visitorJson.put("trueName", currentUserNick);
			visitorJson.put("qq", "10000");
			visitorJson.put("phone", "13512345678");
			visitorJson.put("companyName", "环信");
			visitorJson.put("description", "");
			visitorJson.put("email", "abc@123.com");
			weichatJson.put("visitor", visitorJson);

			message.setAttribute("weichat", weichatJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void setVisitorInfoSrc(EMMessage message){
		//传递用户的属性到自定义的iframe界面
		String strName = "name-test from hxid:" + KefuChatManager.getInstance().getCurrentUser();
		JSONObject cmdJson = new JSONObject();
		try {
			JSONObject updateVisitorInfosrcJson = new JSONObject();
			JSONObject paramsJson = new JSONObject();
			paramsJson.put("name", strName);
			updateVisitorInfosrcJson.put("params", paramsJson);
			cmdJson.put("updateVisitorInfoSrc", updateVisitorInfosrcJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		message.setAttribute("cmd", cmdJson);
	}
	
	/**
	 * 获取消息中的扩展 weichat是否存在并返回jsonObject
	 * @param message
	 * @return
	 */
	private JSONObject getWeichatJSONObject(EMMessage message){
		JSONObject weichatJson = null;
		try {
			String weichatString = message.getStringAttribute("weichat", null);
			if(weichatString == null){
				weichatJson = new JSONObject();
			}else{
				weichatJson = new JSONObject(weichatString);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return weichatJson;
	}
	
	/**
	 * 指向某个具体客服，
	 * @param message 消息
	 * @param agentUsername 客服的登录账号
	 */
	private void pointToAgentUser(EMMessage message, String agentUsername){
		try {
			JSONObject weichatJson = getWeichatJSONObject(message);
			weichatJson.put("agentUsername", agentUsername);
			message.setAttribute("weichat", weichatJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 技能组（客服分组）发消息发到某个组
	 * @param message 消息
	 * @param groupName 分组名称
	 */
	private void pointToSkillGroup(EMMessage message, String groupName){
		try {
			JSONObject weichatJson = getWeichatJSONObject(message);
			weichatJson.put("queueName", groupName);
			message.setAttribute("weichat", weichatJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	public void sendRobotMessage(String content, String menuId) {
		EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
		if (!TextUtils.isEmpty(menuId)) {
			JSONObject msgTypeJson = new JSONObject();
			try {
				JSONObject choiceJson = new JSONObject();
				choiceJson.put("menuid", menuId);
				msgTypeJson.put("choice", choiceJson);
			} catch (Exception e) {
			}
			message.setAttribute("msgtype", msgTypeJson);
		}
		sendMessage(message);
	}
}
