/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 */

package org.episteme.core.mathematics.loaders.matrixmarket;

import org.episteme.core.io.AbstractResourceReader;
import org.episteme.core.mathematics.linearalgebra.Matrix;
import org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix;
import org.episteme.core.mathematics.linearalgebra.matrices.SparseMatrix;
import org.episteme.core.mathematics.linearalgebra.matrices.storage.MatrixStorage;
import org.episteme.core.mathematics.numbers.real.Real;
import org.episteme.core.mathematics.sets.Reals;
import org.episteme.core.technical.algorithm.AlgorithmManager;

import java.io.*;
import java.util.Scanner;

/**
 * Reader for the NIST MatrixMarket (.mtx) format.
 * Supports Coordinate and Array formats for real matrices.
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.2
 */
public class MatrixMarketReader extends AbstractResourceReader<Matrix<Real>> {

    public MatrixMarketReader() {
    }

    @Override
    public String getResourcePath() {
        return "data/matrices";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Matrix<Real>> getResourceType() {
        return (Class<Matrix<Real>>) (Class<?>) Matrix.class;
    }

    @Override
    public String getName() {
        return "MatrixMarket Reader";
    }

    @Override
    public String getDescription() {
        return "Reads sparse and dense matrices in NIST MatrixMarket (.mtx) format.";
    }

    @Override
    public String getLongDescription() {
        return "Comprehensive reader for the standard MatrixMarket interchange format supporting Coordinate (sparse) and Array (dense) formats.";
    }

    @Override
    public String getCategory() {
        return "Mathematics";
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[] {"2.0"};
    }

    @Override
    public String[] getSupportedExtensions() {
        return new String[] {".mtx", ".mm"};
    }

    @Override
    public Matrix<Real> loadFromSource(String resourceId) throws Exception {
        File file = new File(resourceId);
        if (file.exists()) {
            return read(file);
        }
        try (InputStream is = getClass().getResourceAsStream(resourceId)) {
            if (is != null) {
                return read(is);
            }
        }
        throw new FileNotFoundException("MatrixMarket file not found: " + resourceId);
    }

    @Override
    protected Matrix<Real> loadFromInputStream(InputStream is, String id) throws Exception {
        return read(is);
    }

    public Matrix<Real> read(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return read(fis);
        }
    }

    public Matrix<Real> read(InputStream is) throws IOException {
        return read(new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8));
    }

    public Matrix<Real> read(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String headerLine = br.readLine();
        if (headerLine == null || !headerLine.startsWith("%%MatrixMarket")) {
            throw new IOException("Invalid MatrixMarket header: " + headerLine);
        }

        String[] headerTokens = headerLine.split("\\s+");
        if (headerTokens.length < 5 || !headerTokens[1].equalsIgnoreCase("matrix")) {
            throw new IOException("Unsupported MatrixMarket object type: " + headerLine);
        }

        String format = headerTokens[2].toLowerCase(); // coordinate or array
        String field = headerTokens[3].toLowerCase();   // real, integer, complex, pattern
        String symmetry = headerTokens[4].toLowerCase(); // general, symmetric, skew-symmetric, hermitian

        // Skip comment lines
        String line;
        do {
            line = br.readLine();
            if (line == null) throw new IOException("Unexpected end of file before dimension line");
            line = line.trim();
        } while (line.startsWith("%") || line.isEmpty());

        // Parse dimensions
        Scanner dimScanner = new Scanner(line);
        dimScanner.useLocale(java.util.Locale.US);
        int rows = dimScanner.nextInt();
        int cols = dimScanner.nextInt();

        boolean isCoordinate = "coordinate".equals(format);
        int nonZeros = isCoordinate ? dimScanner.nextInt() : (rows * cols);
        dimScanner.close();

        Reals reals = Reals.getInstance();

        if (isCoordinate) {
            MatrixStorage<Real> storage = AlgorithmManager.getRegistry().createStorage(rows, cols, reals, 0.0);
            boolean isSymmetric = "symmetric".equals(symmetry);
            boolean isSkewSymmetric = "skew-symmetric".equals(symmetry);

            for (int k = 0; k < nonZeros; k++) {
                String entryLine = br.readLine();
                if (entryLine == null) break;
                entryLine = entryLine.trim();
                if (entryLine.isEmpty() || entryLine.startsWith("%")) {
                    k--;
                    continue;
                }

                String[] tokens = entryLine.split("\\s+");
                int r = Integer.parseInt(tokens[0]) - 1; // 1-based to 0-based
                int c = Integer.parseInt(tokens[1]) - 1;
                double val = tokens.length > 2 ? Double.parseDouble(tokens[2]) : 1.0;

                storage.set(r, c, Real.of(val));
                if (isSymmetric && r != c) {
                    storage.set(c, r, Real.of(val));
                } else if (isSkewSymmetric && r != c) {
                    storage.set(c, r, Real.of(-val));
                }
            }
            return new SparseMatrix<>(storage, reals);
        } else {
            // Array format (column-major)
            double[] data = new double[rows * cols];
            int idx = 0;
            while (idx < rows * cols && (line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("%")) continue;
                String[] tokens = line.split("\\s+");
                for (String t : tokens) {
                    if (!t.isEmpty() && idx < rows * cols) {
                        // MatrixMarket array format is column-major
                        int col = idx / rows;
                        int row = idx % rows;
                        data[row * cols + col] = Double.parseDouble(t);
                        idx++;
                    }
                }
            }
            return RealDoubleMatrix.of(data, rows, cols);
        }
    }
}
