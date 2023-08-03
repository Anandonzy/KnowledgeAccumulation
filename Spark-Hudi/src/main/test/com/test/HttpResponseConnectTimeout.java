package com.test;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangziyu1
 * @Date 2022/6/24 18:43
 * @Version 1.0
 */
public class HttpResponseConnectTimeout {

    public static void main(String[] args) throws HttpException, IOException {
        //请求地址
        String url = "";
        //请求重试次数（本次+ 1）=2次
        //超时时间（次/秒）2秒
        System.out.println(httpRequest(url,1,2));
    }


    /**
     * 设置请求响应时长和请求重试次数
     * @param url 请求地址
     * @param maxRentry 请求重试次数（本次+ maxRentry）
     * @param timeout 超时时间（次/秒）
     * @return
     */
    public static String httpRequest(String url,  int maxRentry,int timeout ) {
        //请求响应结果
        String responseResult;
        //响应
        Response response = null;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .addInterceptor(new HttpResponseConnectTimeout.OkhttpInterceptor(maxRentry)) //过滤器，设置最大重试次数
                .retryOnConnectionFailure(false) //不自动重连
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        try {
            response = client.newCall(request).execute();
            if (response != null) {
                responseResult = response.body().string();
                return responseResult;
            } else {
                System.out.println("接口请求超时");
                return null;
            }
            /*如果response可以正常返回，说明能连接上，接着写业务逻辑即可*/
        } catch (Exception e) {
            //捕获异常，intercept must not return null.过滤器不能返回null
            System.out.println("重试2次后,访问超时.");
            return null;
        }

    }


    public static class OkhttpInterceptor implements Interceptor {
        // 最大重试次数
        private int maxRentry;

        public OkhttpInterceptor(int maxRentry) {
            this.maxRentry = maxRentry;
        }

        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            /* 递归 2次下发请求，如果仍然失败 则返回 null ,但是 intercept must not return null.
             * 返回 null 会报 IllegalStateException 异常
             * */
            return retry(chain, 0);//这个递归真的很舒服
        }

        Response retry(Chain chain, int retryCent) {
            Request request = chain.request();
            Response response = null;
            try {
//                System.out.println("第" + (retryCent + 1) + "次执行发http请求.");
                response = chain.proceed(request);
            } catch (Exception e) {
                if (maxRentry > retryCent) {
                    return retry(chain, retryCent + 1);
                }
            } finally {
                return response;
            }
        }
    }
}
