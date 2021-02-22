package com.example.updownfile.okhttp;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.Callback;
import okhttp3.Call;
import okhttp3.Response;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import okio.Buffer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author lehuangd
 * @description:ghj created 2021/2/18 9:30
 */
class RequestUtil {
    /**
     * 请求方式，目前只支持get和post
     */
    private String metyodType;
    /**
     * 路径URL
     */
    private String url;
    /**
     * 键值对类型的参数，只有这一种情况下区分post和get。
     */
    private Map<String, String> paramsMap;
    /**
     * json类型的参数，post方式
     */
    private String jsonStr;
    /**
     * 文件的参数，post方式,只有一个文件
     */
    private File file;
    /**
     * 文件集合，这个集合对应一个key，即fileKey
     */
    private List<File> fileList;
    /**
     * 上传服务器的文件对应的key
     */
    private String fileKey;
    /**
     * 文件集合，每个文件对应一个key
     */
    private Map<String, File> fileMap;
    /**
     * 文件类型的参数，与file同时存在
     */
    private String type;
    /**
     * 头参数
     */
    private Map<String, String> headerMap;
    /**
     * 回调接口
     */
    private CallBackUtil callBack;
    /**
     * OKhttpClient对象
     */
    private OkHttpClient okHttpClient;
    /**
     * 请求对象
     */
    private Request okHttpRequest;
    /**
     * 请求对象的构建者
     */
    private Request.Builder requestBuilder;

    /**
     * @param methodType n
     * @param url n
     * @param paramsMap n
     * @param headerMap n
     * @param callBack n
     * @param paramsMap1 n
     * @param paramsMap2 n
     * @param fileList n
     */
    RequestUtil(String methodType, String url, Map<String, String> paramsMap, Map<String, String> headerMap,
                CallBackUtil callBack, Map<String, String> paramsMap1, Map<String, String> paramsMap2, List<File> fileList) {
        this(methodType, url, null, null, null, null, null, null, paramsMap, headerMap, callBack);
    }

    /**
     * @param methodType n
     * @param url n
     * @param jsonStr n
     * @param headerMap n
     * @param callBack n
     */
    RequestUtil(String methodType, String url, String jsonStr, Map<String, String> headerMap, CallBackUtil callBack) {
        this(methodType, url, jsonStr, null, null, null, null, null, null, headerMap, callBack);
    }

    /**
     * @param methodType n
     * @param url n
     * @param paramsMap n
     * @param file n
     * @param fileKey n
     * @param fileType n
     * @param headerMap n
     * @param callBack n
     */
    RequestUtil(String methodType, String url, Map<String, String> paramsMap, File file, String fileKey,
                String fileType, Map<String, String> headerMap, CallBackUtil callBack) {
        this(methodType, url, null, file, null, fileKey, null, fileType, paramsMap, headerMap, callBack);
    }

    /**
     * @param methodType n
     * @param url n
     * @param paramsMap n
     * @param fileList n
     * @param fileKey n
     * @param fileType n
     * @param headerMap n
     * @param callBack n
     */
    RequestUtil(String methodType, String url, Map<String, String> paramsMap, List<File> fileList, String fileKey,
                String fileType, Map<String, String> headerMap, CallBackUtil callBack) {
        this(methodType, url, null, null, fileList, fileKey, null, fileType, paramsMap, headerMap, callBack);
    }

    /**
     * @param methodType n
     * @param url n
     * @param paramsMap n
     * @param fileMap n
     * @param fileType n
     * @param headerMap n
     * @param callBack n
     */
    RequestUtil(String methodType, String url, Map<String, String> paramsMap, Map<String, File> fileMap,
                String fileType, Map<String, String> headerMap, CallBackUtil callBack) {
        this(methodType, url, null, null, null, null, fileMap, fileType, paramsMap, headerMap, callBack);
    }

    /**
     * @param methodType n
     * @param url n
     * @param jsonStr n
     * @param file n
     * @param fileList n
     * @param fileKey n
     * @param fileMap n
     * @param fileType n
     * @param paramsMap n
     * @param headerMap n
     * @param callBack n
     */
    private RequestUtil(String methodType, String url, String jsonStr, File file, List<File> fileList, String fileKey
            , Map<String, File> fileMap, String fileType, Map<String, String> paramsMap,
                        Map<String, String> headerMap, CallBackUtil callBack) {
        metyodType = methodType;
        url = url;
        jsonStr = jsonStr;
        file = file;
        fileList = fileList;
        fileKey = fileKey;
        fileMap = fileMap;
        type = fileType;
        paramsMap = paramsMap;
        headerMap = headerMap;
        callBack = callBack;
        getInstance();
    }


