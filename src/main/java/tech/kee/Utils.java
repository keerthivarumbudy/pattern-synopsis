package tech.kee;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;

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
    public static List<Integer> getSortedEvents(Map<Integer, Integer> eventTotalCountMap){
        // in descending order
        List<Integer> sortedEvents = new ArrayList<>();
        Map<Integer, Integer> sortedMap = new LinkedHashMap<>();
        eventTotalCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
        for(Integer key : sortedMap.keySet()){
            sortedEvents.add(key);
        }
        return sortedEvents;
    }

    public static int forLoopThroughSketch(int i, List<Integer> numBlocks, List<SubSketch> layerSketches, List<String> event_ids, int count1, int eventIdx, int count){
        if(eventIdx == event_ids.size()) {
            return count1;
        }
        for (int j = i; j < min(i + numBlocks.get(eventIdx-1), layerSketches.size()); j++) {
            int count2 = layerSketches.get(j).eventCountMap.getOrDefault(event_ids.get(eventIdx), 0);
            if(count2 <= 0)
                continue;
            if(count<0)
                System.out.println("count is negative "+ count);
            count+=  forLoopThroughSketch(j, numBlocks, layerSketches, event_ids, count1*count2, eventIdx + 1, count);
        }

        return count;
    }

    public static void writeListToFile(Set<List<Integer>> stringList) throws IOException {
        FileWriter fileWriter = new FileWriter("output.txt");
        for (List<Integer> str : stringList) {
            fileWriter.write(str + System.lineSeparator());
        }
        fileWriter.close();
    }
    static Map<List<Integer>, Integer> sortByValue(Map<List<Integer>, Integer> topKPatternsMap) {
        List<Map.Entry<List<Integer>, Integer>> list = new LinkedList<>(topKPatternsMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<List<Integer>, Integer>>() {
            @Override
            public int compare(Map.Entry<List<Integer>, Integer> o1, Map.Entry<List<Integer>, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        }.reversed());
        Map<List<Integer>, Integer> sortedMap = new LinkedHashMap<>();
        for(Map.Entry<List<Integer>, Integer> entry: list){
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
