package tech.kee;

import tech.kee.model.Event;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Experiments {
    static String[] topKHeader =  {"numOfRows","Event1","Event2" ,"resolution", "timeTaken", "count"};
    static String[] countingHeader = {"numOfRows", "resolution", "timeTaken", "count"};
    public static void topKExperiments(List<Event> events, int numOfRows, int resolution, int k) throws IOException {
        String filename = "topK_2events_standard";
            StreamSummary streamSummary = eventsIntoSketch(numOfRows, events, resolution);
            long startTime1 = System.nanoTime();
            Map<List<Integer>, Integer> topK = QueryAnswering.answerTopK(2, List.of( 5000) , streamSummary, k);
            long endTime1 = System.nanoTime();
            for(List<Integer> pattern: topK.keySet()){
//                System.out.println(pattern.get(0) + " " + pattern.get(1) + " " + topK.get(pattern));
                Integer[] result = {numOfRows, pattern.get(0), pattern.get(1), resolution, (int) ((endTime1 - startTime1) / 1000000), topK.get(pattern)};
                printAndStoreResults(topKHeader, result, filename);
            }
        }
    public static void baseLayertopKExperiments(List<Event> events, int numOfRows, int resolution, int k) throws IOException {
        String filename = "topK_2events_baseLayer";
        StreamSummary streamSummary = eventsIntoSketch(numOfRows, events, resolution);
        long startTime1 = System.nanoTime();
        Map<List<Integer>, Integer> topK = QueryAnswering.answerBaseLayerTopK(2, List.of(500) , streamSummary, k);
        long endTime1 = System.nanoTime();
        Integer[] results = {numOfRows, resolution, (int) ((endTime1 - startTime1) / 1000000)};
        for(List<Integer> pattern: topK.keySet()){
            Integer[] result = {numOfRows, pattern.get(0), pattern.get(1), resolution, (int) ((endTime1 - startTime1) / 1000000), topK.get(pattern)};
            printAndStoreResults(topKHeader, result, filename);
        }
    }
    public static void nonSequentialtopKExperiments( List<Event> events, int numOfRows, int resolution, int k) throws IOException {
        String[] header = {"numOfRows", "resolution", "timeTaken"};
        String filename = "topK_2events_baseLayer";
        StreamSummary streamSummary = eventsIntoSketch(numOfRows, events, resolution);
        long startTime1 = System.nanoTime();
        Map<List<Integer>, Integer> topK = QueryAnswering.answerNonSequentialTopK(2, List.of(500) , streamSummary, k);
        long endTime1 = System.nanoTime();
        for(List<Integer> pattern: topK.keySet()){
            Integer[] result = {numOfRows, pattern.get(0), pattern.get(1), resolution, (int) ((endTime1 - startTime1) / 1000000), topK.get(pattern)};
            printAndStoreResults(topKHeader, result, filename);
        }
    }
    public static void run_insertion_test(int numOfRows, int resolution) throws IOException {
        String[] header = {"numOfRows", "resolution", "timeTaken"};
        String filename = "insertion_time_cm";
        List<Event> events = null;

        // read from csv and perform data preprocessing
        String filePath = "data/2019-oct-10m.csv";
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 0, numOfRows );

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

//        Utils.writeResultsToFile(List.of(header), List.of(results), filename);
    }

    public static void countingExperiment(List<Event> events, int numOfRows, int resolution) {
        String filename = "count_2events";
        StreamSummary streamSummary = eventsIntoSketch(numOfRows, events, resolution);
            // query answering
            long startTime1 = System.nanoTime();
            int count = QueryAnswering.answerCount(List.of(1004856, 1005115), List.of(5000), streamSummary);
            long endTime1 = System.nanoTime();
            Integer[] results = {numOfRows, resolution, (int) ((endTime1 - startTime1) / 1000000), count};
            printAndStoreResults(countingHeader, results, filename);
        }
    public static StreamSummary eventsIntoSketch(int numOfRows, List<Event> events, int resolution){
        if (events.size() != 0) {
            // create sketch
            StreamSummary streamSummary = new StreamSummary(resolution);
            streamSummary.addEvents(events.subList(0, numOfRows));
            return streamSummary;
        }
        System.out.println("No events");
        return null;
    }
    public static void printAndStoreResults(String[] header, Integer[] results, String filename){
        // print the results
        for(int i=0; i<results.length; i++){
            System.out.print(results[i] + " ");
        }
        System.out.println();
//            Utils.writeResultsToFile(List.of(header), List.of(results), filename);

    }

    public static StreamSummary insertIntoSketch(List<Event> events, int resolution) {
        StreamSummary streamSummary = new StreamSummary(
                resolution);
        streamSummary.addEvents(events);
        return streamSummary;
    }


}
