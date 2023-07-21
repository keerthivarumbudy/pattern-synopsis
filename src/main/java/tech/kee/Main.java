package tech.kee;

import com.google.common.collect.ImmutableSet;
import tech.kee.model.Event;

import javax.management.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        //        List<Event> events = null;
        //
        ////     read from csv and perform data preprocessing
        //        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-10m.csv";
        ////         time reading from csv
        //        try {
        //            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 2, 10000000);
        //
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //        }
        //        if (events.size() != 0) {
        //            // create sketch
        ////                        StreamSummary streamSummary = new StreamSummary(100);
        ////                        streamSummary.addEvents(events);
        //
        //            Experiments experiments = new Experiments();

        //            QueryAnswering.testingUpperBounds(List.of(1004856, 1005115), List.of(500), streamSummary);
        //            }
        //            for (int i = 500000; i < 10000000; i += 500000) {
        //                experiments.countingExperiment(events, i, 1);
        //            }
        //            for (int i = 500000; i < 10000000; i += 500000) {
        //                experiments.countingExperiment(events, i, 40);
        //            }
        //            for (int i = 500000; i < 10000000; i += 500000) {
        //                experiments.countingExperiment(events, i,400);
        //            }
        //            }

        //            System.out.println("Time taken for count: " + (endTime1 - startTime1)/1000000 + "ms");
        //            int count_event = QueryAnswering.countEvent(List.of(1004856), List.of(6000), streamSummary);
        //            System.out.println("count_event "+count_event);
        //            QueryAnswering.upperBoundExperiments( List.of(6000, 600), sketch);
        //            List<ImmutableSet<String>> res =
        // QueryAnswering.generateSequentialPatterns(sketch.eventTotalCountMap, 3);

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
        //            long startTimeTopK = System.nanoTime();
        //            Map<List<Integer>, Integer> topKPatterns = QueryAnswering.answerTopK(2, List.of( 500),
        // streamSummary, 10);
        //            long endTimeTopK = System.nanoTime();
        //            System.out.println("Time taken for topKWithSequentialGeneration: " + (endTimeTopK -
        // startTimeTopK)/1000000 + "ms");
        //            for(List<Integer> pattern: topKPatterns.keySet())
        //                System.out.println(pattern+" "+ topKPatterns.get(pattern));

        List<Event> events = null;
//                    String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-10m.csv";
//        String filePath = "data/2019-oct-10m.csv";
        String filePath = "/Users/keerthivarumbudy/Desktop/Thesis/results-from-cluster/no-bias-big-stream";

        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 2, -1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Experiments experiments = new Experiments();
        //                    System.out.println("Start count experiment");
        //                    //print counting header
        //                    for (String str : experiments.countingHeader) {
        //                        System.out.print(str + " ");
        //                    }System.out.println();
        //                for (int j =0; j<3; j++){
        //                    for (int i = 500000; i < 10000000; i += 500000) {
        //                        experiments.countingExperiment(events, i, 100);
        //                    }
        //                    System.out.println();
        //                }
        //                for (int j =0; j<10; j++){
        //                    for (int i = 100000; i <= 1000000; i += 100000) {
        //                        experiments.countingExperiment(events, i, 100);
        //                    }
        //                    System.out.println();
        //                }
        //                    System.out.println("End count experiment");
        //                System.out.println("---------------------------------------------");
        //
//                for (int j = 0; j < 3; j++) {
//                    System.out.println("Start topK with base layer experiment");
//
//                    for (String str : experiments.topKHeader) {
//                        System.out.print(str + " ");
//                    }
//                    System.out.println();
//                    for (int i = 12000; i < 20001; i += 2000) {
//                        experiments.baseLayertopKExperiments(events, i, 100, 10);
//                    }
//                    System.out.println("End topK with base layer experiment");
//                    System.out.println("---------------------------------------------");
//                }
        //
        //

//        System.out.println("Start topK without sequential generation experiment");
//        for (String str : experiments.topKHeader) {
//            System.out.print(str + " ");
//        }
//        System.out.println();
//        for(int j=0; j<3; j++){
//        for (int i = i = 12000; i < 20001; i += 2000) {
//            experiments.nonSequentialtopKExperiments(events, i, 100, 10);
//        }
//        }
//        System.out.println("End topK without sequential generation experiment");
//        System.out.println("---------------------------------------------");

        //
//                    System.out.println("Start topK with sorted pattern generation experiment");


//        for (String str : experiments.topKHeader) {
//                        System.out.print(str + " ");
//                    }System.out.println();
//        for(int j=0; j<1; j++){
//                    for (int i = 5000; i < 20001; i += 5000) {
//                        experiments.topKNewExperiments(events, i, 100, 10);
//                    }
//                }
        System.out.println("First the 10000 patterns");
//
        experiments.topKNewExperiments(events, 20000, 100, 10000);
//            System.out.println("Start topK lowerbound experiment");
//            for (String str : experiments.topKHeader) {
//                System.out.print(str + " ");
//            }
//            System.out.println();
//            for (int i = i = 12000; i < 20001; i += 2000) {
//                experiments.lowerboundTopKExperiments(events, i, 100, 10);
//            }
//            System.out.println("End topK lowerbound experiment");

//            System.out.println("---------------------------------------------");

            System.out.println();
            System.out.println("---------------------------------------------");
            new WaitClass().waitUntilInterrupt();

    }
    }