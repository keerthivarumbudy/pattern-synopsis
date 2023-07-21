package tech.kee;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;

public class TopKHelpersWavelet {

    public static WaveletSummary transformParameterForWavelet(Integer numberOfEvents, List<Integer> windows, StreamSummary streamSummary, int k){
        assert numberOfEvents >= 2 : "Number of events should be greater than or equal to 2";
        assert numberOfEvents == windows.size() + 1 : "Number of windows should be one less than number of events";
        //assert that all windows are greater than resolution

        // choose the smallest prime number to be the smallest window size for the lowest composed block
        List<Integer> blockWindows = Utils.primeFactorization(windows.get(0)); // this can be modified to be the smallest prime factor of all windows

        // get the wavelet summary layers
        WaveletSummary waveletSummary = new WaveletSummary(100);
//        waveletSummary.getWaveletSummaryLayers(blockWindows, streamSummary.baseSummaryLayer);

        return waveletSummary;
    }
}
