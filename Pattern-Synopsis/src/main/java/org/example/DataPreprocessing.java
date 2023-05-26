package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DataPreprocessing{

     public static Timestamp fixTimeFormat(String time){
         time = time.replace(" UTC", "");
         return java.sql.Timestamp.valueOf(time);
     }
     public static List<Event> readFromCsvAndReturnEventsList(String filePath, int timeIdx, int eventIdx) throws IOException {
         File file = new File(filePath);
         List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
         List<Event> eventsList = new ArrayList<>();
         // because first line is header
         for (String line : lines.subList(1, lines.size() - 1)) {
             String[] array = line.split(",");
             Event event = new Event();
             event.eventId = Integer.valueOf(array[eventIdx]);
             event.timestamp = fixTimeFormat(array[timeIdx]);
             eventsList.add(event);
//             System.out.println(event.eventId + " " + event.timestamp);
         }
         return eventsList;
    }

}
