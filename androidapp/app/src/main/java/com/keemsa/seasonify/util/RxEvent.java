package com.keemsa.seasonify.util;

import com.keemsa.colorwheel.ColorPickerView;

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
        // ui events
        COLOR_SELECTION_SELECTED,
        COLOR_COMBINATION_LIKED,
        COLOR_COORDS_SELECTED,
        COLOR_CHANGED,
        // storing events
        COLOR_COMBINATION_UPDATED,
        COLOR_SELECTION_UPDATED
    }
}
