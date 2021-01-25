package com.example.updownfile.handler;

import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class MyEventHandler extends EventHandler {
    /**
     * 日志
     */
    private static final HiLogLabel label = new HiLogLabel(3, 0xD001100, "ThreadDemo");


    public MyEventHandler(EventRunner runner) throws IllegalArgumentException {
        super(runner);
    }

    /**
     * 1. 创建 EventHandler 的子类，在子类中重写实现方法 processEvent()来处理事件
     *
     * @param event
     */
    @Override
    protected void processEvent(InnerEvent event) {
        super.processEvent(event);

        if (event == null) {
            return;
        }

        int eventId = event.eventId;
        long param = event.param;
        switch ((int) (eventId | param)) {
            case 1:
                HiLog.info(label, "eventId | param --->" + 1);
                break;
            default:
                break;
        }
    }

    /**
     * EventHandler 投递 InnerEvent 事件
     */
    private void initInnerEvent() {
        EventRunner runner = EventRunner.create(false);
        if (runner == null) {
            return;
        }
        MyEventHandler myHandler = new MyEventHandler(runner);
        int eventId1 = 0;
        int eventId2 = 1;
        long param = 0;
        Object object = null;
        InnerEvent event1 = InnerEvent.get(eventId1, param, object);
        InnerEvent event2 = InnerEvent.get(eventId2, param, object);

        myHandler.sendEvent(event1, 0, Priority.IMMEDIATE);
        myHandler.sendEvent(event2, 2, Priority.IMMEDIATE);
        runner.run();
        runner.stop();

    }
}
