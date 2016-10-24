/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ldc.aa.huanxdemo;

import android.app.Application;
import android.content.Context;

import com.easemob.chat.EMChat;
import com.easemob.chat.KefuChat;
import com.ldc.aa.huanxdemo.util.DemoHelper;
import com.ldc.aa.huanxdemo.util.HelpDeskPreferenceUtils;

public class DemoApplication extends Application {

	public static Context applicationContext;
	private static DemoApplication instance;
	// login user name
	public final String PREF_USERNAME = "username";

	@Override
	public void onCreate() {
		super.onCreate();
		applicationContext = this;
		instance = this;
		//初始化
		EMChat.getInstance().init(applicationContext);
		/**
		 * debugMode == true 时为打开，SDK会在log里输入调试信息
		 * @param debugMode
		 * 在做代码混淆的时候需要设置成false
		 */
		EMChat.getInstance().setDebugMode(true);//在做打包混淆时，要关闭debug模式，避免消
		//代码中设置环信IM的Appkey
		String appkey = HelpDeskPreferenceUtils.getInstance(this).getSettingCustomerAppkey();
		KefuChat.getInstance().setAppkey(appkey);
		// init demo helper
		DemoHelper.getInstance().init(applicationContext);
	}

	public static DemoApplication getInstance() {
		return instance;
	}

}
