package com.example.updownfile.slice;

import com.example.updownfile.ResourceTable;
import com.example.updownfile.okhttp.OdettaUtil;
import com.example.updownfile.util.LogUtil;
import com.example.updownfile.util.ProgressDownloader;
import com.example.updownfile.util.ProgressResponseBody;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.ProgressBar;
import ohos.app.Environment;

import java.io.File;

/**
 * @author lehuangd
 * @version v
 * @description:ghj created 2021/2/18 9:30
 * created 2021/2/18 10:33
 * @since 2021/2/18 10:33
 */
public class MainAbilitySlice extends AbilitySlice implements Component.ClickedListener,
        ProgressResponseBody.ProgressListener {
    /**
     * progressBar
     */
    private ProgressBar progressBar;
    /**
     * downloader
     */
    private ProgressDownloader downloader;
    /**
     * breakPoints
     */
    private long breakPoints;
    /**
     * totalBytes
     */
    private long totalBytes;
    /**
     * contentLength
     */
    private long contentLength;
    /**
     * PACKAGE_URL
     */
    private static final String PACKAGE_URL = "https://dl.google.com/dl/android/studio/install/3.5.2" +
            ".0/android-studio-ide-191.5977832-windows.exe";

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        try {
            intentView();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     *
     * @throws InterruptedException 'InterruptedException'
     */
    private void intentView() throws InterruptedException {
        String[] per = {"ohos.permission.READ_USER_STORAGE", "ohos.permission.WRITE_MEDIA",
                "ohos.permission.READ_MEDIA", "ohos.permission.WRITE_USER_STORAGE"};
        requestPermissionsFromUser(per, 0);
        progressBar = (ProgressBar) findComponentById(ResourceTable.Id_progress);
        Button button1 = (Button) findComponentById(ResourceTable.Id_button1);
        Button button2 = (Button) findComponentById(ResourceTable.Id_button2);
        Button button3 = (Button) findComponentById(ResourceTable.Id_button3);
        Button button4 = (Button) findComponentById(ResourceTable.Id_button4);
        Button button5 = (Button) findComponentById(ResourceTable.Id_button5);
        Button button6 = (Button) findComponentById(ResourceTable.Id_button6);
        button1.setClickedListener(this);
        button2.setClickedListener(this);
        button3.setClickedListener(this);
        button4.setClickedListener(this);
        button5.setClickedListener(this);
        button6.setClickedListener(this);
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "windows.exe");
        downloader = new ProgressDownloader(PACKAGE_URL, file, this, 0);
        OdettaUtil.okHttpDownloadFile(null, null, null);
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    @Override
    public void onClick(Component component) {
        switch (component.getId()) {
            case ResourceTable.Id_button1:
                // 新下载前清空断点信息
                breakPoints = 0L;
                downloader.download(0L);
                LogUtil.toast(getAbility(), "开始下载");
                break;
            case ResourceTable.Id_button2:
                downloader.pause();
                // 存储此时的totalBytes，即断点位置。
                breakPoints = totalBytes;
                LogUtil.toast(getAbility(), "下载暂停");
                break;
            case ResourceTable.Id_button3:
                downloader.download(breakPoints);
                LogUtil.toast(getAbility(), "下载继续");
                break;
            case ResourceTable.Id_button4:
                LogUtil.toast(getAbility(), "开始单文件上传,等待服务器接收");

                break;
            case ResourceTable.Id_button5:
                LogUtil.toast(getAbility(), "开始多文件上传，等待服务器接收");
                break;
            case ResourceTable.Id_button6:
                Intent intent = new Intent();
                Operation operation = new Intent.OperationBuilder()
                        .withBundleName("com.example.updownfile")
                        .withAbilityName("com.example.updownfile.TaskDownAblity")
                        .build();
                intent.setOperation(operation);
                startAbility(intent);

                break;

        }
    }

    @Override
    public void onPreExecute(long contentLength, int piston) {
        // 文件总长只需记录一次，要注意断点续传后的contentLength只是剩余部分的长度
        if (this.contentLength == 0L) {
            this.contentLength = contentLength;
            getUITaskDispatcher().asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    progressBar.setMaxValue((int) (contentLength / 1024));
                }
            });

        }
    }

    @Override
    public void update(long totalBytes, boolean done, int piston) {
        // 注意加上断点的长度
        this.totalBytes = totalBytes + breakPoints;
        getUITaskDispatcher().asyncDispatch(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgressValue((int) (totalBytes + breakPoints) / 1024);
            }
        });

        if (done) {
            // 切换到主线程
            getUITaskDispatcher().asyncDispatch(new Runnable() {
                @Override
                public void run() {
                    LogUtil.toast(getAbility(), "下载完成");
                }
            });
        }
    }
}
