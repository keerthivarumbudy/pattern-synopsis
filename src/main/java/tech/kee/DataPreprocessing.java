package tech.kee;

import tech.kee.model.Event;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataPreprocessing{

     public static Instant fixTimeFormat(String time){
         time = time.replace(" UTC", "");
         return java.sql.Timestamp.valueOf(time).toInstant();
     }
     public static List<Event> readFromCsvAndReturnEventsList(String filePath, int eventIdx, int numTransactions) throws IOException {
         File file = new File(filePath);
         List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
         List<Event> eventsList = new ArrayList<>();
         if(numTransactions == -1){
             numTransactions = lines.size()-1;
         }
         // because first line is header
         for (int i = 1; i < numTransactions; i++) {
             String[] array = lines.get(i).split(",");
             Event event = new Event(Integer.valueOf(array[eventIdx]), i-1);
             eventsList.add(event);
         }
         return eventsList;
    }

}
