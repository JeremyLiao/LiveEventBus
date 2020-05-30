package com.jeremyliao.liveeventbus.ipc.core;

import android.content.Intent;
import android.os.Bundle;

import com.jeremyliao.liveeventbus.ipc.annotation.IpcConfig;
import com.jeremyliao.liveeventbus.ipc.consts.IpcConst;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liaohailiang on 2019/5/30.
 */
public class ProcessorManager {

    private static class SingletonHolder {
        private static final ProcessorManager INSTANCE = new ProcessorManager();
    }

    public static ProcessorManager getManager() {
        return SingletonHolder.INSTANCE;
    }

    private final List<Processor> baseProcessors;
    private final Map<String, Processor> processorMap;

    {
        baseProcessors = new LinkedList<>(Arrays.asList(
                new StringProcessor(),
                new IntProcessor(),
                new BooleanProcessor(),
                new DoubleProcessor(),
                new FloatProcessor(),
                new LongProcessor(),
                new SerializableProcessor(),
                new ParcelableProcessor()));
        processorMap = new HashMap<>();
        for (Processor processor : baseProcessors) {
            processorMap.put(processor.getClass().getName(), processor);
        }
    }

    private ProcessorManager() {
    }

    public boolean writeTo(Intent intent, Object value) {
        if (intent == null || value == null) {
            return false;
        }
        Bundle bundle = new Bundle();
        boolean processed = false;
        //用指定的processor处理
        IpcConfig config = value.getClass().getAnnotation(IpcConfig.class);
        if (config != null) {
            Class<? extends Processor> processorType = config.processor();
            String processorTypeName = processorType.getName();
            if (!processorMap.containsKey(processorTypeName)) {
                try {
                    processorMap.put(processorTypeName, processorType.newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Processor processor = processorMap.get(processorTypeName);
            if (processor != null) {
                try {
                    boolean handle = processor.writeToBundle(bundle, value);
                    if (handle) {
                        intent.putExtra(IpcConst.KEY_PROCESSOR_NAME, processor.getClass().getName());
                        intent.putExtra(IpcConst.KEY_BUNDLE, bundle);
                        processed = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (processed) {
                return true;
            }
        }
        //用默认的processor处理
        for (Processor processor : baseProcessors) {
            try {
                boolean handle = processor.writeToBundle(bundle, value);
                if (handle) {
                    intent.putExtra(IpcConst.KEY_PROCESSOR_NAME, processor.getClass().getName());
                    intent.putExtra(IpcConst.KEY_BUNDLE, bundle);
                    processed = true;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return processed;
    }

    public Object createFrom(Intent intent) {
        if (intent == null) {
            return null;
        }
        String processorName = intent.getStringExtra(IpcConst.KEY_PROCESSOR_NAME);
        Bundle bundle = intent.getBundleExtra(IpcConst.KEY_BUNDLE);
        if (processorName == null || processorName.length() == 0 || bundle == null) {
            return null;
        }
        if (!processorMap.containsKey(processorName)) {
            try {
                processorMap.put(processorName, (Processor) Class.forName(processorName).newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Processor processor = processorMap.get(processorName);
        if (processor == null) {
            return null;
        }
        try {
            return processor.createFromBundle(bundle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
