package com.serverapp.model;


import com.serverapp.enums.RequestType;

public class CommandModel {
    public String time;
    public RequestType type = RequestType.COMMAND;
    public String message;
}
