package com.example.updownfile.okhttp;


import com.example.updownfile.handler.MyEventHandler;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import okhttp3.Call;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @param <T>
 * @author lehuangd
 * @version j
 * @description:ghj created 2021/2/18 9:30
 * @since 2021/2/18 9:30
 */
public abstract class CallBackUtil<T> {
    /**
     * 定义runner
     */
    private static EventRunner RUNNER = EventRunner.create(false);
    /**
     * 定义handler
     */
    static MyEventHandler HANDLER = new MyEventHandler(RUNNER);

    /**
     * @param progress p
     * @param total    t
     */
    void onProgress(float progress, long total) {

    }

    /**
     * @param call y
     * @param e    i
     */
    void onError(final Call call, final Exception e) {
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                onFailure(call, e);
            }
        };
        HANDLER.postTask(task1, 0, EventHandler.Priority.IMMEDIATE);
        RUNNER.run();
        RUNNER.stop();
    }

    /**
     * @param call     y
     * @param response i
     */
    void onSeccess(Call call, Response response) {
        final T obj = onParseResponse(call, response);
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                onResponse(obj);
            }
        };
        HANDLER.postTask(task2, 0, EventHandler.Priority.IMMEDIATE);
        RUNNER.run();
        RUNNER.stop();
    }


    /**
     * @param call     c
     * @param response r
     * @return d
     */
    public abstract T onParseResponse(Call call, Response response);

    /**
     * @param call c
     * @param e    e
     */
    abstract void onFailure(Call call, Exception e);

    /**
     * @param response r
     */
    abstract void onResponse(T response);

    /**
     * 回调方法
     *
     * @return
     */
    public static class CallBackDefault extends CallBackUtil<Response> {
        @Override
        public Response onParseResponse(Call call, Response response) {
            return response;
        }

        @Override
        void onFailure(Call call, Exception e) {

        }

        @Override
        void onResponse(Response response) {

        }
    }

    /**
     * 请求回调
     *
     * @return d
     */
    public static class CallBackString extends CallBackUtil<String> {
        @Override
        public String onParseResponse(Call call, Response response) {
            try {
                return response.body().string();
            } catch (IOException e) {
                new RuntimeException("failure");
                return "";
            }
        }

        @Override
        void onFailure(Call call, Exception e) {

        }

        @Override
        void onResponse(String response) {

        }
    }

//    public static abstract class CallBackBitmap extends CallBackUtil<PixelMap>{
//        private int mTargetWidth;
//        private int mTargetHeight;
//
//        public CallBackBitmap(){};
//        public CallBackBitmap(int targetWidth,int targetHeight){
//            mTargetWidth = targetWidth;
//            mTargetHeight = targetHeight;
//        };
//        public CallBackBitmap(Image imageView){
//            int width = imageView.getWidth();
//            int height = imageView.getHeight();
//            if(width <=0 || height <=0){
//                throw new RuntimeException("无法获取ImageView的width或height");
//            }
//            mTargetWidth = width;
//            mTargetHeight = height;
//        };
//        @Override
//        public PixelMap onParseResponse(Call call, Response response) {
//            if(mTargetWidth ==0 || mTargetHeight == 0){
//                return BitmapFactory.decodeStream(response.body().byteStream());
//            }else {
//                return getZoomBitmap(response);
//            }
//        }
//
//        /**
//         * 压缩图片，避免OOM异常
//         */
//        private PixelMap getZoomBitmap(Response response) {
//            byte[] data = null;
//            try {
//                data = response.body().bytes();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//
//            BitmapFactory.decodeByteArray(data,0,data.length,options);
//            int picWidth = options.outWidth;
//            int picHeight = options.outHeight;
//            int sampleSize = 1;
//            int heightRatio = (int) Math.floor((float) picWidth / (float) mTargetWidth);
//            int widthRatio = (int) Math.floor((float) picHeight / (float) mTargetHeight);
//            if (heightRatio > 1 || widthRatio > 1){
//                sampleSize = Math.max(heightRatio,widthRatio);
//            }
//            options.inSampleSize = sampleSize;
//            options.inJustDecodeBounds = false;
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length,options);
//
//            if(bitmap == null){
//                throw new RuntimeException("Failed to decode stream.");
//            }
//            return bitmap;
//        }
//    }

    /**
     * 下载文件时的回调类
     *
     * @return d
     */
    public static class CallBackFile extends CallBackUtil<File> {
        /**
         * DestFileDir
         */
        private final String destFileDir;
        private final String destFileName;

        /**
         * @param destFileDir1
         * @param destFileName1
         */
        public CallBackFile(String destFileDir1, String destFileName1) {
            this.destFileDir = destFileDir1;
            this.destFileName = destFileName1;
        }

        @Override
        public File onParseResponse(Call call, Response response) {

            InputStream is = null;
            byte[] buf = new byte[1024];
            int len = 0;
            FileOutputStream fos = null;
            try {
                is = response.body().byteStream();
                final long total = response.body().contentLength();

                long sum = 0;

                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, destFileName);
                fos = new FileOutputStream(file);
                while ((len = is.read(buf)) != -1) {
                    sum += len;
                    fos.write(buf, 0, len);
                    final long finalSum = sum;
                    HANDLER.postTask(new Runnable() {
                        @Override
                        public void run() {
                            onProgress(finalSum * 100.0f / total, total);
                        }
                    });


                }
                fos.flush();

                return file;

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    response.body().close();
                    if (is != null) is.close();
                } catch (IOException ignored) {
                }
                try {
                    if (fos != null) fos.close();
                } catch (IOException ignored) {
                }

            }
            return null;
        }

        @Override
        void onFailure(Call call, Exception e) {

        }

        @Override
        void onResponse(File response) {

        }
    }

}
