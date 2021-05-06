package com.jeremyliao.liveeventbus.processor.gson.converter;

/**
 * Created by liaohailiang on 2019-09-29.
 */
public interface JsonConverter {

    String toJson(Object value);

    <T> T fromJson(String json, Class<T> classOfT);
}
