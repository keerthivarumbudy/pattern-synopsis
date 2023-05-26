package org.example;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class SubSketch {
    Timestamp startTimestamp;
    Timestamp endTimestamp;
    Integer resolution; // not in seconds, but in multiples of the resolution of the sketch
    Map<Integer, Integer> eventCountMap;

    public SubSketch(){
        this.eventCountMap = new HashMap<>();
    }
}
