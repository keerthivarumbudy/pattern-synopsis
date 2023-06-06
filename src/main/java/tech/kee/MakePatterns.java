package tech.kee;

import java.util.*;

import static java.lang.Math.min;

public class MakePatterns {

    public static List<Integer> getNextPartialCombination(List<Integer> partialComboIdx, List<Integer> lastPossiblePartialComboIdx, List<Integer> sortedEventList){
        if(partialComboIdx.containsAll(lastPossiblePartialComboIdx) == true || partialComboIdx.stream().allMatch(i -> i>lastPossiblePartialComboIdx.get(0))){
            return null;
        }if(lastPossiblePartialComboIdx.stream().allMatch(i -> i == 0)){
            System.out.println("lastPossiblePartialComboIdx is all 0");
        }
        // get the index of minimum value in the list
        Integer minVal = partialComboIdx.stream().min(Comparator.comparing(Integer::valueOf)).get();
        Integer minValIdx = partialComboIdx.indexOf(minVal);
        // increment the value at the index
        partialComboIdx.set(minValIdx, partialComboIdx.get(minValIdx) + 1);
        return partialComboIdx;
    }
    static void permutePatternsFromCombinations(List<Integer> s, List<Integer> l, Set<List<Integer>> patternList) {

        if (s.size() < 1){
            s.addAll(l);
            patternList.add(s);
            return;
        }

        HashSet<Integer> uset = new HashSet<Integer>();
        for (int i=0; i<s.size(); i++){
            if(uset.contains(s.get(i)))
                continue;
            else
                uset.add(s.get(i));

            List<Integer> temp = new ArrayList<Integer>();
            if(i<s.size()-1){
                temp.addAll(s.subList(0, i));
                temp.addAll(s.subList(i+1, s.size()));
            }
            else{
                temp.addAll(s.subList(0, i));
            }
            l.add(s.get(i));
            permutePatternsFromCombinations(temp, l, patternList);
            l.remove(l.size()-1);
        }
    }
}
