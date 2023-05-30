package tech.kee;

import tech.kee.model.Event;
import tech.kee.Utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class experiments {
    public static void main(String[] args) {
        for(int j = 0; j < 3; j++){
//            for (int i = 1000000; i < 10000000; i += 1000000) {
//                try {
//                    run_insertion_test(i, 100);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            for(int i=1000000; i<2000000; i+=100000){
//                try{
//                    counting(i, 50);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
            for (int i = 1000; i<20000; i+=1000){
                try{
                    topKExperiments(i, 50, 3);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    public static void topKExperiments(int numOfRows, int resolution, int k) throws IOException {
        String[] header = {"numOfRows", "resolution", "timeTaken"};
        String filename = "topK_3events";
        List<Event> events = null;

        // read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-1m.csv";
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 0, 2, numOfRows );

        } catch (IOException e) {
            e.printStackTrace();
        }

        StreamSummary streamSummary = insertIntoSketch(events, resolution);
        long startTime1 = System.nanoTime();
        Map<List<Integer>, Integer> topK = QueryAnswering.answerTopK(3, List.of(500, 500) , streamSummary, k);
        long endTime1 = System.nanoTime();
        System.out.println(topK);
        System.out.println("Time taken for count: " + (endTime1 - startTime1)/1000000 + "ms");
        Integer[] results = {numOfRows, resolution, (int) ((endTime1 - startTime1) / 1000000)};
        try {
            Utils.writeResultsToFile(List.of(header), List.of(results), filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void run_insertion_test(int numOfRows, int resolution) throws IOException {
        String[] header = {"numOfRows", "resolution", "timeTaken"};
        String filename = "insertion_time_cm";
        List<Event> events = null;

        // read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-10m.csv";
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 0, 2, numOfRows );

        } catch (IOException e) {
            e.printStackTrace();
        }
        // time insertion
        long startTime = System.nanoTime();
        StreamSummary streamSummary = insertIntoSketch(events, resolution);
        long endTime = System.nanoTime();
        // write this time into a log file
        System.out.println("Time taken to insert into sketch: " + (endTime - startTime) / 1000000 + " ms");
        Integer[] results = {numOfRows, resolution, (int) ((endTime - startTime) / 1000000)};

        Utils.writeResultsToFile(List.of(header), List.of(results), filename);
    }
    public static void counting(int numOfRows, int resolution) {
        List<Event> events = null;

        // read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-10m.csv";
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 0, 2, numOfRows );

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(events.size() != 0){
            // create sketch
            StreamSummary streamSummary = new StreamSummary(
                    resolution);
            streamSummary.addEvents(events);
            // query answering
            long startTime1 = System.nanoTime();
            int count = QueryAnswering.answerCount(List.of(1004856,1005115,1004767), List.of(500,500), streamSummary);
            long endTime1 = System.nanoTime();
            System.out.println(count);
            System.out.println("Time taken for count: " + (endTime1 - startTime1)/1000000 + "ms");
            String[] header = {"numOfRows", "resolution", "timeTaken", "count"};
            Integer[] results = {numOfRows, resolution, (int) ((endTime1 - startTime1) / 1000000), count};
            String filename = "count_cm_with_cm_sketch";
            try {
                Utils.writeResultsToFile(List.of(header), List.of(results), filename);
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
    }
    public static StreamSummary insertIntoSketch(List<Event> events, int resolution) {
        StreamSummary streamSummary = new StreamSummary(
                resolution);
        streamSummary.addEvents(events);
        return streamSummary;
    }
}
