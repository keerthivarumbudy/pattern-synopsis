package org.example;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Utils {
    public static List<Integer> primeFactorization(Integer n){
        List<Integer> primeFactors = new ArrayList<>();
        while (n % 2 == 0) {
            primeFactors.add(2);
            n /= 2;
        }
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            while (n % i == 0) {
                primeFactors.add(i);
                n /= i;
            }
        }
        if (n > 2) {
            primeFactors.add(n);
        }
        return primeFactors;
    }

    // create a sorted list of events based on the value from eventTotalCountMap
    public static List<String> getSortedEvents(Map<String, Integer> eventTotalCountMap){
        // in descending order
        List<String> sortedEvents = new ArrayList<>();
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        eventTotalCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        for(String key : sortedMap.keySet()){
            sortedEvents.add(key);
        }
        return sortedEvents;
    }
}
