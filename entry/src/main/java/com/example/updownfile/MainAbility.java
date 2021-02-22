package com.example.updownfile;

import com.example.updownfile.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

/**
 * @author lehuangd
 * @version v
 * @description:ghj created 2021/2/18 9:30
 * created 2021/2/18 10:29
 * @since 2021/2/18 10:29
 */
public class MainAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(MainAbilitySlice.class.getName());
    }
}
