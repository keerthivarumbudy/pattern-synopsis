package org.example;

import java.io.IOException;
import java.util.*;
import org.example.HelperFunctions;

import static java.lang.Math.min;
import static org.example.HelperFunctions.countPattern;
import static org.example.HelperFunctions.transformParameterForTopK;

public class EventCombinations {
    Set<List<String>> eventCombinations;
    Map<List<String>, Integer> eventPatterns;
    // <EventId, SingleEventCombinations>

    // {Combination,{Pattern: UpperBound} }
    Map<List<String>,Map<List<String>, Integer>> combinationPatternMap;

    public EventCombinations(){
        this.eventCombinations = new HashSet<>();
        this.combinationPatternMap = new HashMap<>();
        this.eventPatterns = new HashMap<>();
    }

    public List<Integer> getNextPartialCombination(List<Integer> partialComboIdx, List<Integer> lastPossiblePartialComboIdx, List<String> sortedEventList){
        if(partialComboIdx.containsAll(lastPossiblePartialComboIdx) == true){
            return null;
        }
        // get the index of minimum value in the list
        Integer minVal = partialComboIdx.stream().min(Comparator.comparing(Integer::valueOf)).get();
        Integer minValIdx = partialComboIdx.indexOf(minVal);
        // increment the value at the index
        partialComboIdx.set(minValIdx, partialComboIdx.get(minValIdx) + 1);
        return partialComboIdx;
    }
//    public static PriorityQueue<Map.Entry<List<String>, Integer>> topKWithSequentialGeneration(Integer numberOfEvents, List<Integer> windows, Sketch sketch, int k) throws IOException {
//        // transform the parameters to be used for topK
//        Sketch temporarySketch = transformParameterForTopK(numberOfEvents, windows, sketch, k);
//        // divide all the windows by resolution
//        windows = windows.stream().map(window -> window / sketch.resolution).toList();
//
//        List<String> sortedEventList = Utils.getSortedEvents(temporarySketch.eventTotalCountMap);
//        EventCombinations eventCombinationsObject = new EventCombinations();
//        Map<List<String>, Integer> patternList = new HashMap<>();
//        // creating the first partial combination
//        List<String> partialCombination;
//        List<String> combination;
//        PriorityQueue<Map.Entry<List<String>, Integer>> topKPatterns = null;
//        int kthBestValue = -1;
//        List<Integer> partialComboIdx = new ArrayList<Integer>(){{
//            for(int i=0; i<numberOfEvents-1; i++){
//                add(0);
//            }
//        }};
//        List<Integer> lastPossiblePartialComboIdx = new ArrayList<Integer>(){{
//            for(int i=0; i<numberOfEvents-1; i++){
//                add(sortedEventList.size()-1);
//            }
//        }};
//        // make a copy of the sorted event list
//        List<String> sortedEventListCopy = new ArrayList<String>(sortedEventList);
//        // while we do not reach the end of possible combinations
//        while(partialComboIdx!=null){
//            Boolean pruned = false;
//            List<Integer> finalPartialComboIdx = partialComboIdx;
//            partialCombination = new ArrayList<String>(){{
//                for(int i: finalPartialComboIdx){
//                    add(sortedEventListCopy.get(i));
//                }
//            }};
//
//            for(String event: sortedEventList) {
////                eventCombinationsObject.eventCombinations.add(new ArrayList<String>(partialCombination){{add(event);}});
//                combination = new ArrayList<String>(partialCombination){{add(event);}};
//                CombinationPatterns.permutePatternsFromCombinations(combination, new ArrayList<>(), patternList);
//                eventCombinationsObject.combinationPatternMap.put(combination, patternList);
//                eventCombinationsObject.eventPatterns.putAll(patternList);
//                if(patternList.size()>=k){
//                    Set<List<String>> patterns =patternList.keySet();
//                    for (List<String> pattern : patterns) {
//                        int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(temporarySketch.layerSketchList.size()-1));
//                        if(upperBound<=kthBestValue){
//                            pruned = true;
//                            break;
//                        }
//                        patternList.put(pattern, upperBound);
//                    }
//                    topKPatterns = getTopKPatterns(patternList, temporarySketch, windows, k, topKPatterns);
//                    kthBestValue = topKPatterns.peek().getValue();
//                    System.out.println("kthBestValue=" + kthBestValue);
//                    break;
//                }
//                // get the next partial combination
////                partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
//            }
//            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
//            if(partialCombination.stream().distinct().count() == 1){
//                sortedEventList.remove(partialCombination.get(0));
//            }
//            // get the next partial combination
//            partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
//            if(kthBestValue!=-1){
//                break;
//            }
//        }
//
//        // while we do not reach the end of possible combinations
//        while(partialComboIdx!=null){
//            List<Integer> finalPartialComboIdx = partialComboIdx;
//            Integer lastAddedEventIdx = -1;
//            partialCombination = new ArrayList<String>(){{
//                for(int i: finalPartialComboIdx){
//                    add(sortedEventListCopy.get(i));
//                }
//            }};
//            Boolean pruned = false;
//            for(int i=0; i<sortedEventList.size();i++) {
////                eventCombinationsObject.eventCombinations.add(new ArrayList<String>(partialCombination){{add(event);}});
//                lastAddedEventIdx = i;
//                Integer finalLastAddedEventIdx = lastAddedEventIdx;
//                combination = new ArrayList<String>(partialCombination){{add(sortedEventList.get(finalLastAddedEventIdx));}};
//                Map<List<String>, Integer> patternListNew = new HashMap<>();
//                CombinationPatterns.permutePatternsFromCombinations(combination, new ArrayList<>(), patternListNew);
//                // get upper bound for all the patterns
//                Set<List<String>> patterns = patternListNew.keySet();
//                for (List<String> pattern : patterns) {
//                    if(pattern.containsAll(List.of("1005115", "1004767"))){
//                        System.out.println("pattern="+pattern);
//                    }
//                    int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(temporarySketch.layerSketchList.size()-1));
//                    if (upperBound <= kthBestValue){
//                        pruned = true;
////                        System.out.println("Pattern being pruned - "+pattern.toString()+" with upper bound - "+upperBound);
//                        continue;
//                    }
//                    patternList.put(pattern, upperBound);
//                }
//                if(pruned)
//                    break;
//
//                // get the next partial combination
////                partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
//            }
//            if(pruned){
//                int minIdx = partialComboIdx.stream().min(Integer::compareTo).get();
//                int removeFromIdx = min(minIdx, lastAddedEventIdx);
//                sortedEventList.removeAll(sortedEventList.subList(removeFromIdx+1, sortedEventList.size()));
////                System.out.println("Pruned");
//                if(sortedEventList.size()==0){
//                    System.out.println("Returning because sorted event list is empty");
//                    return topKPatterns;
//                }
//
//            }
//            // check if the combination made has all the same events. If yes, then remove that event from sortedEventList
//            if(partialCombination.stream().distinct().count() == 1){
//                sortedEventList.remove(partialCombination.get(0));
//            }
//            // get the next partial combination
//            partialComboIdx = eventCombinationsObject.getNextPartialCombination(partialComboIdx, lastPossiblePartialComboIdx, sortedEventList);
//        }
//        for(int l=temporarySketch.layerSketchList.size()-2; l>0; l-- ){
//            Map<List<String>, Integer> patternListCopy = new HashMap<>(patternList);
//            Set<List<String>> patterns = patternListCopy.keySet();
//            for(List<String> pattern: patterns){
//                int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(l));
//                if(upperBound<kthBestValue){
//                    patternList.remove(pattern);
//                }
//                else{
//                    patternList.put(pattern, upperBound);
//                }
//            }
//            if(patternList.size()==k){
//                System.out.println("Returning because pattern list size is k");
//                return topKPatterns;
//            }
//
//            topKPatterns =    getTopKPatterns(patternList, temporarySketch, windows, k, topKPatterns);
//            kthBestValue = topKPatterns.peek().getValue();
//            System.out.println("kthBestValue="+kthBestValue);
//        }
////        // if there are still patterns left in the list, then get the top k patterns by calculating best value and sort them
////        for(List<String> pattern: patternList.keySet()){
////            int upperBound = countPattern(pattern, windows, temporarySketch.layerSketchList.get(0));
////            patternList.put(pattern, upperBound);
////        }
////        topKPatterns = getTopKPatternsFromUpperBound(patternList, k);
//        topKPatterns =    getTopKPatterns(patternList, temporarySketch, windows, k, topKPatterns);
//        System.out.println("Returning after the whoooole thing");
//        return topKPatterns;
//
//    }
}
