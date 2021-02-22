package com.example.updownfile.util;

import ohos.aafwk.ability.Ability;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

/**
 * @author lehuangd
 * @version y
 * @description:ghj created 2021/2/18 9:30
 * created 2021/2/18 10:20
 * @since 2021/2/18 10:20
 */
public class LogUtil {

    /**
     * @param tag     t
     * @param message m
     */
    public static void setLog(String tag, String message) {
        HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0xFFFFF, tag);
        HiLog.info(label, message);
    }

    /**
     * @param ability a
     * @param msg     m
     */
    public static void toast(Ability ability, String msg) {
        new ToastDialog(ability)
                .setText(msg)
                .setAlignment(LayoutAlignment.CENTER)
                .show();
    }
}
