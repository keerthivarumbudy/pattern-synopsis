package org.example;

import java.util.List;

import static java.lang.Math.min;

public class QueryAnswering {
    public static int countPattern(List<String> event_ids, List<Integer> windows, List<SubSketch> layerSketches, int resolution){
        // !!!right now the support is only for 2 event patterns
        assert event_ids.size() >= 2;
        assert event_ids.size() == windows.size() + 1;
        //assert that all windows are greater than resolution
        assert windows.stream().allMatch(window -> window >= resolution);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window/resolution).toList();
        int sum_of_windows = windows.stream().mapToInt(Integer::intValue).sum();
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++){
            int count1 = layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
            for(int j=i; j<min(i+windows.get(0), layerSketches.size()); j++) {
                int count2 = layerSketches.get(j).eventCountMap.getOrDefault(event_ids.get(1), 0);
                count +=  count1*count2;
            }
        }
        return count;
    }
    public static List<String> topK(Integer numberOfEvents, List<Integer> windows, Sketch sketch){
        assert numberOfEvents >= 2;
        assert numberOfEvents == windows.size() + 1;
        //assert that all windows are greater than resolution
        assert windows.stream().allMatch(window -> window >= sketch.resolution);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window/sketch.resolution).toList();
        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows
        // create composed sketches from subSketchesList
        Sketch temporarySketch = sketch;
        temporarySketch.composeSketches(blockWindows);
        return null;
    }
    public static int countEvent(List<String> event_ids, List<Integer> windows, List<SubSketch> layerSketches, int resolution){
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++){
            count += layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
        }
        return count;
    }
}
