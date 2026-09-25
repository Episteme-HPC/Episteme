/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.episteme.core.mathematics.linearalgebra.providers;

import org.episteme.core.Episteme;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.episteme.core.mathematics.structures.rings.Field;
import org.episteme.core.mathematics.structures.rings.Ring;
import org.episteme.core.mathematics.linearalgebra.LinearAlgebraProvider;
import org.episteme.core.technical.backend.Backend;
import org.episteme.core.technical.backend.cpu.CPUBackend;
import org.episteme.core.mathematics.linearalgebra.Matrix;
import org.episteme.core.mathematics.linearalgebra.Vector;
import org.episteme.core.mathematics.linearalgebra.matrices.GenericMatrix;
import org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix;
import org.episteme.core.mathematics.linearalgebra.matrices.storage.DenseMatrixStorage;
import org.episteme.core.mathematics.linearalgebra.matrices.solvers.*;
import org.episteme.core.mathematics.linearalgebra.vectors.GenericVector;
import org.episteme.core.mathematics.numbers.real.Real;
import org.episteme.core.mathematics.numbers.complex.Complex;
import org.episteme.core.mathematics.sets.Reals;
import com.google.auto.service.AutoService;


/**
 * 
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
@AutoService({LinearAlgebraProvider.class, Backend.class, CPUBackend.class})
public class CPUDenseLinearAlgebraProvider<E> implements LinearAlgebraProvider<E>, CPUBackend {

    protected final Field<E> field;
    private static final int PARALLEL_THRESHOLD = 1000;

    @SuppressWarnings("unchecked")
    public CPUDenseLinearAlgebraProvider(Field<E> field) {
        this.field = (field != null) ? field : (Field<E>) (Object) org.episteme.core.mathematics.sets.Reals.getInstance();
    }

    /**
     * Public no-arg constructor required by ServiceLoader.
     */
    public CPUDenseLinearAlgebraProvider() {
        this(null);
    }

    @Override
    public String getEnvironmentInfo() {
        return "CPU (Standard JVM)";
    }

    @Override
    public String getName() {
        return "Episteme CPU (Dense)";
    }

    @Override
    public String getType() {
        return org.episteme.core.technical.backend.BackendDiscovery.TYPE_LINEAR_ALGEBRA;
    }

    @Override
    public String getDescription() {
        return "Core CPU Dense Linear Algebra Provider";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }


    @Override
    public void shutdown() {
        // No-op
    }

    @Override
    public org.episteme.core.technical.backend.ExecutionContext createContext() {
        return null;
    }

    @Override
    public String getId() {
        return "cpu-dense";
    }
    @Override public Matrix<E> exp(Matrix<E> a) { return elementWiseTranscendental(a, "exp"); }
    @Override public Matrix<E> log(Matrix<E> a) { return elementWiseTranscendental(a, "log"); }
    @Override public Matrix<E> log10(Matrix<E> a) { return elementWiseTranscendental(a, "log10"); }
    @Override public Matrix<E> sin(Matrix<E> a) { return elementWiseTranscendental(a, "sin"); }
    @Override public Matrix<E> cos(Matrix<E> a) { return elementWiseTranscendental(a, "cos"); }
    @Override public Matrix<E> tan(Matrix<E> a) { return elementWiseTranscendental(a, "tan"); }
    @Override public Matrix<E> asin(Matrix<E> a) { return elementWiseTranscendental(a, "asin"); }
    @Override public Matrix<E> acos(Matrix<E> a) { return elementWiseTranscendental(a, "acos"); }
    @Override public Matrix<E> atan(Matrix<E> a) { return elementWiseTranscendental(a, "atan"); }
    @Override public Matrix<E> sinh(Matrix<E> a) { return elementWiseTranscendental(a, "sinh"); }
    @Override public Matrix<E> cosh(Matrix<E> a) { return elementWiseTranscendental(a, "cosh"); }
    @Override public Matrix<E> tanh(Matrix<E> a) { return elementWiseTranscendental(a, "tanh"); }
    @Override public Matrix<E> sqrt(Matrix<E> a) { return elementWiseTranscendental(a, "sqrt"); }
    @Override public Matrix<E> cbrt(Matrix<E> a) { return elementWiseTranscendental(a, "cbrt"); }

    @Override
    @SuppressWarnings("unchecked")
    public Matrix<E> pow(Matrix<E> a, E exponent) {
        int m = a.rows();
        int n = a.cols();
        Ring<E> r = a.getScalarRing();
        DenseMatrixStorage<E> storage = new DenseMatrixStorage<>(m, n, r.zero());
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                E val = a.get(i, j);
                E res;
                if (val instanceof org.episteme.core.mathematics.numbers.real.Real && exponent instanceof org.episteme.core.mathematics.numbers.real.Real) {
                    res = (E)((org.episteme.core.mathematics.numbers.real.Real)val).pow((org.episteme.core.mathematics.numbers.real.Real)exponent);
                } else if (val instanceof org.episteme.core.mathematics.numbers.complex.Complex && exponent instanceof org.episteme.core.mathematics.numbers.complex.Complex) {
                    res = (E)((org.episteme.core.mathematics.numbers.complex.Complex)val).pow((org.episteme.core.mathematics.numbers.complex.Complex)exponent);
                } else {
                    throw new UnsupportedOperationException("pow not supported for " + val.getClass().getSimpleName());
                }
                storage.set(i, j, res);
            }
        }
        return new GenericMatrix<E>(storage, this, (r instanceof Field) ? (Field<E>)r : null);
    }

    @Override
    public E trace(Matrix<E> a) {
        int n = Math.min(a.rows(), a.cols());
        Ring<E> ring = a.getScalarRing();
        E sum = ring.zero();
        for (int i = 0; i < n; i++) {
            sum = ring.add(sum, a.get(i, i));
        }
        return sum;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> solveTriangular(Matrix<E> A, Vector<E> b, boolean upper, boolean transpose, boolean conjugate, boolean unit) {
        int n = b.dimension();
        Ring<E> ring = b.getScalarRing();
        
        Class<?> componentType = ring.zero().getClass();
        if (ring.zero() instanceof Real) componentType = Real.class;
        else if (ring.zero() instanceof Complex) componentType = Complex.class;
        
        E[] x = (E[]) java.lang.reflect.Array.newInstance(componentType, n);
        
        if (upper) {
            if (transpose) {
                // U^T * x = b (Forward substitution)
                for (int i = 0; i < n; i++) {
                    E sum = ring.zero();
                    for (int j = 0; j < i; j++) {
                        E val = A.get(j, i);
                        if (conjugate) val = conjugate(val);
                        sum = ring.add(sum, ring.multiply(val, x[j]));
                    }
                    E diag = unit ? ring.one() : A.get(i, i);
                    if (conjugate) diag = conjugate(diag);
                    x[i] = ((org.episteme.core.mathematics.structures.rings.Field<E>)ring).divide(ring.subtract(b.get(i), sum), diag);
                }
            } else {
                // U * x = b (Back substitution)
                for (int i = n - 1; i >= 0; i--) {
                    E sum = ring.zero();
                    for (int j = i + 1; j < n; j++) {
                        sum = ring.add(sum, ring.multiply(A.get(i, j), x[j]));
                    }
                    E diag = unit ? ring.one() : A.get(i, i);
                    x[i] = ((org.episteme.core.mathematics.structures.rings.Field<E>)ring).divide(ring.subtract(b.get(i), sum), diag);
                }
            }
        } else {
            if (transpose) {
                // L^T * x = b (Back substitution)
                for (int i = n - 1; i >= 0; i--) {
                    E sum = ring.zero();
                    for (int j = i + 1; j < n; j++) {
                        E val = A.get(j, i);
                        if (conjugate) val = conjugate(val);
                        sum = ring.add(sum, ring.multiply(val, x[j]));
                    }
                    E diag = unit ? ring.one() : A.get(i, i);
                    if (conjugate) diag = conjugate(diag);
                    x[i] = ((org.episteme.core.mathematics.structures.rings.Field<E>)ring).divide(ring.subtract(b.get(i), sum), diag);
                }
            } else {
                // L * x = b (Forward substitution)
                for (int i = 0; i < n; i++) {
                    E sum = ring.zero();
                    for (int j = 0; j < i; j++) {
                        sum = ring.add(sum, ring.multiply(A.get(i, j), x[j]));
                    }
                    E diag = unit ? ring.one() : A.get(i, i);
                    x[i] = ((org.episteme.core.mathematics.structures.rings.Field<E>)ring).divide(ring.subtract(b.get(i), sum), diag);
                }
            }
        }
        return new org.episteme.core.mathematics.linearalgebra.vectors.GenericVector<>(
                new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(x), this, ring);
    }

    @SuppressWarnings("unchecked")
    private Matrix<E> elementWiseTranscendental(Matrix<E> a, String op) {
        int m = a.rows();
        int n = a.cols();
        Ring<E> r = a.getScalarRing();
        DenseMatrixStorage<E> storage = new DenseMatrixStorage<>(m, n, r.zero());
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                E val = a.get(i, j);
                E res;
                if (val instanceof org.episteme.core.mathematics.numbers.real.Real) {
                    org.episteme.core.mathematics.numbers.real.Real rb = (org.episteme.core.mathematics.numbers.real.Real)val;
                    res = switch(op) {
                        case "exp" -> (E)rb.exp();
                        case "log" -> (E)rb.log();
                        case "log10" -> (E)rb.log10();
                        case "sin" -> (E)rb.sin();
                        case "cos" -> (E)rb.cos();
                        case "tan" -> (E)rb.tan();
                        case "asin" -> (E)rb.asin();
                        case "acos" -> (E)rb.acos();
                        case "atan" -> (E)rb.atan();
                        case "sinh" -> (E)rb.sinh();
                        case "cosh" -> (E)rb.cosh();
                        case "tanh" -> (E)rb.tanh();
                        case "sqrt" -> (E)rb.sqrt();
                        case "cbrt" -> (E)rb.cbrt();
                        default -> throw new UnsupportedOperationException(op);
                    };
                } else if (val instanceof org.episteme.core.mathematics.numbers.complex.Complex) {
                    org.episteme.core.mathematics.numbers.complex.Complex c = (org.episteme.core.mathematics.numbers.complex.Complex)val;
                    res = switch(op) {
                        case "exp" -> (E)c.exp();
                        case "log" -> (E)c.log();
                        case "log10" -> (E)c.log10();
                        case "sin" -> (E)c.sin();
                        case "cos" -> (E)c.cos();
                        case "tan" -> (E)c.tan();
                        case "asin" -> (E)c.asin();
                        case "acos" -> (E)c.acos();
                        case "atan" -> (E)c.atan();
                        case "sinh" -> (E)c.sinh();
                        case "cosh" -> (E)c.cosh();
                        case "tanh" -> (E)c.tanh();
                        case "sqrt" -> (E)c.sqrt();
                        case "cbrt" -> (E)c.cbrt();
                        default -> throw new UnsupportedOperationException(op);
                    };
                } else {
                    throw new UnsupportedOperationException(op + " not supported for " + val.getClass().getSimpleName());
                }
                storage.set(i, j, res);
            }
        }
        return new GenericMatrix<E>(storage, this, (r instanceof Field) ? (Field<E>)r : null);
    }

    @Override
    public java.util.Map<String, String> getMetadata() {
        return java.util.Map.of("capabilities", "Transpose,Add,Subtract,Scale,Multiply,Inverse,Determinant,Solve,Dot,Norm,LU,QR,Cholesky,SVD,Eigen,Exp,Sin");
    }

    @Override
    public boolean isCompatible(org.episteme.core.mathematics.structures.rings.Ring<?> ring) {
        return true; 
    }

    @Override
    public double score(org.episteme.core.technical.algorithm.OperationContext context) {
        int dim = (context != null) ? context.getDimensionality() : 0;
        double base = org.episteme.core.technical.algorithm.AutoTuningManager.getDynamicScore(getName(), dim, getPriority());
        if (org.episteme.core.mathematics.context.MathContext.getCurrent().isHighPrecision()) {
            base += 1000.0;
        }
        return base;
    }

    @Override
    public Object createBackend() {
        return this;
    }


    @Override
    public Vector<E> add(Vector<E> a, Vector<E> b) {
        if (a.dimension() != b.dimension()) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }

        final Ring<E> r = a.getScalarRing();
        final Class<?> componentType;
        if (r.zero() instanceof Real) componentType = Real.class;
        else if (r.zero() instanceof Complex) componentType = Complex.class;
        else componentType = r.zero().getClass();

        if (a.dimension() < PARALLEL_THRESHOLD) {
            @SuppressWarnings("unchecked")
            E[] data = (E[]) java.lang.reflect.Array.newInstance(componentType, a.dimension());
            for (int i = 0; i < a.dimension(); i++) {
                data[i] = ((Ring<E>) r).add(a.get(i), b.get(i));
            }
            return new GenericVector<>(
                    new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(data), this, (Ring<E>) r);
        } else {
            return Episteme.computeParallel(() -> {
                List<E> list = java.util.stream.IntStream.range(0, a.dimension())
                    .parallel()
                    .mapToObj(i -> ((Ring<E>) r).add(a.get(i), b.get(i)))
                    .collect(Collectors.toList());
                @SuppressWarnings("unchecked")
                E[] arr = list.toArray((E[]) java.lang.reflect.Array.newInstance(componentType, list.size()));
                return new GenericVector<>(
                        new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(arr),
                        this, (Ring<E>) r);
            });
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> subtract(Vector<E> a, Vector<E> b) {
        if (a.dimension() != b.dimension()) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        if (isReal(a) && isReal(b)) {
            double[] ad = toDoubleArray(a);
            double[] bd = toDoubleArray(b);
            double[] rd = new double[ad.length];
            for (int i = 0; i < ad.length; i++) rd[i] = ad[i] - bd[i];
            return (Vector<E>) (Vector<?>) org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector.of(rd);
        }

        final Ring<E> r = a.getScalarRing();
        final Class<?> componentType;
        if (r.zero() instanceof Real) componentType = Real.class;
        else if (r.zero() instanceof Complex) componentType = Complex.class;
        else componentType = r.zero().getClass();

        if (a.dimension() < PARALLEL_THRESHOLD) {
            List<E> result = new java.util.ArrayList<>(a.dimension());
            for (int i = 0; i < a.dimension(); i++) {
                E negB = ((Ring<E>) r).negate(b.get(i));
                result.add(((Ring<E>) r).add(a.get(i), negB));
            }
            E[] arr = result.toArray((E[]) java.lang.reflect.Array.newInstance(componentType, result.size()));
            return new GenericVector<>(
                    new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(arr), this,
                    (Ring<E>) r);
        } else {
            return org.episteme.core.Episteme.computeParallel(() -> {
                List<E> list = java.util.stream.IntStream.range(0, a.dimension())
                    .parallel()
                    .mapToObj(i -> {
                        if (i % 512 == 0) org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                        E negB = ((Ring<E>) r).negate(b.get(i));
                        return ((Ring<E>) r).add(a.get(i), negB);
                    })
                    .collect(java.util.stream.Collectors.toList());
                E[] arr = list.toArray((E[]) java.lang.reflect.Array.newInstance(componentType, list.size()));
                return new GenericVector<>(
                        new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(arr),
                        this, (Ring<E>) r);
            });
        }
    }

    @Override
    public Vector<E> multiply(Vector<E> vector, E scalar) {
        final Ring<E> ring = vector.getScalarRing();
        final Class<?> componentType;
        if (ring.zero() instanceof Real) componentType = Real.class;
        else if (ring.zero() instanceof Complex) componentType = Complex.class;
        else componentType = ring.zero().getClass();

        if (vector.dimension() < PARALLEL_THRESHOLD) {
            List<E> result = new java.util.ArrayList<>(vector.dimension());
            for (int i = 0; i < vector.dimension(); i++) {
                result.add(((Ring<E>)ring).multiply(vector.get(i), scalar));
            }
            @SuppressWarnings("unchecked")
            E[] arr = result.toArray((E[]) java.lang.reflect.Array.newInstance(componentType, result.size()));
            return new GenericVector<>(
                    new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(arr), this, (Ring<E>)ring);
        } else {
            final Class<?> finalType = componentType;
            return java.util.stream.IntStream.range(0, vector.dimension())
                    .parallel()
                    .mapToObj(i -> {
                        if (i % 512 == 0) org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                        return ((Ring<E>)ring).multiply(vector.get(i), scalar);
                    })
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> {
                                @SuppressWarnings("unchecked")
                                E[] arr = list.toArray((E[]) java.lang.reflect.Array.newInstance(finalType, list.size()));
                                return new GenericVector<>(
                                        new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(
                                                arr),
                                        this, (Ring<E>)ring);
                            }));
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "restricted"})
    public E dot(Vector<E> a, Vector<E> b) {
        if (a.dimension() != b.dimension()) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }
        
        if (isReal(a) && isReal(b)) {
            double sum = 0.0;
            int dim = a.dimension();
            double[] aData = toDoubleArray(a);
            double[] bData = toDoubleArray(b);
            for (int i = 0; i < dim; i++) {
                sum += aData[i] * bData[i];
            }
            if (field.zero() instanceof Real) {
                return (E) Real.of(sum);
            }
        }

        final Ring<E> r = a.getScalarRing();
        if (a.dimension() < PARALLEL_THRESHOLD) {
            E sum = ((Field<E>) r).zero();
            for (int i = 0; i < a.dimension(); i++) {
                E product = ((Field<E>) r).multiply(conjugate(a.get(i)), b.get(i));
                sum = ((Field<E>) r).add(sum, product);
            }
            return sum;
        } else {
            return org.episteme.core.Episteme.computeParallel(() -> java.util.stream.IntStream.range(0, a.dimension())
                    .parallel()
                    .mapToObj(i -> {
                        if (i % 512 == 0) org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                        return ((Field<E>) r).multiply(conjugate(a.get(i)), b.get(i));
                    })
                    .reduce(((Field<E>) r).zero(), ((Field<E>) r)::add));
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "restricted"})
    public E norm(Vector<E> a) {
        if (isReal(a)) {
            double sumSq = 0.0;
            int dim = a.dimension();
            double[] aData = toDoubleArray(a);
            for (int i = 0; i < dim; i++) {
                double val = aData[i];
                sumSq += val * val;
            }
            if (field.zero() instanceof Real) {
                return (E) Real.of(Math.sqrt(sumSq));
            }
        }

        E dotProduct = dot(a, a);
        return sqrt(dotProduct, (Field<E>) a.getScalarRing());
    }

    @Override
    public Vector<E> normalize(Vector<E> a) {
        E n = norm(a);
        if (n == null) return a;
        try {
            if (n instanceof Real r && r.doubleValue() == 0.0) return a;
            if (n instanceof Complex c && c.real() == 0.0 && c.imaginary() == 0.0) return a;
            return multiply(a, ((Field<E>) a.getScalarRing()).inverse(n));
        } catch (Exception e) {
            return a;
        }
    }

    @Override
    public Vector<E> cross(Vector<E> a, Vector<E> b) {
        if (a.dimension() != 3 || b.dimension() != 3) throw new IllegalArgumentException("Cross product only supported for 3D vectors");
        Ring<E> r = a.getScalarRing();
        E a1 = a.get(0); E a2 = a.get(1); E a3 = a.get(2);
        E b1 = b.get(0); E b2 = b.get(1); E b3 = b.get(2);
        E c1 = r.subtract(r.multiply(a2, b3), r.multiply(a3, b2));
        E c2 = r.subtract(r.multiply(a3, b1), r.multiply(a1, b3));
        E c3 = r.subtract(r.multiply(a1, b2), r.multiply(a2, b1));
        return (Vector<E>) org.episteme.core.mathematics.linearalgebra.Vector.of(java.util.Arrays.asList(c1, c2, c3), r);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E angle(Vector<E> a, Vector<E> b) {
        E d = dot(a, b);
        E nA = norm(a);
        E nB = norm(b);
        Ring<E> r = a.getScalarRing();
        try {
            E denom = r.multiply(nA, nB);
            if (denom instanceof Real real && real.doubleValue() == 0.0) return r.zero();
            if (denom instanceof Complex complex && complex.real() == 0.0 && complex.imaginary() == 0.0) return r.zero();
            E cosTheta = ((Field<E>)r).divide(d, denom);
            double cosVal = (cosTheta instanceof Real real) ? real.doubleValue() : ((Complex) cosTheta).real();
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, cosVal)));
            if (r.zero() instanceof Real) return (E) Real.of(angle);
            if (r.zero() instanceof Complex) return (E) Complex.of(angle);
            return r.zero();
        } catch (Exception e) {
            return r.zero();
        }
    }

    @Override
    public Vector<E> projection(Vector<E> a, Vector<E> b) {
        E dAB = dot(a, b);
        E dBB = dot(b, b);
        Ring<E> r = a.getScalarRing();
        try {
            if (dBB instanceof Real real && real.doubleValue() == 0.0) return a;
            if (dBB instanceof Complex complex && complex.real() == 0.0 && complex.imaginary() == 0.0) return a;
            E factor = ((Field<E>)r).divide(dAB, dBB);
            return multiply(b, factor);
        } catch (Exception e) {
            return a;
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public Matrix<E> add(Matrix<E> a, Matrix<E> b) {
        if (a.getClass().getName().endsWith("SIMDRealDoubleMatrix") && b.getClass().getName().endsWith("SIMDRealDoubleMatrix")) {
            return a.add(b);
        }

        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            throw new IllegalArgumentException("Matrix dimensions must match");
        }

        if (isReal(a) && isReal(b)) {
            int rows = a.rows();
            int cols = a.cols();
            double[] dataA = toDoubleArray(a);
            double[] dataB = toDoubleArray(b);
            double[] resData = new double[rows * cols];
            
            if (rows * cols < PARALLEL_THRESHOLD) {
                for (int i = 0; i < rows * cols; i++) {
                    resData[i] = dataA[i] + dataB[i];
                }
            } else {
                
                IntStream.range(0, rows).parallel().forEach(i -> {
                    org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                    int offset = i * cols;
                    for (int j = 0; j < cols; j++) {
                        resData[offset + j] = dataA[offset + j] + dataB[offset + j];
                    }
                });
            }
            return (Matrix<E>) (Matrix<?>) RealDoubleMatrix.of(resData, rows, cols);
        }

        Ring<E> ring = a.getScalarRing();
        DenseMatrixStorage<E> storage = new DenseMatrixStorage<>(a.rows(), a.cols(), ((Field<E>)ring).zero());

        if (a.rows() * a.cols() < PARALLEL_THRESHOLD) {
            for (int i = 0; i < a.rows(); i++) {
                for (int j = 0; j < a.cols(); j++) {
                    storage.set(i, j, ((Field<E>)ring).add(a.get(i, j), b.get(i, j)));
                }
            }
        } else {
            IntStream.range(0, a.rows()).parallel().forEach(i -> {
                org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                for (int j = 0; j < a.cols(); j++) {
                    storage.set(i, j, ((Field<E>)ring).add(a.get(i, j), b.get(i, j)));
                }
            });
        }
        return new GenericMatrix<>(storage, this, (Field<E>)ring);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Matrix<E> subtract(Matrix<E> a, Matrix<E> b) {
        if (a.getClass().getName().endsWith("SIMDRealDoubleMatrix") && b.getClass().getName().endsWith("SIMDRealDoubleMatrix")) {
            return a.subtract(b);
        }

        if (a.rows() != b.rows() || a.cols() != b.cols()) {
            throw new IllegalArgumentException("Matrix dimensions must match");
        }

        if (isReal(a) && isReal(b)) {
            int rows = a.rows();
            int cols = a.cols();
            double[] dataA = toDoubleArray(a);
            double[] dataB = toDoubleArray(b);
            double[] resData = new double[rows * cols];
            
            if (rows * cols < PARALLEL_THRESHOLD) {
                for (int i = 0; i < rows * cols; i++) {
                    resData[i] = dataA[i] - dataB[i];
                }
            } else {
                
                IntStream.range(0, rows).parallel().forEach(i -> {
                    org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                    int offset = i * cols;
                    for (int j = 0; j < cols; j++) {
                        resData[offset + j] = dataA[offset + j] - dataB[offset + j];
                    }
                });
            }
            return (Matrix<E>) (Matrix<?>) RealDoubleMatrix.of(resData, rows, cols);
        }

        Ring<E> ring = a.getScalarRing();
        DenseMatrixStorage<E> storage = new DenseMatrixStorage<>(a.rows(), a.cols(), ((Field<E>)ring).zero());

        if (a.rows() * a.cols() < PARALLEL_THRESHOLD) {
            for (int i = 0; i < a.rows(); i++) {
                for (int j = 0; j < a.cols(); j++) {
                    E negB = ((Field<E>)ring).negate(b.get(i, j));
                    storage.set(i, j, ((Field<E>)ring).add(a.get(i, j), negB));
                }
            }
        } else {
            IntStream.range(0, a.rows()).parallel().forEach(i -> {
                org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                for (int j = 0; j < a.cols(); j++) {
                    E negB = ((Field<E>)ring).negate(b.get(i, j));
                    storage.set(i, j, ((Field<E>)ring).add(a.get(i, j), negB));
                }
            });
        }
        return new GenericMatrix<>(storage, this, (Field<E>)ring);
    }

    @Override
    @SuppressWarnings({"unchecked", "restricted"})
    public Matrix<E> multiply(Matrix<E> a, Matrix<E> b) {
        if (a.getClass().getName().endsWith("SIMDRealDoubleMatrix") && b.getClass().getName().endsWith("SIMDRealDoubleMatrix")) {
            return a.multiply(b);
        }

        if (a.cols() != b.rows()) {
            throw new IllegalArgumentException("Matrix inner dimensions must match: " + a.cols() + " != " + b.rows());
        }

        if (isReal(a) && isReal(b)) {
            int rows = a.rows();
            int cols = b.cols();
            int k = a.cols();
            double[] aData = toDoubleArray(a);
            double[] bData = toDoubleArray(b);

            double[] cData = new double[rows * cols];
            staticTiledMultiply(aData, bData, cData, rows, k, cols);

            return (Matrix<E>) (Matrix<?>) org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix.of(cData, rows, cols);
        }

        int m = a.rows();
        int n = b.cols();
        int k = a.cols();
        final Field<E> f = (Field<E>) a.getScalarRing();
        org.episteme.core.mathematics.linearalgebra.matrices.storage.DenseMatrixStorage<E> storage = 
            new org.episteme.core.mathematics.linearalgebra.matrices.storage.DenseMatrixStorage<>(m, n, f.zero());

        long start = System.nanoTime();
        boolean parallel = m * n * k > 500000;

        if (parallel) {
            IntStream.range(0, m).parallel().forEach(i -> {
                for (int j = 0; j < n; j++) {
                    E sum = f.zero();
                    for (int l = 0; l < k; l++) {
                        sum = f.add(sum, f.multiply(a.get(i, l), b.get(l, j)));
                    }
                    storage.set(i, j, sum);
                }
            });
        } else {
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    E sum = f.zero();
                    for (int l = 0; l < k; l++) {
                        sum = f.add(sum, f.multiply(a.get(i, l), b.get(l, j)));
                    }
                    storage.set(i, j, sum);
                }
            }
        }

        org.episteme.core.util.PerformanceLogger.log("CPU:GenericMultiply", a.getClass().getSimpleName(), System.nanoTime() - start);
        return new GenericMatrix<>(storage, this, f);
    }


    @SuppressWarnings({"unchecked", "restricted"})
    public static <E> Matrix<E> standardMultiply(Matrix<E> a, Matrix<E> b, Field<E> field, LinearAlgebraProvider<E> provider) {
        int rowsA = a.rows();
        int colsA = a.cols();
        int colsB = b.cols();

        // Primitive Fast Path for Reals
        if (isReal(a) && isReal(b)) {
            double[] dataA = toDoubleArray(a);
            double[] dataB = toDoubleArray(b);
            double[] resData = new double[rowsA * colsB];

            long start = System.nanoTime();
            try {
                if (rowsA >= 64 && colsA >= 64 && colsB >= 64) {
                    // Tiled / Blocked multiplication for cache efficiency
                    staticTiledMultiply(dataA, dataB, resData, rowsA, colsA, colsB);
                } else {
                    // Optimized i-k-j loop for small-to-medium matrices
                    for (int i = 0; i < rowsA; i++) {
                        if (i % 64 == 0) org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                        int rowOffsetA = i * colsA;
                        int rowOffsetC = i * colsB;
                        for (int k = 0; k < colsA; k++) {
                            double aik = dataA[rowOffsetA + k];
                            int rowOffsetB = k * colsB;
                            for (int j = 0; j < colsB; j++) {
                                resData[rowOffsetC + j] += aik * dataB[rowOffsetB + j];
                            }
                        }
                    }
                }
            } finally {
                org.episteme.core.util.PerformanceLogger.log("CPU:RealFastMultiply", 
                    a.rows() + "x" + b.cols(), System.nanoTime() - start);
            }
            return (Matrix<E>) (Matrix<?>) RealDoubleMatrix.of(resData, rowsA, colsB);
        }

        Field<E> f = (field != null) ? field : (Field<E>) a.getScalarRing();
        DenseMatrixStorage<E> storage = new DenseMatrixStorage<>(a.rows(), b.cols(), f.zero());
        long start = System.nanoTime();
        try {
            if (a.rows() < 10) {
                for (int i = 0; i < a.rows(); i++) {
                    for (int j = 0; j < b.cols(); j++) {
                        E sum = f.zero();
                        for (int k = 0; k < a.cols(); k++) {
                            E product = f.multiply(a.get(i, k), b.get(k, j));
                            sum = f.add(sum, product);
                        }
                        storage.set(i, j, sum);
                    }
                }
            } else {
                
                IntStream.range(0, a.rows()).parallel().forEach(i -> {
                    if (i % 64 == 0) org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
                    for (int j = 0; j < b.cols(); j++) {
                        E sum = f.zero();
                        for (int k = 0; k < a.cols(); k++) {
                            E product = f.multiply(a.get(i, k), b.get(k, j));
                            sum = f.add(sum, product);
                        }
                        storage.set(i, j, sum);
                    }
                });
            }
        } finally {
            String context = "Unknown";
            if (a instanceof GenericMatrix<?>) {
                context = ((GenericMatrix<?>) a).getStorage().getClass().getSimpleName();
            } else {
                context = a.getClass().getSimpleName();
            }
            org.episteme.core.util.PerformanceLogger.log("CPU:GenericMultiply", context, System.nanoTime() - start);
        }
        return new GenericMatrix<>(storage, provider, field);
    }

    private static boolean isReal(Matrix<?> m) {
        if (m instanceof org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix) {
            return !org.episteme.core.mathematics.context.MathContext.getCurrent().isHighPrecision();
        }
        if (m.getScalarRing() instanceof org.episteme.core.mathematics.sets.Reals) {
            return !org.episteme.core.mathematics.context.MathContext.getCurrent().isHighPrecision();
        }
        return false;
    }

    private static boolean isReal(Vector<?> v) {
        if (v instanceof org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector) {
            return !org.episteme.core.mathematics.context.MathContext.getCurrent().isHighPrecision();
        }
        if (v.getScalarRing() instanceof org.episteme.core.mathematics.sets.Reals) {
            return !org.episteme.core.mathematics.context.MathContext.getCurrent().isHighPrecision();
        }
        return false;
    }

    private static double[] toDoubleArray(Matrix<?> m) {
        int rows = m.rows();
        int cols = m.cols();
        
        if (m instanceof org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix rdm) {
            org.episteme.core.mathematics.linearalgebra.matrices.storage.RealDoubleMatrixStorage storage = rdm.getDoubleStorage();
            double[] data = storage.getData();
            if (data != null) return data;
            return storage.toDoubleArray();
        }
        
        if (m.getClass().getName().endsWith("SIMDRealDoubleMatrix")) {
            try {
                return (double[]) m.getClass().getMethod("getInternalData").invoke(m);
            } catch (Exception e) {}
        }
        
        if (m instanceof GenericMatrix) {
            org.episteme.core.mathematics.linearalgebra.matrices.storage.MatrixStorage<?> storage = 
                ((GenericMatrix<?>) m).getStorage();
            if (storage instanceof org.episteme.core.mathematics.linearalgebra.matrices.storage.RealDoubleMatrixStorage rdms) {
                double[] data = rdms.getData();
                if (data != null) return data;
            }
        }
        
        double[] data = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Object val = m.get(i, j);
                if (val instanceof Real) data[i * cols + j] = ((Real) val).doubleValue();
                else if (val instanceof Number) data[i * cols + j] = ((Number) val).doubleValue();
            }
        }
        return data;
    }

    private static double[] toDoubleArray(Vector<?> v) {
        int n = v.dimension();
        double[] data = new double[n];
        if (v instanceof org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector) {
            return ((org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector) v).toDoubleArray();
        }
        for (int i = 0; i < n; i++) {
            Object val = v.get(i);
            if (val instanceof Real) data[i] = ((Real) val).doubleValue();
            else if (val instanceof Number) data[i] = ((Number) val).doubleValue();
        }
        return data;
    }

    private static void staticTiledMultiply(double[] A, double[] B, double[] C, int M, int K, int N) {
        final int BLOCK_SIZE = 64; 
        
        IntStream.range(0, (M + BLOCK_SIZE - 1) / BLOCK_SIZE).parallel().forEach(bi -> {
            org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
            int i0 = bi * BLOCK_SIZE;
            int iMax = Math.min(i0 + BLOCK_SIZE, M);
            for (int k0 = 0; k0 < K; k0 += BLOCK_SIZE) {
                int kMax = Math.min(k0 + BLOCK_SIZE, K);
                for (int j0 = 0; j0 < N; j0 += BLOCK_SIZE) {
                    int jMax = Math.min(j0 + BLOCK_SIZE, N);
                    
                    for (int i = i0; i < iMax; i++) {
                        int rowA = i * K;
                        int rowC = i * N;
                        for (int k = k0; k < kMax; k++) {
                            double aik = A[rowA + k];
                            int rowB = k * N;
                            for (int j = j0; j < jMax; j++) {
                                C[rowC + j] += aik * B[rowB + j];
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    @SuppressWarnings({"unchecked", "restricted"})
    public Matrix<E> transpose(Matrix<E> a) {
        if (isReal(a)) {
            int rows = a.rows();
            int cols = a.cols();
            double[] data = toDoubleArray(a);
            double[] resData = new double[rows * cols];
            for (int i = 0; i < rows; i++) {
                int offsetI = i * cols;
                for (int j = 0; j < cols; j++) {
                    resData[j * rows + i] = data[offsetI + j];
                }
            }
            return (Matrix<E>) (Matrix<?>) RealDoubleMatrix.of(resData, cols, rows);
        }

        if (a.getClass().getName().endsWith("SIMDRealDoubleMatrix")) {
            try {
                return (Matrix<E>) a.getClass().getMethod("transpose").invoke(a);
            } catch (Exception e) {
                // Fallback handled below
            }
        }

        DenseMatrixStorage<E> storage = new DenseMatrixStorage<>(a.cols(), a.rows(), ((Field<E>)a.getScalarRing()).zero());
        
        IntStream.range(0, a.rows()).parallel().forEach(i -> {
            org.episteme.core.mathematics.context.MathContext.checkCurrentCancelled();
            for (int j = 0; j < a.cols(); j++) {
                storage.set(j, i, a.get(i, j));
            }
        });
        return new GenericMatrix<>(storage, this, (Field<E>) a.getScalarRing());
    }

    @Override
    @SuppressWarnings({"unchecked", "restricted"})
    public Matrix<E> scale(E scalar, Matrix<E> a) {
        if (scalar instanceof Real && isReal(a)) {
            double s = ((Real) scalar).doubleValue();
            int rows = a.rows();
            int cols = a.cols();
            double[] data = toDoubleArray(a).clone();
            for (int i = 0; i < data.length; i++) {
                data[i] *= s;
            }
            return (Matrix<E>) (Matrix<?>) org.episteme.core.mathematics.linearalgebra.matrices.RealDoubleMatrix.of(data, rows, cols);
        }

        if (a.getClass().getName().endsWith("SIMDRealDoubleMatrix") && scalar instanceof Real) {
            try {
                return (Matrix<E>) a.getClass().getMethod("scale", double.class).invoke(a, ((Real) scalar).doubleValue());
            } catch (Exception e) {}
        }

        int m = a.rows();
        int n = a.cols();
        final Field<E> f = (Field<E>) a.getScalarRing();
        org.episteme.core.mathematics.linearalgebra.matrices.storage.DenseMatrixStorage<E> storage = 
            new org.episteme.core.mathematics.linearalgebra.matrices.storage.DenseMatrixStorage<>(m, n, f.zero());
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                storage.set(i, j, f.multiply(scalar, a.get(i, j)));
            }
        }
        return new GenericMatrix<>(storage, this, f);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> multiply(Matrix<E> a, Vector<E> b) {
        if (isReal(a)) {
            int rows = a.rows();
            int cols = a.cols();
            double[] aData = toDoubleArray(a);
            double[] bData = toDoubleArray(b);
            double[] res = new double[rows];
            for (int i = 0; i < rows; i++) {
                double sum = 0.0;
                for (int j = 0; j < cols; j++) {
                    sum += aData[i * cols + j] * bData[j];
                }
                res[i] = sum;
            }
            return (Vector<E>) (Vector<?>) org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector.of(res);
        }

        int m = a.rows();
        int n = a.cols();
        final Field<E> f = (Field<E>) a.getScalarRing();
        Class<?> componentType = f.zero().getClass();
        if (f.zero() instanceof Real) componentType = Real.class;
        else if (f.zero() instanceof Complex) componentType = Complex.class;

        E[] resArray = (E[]) java.lang.reflect.Array.newInstance(componentType, m);
        for (int i = 0; i < m; i++) {
            E sum = f.zero();
            for (int j = 0; j < n; j++) {
                sum = f.add(sum, f.multiply(a.get(i, j), b.get(j)));
            }
            resArray[i] = sum;
        }
        return new GenericVector<>(
                new org.episteme.core.mathematics.linearalgebra.vectors.storage.DenseVectorStorage<>(resArray), this, f);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Matrix<E> inverse(Matrix<E> a) {
        if (a.rows() != a.cols()) {
             // Moore-Penrose Pseudo-inverse via Normal Equations
             Matrix<E> at = transpose(a);
             if (a.rows() > a.cols()) {
                 return multiply(inverse(multiply(at, a)), at);
             } else {
                 return multiply(at, inverse(multiply(a, at)));
             }
        }
        if (isReal(a)) {
            return (Matrix<E>) (Object) JavaLU.inverse((Matrix<Real>) (Object) a);
        }
        return GenericLU.inverse(a, (Field<E>) (Object) a.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E determinant(Matrix<E> a) {
        if (a.rows() != a.cols()) {
            throw new IllegalArgumentException("Matrix must be square");
        }
        if (isReal(a)) {
            return (E) (Object) JavaLU.determinant((Matrix<Real>) (Object) a);
        }
        return GenericLU.determinant(a, (Field<E>) a.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> solve(Matrix<E> a, Vector<E> b) {
        if (a.rows() != b.dimension()) {
            throw new IllegalArgumentException("Matrix rows and vector dimension must match");
        }
        if (a.rows() != a.cols()) {
            if (isReal(a) && isReal(b)) {
                return (Vector<E>) (Object) JavaQR.solve(JavaQR.decompose((Matrix<Real>) (Object) a), (Vector<Real>) (Object) b);
            }
            return GenericQR.solve(GenericQR.decompose(a, (Field<E>) a.getScalarRing(), this), b, (Field<E>) a.getScalarRing(), this);
        }
        if (isReal(a) && isReal(b)) {
            return (Vector<E>) JavaLU.solve((Matrix<Real>) a, (Vector<Real>) b);
        }
        return GenericLU.solve(a, b, (Field<E>) a.getScalarRing(), this);
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LUResult<E> lu(Matrix<E> a) {
        if (a.getScalarRing() instanceof Reals) {
            return (LUResult<E>) JavaLU.decompose((Matrix<Real>) a);
        }
        return GenericLU.decompose(a, (Field<E>) a.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public QRResult<E> qr(Matrix<E> a) {
        if (a.getScalarRing() instanceof Reals) {
            return (QRResult<E>) JavaQR.decompose((Matrix<Real>) a);
        }
        return GenericQR.decompose(a, (Field<E>) a.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CholeskyResult<E> cholesky(Matrix<E> a) {
        if (a.getScalarRing() instanceof Reals) {
            return (CholeskyResult<E>) JavaCholesky.decompose((Matrix<Real>) a);
        }
        return GenericCholesky.decompose(a, (Field<E>) a.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public SVDResult<E> svd(Matrix<E> a) {
        if (a.getScalarRing() instanceof Reals) {
            return (SVDResult<E>) JavaSVD.decompose((Matrix<Real>) a);
        }
        return GenericSVD.decompose(a, (Field<E>) a.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public EigenResult<E> eigen(Matrix<E> a) {
        if (a.getScalarRing() instanceof Reals) {
            return (EigenResult<E>) JavaEigen.decompose((Matrix<Real>) a);
        }
        return GenericEigen.decompose(a, (Field<E>) a.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> solve(LUResult<E> lu, Vector<E> b) {
        if (b.getScalarRing() instanceof Reals) {
            return (Vector<E>) JavaLU.solve((LUResult<Real>) lu, (Vector<Real>) b);
        }
        return GenericLU.solve(lu, b, (Field<E>) b.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> solve(QRResult<E> qr, Vector<E> b) {
        if (b.getScalarRing() instanceof Reals) {
            return (Vector<E>) JavaQR.solve((QRResult<Real>) qr, (Vector<Real>) b);
        }
        return GenericQR.solve(qr, b, (Field<E>) b.getScalarRing(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> solve(CholeskyResult<E> cholesky, Vector<E> b) {
        if (b.getScalarRing() instanceof Reals) {
            return (Vector<E>) JavaCholesky.solve((CholeskyResult<Real>) cholesky, (Vector<Real>) b);
        }
        return GenericCholesky.solve(cholesky, b, (Field<E>) b.getScalarRing(), this);
    }


    // Decompositions now use the top-level generic solvers imported from org.episteme.core.mathematics.linearalgebra.matrices.solvers


    @SuppressWarnings("unchecked")
    private E conjugate(E element) {
        if (element instanceof Complex) {
            return (E) ((Complex) element).conjugate();
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    private E sqrt(E element, Field<E> field) {
        if (element instanceof Real) {
            return (E) ((Real) element).sqrt();
        }
        if (element instanceof Complex) {
            return (E) ((Complex) element).sqrt();
        }
        try {
            java.lang.reflect.Method m = element.getClass().getMethod("sqrt");
            return (E) m.invoke(element);
        } catch (Exception e) {}
        return element;
    }
    
    private static class JavaLU {
        public static LUResult<Real> decompose(Matrix<Real> matrix) {
            int n = matrix.rows();
            if (n != matrix.cols()) throw new IllegalArgumentException("Matrix must be square");

            Real[][] data = new Real[n][n];
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) data[i][j] = matrix.get(i, j);

            int[] perm = new int[n];
            for (int i = 0; i < n; i++) perm[i] = i;

            for (int k = 0; k < n; k++) {
                int maxRow = k;
                Real maxVal = data[k][k].abs();
                for (int i = k + 1; i < n; i++) {
                    Real val = data[i][k].abs();
                    if (val.compareTo(maxVal) > 0) {
                        maxVal = val;
                        maxRow = i;
                    }
                }

                if (maxRow != k) {
                    Real[] temp = data[k];
                    data[k] = data[maxRow];
                    data[maxRow] = temp;
                    int tempPerm = perm[k];
                    perm[k] = perm[maxRow];
                    perm[maxRow] = tempPerm;
                }

                for (int i = k + 1; i < n; i++) {
                    if (!data[k][k].isZero()) {
                        Real factor = data[i][k].divide(data[k][k]);
                        data[i][k] = factor;
                        for (int j = k + 1; j < n; j++) {
                            data[i][j] = data[i][j].subtract(factor.multiply(data[k][j]));
                        }
                    }
                }
            }

            Real[][] lData = new Real[n][n];
            Real[][] uData = new Real[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (i > j) { lData[i][j] = data[i][j]; uData[i][j] = Real.ZERO; }
                    else if (i == j) { lData[i][j] = Real.ONE; uData[i][j] = data[i][j]; }
                    else { lData[i][j] = Real.ZERO; uData[i][j] = data[i][j]; }
                }
            }

            double[] pDouble = new double[n];
            for(int i=0; i<n; i++) pDouble[i] = perm[i];

            return new LUResult<>(
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(lData, Reals.getInstance()),
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(uData, Reals.getInstance()),
                org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector.of(pDouble)
            );
        }

        public static Vector<Real> solve(LUResult<Real> lu, Vector<Real> b) {
            try (CPUDenseLinearAlgebraProvider<Real> provider = new CPUDenseLinearAlgebraProvider<>()) {
                int n = lu.L().rows();
                Real[] pb = new Real[n];

                for (int i = 0; i < n; i++) {
                    Object pVal = lu.P().get(i);
                    int pIdx;
                    if (pVal instanceof org.episteme.core.mathematics.numbers.real.Real) pIdx = (int) ((org.episteme.core.mathematics.numbers.real.Real) pVal).doubleValue();
                    else if (pVal instanceof Number) pIdx = ((Number) pVal).intValue();
                    else pIdx = i;
                    pb[i] = b.get(pIdx);
                }
                
                Vector<Real> pbv = org.episteme.core.mathematics.linearalgebra.vectors.DenseVector.of(java.util.Arrays.asList(pb), Reals.getInstance());
                
                // Forward substitution L*y = Pb
                Vector<Real> y = provider.solveTriangular(lu.L(), pbv, false, false, false, true);
                // Backward substitution U*x = y
                return provider.solveTriangular(lu.U(), y, true, false, false, false);
            }
        }

        public static Real determinant(Matrix<Real> a) {
            LUResult<Real> lu = decompose(a);
            Real det = Real.ONE;
            int n = a.rows();
            for (int i = 0; i < n; i++) det = det.multiply(lu.U().get(i, i));
            
            // Correct Permutation Parity using Cycle Decomposition (March 24 Standard)
            int swaps = 0;
            boolean[] visited = new boolean[n];
            int[] p = new int[n];
            for (int i = 0; i < n; i++) {
                Object pVal = lu.P().get(i);
                if (pVal instanceof Real) p[i] = (int) ((Real) pVal).doubleValue();
                else p[i] = ((Number) pVal).intValue();
            }
            
            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    int curr = i;
                    int cycleSize = 0;
                    while (!visited[curr]) {
                        visited[curr] = true;
                        curr = p[curr];
                        cycleSize++;
                    }
                    if (cycleSize > 1) {
                        swaps += (cycleSize - 1);
                    }
                }
            }
            
            if (swaps % 2 != 0) det = det.negate();
            return det;
        }

        public static Matrix<Real> inverse(Matrix<Real> a) {
            int n = a.rows();
            LUResult<Real> lu = decompose(a);
            Real[][] inv = new Real[n][n];
            for (int j = 0; j < n; j++) {
                Real[] e = new Real[n];
                for (int i = 0; i < n; i++) e[i] = (i == j) ? Real.ONE : Real.ZERO;
                Vector<Real> ev = org.episteme.core.mathematics.linearalgebra.vectors.DenseVector.of(java.util.Arrays.asList(e), Reals.getInstance());
                Vector<Real> x = solve(lu, ev);
                for (int i = 0; i < n; i++) inv[i][j] = x.get(i);
            }
            return new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(inv, Reals.getInstance());
        }

        public static Vector<Real> solve(Matrix<Real> a, Vector<Real> b) {
            return solve(decompose(a), b);
        }
    }

    private static class JavaQR {
        public static QRResult<Real> decompose(Matrix<Real> matrix) {
            int m = matrix.rows();
            int n = matrix.cols();

            Real[][] A = new Real[m][n];
            for (int i = 0; i < m; i++) for (int j = 0; j < n; j++) A[i][j] = matrix.get(i, j);

            Real[][] Q = new Real[m][m];
            for (int i = 0; i < m; i++) for (int j = 0; j < m; j++) Q[i][j] = (i == j) ? Real.ONE : Real.ZERO;

            for (int k = 0; k < Math.min(m, n); k++) {
                Real norm = Real.ZERO;
                for (int i = k; i < m; i++) norm = norm.add(A[i][k].multiply(A[i][k]));
                norm = norm.sqrt();
                if (norm.isZero()) {
                    continue;
                }

                Real a1 = A[k][k];
                Real alpha = (a1.compareTo(Real.ZERO) >= 0) ? norm.negate() : norm;
                
                Real[] v = new Real[m - k];
                v[0] = A[k][k].subtract(alpha);
                for (int i = 1; i < v.length; i++) v[i] = A[k + i][k];

                Real vNorm = Real.ZERO;
                for (Real val : v) vNorm = vNorm.add(val.multiply(val));
                vNorm = vNorm.sqrt();
                if (vNorm.isZero()) continue;
                for (int i = 0; i < v.length; i++) v[i] = v[i].divide(vNorm);

                for (int j = k; j < n; j++) {
                    Real vDotA = Real.ZERO;
                    for (int i = 0; i < v.length; i++) vDotA = vDotA.add(v[i].multiply(A[k + i][j]));
                    Real twoVDotA = vDotA.add(vDotA);
                    for (int i = 0; i < v.length; i++) A[k + i][j] = A[k + i][j].subtract(twoVDotA.multiply(v[i]));
                }

                for (int i = 0; i < m; i++) {
                    Real qDotV = Real.ZERO;
                    for (int j = 0; j < v.length; j++) qDotV = qDotV.add(Q[i][k + j].multiply(v[j]));
                    Real twoQDotV = qDotV.add(qDotV);
                    for (int j = 0; j < v.length; j++) Q[i][k + j] = Q[i][k + j].subtract(twoQDotV.multiply(v[j]));
                }
            }

            for (int i = 0; i < m; i++) for (int j = 0; j < Math.min(i, n); j++) A[i][j] = Real.ZERO;
            return new QRResult<>(
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(Q, Reals.getInstance()), 
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(A, Reals.getInstance())
            );
        }

        public static Vector<Real> solve(QRResult<Real> qr, Vector<Real> b) {
            int m = qr.Q().rows();
            int n = qr.R().cols();
            Real[] qtb = new Real[m];
            for (int i = 0; i < m; i++) {
                Real sum = Real.ZERO;
                for (int j = 0; j < m; j++) sum = sum.add(qr.Q().get(j, i).multiply(b.get(j)));
                qtb[i] = sum;
            }
            Real[] x = new Real[n];
            for (int i = n - 1; i >= 0; i--) {
                Real sum = qtb[i];
                for (int j = i + 1; j < n; j++) sum = sum.subtract(qr.R().get(i, j).multiply(x[j]));
                Real rii = qr.R().get(i, i);
                if (rii.abs().compareTo(Real.of(1e-15)) < 0) x[i] = Real.ZERO;
                else x[i] = sum.divide(rii);
            }
            return org.episteme.core.mathematics.linearalgebra.vectors.DenseVector.of(java.util.Arrays.asList(x), Reals.getInstance());
        }
    }

    private static class JavaCholesky {
        public static CholeskyResult<Real> decompose(Matrix<Real> matrix) {
            int n = matrix.rows();
            Real[][] L = new Real[n][n];
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) L[i][j] = Real.ZERO;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j <= i; j++) {
                    Real sum = Real.ZERO;
                    if (j == i) {
                        for (int k = 0; k < j; k++) sum = sum.add(L[j][k].multiply(L[j][k]));
                        Real diag = matrix.get(j, j).subtract(sum);
                        if (diag.compareTo(Real.ZERO) <= 0) throw new IllegalArgumentException("Not positive definite");
                        L[j][j] = diag.sqrt();
                    } else {
                        for (int k = 0; k < j; k++) sum = sum.add(L[i][k].multiply(L[j][k]));
                        L[i][j] = matrix.get(i, j).subtract(sum).divide(L[j][j]);
                    }
                }
            }
            return new CholeskyResult<>(new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(L, Reals.getInstance()));
        }

        public static Vector<Real> solve(CholeskyResult<Real> cholesky, Vector<Real> b) {
            int n = cholesky.L().rows();
            Real[] y = new Real[n];
            Real[] x = new Real[n];
            for (int i = 0; i < n; i++) {
                Real sum = Real.ZERO;
                for (int j = 0; j < i; j++) sum = sum.add(cholesky.L().get(i, j).multiply(y[j]));
                y[i] = b.get(i).subtract(sum).divide(cholesky.L().get(i, i));
            }
            for (int i = n - 1; i >= 0; i--) {
                Real sum = Real.ZERO;
                for (int j = i + 1; j < n; j++) sum = sum.add(cholesky.L().get(j, i).multiply(x[j]));
                x[i] = y[i].subtract(sum).divide(cholesky.L().get(i, i));
            }
            return org.episteme.core.mathematics.linearalgebra.vectors.DenseVector.of(java.util.Arrays.asList(x), Reals.getInstance());
        }
    }

    private static class JavaSVD {
        public static SVDResult<Real> decompose(Matrix<Real> matrix) {
            int m = matrix.rows();
            int n = matrix.cols();
            double[][] A = new double[m][n];
            for (int i = 0; i < m; i++) for (int j = 0; j < n; j++) A[i][j] = matrix.get(i, j).doubleValue();

            int nu = Math.min(m, n);
            double[] s = new double[Math.min(m + 1, n)];
            double[][] U = new double[m][nu];
            double[][] V = new double[n][n];
            double[] e = new double[n];
            double[] work = new double[m];

            int nct = Math.min(m - 1, n);
            int nrt = Math.max(0, Math.min(n - 2, m));
            for (int k = 0; k < Math.max(nct, nrt); k++) {
                if (k < nct) {
                    s[k] = 0;
                    for (int i = k; i < m; i++) s[k] = Math.hypot(s[k], A[i][k]);
                    if (s[k] != 0.0) {
                        if (A[k][k] < 0.0) s[k] = -s[k];
                        for (int i = k; i < m; i++) A[i][k] /= s[k];
                        A[k][k] += 1.0;
                    }
                    s[k] = -s[k];
                }
                for (int j = k + 1; j < n; j++) {
                    if ((k < nct) && (s[k] != 0.0)) {
                        double t = 0;
                        for (int i = k; i < m; i++) t += A[i][k] * A[i][j];
                        t = -t / A[k][k];
                        for (int i = k; i < m; i++) A[i][j] += t * A[i][k];
                    }
                    e[j] = A[k][j];
                }
                if (k < nct) {
                    for (int i = k; i < m; i++) U[i][k] = A[i][k];
                }
                if (k < nrt) {
                    e[k] = 0;
                    for (int i = k + 1; i < n; i++) e[k] = Math.hypot(e[k], e[i]);
                    if (e[k] != 0.0) {
                        if (e[k + 1] < 0.0) e[k] = -e[k];
                        for (int i = k + 1; i < n; i++) e[i] /= e[k];
                        e[k + 1] += 1.0;
                    }
                    e[k] = -e[k];
                    if ((k + 1 < m) && (e[k] != 0.0)) {
                        for (int i = k + 1; i < m; i++) work[i] = 0.0;
                        for (int j = k + 1; j < n; j++) {
                            for (int i = k + 1; i < m; i++) work[i] += e[j] * A[i][j];
                        }
                        for (int j = k + 1; j < n; j++) {
                            double t = -e[j] / e[k + 1];
                            for (int i = k + 1; i < m; i++) A[i][j] += t * work[i];
                        }
                    }
                    for (int i = k + 1; i < n; i++) V[i][k] = e[i];
                }
            }

            int p = Math.min(n, m + 1);
            if (nct < n) s[nct] = A[nct][nct];
            if (m < p) s[p - 1] = 0.0;
            if (nrt + 1 < p) e[nrt] = A[nrt][p - 1];
            e[p - 1] = 0.0;

            for (int j = nct; j < nu; j++) {
                for (int i = 0; i < m; i++) U[i][j] = 0.0;
                U[j][j] = 1.0;
            }
            for (int k = nct - 1; k >= 0; k--) {
                if (s[k] != 0.0) {
                    for (int j = k + 1; j < nu; j++) {
                        double t = 0;
                        for (int i = k; i < m; i++) t += U[i][k] * U[i][j];
                        t = -t / U[k][k];
                        for (int i = k; i < m; i++) U[i][j] += t * U[i][k];
                    }
                    for (int i = k; i < m; i++) U[i][k] = -U[i][k];
                    U[k][k] = 1.0 + U[k][k];
                    for (int i = 0; i < k - 1; i++) U[i][k] = 0.0;
                } else {
                    for (int i = 0; i < m; i++) U[i][k] = 0.0;
                    U[k][k] = 1.0;
                }
            }

            for (int k = n - 1; k >= 0; k--) {
                if ((k < nrt) && (e[k] != 0.0)) {
                    for (int j = k + 1; j < nu; j++) {
                        double t = 0;
                        for (int i = k + 1; i < n; i++) t += V[i][k] * V[i][j];
                        t = -t / V[k + 1][k];
                        for (int i = k + 1; i < n; i++) V[i][j] += t * V[i][k];
                    }
                }
                for (int i = 0; i < n; i++) V[i][k] = 0.0;
                V[k][k] = 1.0;
            }

            int pp = p - 1;
            int iter = 0;
            double eps = Math.pow(2.0, -52.0);
            double tiny = Math.pow(2.0, -966.0);
            while (p > 0) {
                int k, kase;
                if (iter >= 500) break;
                for (k = p - 2; k >= -1; k--) {
                    if (k == -1) break;
                    if (Math.abs(e[k]) <= tiny + eps * (Math.abs(s[k]) + Math.abs(s[k + 1]))) {
                        e[k] = 0.0;
                        break;
                    }
                }
                if (k == p - 2) {
                    kase = 4;
                } else {
                    int ks;
                    for (ks = p - 1; ks >= k; ks--) {
                        if (ks == k) break;
                        double t = (ks != p ? Math.abs(e[ks]) : 0.) + (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
                        if (Math.abs(s[ks]) <= tiny + eps * t) {
                            s[ks] = 0.0;
                            break;
                        }
                    }
                    if (ks == k) kase = 3;
                    else if (ks == p - 1) kase = 1;
                    else { kase = 2; k = ks; }
                }
                k++;

                switch (kase) {
                    case 1: {
                        double f = e[p - 2];
                        e[p - 2] = 0.0;
                        for (int j = p - 2; j >= k; j--) {
                            double t = Math.hypot(s[j], f);
                            double cs = s[j] / t;
                            double sn = f / t;
                            s[j] = t;
                            if (j != k) { f = -sn * e[j - 1]; e[j - 1] = cs * e[j - 1]; }
                            for (int i = 0; i < n; i++) {
                                t = cs * V[i][j] + sn * V[i][p - 1];
                                V[i][p - 1] = -sn * V[i][j] + cs * V[i][p - 1];
                                V[i][j] = t;
                            }
                        }
                    }
                    break;
                    case 2: {
                        double f = e[k - 1];
                        e[k - 1] = 0.0;
                        for (int j = k; j < p; j++) {
                            double t = Math.hypot(s[j], f);
                            double cs = s[j] / t;
                            double sn = f / t;
                            s[j] = t;
                            f = -sn * e[j];
                            e[j] = cs * e[j];
                            for (int i = 0; i < m; i++) {
                                t = cs * U[i][j] + sn * U[i][k - 1];
                                U[i][k - 1] = -sn * U[i][j] + cs * U[i][k - 1];
                                U[i][j] = t;
                            }
                        }
                    }
                    break;
                    case 3: {
                        double scale = Math.max(Math.max(Math.max(Math.max(Math.abs(s[p - 1]), Math.abs(s[p - 2])), Math.abs(e[p - 2])), Math.abs(s[k])), Math.abs(e[k]));
                        double sp = s[p - 1] / scale;
                        double spm1 = s[p - 2] / scale;
                        double epm1 = e[p - 2] / scale;
                        double sk = s[k] / scale;
                        double ek = e[k] / scale;
                        double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
                        double c = (sp * epm1) * (sp * epm1);
                        double shift = 0.0;
                        if ((b != 0.0) || (c != 0.0)) {
                            shift = Math.sqrt(b * b + c);
                            if (b < 0.0) shift = -shift;
                            shift = c / (b + shift);
                        }
                        double f = (sk + sp) * (sk - sp) + shift;
                        double g = sk * ek;
                        for (int j = k; j < p - 1; j++) {
                            double t = Math.hypot(f, g);
                            double cs = f / t;
                            double sn = g / t;
                            if (j != k) e[j - 1] = t;
                            f = cs * s[j] + sn * e[j];
                            e[j] = cs * e[j] - sn * s[j];
                            g = sn * s[j + 1];
                            s[j + 1] = cs * s[j + 1];
                            for (int i = 0; i < n; i++) {
                                t = cs * V[i][j] + sn * V[i][j + 1];
                                V[i][j + 1] = -sn * V[i][j] + cs * V[i][j + 1];
                                V[i][j] = t;
                            }
                            t = Math.hypot(f, g);
                            cs = f / t;
                            sn = g / t;
                            s[j] = t;
                            f = cs * e[j] + sn * s[j + 1];
                            s[j + 1] = -sn * e[j] + cs * s[j + 1];
                            g = sn * e[j + 1];
                            e[j + 1] = cs * e[j + 1];
                            for (int i = 0; i < m; i++) {
                                t = cs * U[i][j] + sn * U[i][j + 1];
                                U[i][j + 1] = -sn * U[i][j] + cs * U[i][j + 1];
                                U[i][j] = t;
                            }
                        }
                        e[p - 2] = f;
                        iter = iter + 1;
                    }
                    break;
                    case 4: {
                        if (s[k] <= 0.0) {
                            s[k] = (s[k] < 0.0 ? -s[k] : 0.0);
                            for (int i = 0; i <= pp; i++) V[i][k] = -V[i][k];
                        }
                        while (k < pp) {
                            if (s[k] >= s[k + 1]) break;
                            double t = s[k]; s[k] = s[k + 1]; s[k + 1] = t;
                            for (int i = 0; i < n; i++) { t = V[i][k + 1]; V[i][k + 1] = V[i][k]; V[i][k] = t; }
                            for (int i = 0; i < m; i++) { t = U[i][k + 1]; U[i][k + 1] = U[i][k]; U[i][k] = t; }
                            k++;
                        }
                        iter = 0;
                        p--;
                    }
                    break;
                }
            }

            Real[] sigmaReal = new Real[nu];
            for (int i = 0; i < nu; i++) sigmaReal[i] = Real.of(s[i]);
            Real[][] UReal = new Real[m][nu];
            for (int i = 0; i < m; i++) for (int j = 0; j < nu; j++) UReal[i][j] = Real.of(U[i][j]);
            Real[][] VReal = new Real[n][nu]; // Economy V
            for (int i = 0; i < n; i++) for (int j = 0; j < nu; j++) VReal[i][j] = Real.of(V[i][j]);

            return new SVDResult<>(
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(UReal, Reals.getInstance()),
                org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector.of(s),
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(VReal, Reals.getInstance())
            );
        }
    }

    private static class JavaEigen {
        public static EigenResult<Real> decompose(Matrix<Real> matrix) {
            int n = matrix.rows();
            if (n != matrix.cols()) throw new IllegalArgumentException("Matrix must be square");

            // Check for symmetry - Jacobi is very robust for symmetric matrices
            boolean isSymmetric = true;
            for (int i = 0; i < n && isSymmetric; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (Math.abs(matrix.get(i, j).doubleValue() - matrix.get(j, i).doubleValue()) > 1e-10) {
                        isSymmetric = false;
                        break;
                    }
                }
            }

            if (isSymmetric) {
                return jacobi(matrix);
            }

            // Fallback to QR for non-symmetric matrices
            Real[][] A = new Real[n][n];
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) A[i][j] = matrix.get(i, j);

            Real[][] Q_acc = new Real[n][n];
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) Q_acc[i][j] = (i == j) ? Real.ONE : Real.ZERO;

            int maxIter = 200;
            for (int iter = 0; iter < maxIter; iter++) {
                QRResult<Real> qr = JavaQR.decompose(new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(A, Reals.getInstance()));
                Matrix<Real> nextA = qr.R().multiply(qr.Q());
                
                Matrix<Real> nextQ = new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(Q_acc, Reals.getInstance()).multiply(qr.Q());
                for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) Q_acc[i][j] = nextQ.get(i, j);

                double offDiagNorm = 0.0;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        A[i][j] = nextA.get(i, j);
                        if (i != j) offDiagNorm += Math.abs(A[i][j].doubleValue());
                    }
                }
                if (offDiagNorm < 1e-12) break;
            }

            Real[] values = new Real[n];
            for (int i = 0; i < n; i++) values[i] = A[i][i];

            return new org.episteme.core.mathematics.linearalgebra.matrices.solvers.EigenResult<Real>(
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(Q_acc, Reals.getInstance()), 
                org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector.of(java.util.Arrays.stream(values).mapToDouble(Real::doubleValue).toArray()));
        }

        private static EigenResult<Real> jacobi(Matrix<Real> a) {
            int n = a.rows();
            double[] data = new double[n * n];
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) data[i * n + j] = a.get(i, j).doubleValue();
            
            double[] vData = new double[n * n];
            for (int i = 0; i < n; i++) vData[i * n + i] = 1.0;
            
            int maxSweeps = 50;
            double eps = 1e-15;
            
            for (int sweep = 0; sweep < maxSweeps; sweep++) {
                double offDiag = 0;
                for (int i = 0; i < n; i++) {
                    for (int j = i + 1; j < n; j++) offDiag += Math.abs(data[i * n + j]);
                }
                if (offDiag < eps) break;
                
                for (int p = 0; p < n - 1; p++) {
                    for (int q = p + 1; q < n; q++) {
                        double apq = data[p * n + q];
                        if (Math.abs(apq) < eps) continue;
                        
                        double app = data[p * n + p];
                        double aqq = data[q * n + q];
                        
                        double tau = (aqq - app) / (2.0 * apq);
                        double t = (tau >= 0) ? 1.0 / (tau + Math.sqrt(1.0 + tau * tau)) 
                                             : 1.0 / (tau - Math.sqrt(1.0 + tau * tau));
                        double c = 1.0 / Math.sqrt(1.0 + t * t);
                        double s = t * c;

                        // Update A
                        for (int i = 0; i < n; i++) {
                            double tp = data[p * n + i];
                            double tq = data[q * n + i];
                            data[p * n + i] = c * tp - s * tq;
                            data[q * n + i] = s * tp + c * tq;
                        }
                        for (int j = 0; j < n; j++) {
                            data[j * n + p] = data[p * n + j];
                            data[j * n + q] = data[q * n + j];
                        }
                        
                        data[p * n + p] = app - t * apq;
                        data[q * n + q] = aqq + t * apq;
                        data[p * n + q] = 0.0;
                        data[q * n + p] = 0.0;
                        
                        // Update V
                        for (int i = 0; i < n; i++) {
                            double vp = vData[i * n + p];
                            double vq = vData[i * n + q];
                            vData[i * n + p] = c * vp - s * vq;
                            vData[i * n + q] = s * vp + c * vq;
                        }
                    }
                }
            }
            
            double[] eigenvalues = new double[n];
            for (int i = 0; i < n; i++) eigenvalues[i] = data[i * n + i];
            
            Real[][] V = new Real[n][n];
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) V[i][j] = Real.of(vData[i * n + j]);

            return new EigenResult<>(
                new org.episteme.core.mathematics.linearalgebra.matrices.DenseMatrix<>(V, Reals.getInstance()),
                org.episteme.core.mathematics.linearalgebra.vectors.RealDoubleVector.of(eigenvalues)
            );
        }
    }
}



