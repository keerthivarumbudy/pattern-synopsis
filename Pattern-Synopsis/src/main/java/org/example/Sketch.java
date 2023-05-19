package org.example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sketch {
    int resolution; // in seconds
    List<List<SubSketch>> layerSketchList = new ArrayList<>();
    Map<String, Integer> eventTotalCountMap = new HashMap<>();
    public Sketch(int resolution, List<Event> eventsList){
        this.resolution = resolution;
        SubSketch subSketch = new SubSketch();
        subSketch.startTimestamp = eventsList.get(0).timestamp;
        subSketch.endTimestamp = new Timestamp(subSketch.startTimestamp.getTime() + resolution* 1000L);
        subSketch.resolution = 1;
        List<SubSketch> subSketches = new ArrayList<>();
        this.layerSketchList.add(subSketches);
        this.layerSketchList.get(0).add(subSketch);
        for (Event event : eventsList) {
            if((event.timestamp.getTime() > subSketch.endTimestamp.getTime())){
                // create new subsketch
                // update start and end timestamp
                // add subsketch to subSketchesList
                Timestamp tempEndTimestamp = subSketch.endTimestamp;
                subSketch = new SubSketch();
                subSketch.startTimestamp = tempEndTimestamp;
                subSketch.resolution = 1;
                subSketch.endTimestamp = new Timestamp(subSketch.startTimestamp.getTime() + resolution* 1000L);
                this.eventTotalCountMap.put(event.eventId, this.eventTotalCountMap.getOrDefault(event.eventId, 0) + 1);
                this.layerSketchList.get(0).add(subSketch);

            }
            // add event to subsketch
            this.layerSketchList.get(0).get(this.layerSketchList.get(0).size()-1).eventCountMap.put(
                    event.eventId, this.layerSketchList.get(0).get(this.layerSketchList.get(0).size()-1).
                            eventCountMap.getOrDefault(event.eventId, 0) + 1);
            this.eventTotalCountMap.put(event.eventId, this.eventTotalCountMap.getOrDefault(event.eventId, 0) + 1);

        }
    }

    public void composeSketches(List<Integer> blockWindows){
        // create layerSketch for each blockWindow, and group(concatenate) blockWindow number of the subsketches from the previous blockwindow
        int resolution_tracker = 1;
        for(int i : blockWindows){
            // get the index of the previous layerSketch
            resolution_tracker*=i;
            int prevBlockIdx = this.layerSketchList.size()-1;
            this.layerSketchList.add(new ArrayList<>());
            for(int j = 0; j< this.layerSketchList.get(prevBlockIdx).size(); j+=i){
                SubSketch subSketch = new SubSketch();
                subSketch.startTimestamp = this.layerSketchList.get(prevBlockIdx).get(j).startTimestamp;
                if(j+i < this.layerSketchList.get(prevBlockIdx).size())
                    subSketch.endTimestamp = this.layerSketchList.get(prevBlockIdx).get(j+i-1).endTimestamp;
                else
                    subSketch.endTimestamp = this.layerSketchList.get(prevBlockIdx).get(this.layerSketchList.get(prevBlockIdx).size()-1).endTimestamp;
                subSketch.resolution = resolution_tracker;
                try{
                    for(int k=j; k<j+i; k++){
                        for(String eventId : this.layerSketchList.get(prevBlockIdx).get(k).eventCountMap.keySet()){
                            subSketch.eventCountMap.put(eventId, subSketch.eventCountMap.getOrDefault(eventId, 0) +
                                    this.layerSketchList.get(prevBlockIdx).get(k).eventCountMap.get(eventId));
                        }
                    }
                } catch (Exception e){

                }
                this.layerSketchList.get(prevBlockIdx+1).add(subSketch);
            }
        }
    }

}


