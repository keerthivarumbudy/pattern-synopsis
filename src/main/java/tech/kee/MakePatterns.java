package tech.kee;

import java.util.*;

import static java.lang.Math.min;
import static tech.kee.CountingHelpers.countPattern;

public class MakePatterns {
    // Helper functions to make patterns and combinations

    public static List<Integer> getNextPartialCombination(
            List<Integer> partialComboIdx, List<Integer> lastPossiblePartialComboIdx, List<Integer> sortedEventList) {
        if (partialComboIdx.containsAll(lastPossiblePartialComboIdx) == true
                || partialComboIdx.stream().allMatch(i -> i > lastPossiblePartialComboIdx.get(0))) {
            return null;
        }
        if (lastPossiblePartialComboIdx.stream().allMatch(i -> i == 0)) {
            System.out.println("lastPossiblePartialComboIdx is all 0");
        }
        // get the index of minimum value in the list
        Integer minVal = partialComboIdx.stream()
                .min(Comparator.comparing(Integer::valueOf))
                .get();
        Integer minValIdx = partialComboIdx.indexOf(minVal);
        // increment the value at the index
        partialComboIdx.set(minValIdx, partialComboIdx.get(minValIdx) + 1);
        return partialComboIdx;
    }

    static void permutePatternsFromCombinations(List<Integer> s, List<Integer> l, Set<List<Integer>> patternList) {

        if (s.size() < 1) {
            s.addAll(l);
            patternList.add(s);
            return;
        }

        HashSet<Integer> uset = new HashSet<Integer>();
        for (int i = 0; i < s.size(); i++) {
            if (uset.contains(s.get(i))) continue;
            else uset.add(s.get(i));

            List<Integer> temp = new ArrayList<Integer>();
            if (i < s.size() - 1) {
                temp.addAll(s.subList(0, i));
                temp.addAll(s.subList(i + 1, s.size()));
            } else {
                temp.addAll(s.subList(0, i));
            }
            l.add(s.get(i));
            permutePatternsFromCombinations(temp, l, patternList);
            l.remove(l.size() - 1);
        }
    }

    static Map<Integer, Integer> getNeighbourCombinationIndices(
            Map<Integer, Integer> combinationIndices, Integer sortedEventListSize) {
        // increase the index of the smallest key in the map combinationsIndices, example: converting pattern 1112 to
        // 1113
        // if the smallest key is already at the end of the list, then return null
        // if the smallest key is not at the end of the list, then return the new combinationIndices
        Map<Integer, Integer> newCombinationIndices = new HashMap<>(combinationIndices);
        Integer minIdx = Collections.min(newCombinationIndices.keySet());
        if (minIdx == sortedEventListSize - 1) {
            return null;
        }
        newCombinationIndices.put(minIdx + 1, newCombinationIndices.getOrDefault(minIdx + 1, 0) + 1);
        newCombinationIndices.put(minIdx, newCombinationIndices.get(minIdx) - 1);
        if (newCombinationIndices.get(minIdx) == 0) {
            newCombinationIndices.remove(minIdx);
        }
        return newCombinationIndices;
    }

    static Map<Integer, Integer> getChildCombinationIndices(
            Map<Integer, Integer> combinationIndices, Integer sortedEventListSize) {
        // increase the index of the greatest key in the map combinationsIndices, example: converting pattern 1112 to
        // 1122
        // if the greatest key is already at the end of the list, then return null
        // if the greatest key is not at the end of the list, then return the newCombinationIndices
        Map<Integer, Integer> newCombinationIndices = new HashMap<>(combinationIndices);
        Integer maxIdx = Collections.max(newCombinationIndices.keySet());
        if (maxIdx == sortedEventListSize - 1) {
            return null;
        }
        newCombinationIndices.put(maxIdx + 1, newCombinationIndices.getOrDefault(maxIdx + 1, 0) + 1);
        newCombinationIndices.put(maxIdx, newCombinationIndices.get(maxIdx) - 1);
        if (newCombinationIndices.get(maxIdx) == 0) {
            newCombinationIndices.remove(maxIdx);
        }
        return newCombinationIndices;
    }

