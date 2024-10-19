package com.serverapp.util;

import com.serverapp.enums.RequestType;

public class CurrentType {
    private static CurrentType _instance;
    private RequestType type;

    private CurrentType() {
        type = RequestType.SYSTEM_INFO;
    }

    public static synchronized CurrentType getInstance() {
        if (_instance == null) {
            _instance = new CurrentType();
        }
        return _instance;
    }

    public RequestType getType() {
        return type;
    }

    public synchronized void setType(RequestType type) {
        this.type = type;
    }
}
