package tech.kee;

import tech.kee.CountMin.CountMinSketch;
import tech.kee.model.Event;
import tech.kee.model.EventMapping;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;

public class Sketch {
    Integer startOrder;
    Integer endOrder;
//    EventMapping eventCountMap;
    CountMinSketch eventCountMap;


    public Sketch(Integer startOrder, Integer endOrder) {
        this.startOrder = startOrder;
        this.endOrder = endOrder;
        this.eventCountMap = new CountMinSketch(200,7); // error = 0.01 and probability = 1 - 0.01
//        this.eventCountMap = new EventMapping();
    }

    void addEvent(Event event) {
        checkArgument(isWithinOrderRange(event), "This event is not in the sketch's time range");
        eventCountMap.add(event.id());
    }

    boolean isWithinOrderRange(Event event) {
        return event.order()>=startOrder && event.order()<endOrder;
    }

    Sketch merge(Sketch toMerge) {
        checkArgument(isNeighboringSketch(toMerge), "The sketch to merge is not a temporal neighbor of this sketch");
        Sketch mergedSketch = new Sketch(
                min(this.startOrder, toMerge.startOrder), max(this.endOrder, toMerge.endOrder));
//        mergedSketch.eventCountMap = new EventMapping().merge(this.eventCountMap).merge(toMerge.eventCountMap);
        mergedSketch.eventCountMap = new CountMinSketch(this.eventCountMap.width, this.eventCountMap.depth).merge(this.eventCountMap).merge(toMerge.eventCountMap);
        return mergedSketch;
    }


    boolean isNeighboringSketch(Sketch otherSketch) {
        return this.endOrder.equals(otherSketch.startOrder)
                || this.startOrder.equals(otherSketch.endOrder);
    }
}
