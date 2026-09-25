package org.episteme.core.technical.algorithm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.episteme.core.mathematics.linearalgebra.LinearAlgebraProvider;
import org.episteme.core.mathematics.linearalgebra.Matrix;
import org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix;
import org.episteme.core.mathematics.numbers.real.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoTuningRunner {
    private static final Logger logger = LoggerFactory.getLogger(AutoTuningRunner.class);
    private static final int[] SIZES = {64, 256, 512};
    private static final int WARMUP = 1;
    private static final int ITERATIONS = 2;

    public static void runAll() {
        if (Boolean.getBoolean("episteme.benchmark.skip")) return;
        logger.info("Starting Linear Algebra Auto-Benchmark...");
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<LinearAlgebraProvider<Real>> providers = (List) AlgorithmManager.getProviders(LinearAlgebraProvider.class);
        List<AutoTuningResult> results = new ArrayList<>();

        for (LinearAlgebraProvider<Real> provider : providers) {
            if (!provider.isAvailable()) continue;
            
            logger.info("Benchmarking provider: {} ({})", provider.getName(), provider.getEnvironmentInfo());
            Map<Integer, Double> gflopsMap = new LinkedHashMap<>();
            
            try {
                for (int size : SIZES) {
                    double gflops = benchmarkMultiply(provider, size);
                    gflopsMap.put(size, gflops);
                    logger.info("  Size {}: {} GFLOPS", size, String.format("%.2f", gflops));
                }
                results.add(new AutoTuningResult(provider.getName(), provider.getEnvironmentInfo(), gflopsMap));
            } catch (Throwable t) {
                logger.warn("  Failed to benchmark {}: {}", provider.getName(), t.getMessage());
            }
        }
        
        AutoTuningManager.registerResults(results);
        AutoTuningManager.saveResults();
        logger.info("Auto-Benchmark completed. Registered {} provider profiles.", results.size());
    }

    private static double benchmarkMultiply(LinearAlgebraProvider<Real> provider, int n) {
        double[] aData = new double[n * n];
        double[] bData = new double[n * n];
        Random rand = new Random(42);
        for (int i = 0; i < aData.length; i++) {
            aData[i] = rand.nextDouble();
            bData[i] = rand.nextDouble();
        }
        Matrix<Real> A = RealDoubleMatrix.of(aData, n, n);
        Matrix<Real> B = RealDoubleMatrix.of(bData, n, n);

        for (int i = 0; i < WARMUP; i++) {
            provider.multiply(A, B);
        }

        long totalTime = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            provider.multiply(A, B);
            totalTime += (System.nanoTime() - start);
        }

        double avgSeconds = (totalTime / (double) ITERATIONS) / 1_000_000_000.0;
        double operations = 2.0 * n * n * n;
        return (operations / avgSeconds) / 1_000_000_000.0;
    }
}
