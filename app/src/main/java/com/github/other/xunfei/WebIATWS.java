package com.github.other.xunfei;

import android.os.Handler;
import android.os.Message;

import com.github.tvbox.osc.util.LOG;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
//import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 语音听写流式 WebAPI 接口调用示例 接口文档（必看）：https://doc.xfyun.cn/rest_api/语音听写（流式版）.html
 * webapi 听写服务参考帖子（必看）：http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=38947&extra=
 * 语音听写流式WebAPI 服务，热词使用方式：登陆开放平台https://www.xfyun.cn/后，找到控制台--我的应用---语音听写---个性化热词，上传热词
 * 注意：热词只能在识别的时候会增加热词的识别权重，需要注意的是增加相应词条的识别率，但并不是绝对的，具体效果以您测试为准。
 * 错误码链接：https://www.xfyun.cn/document/error-code （code返回错误码时必看）
 * 语音听写流式WebAPI 服务，方言或小语种试用方法：登陆开放平台https://www.xfyun.cn/后，在控制台--语音听写（流式）--方言/语种处添加
 * 添加后会显示该方言/语种的参数值
 * @author iflytek
 */

public class WebIATWS extends WebSocketListener {
    private static final String hostUrl = "https://iat-api.xfyun.cn/v2/iat"; //中英文，http url 不支持解析 ws/wss schema
    // private static final String hostUrl = "https://iat-niche-api.xfyun.cn/v2/iat";//小语种
    private static String appid = ""; //在控制台-我的应用获取
    private static String apiSecret = ""; //在控制台-我的应用-语音听写（流式版）获取
    private static String apiKey = ""; //在控制台-我的应用-语音听写（流式版）获取
    private static String filePath = "raw/iat/16k_10.pcm"; // 中文
    public static final int StatusFirstFrame = 0;
    public static final int StatusContinueFrame = 1;
    public static final int StatusLastFrame = 2;
    public static final Gson json = new Gson();
    Decoder decoder = new Decoder();
    // 开始时间
    private static Date dateBegin = new Date();
    // 结束时间
    private static Date dateEnd = new Date();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
    // 外部事件
    private static Handler.Callback Callback;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        new Thread(()->{
            //连接成功，开始发送数据
            int frameSize = 1280; //每一帧音频的大小,建议每 40ms 发送 122B
            int intervel = 40;
            int status = 0;  // 音频的状态
            LOG.d("WebIATWS", "讯飞上传识别", filePath);
            try (FileInputStream fs = new FileInputStream(filePath)) {
                byte[] buffer = new byte[frameSize];
                // 发送音频
                end:
                while (true) {
                    int len = fs.read(buffer);
                    if (len == -1) {
                        status = StatusLastFrame;  //文件读完，改变status 为 2
                    }
                    switch (status) {
                        case StatusFirstFrame:   // 第一帧音频status = 0
                            JsonObject frame = new JsonObject();
                            JsonObject business = new JsonObject();  //第一帧必须发送
                            JsonObject common = new JsonObject();  //第一帧必须发送
                            JsonObject data = new JsonObject();  //每一帧都要发送
                            // 填充common
                            common.addProperty("app_id", appid);
                            //填充business
                            business.addProperty("language", "zh_cn");
                            //business.addProperty("language", "en_us");//英文
                            //business.addProperty("language", "ja_jp");//日语，在控制台可添加试用或购买
                            //business.addProperty("language", "ko_kr");//韩语，在控制台可添加试用或购买
                            //business.addProperty("language", "ru-ru");//俄语，在控制台可添加试用或购买
                            business.addProperty("domain", "iat");
                            business.addProperty("accent", "mandarin");//中文方言请在控制台添加试用，添加后即展示相应参数值
                            business.addProperty("nunum", 1); //阿拉伯数字 0关闭 1开启-默认
                            business.addProperty("ptt", 0);//标点符号 0关闭 1开启-默认
                            //business.addProperty("rlang", "zh-hk"); // zh-cn :简体中文（默认值）zh-hk :繁体香港(若未授权不生效，在控制台可免费开通)
                            //business.addProperty("vinfo", 1);
                            business.addProperty("dwa", "wpgs");//动态修正(若未授权不生效，在控制台可免费开通)
                            //business.addProperty("nbest", 5);// 句子多候选(若未授权不生效，在控制台可免费开通)
                            //business.addProperty("wbest", 3);// 词级多候选(若未授权不生效，在控制台可免费开通)
                            //填充data
                            data.addProperty("status", StatusFirstFrame);
                            data.addProperty("format", "audio/L16;rate=16000");
                            data.addProperty("encoding", "raw");
                            data.addProperty("audio", Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, len)));
                            //填充frame
                            frame.add("common", common);
                            frame.add("business", business);
                            frame.add("data", data);
                            webSocket.send(frame.toString());
                            status = StatusContinueFrame;  // 发送完第一帧改变status 为 1
                            break;
                        case StatusContinueFrame:  //中间帧status = 1
                            JsonObject frame1 = new JsonObject();
                            JsonObject data1 = new JsonObject();
                            data1.addProperty("status", StatusContinueFrame);
                            data1.addProperty("format", "audio/L16;rate=16000");
                            data1.addProperty("encoding", "raw");
                            data1.addProperty("audio", Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, len)));
                            frame1.add("data", data1);
                            webSocket.send(frame1.toString());
                            // System.out.println("send continue");
                            break;
                        case StatusLastFrame:    // 最后一帧音频status = 2 ，标志音频发送结束
                            JsonObject frame2 = new JsonObject();
                            JsonObject data2 = new JsonObject();
                            data2.addProperty("status", StatusLastFrame);
                            data2.addProperty("audio", "");
                            data2.addProperty("format", "audio/L16;rate=16000");
                            data2.addProperty("encoding", "raw");
                            frame2.add("data", data2);
                            webSocket.send(frame2.toString());
                            System.out.println("sendlast");
                            break end;
                    }
                    Thread.sleep(intervel); //模拟音频采样延时
                }
                System.out.println("all data is send");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        //System.out.println(text);
        ResponseData resp = json.fromJson(text, ResponseData.class);
        if (resp != null) {
            if (resp.getCode() != 0) {
                System.out.println( "code=>" + resp.getCode() + " error=>" + resp.getMessage() + " sid=" + resp.getSid());
                System.out.println( "错误码查询链接：https://www.xfyun.cn/document/error-code");
                return;
            }
            if (resp.getData() != null) {
                if (resp.getData().getResult() != null) {
                    Text te = resp.getData().getResult().getText();
                    //System.out.println(te.toString());
                    try {
                        decoder.decode(te);
                        System.out.println("中间识别结果 ==》" + decoder.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (resp.getData().getStatus() == 2) {
                    // todo  resp.data.status ==2 说明数据全部返回完毕，可以关闭连接，释放资源
                    System.out.println("session end ");
                    dateEnd = new Date();
                    System.out.println(sdf.format(dateBegin) + "开始");
                    System.out.println(sdf.format(dateEnd) + "结束");
                    System.out.println("耗时:" + (dateEnd.getTime() - dateBegin.getTime()) + "ms");
                    System.out.println("最终识别结果 ==》" + decoder.toString());
                    System.out.println("本次识别sid ==》" + resp.getSid());
                    if (WebIATWS.Callback!=null){
                        Message message = new Message();
                        message.obj = decoder.toString();
                        WebIATWS.Callback.handleMessage(message);
                    }
                    decoder.discard();
                    webSocket.close(1000, "");
                } else {
                    // todo 根据返回的数据处理
                }
            }
        }
    }
    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        try {
            if (null != response) {
                int code = response.code();
                System.out.println("onFailure code:" + code);
                System.out.println("onFailure body:" + response.body().string());
                if (101 != code) {
                    System.out.println("connection failed");
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                append("date: ").append(date).append("\n").//
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        //System.out.println(builder);
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        //System.out.println(sha);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        //System.out.println(authorization);
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(charset))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();
        return httpUrl.toString();
    }
    public static class ResponseData {
        private int code;
        private String message;
        private String sid;
        private Data data;
        public int getCode() {
            return code;
        }
        public String getMessage() {
            return this.message;
        }
        public String getSid() {
            return sid;
        }
        public Data getData() {
            return data;
        }
    }
    public static class Data {
        private int status;
        private Result result;
        public int getStatus() {
            return status;
        }
        public Result getResult() {
            return result;
        }
    }
    public static class Result {
        int bg;
        int ed;
        String pgs;
        int[] rg;
        int sn;
        Ws[] ws;
        boolean ls;
        JsonObject vad;
        public Text getText() {
            Text text = new Text();
            StringBuilder sb = new StringBuilder();
            for (Ws ws : this.ws) {
                sb.append(ws.cw[0].w);
            }
            text.sn = this.sn;
            text.text = sb.toString();
            text.sn = this.sn;
            text.rg = this.rg;
            text.pgs = this.pgs;
            text.bg = this.bg;
            text.ed = this.ed;
            text.ls = this.ls;
            text.vad = this.vad==null ? null : this.vad;
            return text;
        }
    }
    public static class Ws {
        Cw[] cw;
        int bg;
        int ed;
    }
    public static class Cw {
        int sc;
        String w;
    }
    public static class Text {
        int sn;
        int bg;
        int ed;
        String text;
        String pgs;
        int[] rg;
        boolean deleted;
        boolean ls;
        JsonObject vad;
        @Override
        public String toString() {
            return "Text{" +
                    "bg=" + bg +
                    ", ed=" + ed +
                    ", ls=" + ls +
                    ", sn=" + sn +
                    ", text='" + text + '\'' +
                    ", pgs=" + pgs +
                    ", rg=" + Arrays.toString(rg) +
                    ", deleted=" + deleted +
                    ", vad=" + (vad==null ? "null" : vad.getAsJsonArray("ws").toString()) +
                    '}';
        }
        public String getText(){
            return text;
        }
    }
    //解析返回数据，仅供参考
    public static class Decoder {
        private Text[] texts;
        private int defc = 10;
        public Decoder() {
            this.texts = new Text[this.defc];
        }
        public synchronized void decode(Text text) {
            if (text.sn >= this.defc) {
                this.resize();
            }
            if ("rpl".equals(text.pgs)) {
                for (int i = text.rg[0]; i <= text.rg[1]; i++) {
                    this.texts[i].deleted = true;
                }
            }
            this.texts[text.sn] = text;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Text t : this.texts) {
                if (t != null && !t.deleted) {
                    sb.append(t.text);
                }
            }
            return sb.toString();
        }
        public void resize() {
            int oc = this.defc;
            this.defc <<= 1;
            Text[] old = this.texts;
            this.texts = new Text[this.defc];
            for (int i = 0; i < oc; i++) {
                this.texts[i] = old[i];
            }
        }
        public void discard(){
            for(int i=0;i<this.texts.length;i++){
                this.texts[i]= null;
            }
        }
        public Text[] getTexts(){
            return this.texts;
        }
    }

    // 开始转换
    public static void StartConvert(String filePath, Handler.Callback callback) throws Exception {
        WebIATWS.filePath = filePath;
        WebIATWS.Callback = callback;
        // 构建鉴权url
        String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
        OkHttpClient client = new OkHttpClient.Builder().build();
        //将url中的 schema http://和https://分别替换为ws:// 和 wss://
        String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
        //System.out.println(url);
        Request request = new Request.Builder().url(url).build();
        // System.out.println(client.newCall(request).execute());
        //System.out.println("url===>" + url);
        WebSocket webSocket = client.newWebSocket(request, new WebIATWS());
    }
    // 远程设置参数
    public static void RemoteSetKey(String appid, String apiSecret, String apiKey){
        WebIATWS.appid = appid;
        WebIATWS.apiSecret = apiSecret;
        WebIATWS.apiKey = apiKey;
        LOG.d("讯飞设置","appid:"+appid, "apiSecret:"+apiSecret, "apiKey:"+apiKey);
    }
}