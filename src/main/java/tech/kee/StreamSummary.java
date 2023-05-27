package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import tech.kee.model.Event;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamSummary {
    final int resolutionSeconds;

    List<Sketch> baseSummaryLayer;
    List<List<Sketch>> layerSketchList;
    Map<Integer, Integer> eventTotalCountMap;

    public StreamSummary(int resolutionSeconds) {
        this.resolutionSeconds = resolutionSeconds;
        this.baseSummaryLayer = new ArrayList<>();
        this.layerSketchList = new ArrayList<>();
        this.eventTotalCountMap = new HashMap<>();
    }

    /**
     * Adds an event to the stream summary, updating the sketch of the base layer.
     * @param event the event to add.
     */
    void addEvent(Event event) {
        Sketch currentSketch = baseSummaryLayer.isEmpty()
                ? new Sketch(event.timestamp(), event.timestamp().plus(resolutionSeconds, ChronoUnit.SECONDS))
                : baseSummaryLayer.get(baseSummaryLayer.size() - 1);
        if (currentSketch.isWithinTimeRange(event)) {
            currentSketch.addEvent(event);
        } else {
            Sketch newSketch = new Sketch(
                    currentSketch.endTimestamp, currentSketch.endTimestamp.plus(resolutionSeconds, ChronoUnit.SECONDS));
            newSketch.addEvent(event);
            baseSummaryLayer.add(newSketch);
        }
        eventTotalCountMap.put(event.id(), eventTotalCountMap.getOrDefault(event.id(), 0) + 1);
    }

    void legacyAddEvents(List<Event> events) {
        Sketch sketch = new Sketch(
                events.get(0).timestamp(), events.get(0).timestamp().plus(resolutionSeconds, ChronoUnit.SECONDS));
        layerSketchList.add(new ArrayList<>());
        layerSketchList.get(0).add(sketch);
        Sketch currentSketch = sketch;
        for (Event event : events) {
            if ((event.timestamp().isAfter(currentSketch.endTimestamp))) {
                Sketch newSketch = new Sketch(
                        currentSketch.endTimestamp,
                        currentSketch.endTimestamp.plus(resolutionSeconds, ChronoUnit.SECONDS));
                // First increment...
                eventTotalCountMap.merge(event.id(), 1, Integer::sum);
                layerSketchList.get(0).add(newSketch);
                currentSketch = newSketch;
            }
            currentSketch.eventCountMap.merge(event.id(), 1, Integer::sum);
            // Second increment??
            eventTotalCountMap.merge(event.id(), 1, Integer::sum);
        }
    }

    void addEvents(List<Event> events) {
//        events.forEach(this::addEvent);
        this.legacyAddEvents(events);
    }

    /**
     * Creates a summary layer for each merging factor, and returns a map of the summary layers.
     * @param mergingFactors the sorted merging factors to use for creating the summary layers.
     * @return a map of the summary layers, where the key is the level, and the value is the summary layer.
     */
    ImmutableMap<Integer, ImmutableList<Sketch>> getSummaryLayers(List<Integer> mergingFactors) {
        ImmutableMap.Builder<Integer, ImmutableList<Sketch>> summaryLayers = ImmutableMap.builder();
        summaryLayers.put(0, ImmutableList.copyOf(baseSummaryLayer));
        int nextLevel = 1;
        List<Sketch> currentLayer = baseSummaryLayer;
        for (int mergeFactor : mergingFactors) {
            ImmutableList.Builder<Sketch> nextLayer = ImmutableList.builder();
            for (int start = 0; start < currentLayer.size(); start += mergeFactor) {
                nextLayer.add(currentLayer.subList(start, Math.min(start + mergeFactor, currentLayer.size())).stream()
                        .reduce(Sketch::merge)
                        .orElseThrow());
            }
            summaryLayers.put(nextLevel, nextLayer.build());
            nextLevel++;
        }
        return summaryLayers.build();
    }

    public void composeSketches(List<Integer> blockWindows) {
        // create layerSketch for each blockWindow, and group(concatenate) blockWindow number of the subsketches from
        // the previous blockwindow
        for (int i : blockWindows) {
            // get the index of the previous layerSketch
            int prevBlockIdx = this.layerSketchList.size() - 1;
            this.layerSketchList.add(new ArrayList<>());
            for (int j = 0; j < this.layerSketchList.get(prevBlockIdx).size(); j += i) {

                Sketch sketch = new Sketch(
                        this.layerSketchList.get(prevBlockIdx).get(j).startTimestamp,
                        j + i < this.layerSketchList.get(prevBlockIdx).size()
                                ? this.layerSketchList.get(prevBlockIdx).get(j + i - 1).endTimestamp
                                : this.layerSketchList
                                        .get(prevBlockIdx)
                                        .get(this.layerSketchList
                                                        .get(prevBlockIdx)
                                                        .size()
                                                - 1)
                                        .endTimestamp);

                try {
                    for (int k = j; k < j + i; k++) {
                        for (Integer eventId : this.layerSketchList
                                .get(prevBlockIdx)
                                .get(k)
                                .eventCountMap
                                .keySet()) {
                            sketch.eventCountMap.put(
                                    eventId,
                                    sketch.eventCountMap.getOrDefault(eventId, 0)
                                            + this.layerSketchList
                                                    .get(prevBlockIdx)
                                                    .get(k)
                                                    .eventCountMap
                                                    .get(eventId));
                        }
                    }
                } catch (Exception e) {

                }
                this.layerSketchList.get(prevBlockIdx + 1).add(sketch);
            }
        }
    }
}
