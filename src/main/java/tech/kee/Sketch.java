package tech.kee;

import tech.kee.model.Event;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;

public class Sketch {
    Instant startTimestamp;
    Instant endTimestamp;
    Map<Integer, Integer> eventCountMap;

    public Sketch(Instant startTimestamp, Instant endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.eventCountMap = new HashMap<>();
    }

    void addEvent(Event event) {
        checkArgument(isWithinTimeRange(event), "This event is not in the sketch's time range");
        eventCountMap.put(event.id(), eventCountMap.getOrDefault(event.id(), 0) + 1);
    }

    boolean isWithinTimeRange(Event event) {
        return !event.timestamp().isBefore(startTimestamp) && event.timestamp().isBefore(endTimestamp);
    }

    Sketch merge(Sketch toMerge) {
        checkArgument(isNeighboringSketch(toMerge), "The sketch to merge is not a temporal neighbor of this sketch");
        Sketch mergedSketch = new Sketch(
                min(this.startTimestamp, toMerge.startTimestamp), max(this.endTimestamp, toMerge.endTimestamp));
        mergedSketch.eventCountMap = Stream.concat(
                        this.eventCountMap.entrySet().stream(), toMerge.eventCountMap.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
        return mergedSketch;
    }

    boolean isNeighboringSketch(Sketch otherSketch) {
        return this.endTimestamp.equals(otherSketch.startTimestamp)
                || this.startTimestamp.equals(otherSketch.endTimestamp);
    }
}
