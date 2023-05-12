package org.example;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
    // read from csv and perform data preprocessing
        String filePath = "/Users/keerthivarumbudy/Downloads/archive (1)/2019-oct_10k.csv";
        try {
            List<Event> eventsList = DataPreprocessing.readFromCsvAndReturnEventsList(filePath, 0, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}