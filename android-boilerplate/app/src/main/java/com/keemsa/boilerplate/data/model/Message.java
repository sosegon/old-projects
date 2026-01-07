package com.keemsa.boilerplate.data.model;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 * Based on code from https://github.com/ribot/android-boilerplate
 */

@AutoValue
public abstract class Message implements Parcelable {
    public abstract String content();

    public static Builder builder() {
        return new AutoValue_Message.Builder();
    }

    public static TypeAdapter<Message> typeAdapter(Gson gson) {
        return new AutoValue_Message.GsonTypeAdapter(gson);
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setContent(String content);
        public abstract Message build();
    }
}