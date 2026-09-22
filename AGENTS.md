# AGENTS.md — Agent & AI Assistant Guidelines for Episteme

Welcome to the **Episteme** repository! This document serves as the primary technical guide for AI agents, coding assistants, and automated contributors working on this codebase.

---

## 🌌 Project Overview

**Episteme** is an enterprise-grade, high-performance scientific computing framework and distributed computing platform designed for modern Java (Java 21 LTS & Java 25 EA). It bridges low-level bare-metal performance (C/C++, CUDA, OpenCL, SIMD) with high-level architectural elegance, providing:

- **Science-First Object Model**: Mirroring natural reality from abstract Mathematics (Rings, Fields, Vector Spaces) up to Physics, Chemistry, Biology, and Social Sciences.
- **Bare-Metal Hardware Acceleration**: Utilizing the Java Foreign Function & Memory (FFM / Panama) API (JEP 454) and SIMD Vector API (JEP 448).
- **Dynamic Backend Auto-Tuning**: Real-time evaluation and routing of compute operations across CPU, SIMD, OpenBLAS, LAPACK, CUDA, and OpenCL backends.
- **Distributed Computing & AI Connectivity**: gRPC-orchestrated compute grid, distributed task scheduler, and native **Model Context Protocol (MCP)** server for LLM integration.

---

## 🏗️ Repository Architecture & Modules

```text
Episteme/
├── episteme-core/          # Mathematical kernel, abstract algebra, units (JSR-385), constants
├── episteme-native/        # Panama FFM bindings, Vector API SIMD, native BLAS/CUDA/OpenCL
├── episteme-natural/       # Physics (N-body, Lattice Boltzmann), Chemistry (CML), Biology (Genomics, CRISPR)
├── episteme-social/        # Economics, Demographics, Geography (GML), Linguistics (TigerXML)
├── episteme-database/      # Persistence engine (H2, PostgreSQL, RocksDB)
├── episteme-server/        # Cluster coordinator, gRPC scheduler, REST API, MCP SSE endpoint
├── episteme-worker/        # Distributed compute daemon consuming tasks from server
├── episteme-client/        # High-level Java client library for distributed jobs
├── episteme-jupyter/       # Python client & Jupyter notebook bindings
├── episteme-dashboard/     # Cluster and benchmark monitoring interface
├── episteme-benchmarks/    # JMH-based performance benchmarks & audit suites
├── docs/                   # System design, architecture diagrams, FFM guides, benchmarks
└── launchers/              # Shell and batch scripts for running servers, workers, and demos
```

---

## ⚙️ Build, Test & Environment Guide

### Prerequisites
- **JDK 25** (recommended for full build including `episteme-native` with modern Panama FFM).
- **JDK 21+** (compatible for core modules: `episteme-core`, `episteme-natural`, `episteme-social`, `episteme-database`).
- **Maven 3.9+**.

### Standard Maven Commands

- **Fast Build (Skip Tests)**:
  ```bash
  mvn clean install -DskipTests
  ```

- **Run Core Tests**:
  ```bash
  mvn test -pl episteme-core
  ```

- **Build on JDK 21 (Excluding Native Modules)**:
  ```bash
  mvn clean install -DskipTests -pl '!episteme-native,!episteme-server,!episteme-client,!episteme-worker,!episteme-benchmarks'
  ```

- **Run Linear Algebra Compliance Tests**:
  ```bash
  mvn test -pl episteme-core -Dtest=LinearAlgebraComplianceTest
  ```

- **Run Benchmarks (JMH)**:
  ```bash
  mvn clean package -pl episteme-benchmarks -DskipTests
  java -jar episteme-benchmarks/target/benchmarks.jar
  ```

---

## 📐 Core Engineering & Design Rules

When developing or modifying code in Episteme, agents must strictly observe the following principles:

### 1. Performance-First & Zero-Garbage in Hot Paths
- **Off-Heap Memory Safety**: For heavy vector/matrix operations in `episteme-native`, use deterministic Panama arenas (`Arena.ofConfined()` or `Arena.ofShared()`). Avoid unmanaged heap allocations inside tight loops.
- **SIMD Vectorization**: Prefer the Java Vector API for compute-intensive algorithms before falling back to scalar loops.
- **Primitive Specialization**: Use primitive arrays (`double[]`, `float[]`, `long[]`) for heavy computation; wrap in higher-level objects only at domain boundaries.

### 2. Numerical Parity & Mathematical Rigor
- **Multi-Backend Parity**: Any new linear algebra or mathematical operator must produce identical results across pure Java, SIMD, OpenBLAS, and GPU backends (within floating-point epsilon tolerances).
- **Precision Support**: Respect arbitrary-precision (`RealBig`, MPFR) and complex domains (`Complex`) alongside standard IEEE 754 floating-point types (`Real`, `Float`).
- **Dimension Safety**: When manipulating physical quantities, strictly respect unit dimensions (JSR-385) and standard physical constants (CODATA).

### 3. Modularity & Dependency Isolation
- Keep `episteme-core` thin, dependency-light, and universally compatible across standard JVMs.
- Isolate native C/C++/CUDA bridges inside `episteme-native` and `episteme-jni`.
- Use the **Service Provider Interface (SPI)** and `ProviderSelector` for dynamic backend discovery and runtime autotuning.

### 4. Security & Robustness
- **Safe Parsing**: Always use `SecureXMLFactory` when parsing XML (e.g., CML, GML, TigerXML) to protect against XXE attacks.
- **Fail-Safe Fallbacks**: If a native backend (CUDA/BLAS/SIMD) fails or is unavailable on the target host, the system must transparently fall back to the reference Java implementation.

### 5. Documentation & Code Integrity
- **Javadoc Requirements**: Every new public class and method must provide clear Javadoc describing its mathematical domain, time complexity, and precision semantics.
- **Preserve Existing Documentation**: Do not remove, alter, or simplify existing comments, docstrings, or architectural notes without explicit instructions.

---

## 🤖 AI Agent Workflow Instructions

1. **Investigate Before Modifying**: Inspect existing interfaces (e.g., in `episteme-core/src/main/java/...`) before introducing new abstraction patterns.
2. **Verify Multi-Module Impact**: Ensure changes in `episteme-core` do not break downstream modules (`episteme-natural`, `episteme-social`, `episteme-native`, `episteme-server`).
3. **Run Targeted Tests**: After any code edit, run the relevant Maven unit test suite before finalizing the task.
4. **Follow Mixed-JDK Compatibility**: Ensure code written in core modules compiles cleanly under standard Java 21 LTS rules.
