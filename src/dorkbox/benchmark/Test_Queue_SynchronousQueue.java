package dorkbox.benchmark;

import dorkbox.benchmark.common.Base_BlockingQueue;
import dorkbox.benchmark.common.Base_Queue;

public class Test_Queue_SynchronousQueue {
    public static final int REPETITIONS = 50 * 1000 * 100;

    private static final int bestRunsToAverage = 4;
    private static final int runs = 10;
    private static final int warmups = 3;

    public static void main(final String[] args) throws Exception {
        System.out.format("reps: %,d  %s\n", REPETITIONS, Test_Queue_SynchronousQueue.class.getSimpleName());

        for (int concurrency = 1; concurrency < 5; concurrency++) {
            final java.util.concurrent.SynchronousQueue<Integer> queue = new java.util.concurrent.SynchronousQueue<Integer>();
            final Integer initialValue = Integer.valueOf(777);
            new SQ_Block().run(REPETITIONS, concurrency, concurrency, warmups, runs, bestRunsToAverage, false, queue,
                                initialValue);
        }

        System.out.println("");
        System.out.println("");

        for (int concurrency = 1; concurrency < 5; concurrency++) {
            final java.util.concurrent.SynchronousQueue<Integer> queue = new java.util.concurrent.SynchronousQueue<Integer>();
            final Integer initialValue = Integer.valueOf(777);
            new SQ_NonBlock().run(REPETITIONS, concurrency, concurrency, warmups, runs, bestRunsToAverage, false, queue,
                                   initialValue);
        }
    }

    static
    class SQ_Block extends Base_BlockingQueue<Integer> {}

    static
    class SQ_NonBlock extends Base_Queue<Integer> {}
}
