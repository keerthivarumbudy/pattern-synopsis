package tech.kee;

import java.util.List;

import static java.lang.Math.min;

public class CountingHelpers {
    public static int countPattern(List<Integer> eventIds, List<Integer> numBlocks, List<Sketch> layer){
        if(eventIds.size()==2)
            return countPattern2(eventIds, numBlocks, layer);
        else
            return countPattern3(eventIds, numBlocks, layer);
    }

    public static int countPattern2(List<Integer> eventIds, List<Integer> numBlocks, List<Sketch> baseLayer){
        int count = 0;
        for(int i=0; i<baseLayer.size(); i++) {
            int count1 = baseLayer.get(i).eventCountMap.estimateCount(eventIds.get(0));
            if(count1 <= 0)
                continue;
            for (int j = i; j < min(i + numBlocks.get(0)+1, baseLayer.size()); j++) {
                int count2 = baseLayer.get(j).eventCountMap.estimateCount(eventIds.get(1));
                if(eventIds.get(0).equals(eventIds.get(1)) && i==j && count2>0)
                    count2 =  - 1;
                count += count1 * count2;
            }
        }
        return count;
    }

    public static int countPattern3(List<Integer> eventIds, List<Integer> numBlocks, List<Sketch> layerSketches){
        int count = 0;
        for(int i=0; i<layerSketches.size(); i++) {
            int count1 = layerSketches.get(i).eventCountMap.estimateCount(eventIds.get(0));
            if(count1 <= 0)
                continue;
            for (int j = i; j < min(i + numBlocks.get(0), layerSketches.size()); j++) {
                int count2 = 0;
                if(eventIds.get(0).equals(eventIds.get(1)) && i==j)
                    count2 = layerSketches.get(j).eventCountMap.estimateCount(eventIds.get(1)) - 1;
                else
                    count2 = layerSketches.get(j).eventCountMap.estimateCount(eventIds.get(1));
                if(count2 <= 0)
                    continue;
                int count3 = 0;
                if(numBlocks.size()>1) {
                    for (int k = j; k < min(j + numBlocks.get(1), layerSketches.size()); k++) {
                        count3 = layerSketches.get(k).eventCountMap.estimateCount(eventIds.get(2));
                        count += count1 * count2 * count3;
                    }
                }
            }
        }

        return count;
    }
}
