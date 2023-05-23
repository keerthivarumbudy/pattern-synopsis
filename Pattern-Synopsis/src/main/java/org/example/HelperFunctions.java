package org.example;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static org.example.HelperFunctions.transformParameterForTopK;

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
        List<String> sortedEventList = sortedEventList_orig.subList(0, 100);
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
//                if(!eventCombinationsObject.singleEventCombinationsMap.containsKey(event))
//                    eventCombinationsObject.singleEventCombinationsMap.put(event,new ArrayList<>());
//                eventCombinationsObject.singleEventCombinationsMap.get(event).add(combination);
                //

                    //
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
    public static Sketch transformParameterForTopK(Integer numberOfEvents, List<Integer> windows, Sketch sketch, int k){
        assert numberOfEvents >= 2;
        assert numberOfEvents == windows.size() + 1;
        //assert that all windows are greater than resolution
        assert windows.stream().allMatch(window -> window >= sketch.resolution);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window / sketch.resolution).toList();
        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows
        // create composed sketches from subSketchesList
        Sketch temporarySketch = sketch;
        Collections.sort(blockWindows, Collections.reverseOrder());
        temporarySketch.composeSketches(blockWindows);
        return temporarySketch;
    }
    public static PriorityQueue<Map.Entry<List<String>, Integer>> topKWithoutSequentialGeneration(Integer numberOfEvents, List<Integer> windows, Sketch sketch, int k) throws IOException {
        // transform the parameters to be used for topK
        Sketch temporarySketch = transformParameterForTopK(numberOfEvents, windows, sketch, k);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window / sketch.resolution).toList();
        // create combinations and patterns for all events
        //time generateAllPatterns
        long startTime = System.nanoTime();
        Set<List<String>> patterns = generateSequentialPatterns(temporarySketch.eventTotalCountMap, numberOfEvents);
        long endTime = System.nanoTime();
        System.out.println("Time to generate patterns: " + (endTime - startTime) / 1000000 + "ms");
        // get upperbound for those patterns
        Map<List<String>, Integer> patternMap = new HashMap<>();
        startTime = System.nanoTime();
        PriorityQueue<Map.Entry<List<String>, Integer>> topKPatterns = null;
        for (int i = temporarySketch.layerSketchList.size() - 1; i > 0; i--) {
            for (List<String> pattern : patterns) {
                int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(i));
                patternMap.put(pattern, upperBound);
            }
            // get topK patterns
            PriorityQueue<Map.Entry<List<String>, Integer>> pq = getTopKPatternsFromUpperBound(patternMap, k);
            // get the best resolution count for those patterns
            topKPatterns = new PriorityQueue<>(k, (a, b) -> a.getValue() - b.getValue());
            while (pq.size() > 0) {
                List<String> pattern = pq.poll().getKey();
                if (topKPatterns.contains(pattern))
                    continue;
                int bestValue = countPattern(pattern, windows, temporarySketch.layerSketchList.get(0));
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
        for (List<String> pattern : patterns) {
            int bestValue = countPattern(pattern, windows, temporarySketch.layerSketchList.get(0));
            patternMap.put(pattern, bestValue);
        }
        // get topK patterns
        PriorityQueue<Map.Entry<List<String>, Integer>> pq = getTopKPatternsFromUpperBound(patternMap, k);
        // return the topKPatterns
        endTime = System.nanoTime();
        System.out.println("Time to get topK after pattern generation: " + (endTime - startTime) / 1000000 + "ms");
        return pq;
    }

    public static PriorityQueue<Map.Entry<List<String>, Integer>> topKWithSequentialGeneration(Integer numberOfEvents, List<Integer> windows, Sketch sketch, int k) throws IOException {
        // transform the parameters to be used for topK
        Sketch temporarySketch = transformParameterForTopK(numberOfEvents, windows, sketch, k);
        // divide all the windows by resolution
        windows = windows.stream().map(window -> window / sketch.resolution).toList();

        List<String> sortedEventList_orig = Utils.getSortedEvents(temporarySketch.eventTotalCountMap);
//        List<String> sortedEventList = sortedEventList_orig.subList(0, 1000);
        List<String> sortedEventList = sortedEventList_orig;
        EventCombinations eventCombinationsObject = new EventCombinations();
        Map<List<String>, Integer> patternList = new HashMap<>();
        // creating the first partial combination
        List<String> partialCombination;
        List<String> combination;
        PriorityQueue<Map.Entry<List<String>, Integer>> topKPatterns = null;
        int kthBestValue = -1;
        List<Integer> partialComboIdx = new ArrayList<Integer>(){{
            for(int i=0; i<numberOfEvents-1; i++){
                add(0);
            }
        }};
        List<Integer> lastPossiblePartialComboIdx = new ArrayList<Integer>(){{
            for(int i=0; i<numberOfEvents-1; i++){
                add(sortedEventList.size()-1);
            }
        }};
        // make a copy of the sorted event list
        List<String> sortedEventListCopy = new ArrayList<String>(sortedEventList);
        // while we do not reach the end of possible combinations
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
                CombinationPatterns.permutePatternsFromCombinations(combination, new ArrayList<>(), patternList);
                eventCombinationsObject.combinationPatternMap.put(combination, patternList);
                eventCombinationsObject.eventPatterns.putAll(patternList);
                if(patternList.size()>=k){
                    Set<List<String>> patterns =patternList.keySet();
                    for (List<String> pattern : patterns) {
                        int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(temporarySketch.layerSketchList.size()-1));
                        patternList.put(pattern, upperBound);
                    }
                    PriorityQueue<Map.Entry<List<String>, Integer>> pq = getTopKPatternsFromUpperBound(patternList, k);
                    // get the best resolution count for those patterns
                    topKPatterns = new PriorityQueue<>(k, (a, b) -> a.getValue() - b.getValue());
                    while (pq.size() > 0) {
                        List<String> pattern = pq.poll().getKey();
                        if (topKPatterns.contains(pattern))
                            continue;
                        int bestValue = countPattern(pattern, windows, temporarySketch.layerSketchList.get(0));
                        topKPatterns.add(Map.entry(pattern, bestValue));
                    }
                    kthBestValue = topKPatterns.peek().getValue();
                    System.out.println("kthBestValue=" + kthBestValue);
                    break;
                }
                // get the next partial combination
                partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
            }
            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
            if(partialCombination.stream().distinct().count() == 1){
                sortedEventList.remove(partialCombination.get(0));
            }
            // get the next partial combination
            partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
            if(kthBestValue!=-1){
                break;
            }
        }
        // while we do not reach the end of possible combinations
        while(partialComboIdx!=null){
            Integer lastAddedEventIdx = -1;
            List<Integer> finalPartialComboIdx = partialComboIdx;
            partialCombination = new ArrayList<String>(){{
                for(int i: finalPartialComboIdx){
                    add(sortedEventListCopy.get(i));
                }
            }};
            Boolean pruned = false;
            for(int i=0; i<sortedEventList.size();i++) {
//                eventCombinationsObject.eventCombinations.add(new ArrayList<String>(partialCombination){{add(event);}});
                lastAddedEventIdx = i;
                Integer finalLastAddedEventIdx = lastAddedEventIdx;
                combination = new ArrayList<String>(partialCombination){{add(sortedEventList.get(finalLastAddedEventIdx));}};
                Map<List<String>, Integer> patternListNew = new HashMap<>();
                CombinationPatterns.permutePatternsFromCombinations(combination, new ArrayList<>(), patternListNew);
                // get upper bound for all the patterns
                Set<List<String>> patterns = patternListNew.keySet();
                for (List<String> pattern : patterns) {
                    int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(temporarySketch.layerSketchList.size()-1));
                    if (upperBound < kthBestValue){
                        pruned = true;
//                        System.out.println("Pattern being pruned - "+pattern.toString()+" with upper bound - "+upperBound);
                        continue;
                    }
                    patternList.put(pattern, upperBound);
                }
                if(pruned)
                    break;

                // get the next partial combination
                partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
            }
            if(pruned){
                int minIdx = partialComboIdx.stream().min(Integer::compareTo).get();
                int removeFromIdx = min(minIdx, lastAddedEventIdx);
                sortedEventList.removeAll(sortedEventList.subList(removeFromIdx+1, sortedEventList.size()));
//                System.out.println("Pruned");
                if(sortedEventList.size()==0){
                    System.out.println("Returning because sorted event list is empty");
                    return topKPatterns;
                }

            }
            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
            if(partialCombination.stream().distinct().count() == 1){
                sortedEventList.remove(partialCombination.get(0));
            }
            // get the next partial combination
            partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
        }
        for(int l=temporarySketch.layerSketchList.size()-2; l>0; l-- ){
            Map<List<String>, Integer> patternListCopy = new HashMap<>(patternList);
            Set<List<String>> patterns = patternListCopy.keySet();
            for(List<String> pattern: patterns){
                int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(l));
                if(upperBound<kthBestValue){
                    patternList.remove(pattern);
                }
                else{
                    patternList.put(pattern, upperBound);
                }
            }
            if(patternList.size()==k){
                System.out.println("Returning because pattern list size is k");
                return topKPatterns;
            }

            PriorityQueue<Map.Entry<List<String>, Integer>> pq = getTopKPatternsFromUpperBound(patternList, k);
            // get the best resolution count for those patterns
            topKPatterns = new PriorityQueue<>(k, (a, b) -> a.getValue() - b.getValue());
            while (pq.size() > 0) {
                List<String> pattern = pq.poll().getKey();
                if (topKPatterns.contains(pattern))
                    continue;
                int bestValue = countPattern(pattern, windows, temporarySketch.layerSketchList.get(0));
                topKPatterns.add(Map.entry(pattern, bestValue));
            }
            kthBestValue = topKPatterns.peek().getValue();
        }
        // if there are still patterns left in the list, then get the top k patterns by calculating best value and sort them
        for(List<String> pattern: patternList.keySet()){
            int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(0));
            patternList.put(pattern, upperBound);
        }
        topKPatterns = getTopKPatternsFromUpperBound(patternList, k);
        System.out.println("Returning after the whoooole thing");
        return topKPatterns;

    }

}
