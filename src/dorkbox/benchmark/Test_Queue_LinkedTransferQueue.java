package dorkbox.benchmark;

import dorkbox.benchmark.common.Base_TransferQueue;

public class Test_Queue_LinkedTransferQueue {
    public static final int REPETITIONS = 50 * 1000 * 100;

    private static final int bestRunsToAverage = 4;
    private static final int runs = 10;
    private static final int warmups = 3;

    public static void main(final String[] args) throws Exception {
        System.out.format("reps: %,d  %s\n", REPETITIONS, Test_Queue_LinkedTransferQueue.class.getSimpleName());

        for (int concurrency = 1; concurrency < 5; concurrency++) {
            final java.util.concurrent.LinkedTransferQueue queue = new java.util.concurrent.LinkedTransferQueue();
            final Integer initialValue = Integer.valueOf(777);
            new LTQ().run(REPETITIONS, concurrency, concurrency, warmups, runs, bestRunsToAverage, false, queue,
                          initialValue);
        }
    }

    static
    class LTQ extends Base_TransferQueue<Integer> {}

}
