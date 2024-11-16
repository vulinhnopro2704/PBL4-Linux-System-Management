package com.clientapp.model;

import com.clientapp.enums.RequestType;

public class CommandModel {
    public String time;
    public RequestType type = RequestType.COMMAND;
    public String message;
}
