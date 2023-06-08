package com.inbody2014.inbody;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WxLogin {

    public static IWXAPI api;
    public static Context mContext;
    private static MyReceiver myReceiver;


    /**
     * @param context
     */
    public static void initWx(Context context) {
        UIUtils.initContext(context);
        mContext = context;
        api = WXAPIFactory.createWXAPI(context, WxData.WEIXIN_APP_ID, true);
        // 앱의 appId를 위챗에 등록
        api.registerApp(WxData.WEIXIN_APP_ID);

        //broadcastreceiver를 관찰하여서 위챗에 등록(필수 아님)
        myReceiver= new MyReceiver();
        context.registerReceiver(myReceiver, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));

    }

    public static void longWx() {
        if (mContext == null) {
            Toast.makeText(mContext, "초기화를 미완성,application에서 initWx(context)를 통하여 초기화를 하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!api.isWXAppInstalled()) {
            Toast.makeText(mContext, "위챗을 다운하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = WxData.SCOPE;
        req.state = WxData.STATE;
        api.sendReq(req);

    }

    public static void destroy(Context context){
        context.unregisterReceiver(myReceiver);
    }

    private static class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            // app를 위챗에 등록
            api.registerApp(WxData.WEIXIN_APP_ID);
        }
    }
}