package tech.kee;

import tech.kee.CountMin.CountMinSketch;
import tech.kee.model.Event;
import tech.kee.model.EventMapping;

import java.time.Instant;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;

public class Sketch {
    Instant startTimestamp;
    Instant endTimestamp;
    EventMapping eventCountMap;


    public Sketch(Instant startTimestamp, Instant endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
//        this.eventCountMap = new CountMinSketch(100, 5); // might want to control this with error and probability
        this.eventCountMap = new EventMapping();
    }

    void addEvent(Event event) {
        checkArgument(isWithinTimeRange(event), "This event is not in the sketch's time range");
        eventCountMap.add(event.id());
    }

    boolean isWithinTimeRange(Event event) {
        return !event.timestamp().isBefore(startTimestamp) && event.timestamp().isBefore(endTimestamp);
    }

    Sketch merge(Sketch toMerge) {
        checkArgument(isNeighboringSketch(toMerge), "The sketch to merge is not a temporal neighbor of this sketch");
        Sketch mergedSketch = new Sketch(
                min(this.startTimestamp, toMerge.startTimestamp), max(this.endTimestamp, toMerge.endTimestamp));
        mergedSketch.eventCountMap = this.eventCountMap.merge(toMerge.eventCountMap);
        return mergedSketch;
    }


    boolean isNeighboringSketch(Sketch otherSketch) {
        return this.endTimestamp.equals(otherSketch.startTimestamp)
                || this.startTimestamp.equals(otherSketch.endTimestamp);
    }
}
