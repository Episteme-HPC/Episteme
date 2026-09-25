package org.episteme.core.technical.algorithm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.episteme.core.io.UserPreferences;

/**
 * Manages performance-based tuning for providers.
 * Loads benchmark results and provides scoring logic.
 */
public class AutoTuningManager {

    public enum Mode {
        ON, OFF, AUTO
    }
    private static final Logger logger = LoggerFactory.getLogger(AutoTuningManager.class);
    private static final Map<String, AutoTuningResult> RESULTS = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        loadResults();
    }

    public static Mode getMode() {
        return Mode.valueOf(UserPreferences.getInstance().getAutoTuningMode());
    }

    public static void loadResults() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), ".episteme");
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path path = dir.resolve("autotuning_performance.json");
            if (Files.exists(path)) {
                List<AutoTuningResult> list = mapper.readValue(path.toFile(), new TypeReference<List<AutoTuningResult>>() {});
                for (AutoTuningResult res : list) {
                    RESULTS.put(res.getProviderName(), res);
                }
                logger.debug("Loaded {} benchmark results for auto-tuning", RESULTS.size());
            }
        } catch (Exception e) {
            logger.warn("Failed to load benchmark results: {}", e.getMessage());
        }
    }

    public static void registerResult(AutoTuningResult result) {
        if (result != null && result.getProviderName() != null) {
            RESULTS.put(result.getProviderName(), result);
        }
    }

    public static void registerResults(Collection<AutoTuningResult> results) {
        if (results != null) {
            for (AutoTuningResult res : results) {
                registerResult(res);
            }
        }
    }

    public static void saveResults() {
        try {
            Path path = Paths.get(System.getProperty("user.home"), ".episteme", "autotuning_performance.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), new ArrayList<>(RESULTS.values()));
        } catch (Exception e) {
            logger.error("Failed to save benchmark results: {}", e.getMessage());
        }
    }

    /**
     * Records a live performance sample from an operation.
     */
    public static void recordSample(String providerName, int dim, double durationMs, long ops) {
        if (getMode() == Mode.OFF) return;
        
        double gflops = (ops / (durationMs / 1000.0)) / 1e9;
        
        AutoTuningResult res = RESULTS.computeIfAbsent(providerName, k -> new AutoTuningResult(k, "Unknown", new java.util.concurrent.ConcurrentHashMap<>()));
        res.getGflops().put(dim, gflops);
        
        // Background save periodically or on certain events? For now, save every 10 samples for simplicity.
        // Actually, maybe just keep in memory until app close or manual save.
        // For JIT, we probably want it to persist.
    }

    private static final java.util.concurrent.atomic.AtomicBoolean calibrating = new java.util.concurrent.atomic.AtomicBoolean(false);

    /**
     * Triggers asynchronous background calibration if no cached profiles exist.
     * Non-blocking, runs on a daemon thread with normal priority.
     */
    public static void ensureCalibratedAsync() {
        if (getMode() == Mode.OFF || Boolean.getBoolean("episteme.benchmark.skip")) return;
        if (RESULTS.isEmpty() && calibrating.compareAndSet(false, true)) {
            Thread thread = new Thread(() -> {
                try {
                    AutoTuningRunner.runAll();
                } catch (Throwable t) {
                    logger.debug("Background auto-tuning calibration completed with notice: {}", t.getMessage());
                } finally {
                    calibrating.set(false);
                }
            }, "Episteme-AutoTuning-Calibrator");
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.start();
        }
    }

    /**
     * Calculates a dynamic score based on benchmark data with linear interpolation.
     * 
     * @param providerName name of the provider
     * @param dim dimensionality (e.g. matrix rows)
     * @param defaultPriority the hardcoded priority
     * @return dynamic score
     */
    public static double getDynamicScore(String providerName, int dim, int defaultPriority) {
        if (!org.episteme.core.io.UserPreferences.getInstance().isAutoTuningEnabled()) return defaultPriority;
        
        Mode mode = getMode();
        if (mode == Mode.OFF) return defaultPriority;

        // Auto trigger background calibration if cache is cold
        if (RESULTS.isEmpty()) {
            ensureCalibratedAsync();
            return defaultPriority;
        }
        
        AutoTuningResult res = RESULTS.get(providerName);
        if (res == null || res.getGflops() == null || res.getGflops().isEmpty()) {
            return defaultPriority;
        }

        Map<Integer, Double> gflops = res.getGflops();
        if (dim <= 0) {
            // Default size evaluation
            double maxGflops = gflops.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            return defaultPriority + (maxGflops * 10.0);
        }

        // Interpolate performance
        double perf;
        if (gflops.containsKey(dim)) {
            perf = gflops.get(dim);
        } else {
            // Find lower and upper bounds for linear interpolation
            Integer lowerKey = null;
            Integer upperKey = null;
            List<Integer> sortedSizes = new ArrayList<>(gflops.keySet());
            Collections.sort(sortedSizes);

            for (int size : sortedSizes) {
                if (size <= dim) {
                    lowerKey = size;
                } else if (upperKey == null) {
                    upperKey = size;
                }
            }

            if (lowerKey == null) {
                perf = gflops.get(sortedSizes.get(0));
            } else if (upperKey == null) {
                perf = gflops.get(sortedSizes.get(sortedSizes.size() - 1));
            } else {
                double lowerGflops = gflops.get(lowerKey);
                double upperGflops = gflops.get(upperKey);
                double ratio = (double) (dim - lowerKey) / (double) (upperKey - lowerKey);
                perf = lowerGflops + ratio * (upperGflops - lowerGflops);
            }
        }

        // Penalty for boundary overhead on tiny matrices (dim < 32)
        if (dim < 32 && (providerName.toLowerCase().contains("native") || providerName.toLowerCase().contains("openblas") || providerName.toLowerCase().contains("cuda"))) {
            return Math.max(0, defaultPriority - 20.0);
        }

        return defaultPriority + (perf * 10.0);
    }
}
