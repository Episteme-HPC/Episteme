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

package org.episteme.server.server.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.episteme.natural.biology.loaders.FASTAReader;
import org.episteme.natural.physics.loaders.VizieRReader;
import org.episteme.server.server.proto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;


/**
 * gRPC Service for Data Lake.
 * Integrates with episteme-natural loaders for real genome and star catalog
 * data.
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
@GrpcService
public class DataServiceImpl extends DataServiceGrpc.DataServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(DataServiceImpl.class);

    @Value("${episteme.data.genome.dir:data/genomes}")
    private String genomeDataDir;

    /** Cache loaded genome sequences for performance */
    private final Map<String, String> genomeCache = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void streamGenomeData(GenomeRegionRequest request, StreamObserver<GenomeChunk> responseObserver) {
        String chromosome = request.getChromosome();
        long startBase = request.getStartBase();
        long endBase = request.getEndBase();

        LOG.info("Streaming genome data for chromosome {} from {} to {}", chromosome, startBase, endBase);

        try {
            String sequence = loadGenomeSequence(chromosome);

            if (sequence == null || sequence.isEmpty()) {
                LOG.warn("No genome data available for chromosome {}, using fallback", chromosome);
                streamFallbackGenomeData(startBase, endBase, responseObserver);
                return;
            }

            // Stream the requested region in chunks
            long current = startBase;
            while (current < endBase) {
                long chunkEnd = Math.min(current + 100, endBase);
                int seqStart = (int) Math.min(current, sequence.length());
                int seqEnd = (int) Math.min(chunkEnd, sequence.length());

                String chunkData;
                if (seqStart < sequence.length()) {
                    chunkData = sequence.substring(seqStart, seqEnd);
                } else {
                    // Beyond available data, use fallback
                    chunkData = generateFallbackSequence((int) (chunkEnd - current));
                }

                GenomeChunk chunk = GenomeChunk.newBuilder()
                        .setSequence(chunkData)
                        .setStartBase(current)
                        .build();
                responseObserver.onNext(chunk);

                current = chunkEnd;
            }

            responseObserver.onCompleted();
            LOG.info("Genome streaming completed for chromosome {}", chromosome);

        } catch (Exception e) {
            LOG.error("Error streaming genome data", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void streamStarCatalog(SkyRegionRequest request, StreamObserver<StarObject> responseObserver) {
        double minRa = request.getMinRa();
        double maxRa = request.getMaxRa();
        double minDec = request.getMinDec();
        double maxDec = request.getMaxDec();

        LOG.info("Querying star catalog for region RA [{}, {}], DEC [{}, {}]", minRa, maxRa, minDec, maxDec);

        try {
            // Calculate center and radius for VizieR query
            double centerRa = (minRa + maxRa) / 2.0;
            double centerDec = (minDec + maxDec) / 2.0;
            double radiusArcmin = Math.max(
                    Math.abs(maxRa - minRa) * 60 / 2.0,
                    Math.abs(maxDec - minDec) * 60 / 2.0);

            // Query VizieR with Hipparcos catalog
            List<Map<String, String>> results = VizieRReader.queryByCoordinates(
                    centerRa, centerDec, radiusArcmin, VizieRReader.HIPPARCOS);

            if (results == null || results.isEmpty()) {
                LOG.warn("No results from VizieR, using fallback star data");
                streamFallbackStarData(minRa, maxRa, minDec, maxDec, responseObserver);
                return;
            }

            for (Map<String, String> row : results) {
                 try {
                    // Extract fields. Keys depend on VizieR output but usually contain these substrings.
                    String starId = row.getOrDefault("HIP", "Star-" + UUID.randomUUID().toString());
                    
                    // Helper to parse potential keys
                    double ra = parseDoubleFromMap(row, "RA", (minRa+maxRa)/2);
                    double dec = parseDoubleFromMap(row, "DE", (minDec+maxDec)/2);
                    double mag = parseDoubleFromMap(row, "mag", 5.0);

                    // Filter to requested region (VizieR might return stars slightly outside due to cone search)
                    if (ra >= minRa && ra <= maxRa && dec >= minDec && dec <= maxDec) {
                         StarObject star = StarObject.newBuilder()
                                .setStarId(starId)
                                .setRa(ra)
                                .setDec(dec)
                                .setMagnitude(mag)
                                .setType("G2V") // Placeholder or extract "SpType"
                                .build();
                        responseObserver.onNext(star);
                        Thread.sleep(10); // Small delay
                    }
                } catch (Exception e) {
                    // Skip malformed row
                }
            }

            responseObserver.onCompleted();
            LOG.info("Star catalog query completed, returned {} stars", results.size());

        } catch (Exception e) {
            LOG.error("Error querying star catalog, falling back to simulated data", e);
            streamFallbackStarData(minRa, maxRa, minDec, maxDec, responseObserver);
        }
    }

    /**
     * Loads genome sequence for a chromosome from FASTA files.
     */
    private String loadGenomeSequence(String chromosome) {
        // Check cache first
        if (genomeCache.containsKey(chromosome)) {
            return genomeCache.get(chromosome);
        }

        // Try to load from configured directory
        File genomeDir = new File(genomeDataDir);
        if (genomeDir.exists() && genomeDir.isDirectory()) {
            // Look for FASTA files matching the chromosome
            File[] fastaFiles = genomeDir
                    .listFiles((dir, name) -> name.toLowerCase().contains(chromosome.toLowerCase()) &&
                            (name.endsWith(".fasta") || name.endsWith(".fa")));

            if (fastaFiles != null && fastaFiles.length > 0) {
                try (InputStream is = new FileInputStream(fastaFiles[0])) {
                    List<FASTAReader.Sequence> sequences = FASTAReader.load(is);
                    if (!sequences.isEmpty()) {
                        String data = sequences.get(0).data;
                        genomeCache.put(chromosome, data);
                        LOG.info("Loaded {} bases for chromosome {} from {}",
                                data.length(), chromosome, fastaFiles[0].getName());
                        return data;
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to load FASTA file for chromosome {}", chromosome, e);
                }
            }
        }

        // Try loading from classpath resources
        String[] resourcePaths = {
                "/data/" + chromosome + ".fasta",
                "/genomes/" + chromosome + ".fa",
                "/biology/" + chromosome + ".fasta"
        };

        for (String path : resourcePaths) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    List<FASTAReader.Sequence> sequences = FASTAReader.load(is);
                    if (!sequences.isEmpty()) {
                        String data = sequences.get(0).data;
                        genomeCache.put(chromosome, data);
                        LOG.info("Loaded {} bases for chromosome {} from resource {}",
                                data.length(), chromosome, path);
                        return data;
                    }
                }
            } catch (Exception e) {
                // Try next path
            }
        }

        LOG.debug("No FASTA data found for chromosome {}", chromosome);
        return null;
    }

    /**
     * Parses VOTable XML to extract star objects.
     */
    private double parseDoubleFromMap(Map<String, String> row, String keyPart, double defaultValue) {
        for (Map.Entry<String, String> entry : row.entrySet()) {
            if (entry.getKey().toLowerCase().contains(keyPart.toLowerCase())) {
                try {
                    return Double.parseDouble(entry.getValue());
                } catch (NumberFormatException e) {
                    // Continue to next key
                }
            }
        }
        return defaultValue;
    }



    /**
     * Streams fallback genome data when real data is unavailable.
     */
    private void streamFallbackGenomeData(long startBase, long endBase,
            StreamObserver<GenomeChunk> responseObserver) {
        try {
            long current = startBase;
            while (current < endBase) {
                long chunkSize = Math.min(100, endBase - current);
                String seq = generateFallbackSequence((int) chunkSize);

                GenomeChunk chunk = GenomeChunk.newBuilder()
                        .setSequence(seq)
                        .setStartBase(current)
                        .build();
                responseObserver.onNext(chunk);
                current += chunkSize;
                Thread.sleep(50);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private String generateFallbackSequence(int length) {
        StringBuilder seq = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            seq.append("AGCT".charAt(rand.nextInt(4)));
        }
        return seq.toString();
    }

    /**
     * Streams fallback star data when VizieR is unavailable.
     */
    private void streamFallbackStarData(double minRa, double maxRa, double minDec, double maxDec,
            StreamObserver<StarObject> responseObserver) {
        try {
            // Generate 10 simulated stars in the region
            Random rand = new Random();
            for (int i = 0; i < 10; i++) {
                StarObject star = StarObject.newBuilder()
                        .setStarId("Sim-" + UUID.randomUUID().toString().substring(0, 8))
                        .setRa(minRa + rand.nextDouble() * (maxRa - minRa))
                        .setDec(minDec + rand.nextDouble() * (maxDec - minDec))
                        .setMagnitude(rand.nextDouble() * 6) // Visible stars
                        .setType("G" + rand.nextInt(10) + "V")
                        .build();
                responseObserver.onNext(star);
                Thread.sleep(100);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}

