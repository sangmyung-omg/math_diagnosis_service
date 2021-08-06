package com.tmax.WaplMath.Common.util.kafka;

public enum KafkaEventLevel {
    DEBUG("debug"),
    INFO("info"),
    WARN("warn"),
    ERROR("error");

    private String msg;

    private KafkaEventLevel(String msg){
        this.msg = msg;
    }

    public String toString() {
        return this.msg;
    }
}
