package com.keemsa.boilerplate.util;

/**
 * Created by sebastian on 15/07/17.
 */

public class RxEvent {

    private RX_EVENT_TYPE type;
    private Object argument;

    public RxEvent(RX_EVENT_TYPE type, Object argument) {
        this.type = type;
        this.argument = argument;
    }

    public RxEvent(RX_EVENT_TYPE type) {
        this.type = type;
    }

    public RX_EVENT_TYPE getType() {
        return type;
    }

    public Object getArgument() {
        return argument;
    }

    public enum RX_EVENT_TYPE {
    }
}
