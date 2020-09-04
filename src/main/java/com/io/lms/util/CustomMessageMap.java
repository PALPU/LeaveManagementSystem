package com.io.lms.util;

import java.util.HashMap;

public class CustomMessageMap {
    HashMap<String,String>messageMap;

    public CustomMessageMap(String key, String value){
        messageMap=new HashMap<>();
        messageMap.put(key, value);
    }

    public HashMap<String, String> getMessageMap() {
        return messageMap;
    }

    public void setMessageMap(HashMap<String, String> messageMap) {
        this.messageMap = messageMap;
    }
}
