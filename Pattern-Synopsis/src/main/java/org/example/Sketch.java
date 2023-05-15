package org.example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sketch {
    int resolution; // in seconds
    List<List<SubSketch>> subSketchesList = new ArrayList<>();

    public Sketch(int resolution, List<Event> eventsList){
        this.resolution = resolution;
        SubSketch subSketch = new SubSketch();
        subSketch.startTimestamp = eventsList.get(0).timestamp;
        subSketch.endTimestamp = new Timestamp(subSketch.startTimestamp.getTime() + resolution*1000);
        List<SubSketch> subSketches = new ArrayList<>();
        this.subSketchesList.add(subSketches);
        this.subSketchesList.get(0).add(subSketch);
        for (Event event : eventsList) {
            if((event.timestamp.getTime() > subSketch.endTimestamp.getTime())){
                // create new subsketch
                // update start and end timestamp
                // add subsketch to subSketchesList
                Timestamp tempEndTimestamp = subSketch.endTimestamp;
                subSketch = new SubSketch();
                subSketch.startTimestamp = tempEndTimestamp;
                subSketch.endTimestamp = new Timestamp(subSketch.startTimestamp.getTime() + resolution*1000);
                this.subSketchesList.get(0).add(subSketch);

            }
            // add event to subsketch
            this.subSketchesList.get(0).get(this.subSketchesList.get(0).size()-1).eventCountMap.put(
                    event.eventId, this.subSketchesList.get(0).get(this.subSketchesList.get(0).size()-1).
                            eventCountMap.getOrDefault(event.eventId, 0) + 1);

        }
    }

}