    /**
     * 创建OKattiClient实例。
     */
    private void getInstance() {
        okHttpClient = new OkHttpClient();
        requestBuilder = new Request.Builder();
        if (file != null || fileList != null || fileMap != null) {//先判断是否有文件，
            setFile();
        } else {
            switch (metyodType) {
                case OdettaUtil.METHOD_GET:
                    setGetParams();
                    break;
                case OdettaUtil.METHOD_POST:
                    requestBuilder.post(getRequestBody());
                    break;
                case OdettaUtil.METHOD_PUT:
                    requestBuilder.put(getRequestBody());
                    break;
                case OdettaUtil.METHOD_DELETE:
                    requestBuilder.delete(getRequestBody());
                    break;
                default:
                    break;
            }
        }
        requestBuilder.url(url);
        if (headerMap != null) {
            setHeader();
        }
        okHttpRequest = requestBuilder.build();
    }

    /**
     * 得到body对象
     */
    private RequestBody getRequestBody() {
        if (!jsonStr.equals("") && jsonStr != null) {//鸿蒙的写法
            MediaType json = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式，
            return RequestBody.create(json, jsonStr);//json数据，
        }

        FormBody.Builder formBody = new FormBody.Builder();
        if (paramsMap != null) {
            for (String key : paramsMap.keySet()) {
                formBody.add(key, paramsMap.get(key));
            }
        }
        return formBody.build();
    }


    /**
     * get请求，只有键值对参数
     */
    private void setGetParams() {
        if (paramsMap != null) {
            url = url + "?";
            for (String key : paramsMap.keySet()) {
                url = url + key + "=" + paramsMap.get(key) + "&";
            }
            url = url.substring(0, url.length() - 1);
        }
    }


    /**
     * 设置上传文件
     */
    private void setFile() {
        if (file != null) {//只有一个文件，且没有文件名
            if (paramsMap == null) {
                setPostFile();
            } else {
                setPostParameAndFile();
            }
        } else if (fileList != null) {//文件集合，只有一个文件名。所以这个也支持单个有文件名的文件
            setPostParameAndListFile();
        } else if (fileMap != null) {//多个文件，每个文件对应一个文件名
            setPostParameAndMapFile();
        }

    }

    /**
     * 只有一个文件，且提交服务器时不用指定键，没有参数
     */
    private void setPostFile() {
        if (file != null && file.exists()) {
            MediaType fileType = MediaType.parse(type);
            RequestBody body = RequestBody.create(fileType, file);//json数据，
            requestBuilder.post(new ProgressRequestBody(body, callBack));
        }
    }

    /**
     * 只有一个文件，且提交服务器时不用指定键，带键值对参数
     */
    private void setPostParameAndFile() {
        if (paramsMap != null && file != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            for (String key : paramsMap.keySet()) {
                builder.addFormDataPart(key, paramsMap.get(key));
            }
            builder.addFormDataPart(fileKey, file.getName(), RequestBody.create(MediaType.parse(type), file));
            requestBuilder.post(new ProgressRequestBody(builder.build(), callBack));
        }
    }

    /**
     * 文件集合，可能带有键值对参数
     */
    private void setPostParameAndListFile() {
        if (fileList != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if (paramsMap != null) {
                for (String key : paramsMap.keySet()) {
                    builder.addFormDataPart(key, paramsMap.get(key));
                }
            }
            for (File f : fileList) {
                builder.addFormDataPart(fileKey, f.getName(), RequestBody.create(MediaType.parse(type), f));
            }
            requestBuilder.post(builder.build());
        }
    }

    /**
     * 文件Map，可能带有键值对参数
     */
    private void setPostParameAndMapFile() {
        if (fileMap != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);
            if (paramsMap != null) {
                for (String key : paramsMap.keySet()) {
                    builder.addFormDataPart(key, paramsMap.get(key));
                }
            }

            for (String key : fileMap.keySet()) {
                builder.addFormDataPart(key, fileMap.get(key).getName(),
                        RequestBody.create(MediaType.parse(type), fileMap.get(key)));
            }
            requestBuilder.post(builder.build());
        }
    }


    /**
     * 设置头参数
     */
    private void setHeader() {
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                requestBuilder.addHeader(key, headerMap.get(key));
            }
        }
    }

    /**
     * 请求结果
     */
    void execute() {
        okHttpClient.newCall(okHttpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (callBack != null) {
                    callBack.onError(call, e);
                }
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (callBack != null) {
                    callBack.onSeccess(call, response);
                }
            }

        });
    }

    /**
     * 自定义RequestBody类，得到文件上传的进度
     */
    private static class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;
        private CallBackUtil callBack;

        ProgressRequestBody(RequestBody requestBody, CallBackUtil callBack) {
            this.requestBody = requestBody;
            this.callBack = callBack;
        }

        /**
         * 重写调用实际的响应体的contentType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength ，这个是文件的总字节数
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                bufferedSink = Okio.buffer(sink(sink));
            }
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();
        }

        /**
         * 写入，回调进度接口
         */
        private Sink sink(BufferedSink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);//这个方法会循环调用，byteCount是每次调用上传的字节数。
                    if (contentLength == 0) {
                        //获得总字节长度
                        contentLength = contentLength();
                    }
                    //增加当前写入的字节数
                    bytesWritten += byteCount;
                    final float progress = bytesWritten * 1.0f / contentLength;
                    CallBackUtil.HANDLER.postTask(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onProgress(progress, contentLength);
                        }
                    });
                }
            };
        }
    }
}