package tech.kee;

import java.io.IOException;
import java.util.*;

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

    public static Map<List<Integer>, Integer> answerTopK(
            Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k) throws IOException {
        windows = windows.stream()
                .map(window -> window / streamSummary.resolutionEvents)
                .toList();

        PriorityQueue<Map.Entry<List<Integer>, Integer>> topKPatterns =
                topKWithSequentialGeneration(numberOfEvents, windows, streamSummary, k);
        Map<List<Integer>, Integer> topKPatternsMap = new HashMap<>();
        for (Map.Entry<List<Integer>, Integer> entry : topKPatterns)
            topKPatternsMap.put(entry.getKey(), entry.getValue());
        // sort the map by value
        topKPatternsMap = sortByValue(topKPatternsMap);
        return topKPatternsMap;
    }

    public static int countEvent(List<Integer> eventIds, List<Integer> windows, StreamSummary streamSummary) {
        int count = 0;
        for (int i = 0; i < streamSummary.baseSummaryLayer.size(); i++) {
            count += streamSummary.baseSummaryLayer.get(i).eventCountMap.estimateCount(eventIds.get(0));
        }
        if (count != streamSummary.eventTotalCountMap.getOrDefault(eventIds.get(0), 0))
            System.out.println("countEvent from eventTotalCountMap: "
                    + streamSummary.eventTotalCountMap.getOrDefault(eventIds.get(0), 0));
        return count;
    }


}
