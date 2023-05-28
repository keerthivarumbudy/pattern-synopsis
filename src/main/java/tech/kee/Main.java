package tech.kee;

import com.google.common.collect.ImmutableSet;
import tech.kee.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        List<Event> events = null;

    // read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct_10k.csv";
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 0, 2 );

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(events.size() != 0){
            // create sketch
            StreamSummary streamSummary = new StreamSummary(
                    10);
            streamSummary.addEvents(events);
            // query answering
//            int count = QueryAnswering.answerCount(List.of(1005115,1004856,1004767), List.of(600, 600), streamSummary);
//            System.out.println(count);
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
            long startTime = System.nanoTime();
            Map<List<Integer>, Integer> topKPatterns = QueryAnswering.answerTopK(2, List.of( 600), streamSummary, 10);
            long endTime = System.nanoTime();
            System.out.println("Time taken for topKWithSequentialGeneration: " + (endTime - startTime)/1000000 + "ms");
            for(List<Integer> pattern: topKPatterns.keySet())
                System.out.println(pattern+" "+ topKPatterns.get(pattern));
            return;
        }
    }
}