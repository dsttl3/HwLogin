package cn.dsttl3.hwlogin.utils;

import okhttp3.*;
import java.io.IOException;
import java.util.Objects;

public class OkUtil {
    private static final String COOKIE = "UA=dsttl3";
    private static final String USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.75 Safari/537.36";

    /**
     * 无需cookie的get请求
     * @param url url
     * @return 请求结果
     */
    public static String get(String url){
        return get(url,COOKIE);
    }
    public static String get(String url, String cookie){
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url)
                    .header("user-agent", USER_AGENT)
                    .header("Cookie",cookie)
                    .build();
            Call call = client.newCall(request);
            return Objects.requireNonNull(call.execute().body()).string();
        } catch (IOException e) {
            System.out.print("请求出错：");
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * 无需cookie的post请求
     * @param url url
     * @param body 请求体
     * @return 请求结果
     */
    public static String post(String url, RequestBody body){
        return post(url,COOKIE,body);
    }
    public static String post(String url,String cookie, RequestBody body) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url)
                    .header("user-agent",USER_AGENT)
                    .header("Content-type","application/x-www-form-urlencoded")
                    .header("Cookie",cookie)
                    .post(body).build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            System.out.print("请求出错：");
            System.out.println(e.getMessage());
            return null;
        }
    }

}