    public static List<Integer> getPatternFromCombinationIndices(
            Map<Integer, Integer> combinationIndices, List<Integer> sortedEventList) {
        List<Integer> pattern = new ArrayList<>();
        // add as many times as the value of the key
        for (Map.Entry<Integer, Integer> entry : combinationIndices.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                pattern.add(sortedEventList.get(entry.getKey()));
            }
        }
        return pattern;
    }

    public static Map<Integer, Integer> getNextCombinationIndices(
            Map<Integer, Integer> combinationIndices,
            Map<Integer, Integer> lastPossibleCombinationIndices,
            List<Integer> sortedEventList,
            List<Integer> windows,
            PriorityQueue<Map.Entry<Map<Integer, Integer>, Integer>> candidates,
            List<Sketch> layer,
            Set<Map<Integer,Integer>> combinationsHistory) {
        if(combinationIndices==null){
            return null;
        }
        // get the neighbour and child of the current combination
        Map<Integer, Integer> neighbourCombinationIndices =
                getNeighbourCombinationIndices(combinationIndices, sortedEventList.size());
        Map<Integer, Integer> childCombinationIndices =
                getChildCombinationIndices(combinationIndices, sortedEventList.size());
        if(neighbourCombinationIndices!=null){
            if (neighbourCombinationIndices.equals(lastPossibleCombinationIndices) || combinationsHistory.contains(neighbourCombinationIndices)) {
            // if we reached the end of the combinations
            neighbourCombinationIndices = null;
        }}
        if(childCombinationIndices!=null){
            if (childCombinationIndices.equals(lastPossibleCombinationIndices) || combinationsHistory.contains(childCombinationIndices)) {
                // if we reached the end of the combinations
                childCombinationIndices = null;
            }
        }
        int neighbourCount = 0;
        int childCount = 0;
        Map<Integer, Integer> combinationIndicesToReturn = null;
        if (neighbourCombinationIndices!=null && neighbourCombinationIndices.equals(childCombinationIndices)) {
            // just to remove one of the combinations from the candidates because they are both the same
            childCombinationIndices = null;
        }

        if (neighbourCombinationIndices == null && childCombinationIndices == null) {
            if (!candidates.isEmpty())
                combinationIndicesToReturn = candidates.poll().getKey();
        } else{
            if (neighbourCombinationIndices != null)
                neighbourCount = countPattern(
                        getPatternFromCombinationIndices(neighbourCombinationIndices, sortedEventList), windows, layer);
            if (childCombinationIndices != null)
                childCount = countPattern(
                        getPatternFromCombinationIndices(childCombinationIndices, sortedEventList), windows, layer);

                if (!candidates.isEmpty()) {
                    if (neighbourCount >= childCount && neighbourCount > candidates.peek().getValue()) {
                        // combination is assigned the neighbour
                        combinationIndicesToReturn = neighbourCombinationIndices;
                        // add the child to candidates
                        if (childCombinationIndices != null)
                            candidates.add(new AbstractMap.SimpleEntry<>(childCombinationIndices, childCount));
                } else if (childCount > neighbourCount && childCount > candidates.peek().getValue()) {
                        // combination is assigned the child
                        combinationIndicesToReturn = childCombinationIndices;
                        // add the neighbour to candidates
                        if (neighbourCombinationIndices != null)
                            candidates.add(new AbstractMap.SimpleEntry<>(neighbourCombinationIndices, neighbourCount));
                } else {
                        // dequeue the peek of candidates and add both neighbour and child to candidates
                        combinationIndicesToReturn = candidates.poll().getKey();
                        if (neighbourCombinationIndices != null)
                            candidates.add(new AbstractMap.SimpleEntry<>(neighbourCombinationIndices, neighbourCount));
                        if (childCombinationIndices != null)
                            candidates.add(new AbstractMap.SimpleEntry<>(childCombinationIndices, childCount));
                    }
                }else{
                    if (neighbourCount >= childCount) {
                        // combination is assigned the neighbour
                        combinationIndicesToReturn = neighbourCombinationIndices;
                        // add the child to candidates
                        if (childCombinationIndices != null)
                            candidates.add(new AbstractMap.SimpleEntry<>(childCombinationIndices, childCount));
                    } else if (childCount > neighbourCount) {
                        // combination is assigned the child
                        combinationIndicesToReturn = childCombinationIndices;
                        // add the neighbour to candidates
                        if (neighbourCombinationIndices != null)
                            candidates.add(new AbstractMap.SimpleEntry<>(neighbourCombinationIndices, neighbourCount));
                    }
                }
        }
        combinationsHistory.add(neighbourCombinationIndices);
        combinationsHistory.add(childCombinationIndices);
        return combinationIndicesToReturn;
    }
}
