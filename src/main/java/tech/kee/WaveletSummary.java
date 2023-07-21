package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import tech.kee.model.Event;

import java.util.ArrayList;
import java.util.List;

public class WaveletSummary {
    List<Wavelet> baseLayer;
    Integer baseResolution;

    public WaveletSummary(int resolution) {
        this.baseLayer = new ArrayList<>();
        this.baseResolution = resolution;
    }
    void addEvent(Event event) {
        if(baseLayer.isEmpty()){
            Wavelet wavelet = new Wavelet(
                    event.order(), event.order() + baseResolution, baseResolution);
            baseLayer.add(wavelet);
        }
        Wavelet currentWavelet = baseLayer.get(baseLayer.size() - 1);
        if (currentWavelet.isWithinOrderRange(event)) {
            currentWavelet.addEvent(event);
        } else {
            Wavelet newWavelet=null;
            while(!currentWavelet.isWithinOrderRange(event)){
                newWavelet = new Wavelet(
                        currentWavelet.endOrder, currentWavelet.endOrder + baseResolution, baseResolution);
                if(newWavelet.isWithinOrderRange(event)){
                    break;
                }
                baseLayer.add(newWavelet);
                currentWavelet = newWavelet;
            }
            newWavelet.addEvent(event);
            baseLayer.add(newWavelet);
        }
    }

    void addEvents(List<Event> events) {
        events.forEach(this::addEvent);
    }

    ImmutableMap<Integer, ImmutableList<Wavelet>> getSummaryLayers(List<Integer> mergingFactors) {
        ImmutableMap.Builder<Integer, ImmutableList<Wavelet>> summaryLayers = ImmutableMap.builder();
        int nextLevel = 1;
        List<Wavelet> currentLayer = baseLayer;
        for (int mergeFactor : mergingFactors) {
            ImmutableList.Builder<Wavelet> nextLayer = ImmutableList.builder();
            for (int start = 0; start < currentLayer.size(); start += mergeFactor) {
                var sketchesToMerge = currentLayer.subList(start, Math.min(start + mergeFactor, currentLayer.size()));
                nextLayer.add(sketchesToMerge.stream().reduce(Wavelet::merge).orElseThrow());
            }
            summaryLayers.put(nextLevel, nextLayer.build());
            nextLevel++;
            currentLayer = nextLayer.build();
        }
        // making the whole stream into a layer
        ImmutableList.Builder<Wavelet> nextLayer = ImmutableList.builder();
        nextLayer.add(currentLayer.subList(0, currentLayer.size()).stream()
                .reduce(Wavelet::merge)
                .orElseThrow());
        summaryLayers.put(nextLevel, nextLayer.build());
        return summaryLayers.build();
    }
    

}
