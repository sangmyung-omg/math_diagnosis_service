package com.tmax.WaplMath.Common.util.kafka;

public enum KafkaEventType {
    EXCEPTION("exception"), //Normal exception. Java exception or input errors
    USER_EXCEPTION("userexception"), //exception during user based operation
    PLATFORM_EXCEPTION("platformexception"), //exception during platform operations
    
    EVENT("event"), //Normal event.
    USER_EVENT("userevent"), //Event of specific user.
    PLATFORM_EVENT("platformevent"), //platform event.

    MISC("misc"); //Misc events

    private String type;

    private KafkaEventType(String type){
        this.type = type;
    }

    public String toString() {
        return this.type;
    }
}
