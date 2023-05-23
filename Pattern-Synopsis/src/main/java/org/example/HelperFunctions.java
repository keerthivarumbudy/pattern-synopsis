package org.example;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;

public class HelperFunctions {
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
//                if(i==j)
//                    count2 /= 2;
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
//                if(i==j)
//                    count2 /= 2;
                for(int k = j; k < min(j + numBlocks.get(1), layerSketches.size()); k++){
                    int count3 = layerSketches.get(k).eventCountMap.getOrDefault(event_ids.get(2), 0);
//                    if(j==k)
//                        count3 /= 2;
                    count += count1 * count2 * count3;
                }
            }
        }

        return count;
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

    public static PriorityQueue<Map.Entry<List<String>, Integer>> getTopKPatternsFromUpperBound(Map<List<String>, Integer> patternCountMap, Integer k) {

        // use space-saving algorithm to get the top k patterns
        // create a priority queue of size k
        PriorityQueue<Map.Entry<List<String>, Integer>> pq = new PriorityQueue<>(k, (a,b)->a.getValue()-b.getValue());
        // add the first k elements to the priority queue
        for(Map.Entry<List<String>, Integer> entry: patternCountMap.entrySet()){
            if(pq.size()<k)
                pq.add(entry);
            else{
                if(entry.getValue()>pq.peek().getValue()){
                    pq.poll();
                    pq.add(entry);
                }
            }
        }
        return pq;
    }
}
