/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 */

package org.episteme.core.mathematics.linearalgebra.algorithms;

import org.episteme.core.mathematics.linearalgebra.LinearAlgebraProvider;
import org.episteme.core.mathematics.linearalgebra.Matrix;
import org.episteme.core.mathematics.linearalgebra.matrices.SIMDRealDoubleMatrix;
import org.episteme.core.mathematics.linearalgebra.matrices.TiledMatrix;
import org.episteme.core.technical.backend.distributed.DistributedContext;
import org.episteme.core.distributed.DistributedCompute;

import org.episteme.core.technical.algorithm.ProviderSelector;
import org.episteme.core.technical.algorithm.OperationContext;
import org.episteme.core.mathematics.linearalgebra.providers.StrassenLinearAlgebraProvider;
import org.episteme.core.mathematics.linearalgebra.providers.CARMALinearAlgebraProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Intelligent Planner for Distributed Matrix Multiplication.
 * Selects the optimal algorithm (Cannon, Fox, SUMMA, 2.5D, CARMA) based on runtime metrics.
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.2
 */
public class MatrixMultiplicationPlanner {

    private static final Logger logger = LoggerFactory.getLogger(MatrixMultiplicationPlanner.class);

    public enum Algorithm {
        STANDARD,
        SUMMA,
        CANNON,
        FOX,
        ALGORITHM_25D,
        CARMA
    }

    private static final int STRASSEN_THRESHOLD = 1024;

    @SuppressWarnings("unchecked")
    public static <E> Matrix<E> multiply(Matrix<E> A, Matrix<E> B) {
        int n = Math.max(A.rows(), A.cols());
        
        if (A instanceof SIMDRealDoubleMatrix && B instanceof SIMDRealDoubleMatrix && A.getScalarRing() instanceof org.episteme.core.mathematics.sets.Reals) {
            if (n >= STRASSEN_THRESHOLD && isPowerOfTwo(n)) {
                return (Matrix<E>) RealDoubleStrassenAlgorithm.multiply((SIMDRealDoubleMatrix) A, (SIMDRealDoubleMatrix) B);
            }
            return (Matrix<E>) RealDoubleCARMAAlgorithm.multiply((SIMDRealDoubleMatrix) A, (SIMDRealDoubleMatrix) B);
        }
        
        org.episteme.core.technical.algorithm.OperationContext opCtx = new org.episteme.core.technical.algorithm.OperationContext.Builder()
                .dimensionality(n)
                .dataSize((long) n * n)
                .build();
        org.episteme.core.mathematics.structures.rings.Ring<E> ring = A.getScalarRing();
        
        if (n >= STRASSEN_THRESHOLD && isPowerOfTwo(n)) {
            LinearAlgebraProvider<E> leaf = (LinearAlgebraProvider<E>) org.episteme.core.technical.algorithm.ProviderSelector.select(
                LinearAlgebraProvider.class,
                opCtx,
                p -> !(p instanceof org.episteme.core.mathematics.linearalgebra.providers.StrassenLinearAlgebraProvider) &&
                     !(p instanceof org.episteme.core.mathematics.linearalgebra.providers.CARMALinearAlgebraProvider) &&
                     (ring == null || ((LinearAlgebraProvider<?>)p).isCompatible(ring))
            );
            return RealStrassenAlgorithm.multiply(A, B, leaf);
        }
        
        org.episteme.core.mathematics.linearalgebra.LinearAlgebraProvider<E> leaf = (org.episteme.core.mathematics.linearalgebra.LinearAlgebraProvider<E>) org.episteme.core.technical.algorithm.ProviderSelector.select(
            org.episteme.core.mathematics.linearalgebra.LinearAlgebraProvider.class,
            opCtx,
            p -> !(p instanceof org.episteme.core.mathematics.linearalgebra.providers.StrassenLinearAlgebraProvider) &&
                 !(p instanceof org.episteme.core.mathematics.linearalgebra.providers.CARMALinearAlgebraProvider) &&
                 (ring == null || p.isCompatible(ring))
        );
        return RealCARMAAlgorithm.multiply(A, B, leaf);
    }

    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    /**
     * Selects and executes the best distributed multiplication algorithm.
     */
    public static <E> TiledMatrix<E> multiply(TiledMatrix<E> A, TiledMatrix<E> B) {
        DistributedContext ctx = DistributedCompute.getContext();
        int p = ctx.getParallelism();
        Algorithm algo = selectAlgorithm(A, B, p);
        
        logger.info("Planner selected algorithm: {}", algo);
        
        @SuppressWarnings("unchecked")
        LinearAlgebraProvider<E> leaf = (LinearAlgebraProvider<E>) ProviderSelector.select(
            LinearAlgebraProvider.class,
            OperationContext.DEFAULT,
            prov -> !(prov instanceof StrassenLinearAlgebraProvider) &&
                 !(prov instanceof CARMALinearAlgebraProvider) &&
                 ((LinearAlgebraProvider<?>)prov).isCompatible(A.getScalarRing())
        );

        switch (algo) {
            case CANNON:
                return DistributedCannonAlgorithm.multiply(A, B, leaf);
            case FOX:
                 // Ensure Fox is valid (square grid)
                if (isSquareGrid(p)) return DistributedFoxAlgorithm.multiply(A, B, leaf);
                return DistributedSUMMAAlgorithm.multiply(A, B, leaf);
            case ALGORITHM_25D:
                int cCount = Integer.getInteger("org.episteme.multiply.25d.layers", 2);
                return Distributed25DAlgorithm.multiply(A, B, cCount, leaf); 
            case CARMA:
                return DistributedCARMAAlgorithm.multiply(A, B, leaf);
            case SUMMA:
            default:
                return DistributedSUMMAAlgorithm.multiply(A, B, leaf);
        }
    }

    /**
     * Heuristic Selection Logic.
     */
    public static <E> Algorithm selectAlgorithm(TiledMatrix<E> A, TiledMatrix<E> B, int p) {
        String forced = System.getProperty("org.episteme.multiply.algorithm");
        if (forced != null) {
            try {
                return Algorithm.valueOf(forced.toUpperCase());
            } catch (Exception e) {
                logger.warn("Invalid forced algorithm: {}, falling back to heuristics", forced);
            }
        }
        
        long m = A.rows();
        long n = B.cols();
        long k = A.cols();
        
        boolean isSquare = (m == n && n == k);
        boolean isSquareGrid = isSquareGrid(p);
        
        // 1. CARMA is generally best for non-square or recursive arbitrary dims
        if (!isSquare) {
            return Algorithm.CARMA;
        }
        
        // 2. 2.5D if we have 3D-like topology or high communication cost
        if (p >= 64) {
             return Algorithm.ALGORITHM_25D;
        }

        // 3. Cannon/Fox for square grids
        if (isSquareGrid) {
            return Algorithm.CANNON; 
        }
        
        // 4. Fallback
        return Algorithm.SUMMA;
    }
    
    private static boolean isSquareGrid(int p) {
        int sqrt = (int) Math.sqrt(p);
        return sqrt * sqrt == p;
    }
}

