package com.example.updownfile.util;

import ohos.aafwk.ability.Ability;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.ToastDialog;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class LogUtil {
    /**
     * 封装logo日志
     * @param str
     * @param format
     */
    public static void setLog(String str,String format){
        HiLogLabel label = new HiLogLabel(3, 0xD001100, str);
        HiLog.info(label, format);
    }
    /**
     * Toast提示
     */
    public static void Toast(Ability ability,String msg){
        new ToastDialog(ability)
                .setText(msg)
                .setAlignment(LayoutAlignment.CENTER)
                .show();
    }
}
