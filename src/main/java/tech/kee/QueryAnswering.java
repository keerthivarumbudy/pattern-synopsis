package tech.kee;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static java.lang.Math.negateExact;
import static tech.kee.HelperFunctions.*;
import static tech.kee.Utils.*;

public class QueryAnswering {
    public static int answerCount(List<Integer> eventIds, List<Integer> windows, StreamSummary streamSummary){
        // !!!right now the support is only for 2 event patterns
        assert eventIds.size() >= 2;
        assert eventIds.size() == windows.size() + 1;
        //assert that all windows are greater than resolution
        assert windows.stream().allMatch(window -> window >= streamSummary.resolutionSeconds);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window/ streamSummary.resolutionSeconds).toList();
        return countPattern(eventIds, windows, streamSummary.layerSketchList.get(0));
    }

    public static Map<List<Integer>, Integer> answerTopKWithoutSequentialGeneration(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns = topKWithSequentialGeneration(numberOfEvents, windows, streamSummary, k);
        Map<List<Integer>, Integer> topKPatternsMap = new HashMap<>();
        for(Map.Entry<List<Integer>, Integer> entry: topKPatterns)
            topKPatternsMap.put(entry.getKey(), entry.getValue());
        //sort the map by value
        topKPatternsMap = sortByValue(topKPatternsMap);
        return topKPatternsMap;
    }


    public static int countEvent(List<Integer> event_ids, List<Integer> windows, List<Sketch> layerSketches){
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++){
            count += layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
        }
        return count;
    }

    public static void upperBoundExperiments( List<Integer> windows, StreamSummary streamSummary) throws IOException {
        assert windows.stream().allMatch(window -> window >= streamSummary.resolutionSeconds) : "All windows should be greater than or equal to resolution";
        windows = windows.stream().map(window -> window/ streamSummary.resolutionSeconds).toList() ;
        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows
        // create composed sketches from subSketchesList
        StreamSummary temporaryStreamSummary = streamSummary;
        Collections.sort(blockWindows, Collections.reverseOrder());
        temporaryStreamSummary.composeSketches(blockWindows);
        // get the upper bound one by one through the layers
        Map<List<Integer>, Map<Integer, Integer>> patternMapsPerLayer = new HashMap<>();
        Set<List<Integer>> patterns = generateSequentialPatterns(temporaryStreamSummary.eventTotalCountMap, 2);
        for(List<Integer> pattern: patterns){
            for(int i = temporaryStreamSummary.layerSketchList.size()-1; i>=0; i--){
                int upperBound = countPattern(pattern, windows, temporaryStreamSummary.layerSketchList.get(i));
                patternMapsPerLayer.putIfAbsent(pattern, new HashMap<>());
                patternMapsPerLayer.get(pattern).put(i, upperBound);
//                System.out.println("Layer with resolution="+temporarySketch.layerSketchList.get(i).get(0).resolution+" has upperbound="+upperBound+
//                        " for pattern "+event_ids.toString()+" with window "+(windows.get(0)*sketch.resolution));
            }
        }
        for(List<Integer> pattern: patternMapsPerLayer.keySet()){
            // check if upperbound of higher layer is greater than lower layer
            for(int i = 0; i< temporaryStreamSummary.layerSketchList.size()-1; i++){
                if(patternMapsPerLayer.get(pattern).get(i) > patternMapsPerLayer.get(pattern).get(i+1))
//                    System.out.println("Upperbound of layer with resolution="+ temporaryStreamSummary.layerSketchList.get(i).get(0).resolution+" is less than layer with resolution="+ temporaryStreamSummary.layerSketchList.get(i+1).get(0).resolution);
                    System.out.println("YAGNI");
            }
        }
        return ;
    }



}
