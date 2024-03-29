package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import tech.kee.model.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamSummary {
    final int resolutionEvents;

    List<Sketch> baseSummaryLayer;
    List<List<Sketch>> layerSketchList;
    Map<Integer, Integer> eventTotalCountMap;

    public StreamSummary(int resolutionEvents) {
        this.resolutionEvents = resolutionEvents;
        this.baseSummaryLayer = new ArrayList<>();
        this.layerSketchList = new ArrayList<>();
        this.eventTotalCountMap = new HashMap<>();
    }

    /**
     * Adds an event to the stream summary, updating the sketch of the base layer.
     * @param event the event to add.
     */
    void addEvent(Event event) {
        if(baseSummaryLayer.isEmpty()){
            Sketch sketch = new Sketch(
                    event.order(), event.order() + resolutionEvents);
            baseSummaryLayer.add(sketch);
        }
        Sketch currentSketch = baseSummaryLayer.get(baseSummaryLayer.size() - 1);
        if (currentSketch.isWithinOrderRange(event)) {
            currentSketch.addEvent(event);
        } else {
            Sketch newSketch=null;
            while(!currentSketch.isWithinOrderRange(event)){
                newSketch = new Sketch(
                        currentSketch.endOrder, currentSketch.endOrder + resolutionEvents);
                if(newSketch.isWithinOrderRange(event)){
                    break;
                }
                baseSummaryLayer.add(newSketch);
                currentSketch = newSketch;
            }
            newSketch.addEvent(event);
            baseSummaryLayer.add(newSketch);
        }
        eventTotalCountMap.put(event.id(), eventTotalCountMap.getOrDefault(event.id(), 0) + 1);
    }


    void addEvents(List<Event> events) {
        events.forEach(this::addEvent);
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
                var sketchesToMerge = currentLayer.subList(start, Math.min(start + mergeFactor, currentLayer.size()));
                nextLayer.add(sketchesToMerge.stream()
                        .reduce(Sketch::merge)
                        .orElseThrow());
            }
            summaryLayers.put(nextLevel, nextLayer.build());
            nextLevel++;
            currentLayer = nextLayer.build();
        }
        // making the whole stream into a layer
        ImmutableList.Builder<Sketch> nextLayer = ImmutableList.builder();
        nextLayer.add(currentLayer.subList(0, currentLayer.size()).stream()
                .reduce(Sketch::merge)
                .orElseThrow());
        summaryLayers.put(nextLevel, nextLayer.build());

        return summaryLayers.build();
    }


}
