package tech.kee.CountMin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CountMinSketch {
    public int width;
    public int depth;
    private int[][] sketch;

    public CountMinSketch(int width, int depth) {
        this.width = width;
        this.depth = depth;
        this.sketch = new int[depth][width];
    }

    public void add(Integer key) {
        int[] hashes = hash(key);
        for (int i = 0; i < depth; i++) {
            sketch[i][hashes[i]]++;
        }
    }

    public int estimateCount(Integer key) {
        int minCount = Integer.MAX_VALUE;
        int[] hashes = hash(key);
        for (int i = 0; i < depth; i++) {
            minCount = Math.min(minCount, sketch[i][hashes[i]]);
        }
        return minCount;
    }

    private int[] hash(Integer attrValue) {
        Random rn = new Random();
        int[] hash = new int[depth];
        rn.setSeed(attrValue);
        for (int i = 0; i < depth; i++) hash[i] = rn.nextInt(width);
        return hash;
    }
    public CountMinSketch merge(CountMinSketch toMerge) {

        for (int i = 0; i < this.depth; i++) {
            for (int j = 0; j < this.width; j++) {
                this.sketch[i][j] = this.sketch[i][j] + toMerge.sketch[i][j];
            }
        }

        return this;
    }

    public static void main(String[] args) {
        CountMinSketch sketch = new CountMinSketch(10, 5);
        sketch.add(1);
        sketch.add(2);
        sketch.add(1);
        sketch.add(1);
        sketch.add(1);
        sketch.add(3);
        sketch.add(4);
        sketch.add(5);
        sketch.add(6);

        System.out.println(sketch.estimateCount(10));
    }
}
