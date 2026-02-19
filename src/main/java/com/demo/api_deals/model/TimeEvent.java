package com.demo.api_deals.model;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TimeEvent{
    private LocalTime time;
    private EventType eventType;

    public TimeEvent(LocalTime time, EventType eventType) {
        this.time = time;
        this.eventType = eventType;
    }

    public enum EventType {
        START, END
    }
}
