/*
 * Copyright 2015 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dorkbox.benchmark.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("Duplicates")
public
class PerfTest_BaseSync {
    public static final Integer TEST_VALUE = Integer.valueOf(777);

    public
    long averageRun(int warmUpRuns, int sumCount, IEventBus bus, boolean showStats, int concurrency, int repetitions, int bestRunsToAverage) throws Exception {
        int runs = warmUpRuns + sumCount;
        final Long[] results = new Long[runs];
        for (int i = 0; i < runs; i++) {
            System.gc();
            results[i] = performanceRun(i, bus, showStats, concurrency, repetitions);
        }


        // average best results for summary
        List<Long> list = Arrays.asList(results);
        Collections.sort(list);

        long sum = 0;
        // ignore the highest one
        int limit = runs - 1;
        for (int i = limit - bestRunsToAverage; i < limit; i++) {
            sum += list.get(i);
        }

        return sum / bestRunsToAverage;
    }

    private
    long performanceRun(int runNumber, IEventBus bus, boolean showStats, int concurrency, int repetitions) throws Exception {

        Producer[] producers = new Producer[concurrency];
        Thread[] threads = new Thread[concurrency * 2];

        for (int i = 0; i < concurrency; i++) {
            producers[i] = new Producer(bus, repetitions);
            threads[i] = new Thread(producers[i], "Producer " + i);
        }

        for (int i = 0; i < concurrency; i++) {
            threads[i].start();
        }

        for (int i = 0; i < concurrency; i++) {
            threads[i].join();
        }

        long start = Long.MAX_VALUE;
        long end = -1;

        for (int i = 0; i < concurrency; i++) {
            if (producers[i].start < start) {
                start = producers[i].start;
            }

            if (producers[i].end > end) {
                end = producers[i].end;
            }
        }


        long duration = end - start;
        long ops = repetitions * 1000000000L / duration;

        if (showStats) {
            System.out.format("%d - ops/sec=%,d\n", runNumber, ops);
        }
        return ops;
    }

    public static
    class Producer implements Runnable {
        private final IEventBus bus;
        volatile long start;
        volatile long end;
        private int repetitions;

        public
        Producer(IEventBus bus, int repetitions) {
            this.bus = bus;
            this.repetitions = repetitions;
        }

        @Override
        public
        void run() {
            IEventBus bus = this.bus;
            int i = this.repetitions;
            this.start = System.nanoTime();

            do {
                bus.publish(TEST_VALUE);
            } while (0 != --i);

            this.end = System.nanoTime();
        }
    }
}
