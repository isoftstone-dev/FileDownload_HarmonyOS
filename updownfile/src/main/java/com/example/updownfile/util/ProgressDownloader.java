package com.example.updownfile.util;


import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Protocol;
import okhttp3.Callback;
import okhttp3.ResponseBody;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.cert.CertificateException;
import java.util.Collections;

/**
 * @author lehuangd
 * @version y
 * @description: ty
 * created 2021/2/18 10:14
 * @since 2021/2/18 10:14
 */
public class ProgressDownloader {
    /**
     * progressListener
     */
    private ProgressResponseBody.ProgressListener progressListener;
    /**
     * url
     */
    private String url;
    /**
     * client
     */
    private OkHttpClient client;
    /**
     * destination
     */
    private File destination;
    /**
     * call
     */
    private Call call;
    /**
     * portion
     */
    private int portion;

    /**
     * 构造方法
     *
     * @param url              路径
     * @param destination      e
     * @param progressListener 回调进度
     * @param portion          portion
     */
    public ProgressDownloader(String url, File destination, ProgressResponseBody.ProgressListener progressListener,
                              int portion) {
        this.url = url;
        this.destination = destination;
        this.progressListener = progressListener;
        this.portion = portion;
        //在下载、暂停后的继续下载中可复用同一个client对象
        client = getProgressClient();
    }

    /**
     * 每次下载需要新建新的Call对象
     *
     * @param startPoints s
     * @return s
     */
    private Call newCall(long startPoints) {
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startPoints + "-")//断点续传要用到的，指示下载的区间
                .build();
        return client.newCall(request);
    }

    /**
     * @return d
     */
    private OkHttpClient getProgressClient() {
        // 拦截器，用上ProgressResponseBody
        Interceptor interceptor = chain -> {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener, portion))
                    .build();
        };
        /**
         * sd
         */
        return getUnsafeOkHttpClient().newBuilder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .addNetworkInterceptor(interceptor)
                .build();
    }


    /**
     * 指定开始下载的点
     *
     * @param startsPoint d
     */
    public void download(final long startsPoint) {
        call = newCall(startsPoint);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    save(response, startsPoint);
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });
    }

    /**
     * 暂停下载
     */
    public void pause() {
        if (call != null) {
            call.cancel();
        }
    }

    /**
     * 保存资源
     *
     * @param response    r
     * @param startsPoint s
     */
    private void save(Response response, long startsPoint) {
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        FileChannel channelOut = null;
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(destination, "rwd");
            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
            channelOut = randomAccessFile.getChannel();
            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startsPoint,
                    body.contentLength());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return d
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
