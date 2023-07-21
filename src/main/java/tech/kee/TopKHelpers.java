package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Hash;
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
        Set<List<Integer>> patterns = generateSequentialPatterns(streamSummary.eventTotalCountMap, numberOfEvents);
        // get upperbound for those patterns
        Map<List<Integer>, Integer> patternMap = new HashMap<>((int) patterns.size());
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
                int bestValue = countPattern(pattern, windows, layerSketches.get(0));
                topKPatterns.add(Map.entry(pattern, bestValue));
            }

            // get the kth best resolution count out of the top-k patterns
            int kthBestValue = topKPatterns.peek().getValue();
            // prune the patterns from patternMap that have count less than the best resolution count of kth pattern
            patternMap.entrySet().removeIf(entry -> entry.getValue() <= kthBestValue);
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
        return pq;
    }

    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> topkTestingLowerbound(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        // transform the parameters to be used for topK
        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = transformParameterForTopK(numberOfEvents, windows, streamSummary, k);
        // create combinations and patterns for all events
        //time generateAllPatterns
        Set<List<Integer>> patterns = generateSequentialPatterns(streamSummary.eventTotalCountMap, numberOfEvents);
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
                int bestValue = getLowerboundCount(pattern, windows, layerSketches.get(i));
                topKPatterns.add(Map.entry(pattern, bestValue));
            }
            // get the kth best resolution count out of the top-k patterns
            int kthBestValue = topKPatterns.peek().getValue();
            // prune the patterns from patternMap that have count less than the best resolution count of kth pattern
            patternMap.entrySet().removeIf(entry -> entry.getValue() <= kthBestValue);
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
    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> topKWithNewSortedPatterns(Integer numEventsPerPattern, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = transformParameterForTopK(numEventsPerPattern, windows, streamSummary, k);
        List<Integer> sortedEventList = Utils.getSortedEventsFromSketch(streamSummary.eventTotalCountMap.keySet(),layerSketches.get(layerSketches.size()-1).get(0));
        Map<Integer,Integer> combinationIndices = new HashMap<>();
        Set<Map<Integer,Integer>> combinationsHistory = new HashSet<>((int) pow(sortedEventList.size(),numEventsPerPattern));
        // assign the first combination to be the numEventsPerPattern times the first element in the sorted event list
        combinationIndices.put(0,numEventsPerPattern);
        Map<List<Integer>, Integer> patternMap = new HashMap<>((int) pow(sortedEventList.size(),numEventsPerPattern)); // storing all the patterns and their count estimates
        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns = null;
        PriorityQueue<Map.Entry<Map<Integer,Integer>, Integer>> candidates = new PriorityQueue<>(k, (a, b) -> b.getValue() - a.getValue()); // make sure this is storing in descending order
        int kthBestValue = -1;
        Map<Integer,Integer> lastPossibleCombinationIndices = new HashMap<>();
        // assign last combination to be the numEventsPerPattern times the last element in the sorted event list
        lastPossibleCombinationIndices.put(sortedEventList.size()-1,numEventsPerPattern);
        while(combinationIndices != null){
            Set<List<Integer>> patterns = new HashSet<>();
            // get all the patterns, their estimates and store them in patternMap based on the condition
            permutePatternsFromCombinations(getPatternFromCombinationIndices(combinationIndices, sortedEventList), new ArrayList<>(), patterns);
            for (List<Integer> pattern : patterns) {
                int upperBound = countPattern(pattern, windows, layerSketches.get(layerSketches.size() - 1));
                if (patternMap.size() > k) {
                    if (upperBound <= kthBestValue) {
                        combinationIndices = null;
                        break;
                    }
                    if (kthBestValue == -1) {
                        // gets activated in the worst resolution layer only
                        topKPatterns = getTopKPatterns(patternMap, layerSketches, windows, k, topKPatterns);
                        kthBestValue = topKPatterns.peek().getValue();
                    }
                }
                patternMap.put(pattern, upperBound);
                }
            // getting the next combination
            combinationIndices = getNextCombinationIndices(combinationIndices, lastPossibleCombinationIndices, sortedEventList, windows, candidates, layerSketches.get(layerSketches.size()-1), combinationsHistory);
        }
        // now that we have the patternMap, we move to the next layers and prune the patterns
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
