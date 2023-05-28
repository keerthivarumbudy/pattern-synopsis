package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.*;
import static tech.kee.CountingHelpers.*;

import static java.lang.Math.*;

public class TopKHelpers {
    public static Set<List<Integer>> generateSequentialPatterns(Map<Integer, Integer> eventTotalCountMap, Integer numberOfEventsPerPattern) throws IOException {
        List<Integer> sortedEventList_orig = Utils.getSortedEvents(eventTotalCountMap);
        List<Integer> sortedEventList = sortedEventList_orig.subList(0, 100);
        EventCombinations eventCombinationsObject = new EventCombinations();
        Map<List<Integer>, Integer> patternList = new HashMap<>();
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

            for(Integer event: sortedEventList) {
//                eventCombinationsObject.eventCombinations.add(new ArrayList<String>(partialCombination){{add(event);}});
                combination = new ArrayList<Integer>(partialCombination){{add(event);}};

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
        assert windows.stream().allMatch(window -> window >= streamSummary.resolutionSeconds): "All windows should be greater than or equal to resolution";

        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows

        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = streamSummary.getSummaryLayers(blockWindows);
        return layerSketches;
    }

    public static StreamSummary transformParameterForTopKLegacy(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k){
        assert numberOfEvents >= 2 : "Number of events should be greater than or equal to 2";
        assert numberOfEvents == windows.size() + 1 : "Number of windows should be one less than number of events";
        //assert that all windows are greater than resolution
        assert windows.stream().allMatch(window -> window >= streamSummary.resolutionSeconds): "All windows should be greater than or equal to resolution";
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window / streamSummary.resolutionSeconds).toList();
        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows
        // create composed sketches from subSketchesList
        StreamSummary temporaryStreamSummary = streamSummary;
        Collections.sort(blockWindows, Collections.reverseOrder());
        temporaryStreamSummary.legacyComposeSketches(blockWindows);
        return temporaryStreamSummary;
    }

    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> topKWithoutSequentialGeneration(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        // transform the parameters to be used for topK
        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = transformParameterForTopK(numberOfEvents, windows, streamSummary, k);
        // create combinations and patterns for all events
        //time generateAllPatterns
        long startTime = System.nanoTime();
        Set<List<Integer>> patterns = generateSequentialPatterns(streamSummary.eventTotalCountMap, numberOfEvents);
        long endTime = System.nanoTime();
        System.out.println("Time to generate "+ patterns.size()+ " patterns: " + (endTime - startTime) / 1000000 + "ms");
        // get upperbound for those patterns
        Map<List<Integer>, Integer> patternMap = new HashMap<>();
        startTime = System.nanoTime();
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
            System.out.println("kthBestValue=" + kthBestValue);
            System.out.println("Before pruning : patternMap.size()=" + patternMap.size());

            patternMap.entrySet().removeIf(entry -> entry.getValue() <= kthBestValue);
            System.out.println("After pruning  : patternMap.size()=" + patternMap.size());
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
        endTime = System.nanoTime();
        System.out.println("Time to get topK after pattern generation: " + (endTime - startTime) / 1000000 + "ms");
        return pq;
    }
    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> getTopKPatterns(Map<List<Integer>, Integer> patternList, StreamSummary temporaryStreamSummary, List<Integer> windows, int k, PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns){
        PriorityQueue<Map.Entry<List<Integer>, Integer>> pq = getTopKPatternsFromCount(patternList, k);
        // get the best resolution count for those patterns
        if(topKPatterns == null)
        topKPatterns = new PriorityQueue<>(k, (a, b) -> a.getValue() - b.getValue());
        while (pq.size() > 0) {
            List<Integer> pattern = pq.poll().getKey();
            // check if the pattern is already in the topKPatterns
            if(topKPatterns.stream().anyMatch(entry -> entry.getKey().equals(pattern)))
                continue;
            int bestValue = countPattern(pattern, windows, temporaryStreamSummary.layerSketchList.get(0));
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

    public static PriorityQueue<Map.Entry<List<Integer>, Integer>> topKWithSequentialGeneration(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        // transform the parameters to be used for topK
        StreamSummary temporaryStreamSummary = transformParameterForTopKLegacy(numberOfEvents, windows, streamSummary, k);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window / streamSummary.resolutionSeconds).toList();

        List<Integer> sortedEventList = Utils.getSortedEvents(temporaryStreamSummary.eventTotalCountMap);
        EventCombinations eventCombinationsObject = new EventCombinations();
        // creating the first partial combination
        List<Integer> partialCombination;
        List<Integer> combination;
        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns = null;
        int kthBestValue = -1;

        // initialization of the combinations
        List<Integer> partialComboIdx = new ArrayList<Integer>() {{
            for (int i = 0; i < numberOfEvents - 1; i++) {
                add(0);
            }
        }};
        List<Integer> lastPossiblePartialComboIdx = new ArrayList<Integer>() {{
            for (int i = 0; i < numberOfEvents - 1; i++) {
                add(sortedEventList.size() - 1);
            }
        }};
        // make a copy of the sorted event list because we will remove elements from sortedEventList as we start
        // pruning, and we need the original sortedEventList as we create combinations using indices
        // For example, we will create the next partial combination by moving from 0th to 1st element, but we may remove
        // the 0th element from sortedEventList as we prune, so we need the original sortedEventList to create the next
        // partial combination
        List<Integer> sortedEventListCopy = new ArrayList<Integer>(sortedEventList);

            // while we do not reach the end of possible combinations
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
                Map<List<Integer>, Integer> patternList = new HashMap<>();
                CombinationPatterns.permutePatternsFromCombinations(combination, new ArrayList<>(), patternList);
                Set<List<Integer>> patterns = patternList.keySet();
                for (List<Integer> pattern : patterns) {
                    int upperBound = countPattern(pattern, windows, temporaryStreamSummary.layerSketchList.get(temporaryStreamSummary.layerSketchList.size() - 1));
                    if (eventCombinationsObject.eventPatterns.size() > k) {
                        if (upperBound <= kthBestValue) {
                            pruned = true;
                            break;
                        }
                        if (kthBestValue == -1) {
                            // gets activated in the worst resolution layer only
                            topKPatterns = getTopKPatterns(eventCombinationsObject.eventPatterns, temporaryStreamSummary, windows, k, topKPatterns);
                            kthBestValue = topKPatterns.peek().getValue();
                            System.out.println("First while loop kthBestValue=" + kthBestValue);
                        }
                    }
                    eventCombinationsObject.eventPatterns.put(pattern, upperBound);
                }
                if (pruned)
                    break;

            }
            if (pruned) {
                int maxIdx = partialComboIdx.stream().max(Integer::compareTo).get();
                int removeFromIdx = max(maxIdx, lastAddedEventIdx);
                sortedEventList.removeAll(sortedEventList.subList(removeFromIdx + 1, sortedEventList.size()));
                lastPossiblePartialComboIdx = new ArrayList<Integer>() {{
                    for (int i = 0; i < numberOfEvents - 1; i++) {
                        add(sortedEventList.size() - 1);
                    }
                }};
            }
            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
            if (partialCombination.stream().distinct().count() == 1) {
                sortedEventList.remove(partialCombination.get(0));
            }
            // get the next partial combination
            partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);

        }
        for(int l = temporaryStreamSummary.layerSketchList.size()-2; l>=0; l--){
            Set<List<Integer>> patterns = new HashSet<>(eventCombinationsObject.eventPatterns.keySet());
            for(List<Integer> pattern: patterns){
                int upperBound = countPattern(pattern, windows, temporaryStreamSummary.layerSketchList.get(l));
                if (upperBound <= kthBestValue) {
                    eventCombinationsObject.eventPatterns.remove(pattern);
                }else{
                    eventCombinationsObject.eventPatterns.put(pattern, upperBound);
                }
            }
            // re-adjust topKPatterns
            topKPatterns = getTopKPatterns(eventCombinationsObject.eventPatterns, temporaryStreamSummary, windows, k, topKPatterns);
            kthBestValue = topKPatterns.peek().getValue();
            System.out.println("Outside the while loop, at layer "+l+" kthBestValue=" + kthBestValue);
        }
        return topKPatterns;
    }
}
