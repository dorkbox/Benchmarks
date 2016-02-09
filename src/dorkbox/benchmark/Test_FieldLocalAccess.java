// from: https://stackoverflow.com/questions/21613098/java-local-vs-instance-variable-access-speed
// author: Joseph Adams
package dorkbox.benchmark;

public class Test_FieldLocalAccess {
    private static final int testSize = 10;
    private static final long runSize = 1_000_000_000;

    private LocalClass instanceVar;

    private static class LocalClass {
        public void someFunc() {}
    }

    public double testInstanceVar() {
        // System.out.println("Running instance variable benchmark:");
        instanceVar = new LocalClass();

        long start = System.currentTimeMillis();
        for (int i = 0; i < runSize; i++) {
            instanceVar.someFunc();
        }

        long elapsed = System.currentTimeMillis() - start;

        double avg = (elapsed * 1000.0) / runSize;

        // System.out.println("elapsed time = " + elapsed + "ms");
        // System.out.println(avg + " microseconds per execution");

        return avg;
    }

    public double testLocalVar() {
        // System.out.println("Running local variable benchmark:");
        instanceVar = new LocalClass();
        LocalClass localVar = instanceVar;

        long start = System.currentTimeMillis();
        for (int i = 0; i < runSize; i++) {
            localVar.someFunc();
        }

        long elapsed = System.currentTimeMillis() - start;

        double avg = (elapsed * 1000.0) / runSize;

        // System.out.println("elapsed time = " + elapsed + "ms");
        // System.out.println(avg + " microseconds per execution");

        return avg;
    }

    public static void main(String[] args) {
        Test_FieldLocalAccess bench;

        double[] avgInstance = new double[testSize];
        double[] avgLocal = new double[testSize];

        for (int i = 0; i < testSize; i++) {
            bench = new Test_FieldLocalAccess();

            avgInstance[i] = bench.testInstanceVar();
            avgLocal[i] = bench.testLocalVar();

            System.gc();
        }

        double sumInstance = 0.0;
        for (double d : avgInstance) {
            sumInstance += d;
        }
        System.out.println("Average time for instance variable access: " + sumInstance / testSize);

        double sumLocal = 0.0;
        for (double d : avgLocal) {
            sumLocal += d;
        }
        System.out.println("Average time for local variable access: " + sumLocal / testSize);
    }
}
