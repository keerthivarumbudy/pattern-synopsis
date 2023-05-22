package org.example;

import com.google.common.collect.ImmutableSet;

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
        return countPattern(event_ids, windows, sketch.layerSketchList.get(0));
    }
    public static int countPattern(List<String> event_ids, List<Integer> numBlocks, List<SubSketch> layerSketches){
        // numBlocks is the window value of the pattern in terms of subsketches in the layer
        // i.e. numBlocks = window/resolution

        // !!!right now the support is only for 2 event patterns
        // !!!also take care of counting the same event within the same subsketch for a pattern with itself
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++){
            int count1 = layerSketches.get(i).eventCountMap.getOrDefault(event_ids.get(0), 0);
            for(int j=i; j<min(i+numBlocks.get(0), layerSketches.size()); j++) {
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
        for(int i=temporarySketch.layerSketchList.size()-1; i>=0; i--){
            int upperBound = countPattern(event_ids, windows, temporarySketch.layerSketchList.get(i));
            System.out.println("Layer with resolution="+temporarySketch.layerSketchList.get(i).get(0).resolution+" has upperbound="+upperBound+
                    " for pattern "+event_ids.toString()+" with window "+(windows.get(0)*sketch.resolution));
        }
    }

    public static List<ImmutableSet<String>> generateSequentialPatterns(Map<String, Integer> eventTotalCountMap, Integer numberOfEventsPerPattern){
        List<String> sortedEventList = Utils.getSortedEvents(eventTotalCountMap);
        EventCombinations eventCombinationsObject = new EventCombinations();

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
        // while we do not reach the end if the hashmap
        while(partialComboIdx!=null){
            List<Integer> finalPartialComboIdx = partialComboIdx;
            partialCombination = new ArrayList<String>(){{
                for(int i: finalPartialComboIdx){
                    add(sortedEventListCopy.get(i));
                }
            }};

            for(String event: sortedEventList) {
                eventCombinationsObject.eventCombinations.add(new ArrayList<String>(partialCombination){{add(event);}});
                combination = new ArrayList<String>(partialCombination){{add(event);}};
                // create the list if not exists already and add the new combination
                if(!eventCombinationsObject.singleEventCombinationsMap.containsKey(event))
                eventCombinationsObject.singleEventCombinationsMap.put(event,new ArrayList<>());
                eventCombinationsObject.singleEventCombinationsMap.get(event).add(combination);
            }
            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
            if(partialCombination.stream().distinct().count() == 1){
                sortedEventList.remove(partialCombination.get(0));
            }
            // get the next partial combination
            partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);

        }

        return null;

    }

}
