package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import tech.kee.model.Event;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class scratch {
public static void main(String[] args) throws IOException {
    List<Event> events = null;
//
//     read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-10m.csv";
//    String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-1m.csv";

//    String filePath = "/Users/keerthivarumbudy/Desktop/Thesis/results-from-cluster/steepbias-stream-20k-5000window";
//    String filePath = "/Users/keerthivarumbudy/Desktop/Thesis/results-from-cluster/slightly-less-steepbias-stream-20k";
//    String filePath = "/Users/keerthivarumbudy/Desktop/Thesis/results-from-cluster/biased-big-stream";
//    String filePath = "/Users/keerthivarumbudy/Desktop/Thesis/results-from-cluster/no-bias-big-stream";

//         time reading from csv
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 2, 1000000);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (events.size() != 0) {
//            StreamSummary streamSummary = new StreamSummary(100);
//            streamSummary.addEvents(events);
//            Map<List<Integer>, Integer> topK = QueryAnswering.answerTopKNew(2, List.of(5000), streamSummary, 1000);

//            int count = QueryAnswering.answerCount(List.of(4804056, 28717456), List.of(5000), streamSummary);
//            System.out.println("count "+count);

            Experiments experiments = new Experiments();
//            for (String str : experiments.topKHeader) {
//                System.out.print(str + " ");
//            }System.out.println();
//            experiments.topKNewExperiments(events, 20000, 100, 10);

//    System.out.println("End topK with sequential generation experiment");
//            for(int j=0; j<10; j++){
//                for (int i = 1000000; i < 10000000; i += 1000000) {
//                    experiments.run_insertion_test(i, 100);
//                }
//            }
            WaveletSummary waveletSummary = new WaveletSummary(100);
            waveletSummary.addEvents(events);
            ImmutableMap<Integer, ImmutableList<Wavelet>> summaryLayers =  waveletSummary.getSummaryLayers(Collections.singletonList(100));
            return;
        }
    }
}
