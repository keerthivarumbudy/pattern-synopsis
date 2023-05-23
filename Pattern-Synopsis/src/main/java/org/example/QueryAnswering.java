package org.example;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static java.lang.Math.negateExact;
import static org.example.HelperFunctions.*;
import static org.example.Utils.*;

public class QueryAnswering {
    public static int answerCount(List<String> event_ids, List<Integer> windows, Sketch sketch){
        // !!!right now the support is only for 2 event patterns
        assert event_ids.size() >= 2;
        assert event_ids.size() == windows.size() + 1;
        //assert that all windows are greater than resolution
        assert windows.stream().allMatch(window -> window >= sketch.resolution);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window/sketch.resolution).toList();
        if(event_ids.size()==2)
            return countPattern(event_ids, windows, sketch.layerSketchList.get(0));
        else
            return countPattern3(event_ids, windows, sketch.layerSketchList.get(0));
    }

    public static Map<List<String>, Integer> answerTopKWithoutSequentialGeneration(Integer numberOfEvents, List<Integer> windows, Sketch sketch, int k) throws IOException {
//        PriorityQueue<Map.Entry<List<String>, Integer>> topKPatterns = topKWithoutSequentialGeneration(numberOfEvents, windows, sketch, k);
        PriorityQueue<Map.Entry<List<String>, Integer>> topKPatterns = topKWithSequentialGeneration(numberOfEvents, windows, sketch, k);
        Map<List<String>, Integer> topKPatternsMap = new HashMap<>();
        for(Map.Entry<List<String>, Integer> entry: topKPatterns)
            topKPatternsMap.put(entry.getKey(), entry.getValue());
        //sort the map by value
        topKPatternsMap = sortByValue(topKPatternsMap);
        return topKPatternsMap;
    }


    public static int countEvent(List<String> event_ids, List<Integer> windows, List<SubSketch> layerSketches, int resolution){
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++){
            count += layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
        }
        return count;
    }

    public static void upperBoundExperiments( List<Integer> windows, Sketch sketch) throws IOException {

        assert windows.stream().allMatch(window -> window >= sketch.resolution) : "All windows should be greater than or equal to resolution";
        windows = windows.stream().map(window -> window/sketch.resolution).toList() ;
        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows
        // create composed sketches from subSketchesList
        Sketch temporarySketch = sketch;
        Collections.sort(blockWindows, Collections.reverseOrder());
        temporarySketch.composeSketches(blockWindows);
        // get the upper bound one by one through the layers
        Map<List<String>, Map<Integer, Integer>> patternMapsPerLayer = new HashMap<>();
        Set<List<String>> patterns = generateSequentialPatterns(temporarySketch.eventTotalCountMap, 3);
        for(List<String> pattern: patterns){
            for(int i=temporarySketch.layerSketchList.size()-1; i>=0; i--){
                int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(i));
                patternMapsPerLayer.putIfAbsent(pattern, new HashMap<>());
                patternMapsPerLayer.get(pattern).put(i, upperBound);
//                System.out.println("Layer with resolution="+temporarySketch.layerSketchList.get(i).get(0).resolution+" has upperbound="+upperBound+
//                        " for pattern "+event_ids.toString()+" with window "+(windows.get(0)*sketch.resolution));
            }
        }
        return ;
    }



}
