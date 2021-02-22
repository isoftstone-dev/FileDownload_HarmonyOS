package com.example.updownfile.util;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import okio.Buffer;

import java.io.IOException;

/**
 * @author lehuangd
 * @version d
 * @description: s
 * @since 2021/2/18 10:17
 */
public class ProgressResponseBody extends ResponseBody {
    /**
     * responseBody
     */
    private final ResponseBody responseBody;
    /**
     * progressListener
     */
    private final ProgressListener progressListener;
    /**
     * progressListener
     */
    private BufferedSource bufferedSource;
    /**
     * progressListener
     */
    private int portion;

    /**
     * @param responseBody     r
     * @param progressListener p
     * @param portion          p
     */
    ProgressResponseBody(ResponseBody responseBody,
                         ProgressListener progressListener, int portion) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
        this.portion = portion;
        if (progressListener != null) {
            progressListener.onPreExecute(contentLength(), portion);
        }
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * 读取资源
     *
     * @param source d
     * @return r
     */
    private Source source(Source source) {
        return new ForwardingSource(source) {
           private long totalBytes = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytes += bytesRead != -1 ? bytesRead : 0;
                if (null != progressListener) {
                    progressListener.update(totalBytes, bytesRead == -1, portion);
                }
                return bytesRead;
            }
        };
    }

    /**
     * 进度监听
     */
    public interface ProgressListener {
        /**
         * @param contentLength r
         * @param piston        r
         */
        void onPreExecute(long contentLength, int piston);

        /**
         * @param totalBytes d
         * @param done       d
         * @param piston     d
         */
        void update(long totalBytes, boolean done, int piston);
    }
}
