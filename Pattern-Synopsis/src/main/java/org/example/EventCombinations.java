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
}
