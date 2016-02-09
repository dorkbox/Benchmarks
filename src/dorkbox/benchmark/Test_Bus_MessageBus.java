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
package dorkbox.benchmark;

import dorkbox.benchmark.common.IEventBus;
import dorkbox.benchmark.common.PerfTest_BaseSync;
import dorkbox.benchmark.common.StrongListener;


@SuppressWarnings("Duplicates")
public
class Test_Bus_MessageBus {
    public static final int REPETITIONS = 50 * 1000 * 100;

    private static final int concurrency = 1;

    private static final int bestRunsToAverage = 4;
    private static final int runs = 10;
    private static final int warmups = 3;

    public static
    void main(final String[] args) throws Exception {
        System.out.println("reps:" + REPETITIONS + "  Concurrency " + concurrency);

        IEventBus bus = new IEventBus.MessageBusAdapter();

        PerfTest_BaseSync perfTest = new PerfTest_BaseSync();
        StrongListener listener1 = new StrongListener();
        bus.subscribe(listener1);

        long average = perfTest.averageRun(warmups, runs, bus, true, concurrency, REPETITIONS, bestRunsToAverage);

        bus.shutdown();

        System.out.format("summary,%s, %,d\n", Test_Bus_MessageBus.class.getSimpleName(), average);
    }
}
