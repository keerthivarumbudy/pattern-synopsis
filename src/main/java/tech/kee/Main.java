package tech.kee;

import com.google.common.collect.ImmutableSet;
import tech.kee.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<Event> events = null;

    // read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-1m.csv";
        // time reading from csv
        long startTime = System.nanoTime();
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 2, 20000 );

        } catch (IOException e) {
            e.printStackTrace();
        }
//        long endTime = System.nanoTime();
//        System.out.println("Time taken to read from csv: " + (endTime - startTime) / 1000000 + " ms");
        if(events.size() != 0){
            // create sketch
            StreamSummary streamSummary = new StreamSummary(
                    1);
            streamSummary.addEvents(events);
            // query answering
            long startTime1 = System.nanoTime();
            int count = QueryAnswering.answerCount(List.of(1004856,1004856), List.of(500), streamSummary);
            long endTime1 = System.nanoTime();
            System.out.println(count);
            System.out.println("Time taken for count: " + (endTime1 - startTime1)/1000000 + "ms");
//            int count_event = QueryAnswering.countEvent(List.of(1004856), List.of(6000), streamSummary);
//            System.out.println("count_event "+count_event);
//            QueryAnswering.upperBoundExperiments( List.of(6000, 600), sketch);
//            List<ImmutableSet<String>> res = QueryAnswering.generateSequentialPatterns(sketch.eventTotalCountMap, 3);

//             testing permutePatternsFromCombinations
//            List<Integer> testList = new ArrayList<>(){{
//                add(1);
//                add(2);
//                add(3);
//                add(4);
//            }};
//            // sort the list
//            testList.sort(Integer::compareTo);
//            Map<List<Integer>> patternList = new ArrayList<>() ;
//            CombinationPatterns.permutePatternsFromCombinations(testList,new ArrayList<>(), patternList);
//            System.out.println(patternList);

            // testing generateSequentialPatterns
//            QueryAnswering.generateSequentialPatterns(sketch.eventTotalCountMap, 2);
            // time the topKWithoutSequentialGeneration
            long startTimeTopK = System.nanoTime();
            Map<List<Integer>, Integer> topKPatterns = QueryAnswering.answerTopK(2, List.of( 500), streamSummary, 10);
            long endTimeTopK = System.nanoTime();
            System.out.println("Time taken for topKWithSequentialGeneration: " + (endTimeTopK - startTimeTopK)/1000000 + "ms");
            for(List<Integer> pattern: topKPatterns.keySet())
                System.out.println(pattern+" "+ topKPatterns.get(pattern));
            new WaitClass().waitUntilInterrupt();
        }

    }
}