package tech.kee;

import tech.kee.model.Event;
import tech.kee.model.WaveletEventMapping;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;

public class Wavelet {
    Integer startOrder;
    Integer endOrder;
    Integer resolution;
    WaveletEventMapping eventStats; // eventId -> [count, avg, min, max]

    public Wavelet(Integer startOrder, Integer endOrder, Integer resolution) {
        this.startOrder = startOrder;
        this.endOrder = endOrder;
        this.resolution = resolution;
        this.eventStats = new WaveletEventMapping();
    }

    Wavelet merge(Wavelet toMerge) {
        checkArgument(isNeighboringWavelet(toMerge), "The wavelet to merge is not a temporal neighbor of this wavelet");
        Wavelet mergedWavelet = new Wavelet(
                min(this.startOrder, toMerge.startOrder), max(this.endOrder, toMerge.endOrder), this.resolution+toMerge.resolution);
        mergedWavelet.eventStats = new WaveletEventMapping().merge(this.eventStats).merge(toMerge.eventStats);
        return mergedWavelet;
    }

    boolean isNeighboringWavelet(Wavelet otherWavelet) {
        return this.endOrder.equals(otherWavelet.startOrder)
                || this.startOrder.equals(otherWavelet.endOrder);
    }
    boolean isWithinOrderRange(Event event) {
        return event.order()>=startOrder && event.order()<endOrder;
    }

    void addEvent(Event event) {
        checkArgument(isWithinOrderRange(event), "This event is not in the sketch's time range");
        eventStats.add(event.id());
    }
}

