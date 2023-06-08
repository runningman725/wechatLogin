package com.inbody2014.inbody.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.inbody2014.inbody.R;
import com.inbody2014.inbody.WxData;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private IWXAPI mWeixinAPI;

    private TextView tv_code;
    private TextView tv_nickname;
    private TextView tv_sex;
    private ImageView iv_head;
    private Handler handler;
    private OkHttpClient client;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxentry);
        tv_code = findViewById(R.id.tv_code);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_sex = findViewById(R.id.tv_sex);
        iv_head = findViewById(R.id.iv_head);
        handler = new Handler();
        client = new OkHttpClient();

        mWeixinAPI = WXAPIFactory.createWXAPI(this, WxData.WEIXIN_APP_ID, true);
        mWeixinAPI.handleIntent(this.getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mWeixinAPI.handleIntent(intent, this);//必须调用此句话
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.e("-----", "onReq: " + baseReq);
        finish();
    }


    @Override
    public void onResp(BaseResp baseResp) {
        Log.e("-----", "errStr: " + baseResp.errStr);
        Log.e("-----", "openId: " + baseResp.openId);   //유저의 유일한 id
        Log.e("-----", "transaction: " + baseResp.transaction);
        Log.e("-----", "errCode: " + baseResp.errCode);
        Log.e("-----", "getType: " + baseResp.getType());
        Log.e("-----", "checkArgs: " + baseResp.checkArgs());

        switch (baseResp.errCode) {

            case BaseResp.ErrCode.ERR_AUTH_DENIED:  //유저가 거절
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:  //유저가 취소
                break;
            case BaseResp.ErrCode.ERR_OK:   //유저가 동의
                String code = ((SendAuth.Resp) baseResp).code;  //code를 통하여 access_token를 획득할수 있습니다
                getAccess_token(code);
                Log.e("--------", "code: " + code);
                tv_code.setText(code);
                break;
        }
    }

    /**
     * openid accessToken은 유저 정보 획득시 필요
     *
     * @param code
     */
    private void getAccess_token(final String code) {
        String path = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                + WxData.WEIXIN_APP_ID
                + "&secret="
                + WxData.APP_SECRET
                + "&code="
                + code
                + "&grant_type=authorization_code";
        Request request = new Request.Builder()
                .url(path)
                .build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("-----", "onResponse: " + response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    String openid = jsonObject.getString("openid").toString().trim();   //openid는 유저의 유일한 표시
                    String access_token = jsonObject.getString("access_token").toString().trim();
                    String refresh_token = jsonObject.getString("refresh_token").toString().trim();
                    confirmTokenValidation(access_token, openid);
                    getUserMsg(access_token, openid);
                    refreshTokenMethod(refresh_token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * confirm Access_Token validation
     *
     * @param access_token
     * @param openid
     */
    private void confirmTokenValidation(String access_token, String openid) {
        String path = "https://api.weixin.qq.com/sns/auth?access_token="
                +access_token
                +"&openid="
                +openid;
        Request request = new Request.Builder()
                .url(path)
                .build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("------", "data: " + response);
//                JSONObject jsonObject = null;
//                try {
//                    jsonObject = new JSONObject(response.body().string());
//                    String errcode = jsonObject.getString("errcode");
//                    String errmsg = jsonObject.getString("errmsg");
//                    if(errcode.equals("0")){
//                        //access_token 유효
//                    } else {
//                        //access_token 무효
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//
//                }
                // finish();
            }

        });
    }

    /**
     * refresh_token
     *
     * @param refresh_token
     */
    private void refreshTokenMethod(String refresh_token) {
        String path = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
                +WxData.WEIXIN_APP_ID
                +"&grant_type=refresh_token&refresh_token="
                + refresh_token;
        Request request = new Request.Builder()
                .url(path)
                .build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("-----", "onResponse: " + response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    String openid = jsonObject.getString("openid").toString().trim();   //openid는 유저의 유일한 표시
                    String access_token = jsonObject.getString("access_token").toString().trim();
                    String refresh_token = jsonObject.getString("refresh_token").toString().trim();
                    getUserMsg(access_token, openid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }



    /**
     * 위챗 개인정보 획득
     *
     * @param access_token
     * @param openid
     */
    private void getUserMsg(final String access_token, final String openid) {
        String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
                + access_token
                + "&openid="
                + openid;

        Request request = new Request.Builder()
                .url(path)
                .build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.e("------", "data: " + response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    String nickname = jsonObject.getString("nickname");
                    int sex = Integer.parseInt(jsonObject.get("sex").toString());   //0은 남성
                    String headimgurl = jsonObject.getString("headimgurl");
                    String openid1 = jsonObject.getString("openid");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_nickname.setText(nickname);
                            Log.e("---", "sex:       " + sex);
                            if (sex == 0) {
                                tv_sex.setText("male");
                            } else {
                                tv_sex.setText("female");
                            }
                        }
                    });

                    imageUrl(headimgurl);
                    //    startLoca(nickname, openid1);
                } catch (JSONException e) {
                    e.printStackTrace();

                }
                // finish();
            }

        });

    }


    //이미지 처리
    private void imageUrl(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url1 = new URL(url);
                    HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(2000);
                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == 200) {
                        InputStream inputStream = urlConnection.getInputStream();

                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                iv_head.setImageBitmap(bitmap);
                            }
                        });
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
