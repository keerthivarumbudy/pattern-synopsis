package org.example;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;

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
    public static int countPattern(List<String> event_ids, List<Integer> numBlocks, List<SubSketch> layerSketches){
        if(event_ids.size()==2)
            return countPattern2(event_ids, numBlocks, layerSketches);
        else
            return countPattern3(event_ids, numBlocks, layerSketches);
        // numBlocks is the window value of the pattern in terms of subsketches in the layer
        // i.e. numBlocks = window/resolution

        // !!!right now the support is only for 2 event patterns
        // !!!also take care of counting the same event within the same subsketch for a pattern with itself
//        Integer count = 0;
//        int count1;
//        for(int i=0; i<layerSketches.size(); i++){
//            count1 = layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
//            if(count1!=0)
//                for(int j=1; j<event_ids.size(); j++)
//                    count +=Utils.forLoopThroughSketch(i, numBlocks, layerSketches, event_ids, count1, 1, 0);
//            count +=Utils.forLoopThroughSketch(i, numBlocks, layerSketches, event_ids, count1, 1, 0);

//        }
    }
    public static int countPattern2(List<String> event_ids, List<Integer> numBlocks, List<SubSketch> layerSketches){
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++) {
            int count1 = layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
            for (int j = i; j < min(i + numBlocks.get(0), layerSketches.size()); j++) {
                int count2 = layerSketches.get(j).eventCountMap.getOrDefault(event_ids.get(1), 0);
                if(i==j)
                    count2 /= 2;
                count += count1 * count2;
            }
        }
        return count;
    }
    public static int countPattern3(List<String> event_ids, List<Integer> numBlocks, List<SubSketch> layerSketches){
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++) {
            int count1 = layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
            for (int j = i; j < min(i + numBlocks.get(0), layerSketches.size()); j++) {
                int count2 = layerSketches.get(j).eventCountMap.getOrDefault(event_ids.get(1), 0);
                if(i==j)
                    count2 /= 2;
                for(int k = j; k < min(j + numBlocks.get(1), layerSketches.size()); k++){
                    int count3 = layerSketches.get(k).eventCountMap.getOrDefault(event_ids.get(2), 0);
                    if(j==k)
                        count3 /= 2;
                    count += count1 * count2 * count3;
                }
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
        Collections.sort(blockWindows, Collections.reverseOrder());
        temporarySketch.composeSketches(blockWindows);
        // create combinations and patterns for all events

        // get upperbound for those patterns
        // get topK patterns
        // get the best resolution count for those patterns
        // prune the patterns that have count less than the best resolution count of kth pattern
        // get the upperbound for the remaining patterns with better resolution
        return null;
    }
    public static int countEvent(List<String> event_ids, List<Integer> windows, List<SubSketch> layerSketches, int resolution){
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++){
            count += layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
        }
        return count;
    }

    public static void upperBoundExperiments(List<String> event_ids, List<Integer> windows, Sketch sketch){
        assert event_ids.size() >= 2: "The number of events should be greater than or equal to 2";
        assert event_ids.size() == windows.size() + 1: "The number of windows should be one less than the number of events";
        assert windows.stream().allMatch(window -> window >= sketch.resolution) : "All windows should be greater than or equal to resolution";
        windows = windows.stream().map(window -> window/sketch.resolution).toList() ;
        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows
        // create composed sketches from subSketchesList
        Sketch temporarySketch = sketch;
        Collections.sort(blockWindows, Collections.reverseOrder());
        temporarySketch.composeSketches(blockWindows);
        // get the upper bound one by one through the layers
        Map<Integer, Map<List<String>, Integer>> patternMapsPerLayer = new HashMap<>();
        for(int i=temporarySketch.layerSketchList.size()-1; i>=0; i--){
            int upperBound = countPattern(event_ids, windows, temporarySketch.layerSketchList.get(i));
            System.out.println("Layer with resolution="+temporarySketch.layerSketchList.get(i).get(0).resolution+" has upperbound="+upperBound+
                    " for pattern "+event_ids.toString()+" with window "+(windows.get(0)*sketch.resolution));
        }
    }

    public static Set<List<String>> generateSequentialPatterns(Map<String, Integer> eventTotalCountMap, Integer numberOfEventsPerPattern) throws IOException {
        List<String> sortedEventList_orig = Utils.getSortedEvents(eventTotalCountMap);
        List<String> sortedEventList = sortedEventList_orig.subList(0, 10);
        EventCombinations eventCombinationsObject = new EventCombinations();
        Map<List<String>, Integer> patternList = new HashMap<>();
        // creating the first partial combination
        List<String> partialCombination;
        List<String> combination;

        List<Integer> partialComboIdx = new ArrayList<Integer>(){{
            for(int i=0; i<numberOfEventsPerPattern-1; i++){
                add(0);
            }
        }};
        List<Integer> lastPossiblePartialComboIdx = new ArrayList<Integer>(){{
            for(int i=0; i<numberOfEventsPerPattern-1; i++){
                add(sortedEventList.size()-1);
            }
        }};
        // make a copy of the sorted event list
        List<String> sortedEventListCopy = new ArrayList<String>(sortedEventList);
        // while we do not reach the end of the hashmap
        while(partialComboIdx!=null){
            List<Integer> finalPartialComboIdx = partialComboIdx;
            partialCombination = new ArrayList<String>(){{
                for(int i: finalPartialComboIdx){
                    add(sortedEventListCopy.get(i));
                }
            }};



            for(String event: sortedEventList) {
//                eventCombinationsObject.eventCombinations.add(new ArrayList<String>(partialCombination){{add(event);}});
                combination = new ArrayList<String>(partialCombination){{add(event);}};
                // create the list if not exists already and add the new combination
                if(!eventCombinationsObject.singleEventCombinationsMap.containsKey(event))
                    eventCombinationsObject.singleEventCombinationsMap.put(event,new ArrayList<>());
                eventCombinationsObject.singleEventCombinationsMap.get(event).add(combination);
                CombinationPatterns.permutePatternsFromCombinations(combination, new ArrayList<>(), patternList);
                eventCombinationsObject.combinationPatternMap.put(combination, patternList);
                eventCombinationsObject.eventPatterns.putAll(patternList);
            }
            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
            if(partialCombination.stream().distinct().count() == 1){
                sortedEventList.remove(partialCombination.get(0));
            }
            // get the next partial combination
            partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);

        }
        // write the event patterns to a file
        Utils.writeListToFile(eventCombinationsObject.eventPatterns.keySet());
        return eventCombinationsObject.eventPatterns.keySet();

    }

}
