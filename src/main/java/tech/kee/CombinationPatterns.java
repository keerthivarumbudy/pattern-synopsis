package tech.kee;

import java.util.*;

public class CombinationPatterns {

    static void permutePatternsFromCombinations(List<Integer> s, List<Integer> l, Map<List<Integer>, Integer> patternList) {

        if (s.size() < 1){
            s.addAll(l);
            patternList.put(s,0);
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
