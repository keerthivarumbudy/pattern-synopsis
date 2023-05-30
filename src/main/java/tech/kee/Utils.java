package tech.kee;

import java.io.File;
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



    public static void writeListToFile(Set<List<Integer>> stringList, String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(filename+".txt");
        for (List<Integer> str : stringList) {
            fileWriter.write(str + System.lineSeparator());
        }
        fileWriter.close();
    }
    public static void writeResultsToFile(List<String> header,List<Integer> results, String filename) throws IOException {
        // check if file exists
        File f = new File(filename+".txt");
        if(!f.exists() ) {
            // do something
            FileWriter fileWriter = new FileWriter(filename+".txt");
            for (String str : header) {
                fileWriter.write(str + " ");
            }
            fileWriter.write(System.lineSeparator());
            fileWriter.close();
        }
        FileWriter fileWriter = new FileWriter(filename+".txt", true);
        for (int result : results) {
            fileWriter.write(result  + " ");
        }
        fileWriter.write(System.lineSeparator());
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
