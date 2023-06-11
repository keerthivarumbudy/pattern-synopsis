package tech.kee;

import tech.kee.model.Event;

import java.io.IOException;
import java.util.List;

public class scratch {
public static void main(String[] args) throws IOException {
    List<Event> events = null;
//
//     read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct-10m.csv";
//         time reading from csv
        try {
            events = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 2, 20001);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (events.size() != 0) {
            StreamSummary streamSummary = new StreamSummary(100);
            streamSummary.addEvents(events);
            int count = QueryAnswering.answerCount(List.of(21404256, 1004856), List.of(5000), streamSummary);
            System.out.println("count "+count);

            Experiments experiments = new Experiments();
            for (String str : experiments.topKHeader) {
                System.out.print(str + " ");
            }System.out.println();
    experiments.topKExperiments(events, 20000, 100, 200);

    System.out.println("End topK with sequential generation experiment");

    }
}

}
