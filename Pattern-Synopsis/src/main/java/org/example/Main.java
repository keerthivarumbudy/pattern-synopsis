package org.example;

import com.google.common.collect.ImmutableSet;

import javax.management.Query;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        List<Event> eventsList = null;
    // read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct_10k.csv";
        try {
            eventsList = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 0, 2 );

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(eventsList.size() != 0){
            // create sketch
            Sketch sketch = new Sketch(600, eventsList);
            // query answering
//            int count = QueryAnswering.answerCount(List.of("1004856", "1005115"), List.of(6000), sketch);
//            System.out.println(count);
//            int count = QueryAnswering.countEvent(List.of("1005115"), List.of(6000), sketch.layerSketchList.get(0), sketch.resolution);
//            QueryAnswering.upperBoundExperiments(List.of("1004856", "1005115"), List.of(2400), sketch);
            List<ImmutableSet<String>> res = QueryAnswering.generateSequentialPatterns(sketch.eventTotalCountMap, 3);
            return;
        }
    }
}