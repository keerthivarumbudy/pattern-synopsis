package org.example;

import java.util.*;

public class CombinationPatterns {
    // <Combination,[Pattern, UpperBound] >
    Map<List<String>,List<Map<List<String>, Integer>>> combinationPatternMap;

    static void permutePatternsFromCombinations(List<String> s, List<String> l, int n) {
        if (s.size() < 1){
            s.addAll(l);
            System.out.println(s);
            return;
        }

        HashSet<String> uset = new HashSet<String>();
        for (int i=0; i<s.size(); i++){
            if(uset.contains(s.get(i)))
                continue;
            else
                uset.add(s.get(i));

            List<String> temp = new ArrayList<String>();
            if(i<s.size()-1){
                temp.addAll(s.subList(0, i));
                temp.addAll(s.subList(i+1, s.size()));
            }
            else{
                temp.addAll(s.subList(0, i));
            }
            l.add(s.get(i));
            permutePatternsFromCombinations(temp, l, n);
            l.remove(l.size()-1);
        }
    }
}
