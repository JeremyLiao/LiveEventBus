package com.jeremyliao.liveeventbus.ipc.core;

import android.os.Bundle;

/**
 * Created by liaohailiang on 2019/5/30.
 */
public interface Processor {

    boolean writeToBundle(Bundle bundle, Object value) throws Exception;

    Object createFromBundle(Bundle bundle) throws Exception;
}
