package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import tech.kee.model.EventMapping;

import java.io.IOException;
import java.util.*;
import static tech.kee.CountingHelpers.*;
import static tech.kee.MakePatterns.*;

import static java.lang.Math.*;

public class TopKHelpers {
    public static Set<List<Integer>> generateSequentialPatterns(Map<Integer, Integer> eventTotalCountMap, Integer numberOfEventsPerPattern) throws IOException {
        List<Integer> sortedEventList_orig = Utils.getSortedEvents(eventTotalCountMap);
//        List<Integer> sortedEventList = sortedEventList_orig.subList(0, 100);
        List<Integer> sortedEventList = sortedEventList_orig;
        Set<List<Integer>> eventPatterns = new HashSet<>((int) pow(sortedEventList.size(),numberOfEventsPerPattern));
        // creating the first partial combination
        List<Integer> partialCombination;
        List<Integer> combination;

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
        List<Integer> sortedEventListCopy = new ArrayList<Integer>(sortedEventList);
        // while we do not reach the end of the hashmap
        while(partialComboIdx!=null){
            List<Integer> finalPartialComboIdx = partialComboIdx;
            partialCombination = new ArrayList<Integer>(){{
                for(int i: finalPartialComboIdx){
                    add(sortedEventListCopy.get(i));
                }
            }};
            Set<List<Integer>> patterns = new HashSet<>();
            for(Integer event: sortedEventList) {
                combination = new ArrayList<Integer>(partialCombination){{add(event);}};

                permutePatternsFromCombinations(combination, new ArrayList<>(), patterns);
                eventPatterns.addAll(patterns);
            }
            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
            if(partialCombination.stream().distinct().count() == 1){
                sortedEventList.remove(partialCombination.get(0));
            }
            // get the next partial combination
            partialComboIdx = getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);

        }
        // write the event patterns to a file
//        Utils.writeListToFile(eventCombinationsObject.eventPatterns.keySet());
        return eventPatterns;

    }

    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> getTopKPatternsFromCount(Map<List<Integer>, Integer> patternCountMap, Integer k) {
        // use space-saving algorithm to get the top k patterns
        // create a priority queue of size k
        PriorityQueue<Map.Entry<List<Integer>, Integer>> pq = new PriorityQueue<>(k, (a,b)->a.getValue()-b.getValue());
        // add the first k elements to the priority queue
        for(Map.Entry<List<Integer>, Integer> entry: patternCountMap.entrySet()){
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
    public static ImmutableMap<Integer, ImmutableList<Sketch>> transformParameterForTopK(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k){
        assert numberOfEvents >= 2 : "Number of events should be greater than or equal to 2";
        assert numberOfEvents == windows.size() + 1 : "Number of windows should be one less than number of events";
        //assert that all windows are greater than resolution

        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows

        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = streamSummary.getSummaryLayers(blockWindows);
        return layerSketches;
    }

    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> topKWithoutSequentialGeneration(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        // transform the parameters to be used for topK
        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = transformParameterForTopK(numberOfEvents, windows, streamSummary, k);
        // create combinations and patterns for all events
        //time generateAllPatterns
//        long startTime = System.nanoTime();
        Set<List<Integer>> patterns = generateSequentialPatterns(streamSummary.eventTotalCountMap, numberOfEvents);
//        long endTime = System.nanoTime();
//        System.out.println("Time to generate "+ patterns.size()+ " patterns: " + (endTime - startTime) / 1000000 + "ms");
        // get upperbound for those patterns
        Map<List<Integer>, Integer> patternMap = new HashMap<>();
//        startTime = System.nanoTime();
        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns = null;
        for (int i = layerSketches.size()-1; i > 0; i--) {
            for (List<Integer> pattern : patterns) {
                int upperBound = countPattern(pattern, windows, layerSketches.get(i));
                patternMap.put(pattern, upperBound);
            }
            // get topK patterns
            PriorityQueue<Map.Entry<List<Integer>, Integer>> pq = getTopKPatternsFromCount(patternMap, k);
            // get the best resolution count for those patterns
            topKPatterns = new PriorityQueue<>(k, (a, b) -> a.getValue() - b.getValue());
            while (pq.size() > 0) {
                List<Integer> pattern = pq.poll().getKey();
                if (topKPatterns.contains(pattern))
                    continue;
                int bestValue = countPattern(pattern, windows, layerSketches.get(i-1));
                topKPatterns.add(Map.entry(pattern, bestValue));
            }

            // get the kth best resolution count out of the top-k patterns
            int kthBestValue = topKPatterns.peek().getValue();
            // prune the patterns from patternMap that have count less than the best resolution count of kth pattern
//            System.out.println("kthBestValue=" + kthBestValue);
//            System.out.println("Before pruning : patternMap.size()=" + patternMap.size());

            patternMap.entrySet().removeIf(entry -> entry.getValue() <= kthBestValue);
//            System.out.println("After pruning  : patternMap.size()=" + patternMap.size());
            // if patternMap is empty, then return the topKPatterns
            if (patternMap.size() == 0) {
                return topKPatterns;
            }
            // else, continue with the next layer
            patterns = patternMap.keySet();
        }

        // get the best value for the remaining patterns with better resolution
        for (List<Integer> pattern : patterns) {
            int bestValue = countPattern(pattern, windows, layerSketches.get(0));
            patternMap.put(pattern, bestValue);
        }
        // get topK patterns
        PriorityQueue<Map.Entry<List<Integer>, Integer>> pq = getTopKPatternsFromCount(patternMap, k);
        // return the topKPatterns
//        endTime = System.nanoTime();
//        System.out.println("Time to get topK after pattern generation: " + (endTime - startTime) / 1000000 + "ms");
        return pq;
    }
    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> getTopKPatterns(Map<List<Integer>, Integer> patternList, ImmutableMap<Integer, ImmutableList<Sketch>> temporaryStreamSummary, List<Integer> windows, int k, PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns){
        PriorityQueue<Map.Entry<List<Integer>, Integer>> pq = getTopKPatternsFromCount(patternList, k);
        // get the best resolution count for those patterns
        if(topKPatterns == null)
        topKPatterns = new PriorityQueue<>(k, (a, b) -> a.getValue() - b.getValue());
        while (pq.size() > 0) {
            List<Integer> pattern = pq.poll().getKey();
            // check if the pattern is already in the topKPatterns
            if(topKPatterns.stream().anyMatch(entry -> entry.getKey().equals(pattern)))
                continue;
            int bestValue = countPattern(pattern, windows, temporaryStreamSummary.get(0));
            if(topKPatterns.size()<k)
                topKPatterns.add(Map.entry(pattern, bestValue));
            else{
                if(bestValue>topKPatterns.peek().getValue()){
                    topKPatterns.poll();
                    topKPatterns.add(Map.entry(pattern, bestValue));
                }
            }
        }
        return topKPatterns;
    }

    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> topKWithSequentialGeneration(Integer numEventsPerPattern, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        // transform the parameters to be used for topK
        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = transformParameterForTopK(numEventsPerPattern, windows, streamSummary, k);
        List<Integer> sortedEventList = Utils.getSortedEventsFromSketch(streamSummary.eventTotalCountMap.keySet(),layerSketches.get(layerSketches.size()-1).get(0));

        // creating the first partial combination
        List<Integer> partialCombination;
        List<Integer> combination;
        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns = null;
        int kthBestValue = -1;

        // initialization of the combinations
        List<Integer> partialComboIdx = new ArrayList<Integer>() {{
            for (int i = 0; i < numEventsPerPattern - 1; i++) {
                add(0);
            }
        }};
        List<Integer> lastPossiblePartialComboIdx = new ArrayList<Integer>() {{
            for (int i = 0; i < numEventsPerPattern - 1; i++) {
                add(sortedEventList.size() - 1);
            }
        }};
        // make a copy of the sorted event list because we will remove elements from sortedEventList as we start
        // pruning, and we need the original sortedEventList as we create combinations using indices
        // For example, we will create the next partial combination by moving from 0th to 1st element, but we may remove
        // the 0th element from sortedEventList as we prune, so we need the original sortedEventList to create the next
        // partial combination
        List<Integer> sortedEventListCopy = new ArrayList<Integer>(sortedEventList);
        Map<List<Integer>, Integer> patternMap = new HashMap<>();
            // while we do not reach the end of possible combinations
        // Issue with CM-Sketch is due to the fact that the sorted event list is made on the basis of eventTotalCountMap from the stream summary
        // which has the correct count of the event, whereas the upperbound of the event is calculated using the sketch
        // therefore, events with low count, which would either have been ignored, will get a higher count in the CM Sketch
        // due to the overestimation/collisions in CM Sketch
        while (partialComboIdx != null) {
            Integer lastAddedEventIdx = -1;
            Boolean pruned = false;
            List<Integer> PartialComboIdxCopy = partialComboIdx;
            partialCombination = new ArrayList<Integer>() {{
                for (int i : PartialComboIdxCopy) {
                    add(sortedEventListCopy.get(i));
                }
            }};
            for (int i = 0; i < sortedEventList.size(); i++) {
                lastAddedEventIdx = i;
                int iCopy = i;
                combination = new ArrayList<Integer>(partialCombination) {{
                    add(sortedEventList.get(iCopy));
                }};
                Set<List<Integer>> patterns = new HashSet<>();
                permutePatternsFromCombinations(combination, new ArrayList<>(), patterns);
                for (List<Integer> pattern : patterns) {
                    int upperBound = countPattern(pattern, windows, layerSketches.get(layerSketches.size() - 1));
                    if (patternMap.size() > k) {
                        if (upperBound <= kthBestValue) {
                            pruned = true;
                            break;
                        }
                        if (kthBestValue == -1) {
                            // gets activated in the worst resolution layer only
                            topKPatterns = getTopKPatterns(patternMap, layerSketches, windows, k, topKPatterns);
                            kthBestValue = topKPatterns.peek().getValue();
//                            System.out.println("First while loop kthBestValue=" + kthBestValue);
                        }
                    }

                    patternMap.put(pattern, upperBound);
                }
                if (pruned)
                    break;

            }
            if (pruned) {
                int maxIdx = partialComboIdx.stream().max(Integer::compareTo).get();
                int removeFromIdx = max(maxIdx, lastAddedEventIdx);
                if(removeFromIdx < sortedEventList.size() - 1)
                    sortedEventList.removeAll(sortedEventList.subList(removeFromIdx + 1, sortedEventList.size()));
                lastPossiblePartialComboIdx = new ArrayList<Integer>() {{
                    for (int i = 0; i < numEventsPerPattern - 1; i++) {
                        add(sortedEventList.size() - 1);
                    }
                }};
            }
            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
            if (partialCombination.stream().distinct().count() == 1) {
                sortedEventList.remove(partialCombination.get(0));
            }
            // get the next partial combination
            partialComboIdx = getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);

        }

        for(int l = layerSketches.size()-2; l>=0; l--){
            Set<List<Integer>> patterns = new HashSet<>(patternMap.keySet());
            for(List<Integer> pattern: patterns){
                int upperBound = countPattern(pattern, windows, layerSketches.get(l));
                if (upperBound <= kthBestValue) {
                    patternMap.remove(pattern);
                }else{
                    patternMap.put(pattern, upperBound);
                }
            }
            // re-adjust topKPatterns
            topKPatterns = getTopKPatterns(patternMap, layerSketches, windows, k, topKPatterns);
            kthBestValue = topKPatterns.peek().getValue();
//            System.out.println("Outside the while loop, at layer "+l+" kthBestValue=" + kthBestValue);
        }
        return topKPatterns;
    }
    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> getTopKNaive(Integer numEventsPerPattern, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        Set<List<Integer>> patterns = generateSequentialPatterns(streamSummary.eventTotalCountMap, numEventsPerPattern);
        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns = new PriorityQueue<>(k, Map.Entry.comparingByValue());
        Map<List<Integer>, Integer> patternMap = new HashMap<>();
        for (List<Integer> pattern : patterns) {
            int count = countPattern(pattern, windows, streamSummary.baseSummaryLayer);
            patternMap.put(pattern, count);
        }
        topKPatterns = getTopKPatternsFromCount(patternMap, k);
        return topKPatterns;
    }

}
