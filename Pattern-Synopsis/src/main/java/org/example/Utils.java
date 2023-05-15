package org.example;

import java.util.ArrayList;
import java.util.List;

public final class Utils {
    public static List<Integer> primeFactorization(Integer n){
        List<Integer> primeFactors = new ArrayList<>();
        while (n % 2 == 0) {
            primeFactors.add(2);
            n /= 2;
        }
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            while (n % i == 0) {
                primeFactors.add(i);
                n /= i;
            }
        }
        if (n > 2) {
            primeFactors.add(n);
        }
        return primeFactors;
    }
}
