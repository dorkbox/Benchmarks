package dorkbox.benchmark;

import com.lmax.disruptor.*;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

@SuppressWarnings("Duplicates")
public
class Test_Queue_Disruptor<T> {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static boolean SHOW = true;

    public static final int REPETITIONS = 50 * 1000 * 100;

    private static final int bestRunsToAverage = 4;
    private static final int runs = 10;
    private static final int warmups = 0;

    public static void main(final String[] args) throws Exception {
        System.out.format("reps: %,d  %s: \n", REPETITIONS, "Disruptor");

        WaitStrategy[] strategies = new WaitStrategy[] {
                        new LiteBlockingWaitStrategy(), // this is the best so far
                        new BlockingWaitStrategy(),
                        new YieldingWaitStrategy(),
                        new BusySpinWaitStrategy(),
                        new SleepingWaitStrategy(),
                        new PhasedBackoffWaitStrategy(20, 50, TimeUnit.MILLISECONDS, new SleepingWaitStrategy(0)),
                        new PhasedBackoffWaitStrategy(20, 50, TimeUnit.MILLISECONDS, new BlockingWaitStrategy()),
                        new PhasedBackoffWaitStrategy(20, 50, TimeUnit.MILLISECONDS, new LiteBlockingWaitStrategy())
        };


// must be power of 2.
//        final int BUFFER_SIZE = 1 << (32 - Integer.numberOfLeadingZeros((AVAILABLE_PROCESSORS * 32) - 1));
//        final int BUFFER_SIZE = ringBufferSize * 64;
//        final int BUFFER_SIZE = 1024 * 64;
        final int BUFFER_SIZE = 1024;



        for (WaitStrategy strategy : strategies) {
            SHOW = true;

            for (int concurrency = 1; concurrency < 5; concurrency++) {
                final Integer initialValue = Integer.valueOf(777);
                new Test_Queue_Disruptor().run(REPETITIONS, concurrency, concurrency, runs, bestRunsToAverage, false,
                                               BUFFER_SIZE, strategy, initialValue);
            }
        }
    }


    public
    void run(final int repetitions,
             final int producersCount,
             final int consumersCount,
             final int runs,
             final int bestRunsToAverage,
             final boolean showStats, final int BUFFER_SIZE, WaitStrategy consumerWaitStrategy,
             final T initialValue) throws Exception {

        final Long[] results = new Long[runs];
        for (int i = 0; i < runs; i++) {
            System.gc();
            results[i] = performanceRun(i, showStats, producersCount, consumersCount, repetitions, BUFFER_SIZE, consumerWaitStrategy,
                                        initialValue);
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

        long average = sum / bestRunsToAverage;
        System.out.format("%s,%s  %dP/%dC %,d\n", this.getClass().getSimpleName(),
//                          queue.getClass().getSimpleName(),
                          "Disruptor",
                          producersCount, consumersCount, average);
    }


    /**
     * Benchmarks how long it takes to push X number of items total. If there are is 1P and 1C, then X items will be sent from a producer to
     * a consumer. Of there are NP and NC threads, then X/N (for a total of X) items will be sent.
     */
    private
    long performanceRun(final int runNumber,
                        final boolean showStats,
                        final int producersCount,
                        final int consumersCount,
                        int repetitions, final int BUFFER_SIZE, final WaitStrategy consumerWaitStrategy, T initialValue) throws Exception {

        // make sure it's evenly divisible by both producers and consumers
        final int adjusted = repetitions / producersCount / consumersCount;

        int pRepetitions = adjusted * producersCount;

        ExecutorService executor = new ThreadPoolExecutor(consumersCount, consumersCount, 0,
                                                          TimeUnit.NANOSECONDS,
                                                          new java.util.concurrent.LinkedTransferQueue<Runnable>(),
                                                          DaemonThreadFactory.INSTANCE);


        final PubExceptionHandler<ValueHolder<T>> exceptionHandler = new PubExceptionHandler<ValueHolder<T>>();
        ValueFactory<T> factory = new ValueFactory<T>();

        //noinspection unchecked
        WorkHandler<ValueHolder<T>> handlers[] = new EventHandler[consumersCount];
        for (int i = 0; i < handlers.length; i++) {
            handlers[i] = new EventHandler<T>();  // exactly one per thread
        }



        if (SHOW) {
            SHOW = false;
            System.err.println("BufferSize: " + BUFFER_SIZE + " : " + consumerWaitStrategy.getClass().getSimpleName());
        }


        RingBuffer<ValueHolder<T>> ringBuffer = RingBuffer.createMultiProducer(factory, BUFFER_SIZE, consumerWaitStrategy);
        SequenceBarrier sequenceBarrier = ringBuffer.newBarrier();


        // setup the WorkProcessors (these consume from the ring buffer -- one at a time) and tell the "handler" to execute the item
        final int numWorkers = handlers.length;
        WorkProcessor[] workProcessors = new WorkProcessor[numWorkers];
        Sequence workSequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);

        for (int i = 0; i < numWorkers; i++) {
            workProcessors[i] = new WorkProcessor<ValueHolder<T>>(ringBuffer,
                                                                    sequenceBarrier,
                                                                    handlers[i],
                                                                    exceptionHandler,
                                                                    workSequence);
        }

        // setup the WorkProcessor sequences (control what is consumed from the ring buffer)
        final Sequence[] sequences = new Sequence[workProcessors.length + 1];
        for (int i = 0, size = workProcessors.length; i < size; i++) {
            sequences[i] = workProcessors[i].getSequence();
        }
        sequences[sequences.length - 1] = workSequence;  // always add the work sequence
        ringBuffer.addGatingSequences(sequences);




        // configure the start position for the WorkProcessors, and start them
        final long cursor = ringBuffer.getCursor();
        long expected = cursor + (producersCount * pRepetitions);  // saved so we know when it is finished producing events
        workSequence.set(cursor);

        for (WorkProcessor<?> processor : workProcessors) {
            processor.getSequence()
                     .set(cursor);
            executor.execute(processor);
        }



        Producer[] producers = new Producer[producersCount];
        Thread[] pThreads = new Thread[producersCount];

        for (int i = 0; i < producersCount; i++) {
            producers[i] = new Producer<T>(ringBuffer, pRepetitions, initialValue);
        }

        for (int i = 0; i < producersCount; i++) {
            pThreads[i] = new Thread(producers[i], "Producer " + i);
        }



        for (int i = 0; i < producersCount; i++) {
            pThreads[i].start();
        }

        for (int i = 0; i < producersCount; i++) {
            pThreads[i].join();
        }



        while (workSequence.get() < expected) {
            LockSupport.parkNanos(1L);
        }

        for (WorkProcessor<?> processor : workProcessors) {
            processor.halt();
        }



        for (int i = 0; i < consumersCount; i++) {
            EventHandler h = (EventHandler) handlers[i];
            while (!h.isShutdown()) {
                Thread.yield();
            }
        }


        long start = Long.MAX_VALUE;
        long end = -1;

        for (int i = 0; i < producersCount; i++) {
            if (producers[i].start - start < 0) {
                start = producers[i].start;
            }
        }
        for (int i = 0; i < consumersCount; i++) {
            EventHandler h = (EventHandler) handlers[i];
            final long end1 = h.getEnd();
            if (end1 - end > 0) {
                end = end1;
            }
        }

        long duration = end - start;
        long ops = repetitions * 1000000000L / duration;

        if (showStats) {
            System.out.format("%d - ops/sec=%,d\n", runNumber, ops);
        }
        return ops;
    }


