/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 */

package org.episteme.core.mathematics.loaders.matrixmarket;

import org.episteme.core.mathematics.linearalgebra.Matrix;
import org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix;
import org.episteme.core.mathematics.numbers.real.Real;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class MatrixMarketTest {

    @Test
    public void testReadCoordinateMatrix() throws Exception {
        String mtxData = "%%MatrixMarket matrix coordinate real general\n" +
                "% Sample Matrix\n" +
                "3 3 4\n" +
                "1 1 1.0\n" +
                "2 2 10.5\n" +
                "3 1 2.5\n" +
                "3 3 4.0\n";

        MatrixMarketReader reader = new MatrixMarketReader();
        Matrix<Real> matrix = reader.read(new StringReader(mtxData));

        assertEquals(3, matrix.rows());
        assertEquals(3, matrix.cols());
        assertEquals(1.0, matrix.get(0, 0).doubleValue(), 1e-9);
        assertEquals(0.0, matrix.get(0, 1).doubleValue(), 1e-9);
        assertEquals(10.5, matrix.get(1, 1).doubleValue(), 1e-9);
        assertEquals(2.5, matrix.get(2, 0).doubleValue(), 1e-9);
        assertEquals(4.0, matrix.get(2, 2).doubleValue(), 1e-9);
    }

    @Test
    public void testWriteAndReadRoundTrip() throws Exception {
        RealDoubleMatrix orig = RealDoubleMatrix.of(new double[][]{
                {1.0, 0.0, 3.5},
                {0.0, 4.2, 0.0},
                {7.1, 0.0, 9.9}
        });

        MatrixMarketWriter writer = new MatrixMarketWriter();
        StringWriter sw = new StringWriter();
        writer.write(orig, sw);

        String mtxOutput = sw.toString();
        assertTrue(mtxOutput.contains("%%MatrixMarket matrix coordinate real general"));

        MatrixMarketReader reader = new MatrixMarketReader();
        Matrix<Real> loaded = reader.read(new StringReader(mtxOutput));

        assertEquals(3, loaded.rows());
        assertEquals(3, loaded.cols());
        assertEquals(1.0, loaded.get(0, 0).doubleValue(), 1e-9);
        assertEquals(0.0, loaded.get(0, 1).doubleValue(), 1e-9);
        assertEquals(3.5, loaded.get(0, 2).doubleValue(), 1e-9);
        assertEquals(4.2, loaded.get(1, 1).doubleValue(), 1e-9);
        assertEquals(7.1, loaded.get(2, 0).doubleValue(), 1e-9);
        assertEquals(9.9, loaded.get(2, 2).doubleValue(), 1e-9);
    }
}
