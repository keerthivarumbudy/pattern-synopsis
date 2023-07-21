package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.*;

import static java.lang.Math.min;
import static tech.kee.TopKHelpers.*;
import static tech.kee.Utils.*;
import static tech.kee.CountingHelpers.*;

public class QueryAnswering {
    public static int answerCount(List<Integer> eventIds, List<Integer> windows, StreamSummary streamSummary) {
        // !!!right now the support is only for 2 event patterns
        assert eventIds.size() >= 2;
        assert eventIds.size() == windows.size() + 1;
        // assert that all windows are greater than resolution
        assert windows.stream().allMatch(window -> window >= streamSummary.resolutionEvents);
        // divide all the windows by resolution
        windows = windows.stream()
                .map(window -> window / streamSummary.resolutionEvents)
                .toList();
        return countPattern(eventIds, windows, streamSummary.baseSummaryLayer);
    }

    public static Map<List<Integer>, Integer> answerTopKNew(
            Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        windows = windows.stream()
                .map(window -> window / streamSummary.resolutionEvents)
                .toList();

        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns =
                topKWithNewSortedPatterns(numberOfEvents, windows, streamSummary, k);
        Map<List<Integer>, Integer> topKPatternsMap = new HashMap<>();
        for (Map.Entry<List<Integer>, Integer> entry : topKPatterns)
            topKPatternsMap.put(entry.getKey(), entry.getValue());
        // sort the map by value
        topKPatternsMap = sortByValue(topKPatternsMap);
        return topKPatternsMap;
    }
    public static Map<List<Integer>, Integer> answerBaseLayerTopK(
            Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        windows = windows.stream()
                .map(window -> window / streamSummary.resolutionEvents)
                .toList();

        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns = getTopKNaive(numberOfEvents, windows, streamSummary, k);
        Map<List<Integer>, Integer> topKPatternsMap = new HashMap<>();
        for (Map.Entry<List<Integer>, Integer> entry : topKPatterns)
            topKPatternsMap.put(entry.getKey(), entry.getValue());
        // sort the map by value
        topKPatternsMap = sortByValue(topKPatternsMap);
        return topKPatternsMap;
    }
    public static Map<List<Integer>, Integer> answerNonSequentialTopK(
            Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        windows = windows.stream()
                .map(window -> window / streamSummary.resolutionEvents)
                .toList();

        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns =
                topKWithoutSequentialGeneration(numberOfEvents, windows, streamSummary, k);
        Map<List<Integer>, Integer> topKPatternsMap = new HashMap<>();
        for (Map.Entry<List<Integer>, Integer> entry : topKPatterns)
            topKPatternsMap.put(entry.getKey(), entry.getValue());
        // sort the map by value
        topKPatternsMap = sortByValue(topKPatternsMap);
        return topKPatternsMap;
    }

    public static Map<List<Integer>, Integer> answerLowerboundTopkTest(
            Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        windows = windows.stream()
                .map(window -> window / streamSummary.resolutionEvents)
                .toList();

        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns =
                topkTestingLowerbound(numberOfEvents, windows, streamSummary, k);
        Map<List<Integer>, Integer> topKPatternsMap = new HashMap<>();
        for (Map.Entry<List<Integer>, Integer> entry : topKPatterns)
            topKPatternsMap.put(entry.getKey(), entry.getValue());
        // sort the map by value
        topKPatternsMap = sortByValue(topKPatternsMap);
        return topKPatternsMap;
    }


    public static int countEvent(Integer eventId, StreamSummary streamSummary) {
        int count = 0;
        for (int i = 0; i < streamSummary.baseSummaryLayer.size(); i++) {
            count += streamSummary.baseSummaryLayer.get(i).eventCountMap.estimateCount(eventId);
        }
        return count;
    }

    public static void testingUpperBounds(List<Integer> eventIds, List<Integer> windows, StreamSummary streamSummary){
        windows = windows.stream()
                    .map(window -> window / streamSummary.resolutionEvents)
                    .toList();
        ImmutableMap<Integer, ImmutableList<Sketch>> layerSketches = transformParameterForTopK(2, windows, streamSummary,10);
        for(int i = 0; i < layerSketches.size(); i++){
           System.out.println("Layer " + i + " sketches: ");
           int count = countPattern2(eventIds, windows, layerSketches.get(i));
           System.out.println("Count: " + count);
        }

    }



}
