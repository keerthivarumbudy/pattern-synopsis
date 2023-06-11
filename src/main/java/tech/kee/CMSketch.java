//package tech.kee;
//import com.clearspring.analytics.stream.frequency.CountMinSketch;
//import tech.kee.model.Event;
//
//import static com.google.common.base.Preconditions.checkArgument;
//import static com.google.common.collect.Comparators.max;
//import static com.google.common.collect.Comparators.min;
//
//public class CMSketch {
//    Integer startOrder;
//    Integer endOrder;
//    CountMinSketch eventCountMap;
//
//    public CMSketch(Integer startOrder, Integer endOrder) {
//        this.startOrder = startOrder;
//        this.endOrder = endOrder;
//        this.eventCountMap = new CountMinSketch(0.1,0.99, 1); // might want to control this with error and probability
//    }
//    void addEvent(Event event) {
//        checkArgument(isWithinOrderRange(event), "This event is not in the sketch's time range");
//        eventCountMap.add(event.id(), 1);
//    }
//    boolean isWithinOrderRange(Event event) {
//        return event.order()>=startOrder && event.order()<endOrder;
//    }
//    CMSketch merge(CMSketch toMerge) {
//        checkArgument(isNeighboringSketch(toMerge), "The sketch to merge is not a temporal neighbor of this sketch");
//        CMSketch mergedSketch = new CMSketch(
//                min(this.startOrder, toMerge.startOrder), max(this.endOrder, toMerge.endOrder));
////        mergedSketch.eventCountMap = new EventMapping().merge(this.eventCountMap).merge(toMerge.eventCountMap);
//        try {
//            mergedSketch.eventCountMap =  CountMinSketch.merge(this.eventCountMap,toMerge.eventCountMap);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return mergedSketch;
//    }
//    boolean isNeighboringSketch(CMSketch otherSketch) {
//        return this.endOrder.equals(otherSketch.startOrder)
//                || this.startOrder.equals(otherSketch.endOrder);
//    }
//
//}
