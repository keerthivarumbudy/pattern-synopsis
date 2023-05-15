package org.example;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class SubSketch {
    Timestamp startTimestamp;
    Timestamp endTimestamp;
    Map<String, Integer> eventCountMap;

    public SubSketch(){
        this.eventCountMap = new HashMap<>();
    }
}
