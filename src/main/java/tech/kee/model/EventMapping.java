package tech.kee.model;

import java.util.HashMap;
import java.util.Map;

public class EventMapping {
    Map<Integer, Integer> eventCountMap;

    public EventMapping() {
        this.eventCountMap = new HashMap<>();
    }

    public void add(Integer eventId) {
        if(eventCountMap.containsKey(eventId)) {
            eventCountMap.put(eventId, eventCountMap.get(eventId) + 1);
        } else {
            eventCountMap.put(eventId, 1);
        }
    }

    public int estimateCount(Integer eventId) {
        if(eventCountMap.containsKey(eventId)) {
            return eventCountMap.get(eventId);
        } else {
            return 0;
        }
    }

    public EventMapping merge(EventMapping toMerge) {
        for(Integer eventId : toMerge.eventCountMap.keySet()) {
            if(eventCountMap.containsKey(eventId)) {
                eventCountMap.put(eventId, eventCountMap.get(eventId) + toMerge.eventCountMap.get(eventId));
            } else {
                eventCountMap.put(eventId, toMerge.eventCountMap.get(eventId));
            }
        }
        return this;
    }
}