    public class Producer<T> implements Runnable {
        private final RingBuffer<ValueHolder<T>> queue;
        volatile long start;
        private int repetitions;
        private final T initialValue;

        public Producer(RingBuffer<ValueHolder<T>>  queue, int repetitions, T initialValue) {
            this.queue = queue;
            this.repetitions = repetitions;
            this.initialValue = initialValue;
        }

        @Override
        public void run() {
            RingBuffer<ValueHolder<T>> producer = this.queue;
            int i = this.repetitions;
            this.start = System.nanoTime();
            final T initialValue = this.initialValue;

            try {
                do {
                    // setup the job
                    final long seq = producer.next();
//                    try {
                        ValueHolder<T> eventJob = producer.get(seq);
                        eventJob.item = initialValue;
//                    } finally {
                        // always publish the job
                        producer.publish(seq);
//                    }
                } while (0 != --i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class PubExceptionHandler<E> implements ExceptionHandler<E> {

        public
        PubExceptionHandler() {
        }

        @Override
        public void handleEventException(final Throwable e, final long sequence, final E event) {
            System.err.println("Exception processing: " + sequence + " " + event.getClass() + "(" + event + ")  "  + e.getMessage());
        }

        @Override
        public void handleOnStartException(final Throwable e) {
            System.err.println("Error starting the disruptor " + e.getMessage());
        }

        @Override
        public void handleOnShutdownException(final Throwable e) {
            System.err.println("Error stopping the disruptor  " + e.getMessage());
        }
    }


    class ValueFactory<T> implements EventFactory<ValueHolder<T>> {

        public ValueFactory() {
        }

        @Override
        public ValueHolder<T> newInstance() {
            return new ValueHolder<T>();
        }
    }

    class ValueHolder<T> {

        public T item = null;

        public ValueHolder() {}
    }


    class EventHandler<T> implements WorkHandler<ValueHolder<T>>, LifecycleAware{
        AtomicBoolean shutdown = new AtomicBoolean(false);
        private long end = 0;

        public
        EventHandler() {
        }


        public synchronized
        long getEnd() {
            return end;
        }

        @Override
        public
        void onEvent(final ValueHolder<T> event) throws Exception {
            end = System.nanoTime();
        }

        @Override
        public
        void onStart() {
        }

        @Override
        public synchronized
        void onShutdown() {
            end = System.nanoTime();
            shutdown.set(true);
        }

        public
        boolean isShutdown() {
            return shutdown.get();
        }
    }
}
