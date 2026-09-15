package org.episteme.core.mathematics.optimization.evolutionary.geneticalgorithms;

// import org.episteme.computing.ai.util.Converter;

import java.io.*;

/**
 * Implementation of binary-coded genetic algorithm.
 * <p/>
 * If you don't want to use default implementation of any method. You
 * should extend this class and override that method.
 *
 * @author Levent Bayindir
 * @version 0.1
 */
public abstract class BinaryCodedGA {
    /**
     * Size of the population
     */
    protected int populationSize;

    /**
     * Size of the chromosomes for individuals
     */
    protected int chromosomeLength;

    /**
     * Probability of crossover operation
     */
    protected double crossoverProbability;

    /**
     * Probability of mutation operation
     */
    protected double mutationProbability;

    /**
     * Individuals in the current generation
     */
    protected boolean[][] currentPopulation;

    /**
     * Individuals in the next generation
     */
    protected boolean[][] nextPopulation;

    /**
     * Fitnesses of the individuals in the current generation
     */
    protected double[] currentPopulationFitness;

    /**
     * Current generation number
     */
    protected int generationNo = 0;

    /**
     * Index of the best chromosome in the current generation
     */
    protected int bestChromosomeNo;

    /**
     * Maximum fitness in the current population
     */
    protected double maxFitness;

    /**
     * Minimum fitness in the current population
     */
    protected double minFitness;

    /**
     * Average fitness in the current population
     */
    protected double avgFitness;

    /**
     * Maximum fitnesses over all generations
     */
    protected double[] historyMaxFitness;

    /**
     * Minimum fitnesses over all generations
     */
    protected double[] historyMinFitness;

    /**
     * Average fitnesses over all generations
     */
    protected double[] historyAvgFitness;

    /**
     * Maxiumum hist�ry length for historyMaxFitness, historyMinFitness
     * and historyAvgFitness variables
     */
    protected int historyLength = 0;


    // Total sum of fitnesses for the current population (used for selection)
    protected double sumOfFitnesses;

    /**
     * Creates a binary-coded genetic algorithm with default parameters.
     */
    public BinaryCodedGA() {
    }

    /**
     * Creates a binary-coded genetic algorithm with given parameters.
     *
     * @param populationSize       size of the population
     * @param chromosomeLength     size of the chromosomes for individuals
     * @param crossoverProbability probability of crossover operation
     * @param mutationProbability  probability of mutation operation
     */
    public BinaryCodedGA(int populationSize, int chromosomeLength,
                         double crossoverProbability, double mutationProbability) {
        this.populationSize = populationSize;
        this.chromosomeLength = chromosomeLength;

        this.currentPopulation = new boolean[populationSize][chromosomeLength];
        this.nextPopulation = new boolean[populationSize][chromosomeLength];
        this.currentPopulationFitness = new double[populationSize];

        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
    }

    /**
     * Creates a binary-coded genetic algorithm with given parameters.
     *
     * @param populationSize       size of the population
     * @param chromosomeLength     size of the chromosomes for individuals
     * @param crossoverProbability probability of crossover operation
     * @param mutationProbability  probability of mutation operation
     * @param maxHistory           maxiumum hist�ry length for historyMaxFitness, historyMinFitness
     *                             and historyAvgFitness variables
     */
    public BinaryCodedGA(int populationSize, int chromosomeLength,
                         double crossoverProbability, double mutationProbability, int maxHistory) {
        this.populationSize = populationSize;
        this.chromosomeLength = chromosomeLength;

        this.currentPopulation = new boolean[populationSize][chromosomeLength];
        this.nextPopulation = new boolean[populationSize][chromosomeLength];
        this.currentPopulationFitness = new double[populationSize];

        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;

        this.historyLength = maxHistory;

        if (historyLength > 0) {
            historyMaxFitness = new double[historyLength];
            historyMinFitness = new double[historyLength];
            historyAvgFitness = new double[historyLength];
        }
    }

    /**
     * Intitializes the population.
     * <p/>
     * Individuals in the population are created randomly and their fitnesses
     * are calculated.
     * <p/>
     * If histories (historyAvgFitness ...) are used they are also updated.
     *
     * @throws InvalidFitnessValueException if the fitness value of any individual is lower than zero.
     */
    public void initializePopulation() throws InvalidFitnessValueException {
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < chromosomeLength; j++) {
                if (Math.random() < 0.50000000000000002D) {
                    currentPopulation[i][j] = false;
                } else {
                    currentPopulation[i][j] = true;
                }
            }

            currentPopulationFitness[i] = evaluateIndividual(i,
                    currentPopulation[i]);

            if (currentPopulationFitness[i] < 0) {
                throw new InvalidFitnessValueException("Fitness value can not be negative.");
            }

            if (i == 0) {
                maxFitness = currentPopulationFitness[i];
                minFitness = currentPopulationFitness[i];
                sumOfFitnesses = 0.0;
            }

            if (currentPopulationFitness[i] > maxFitness) {
                maxFitness = currentPopulationFitness[i];
            }

            if (currentPopulationFitness[i] < minFitness) {
                minFitness = currentPopulationFitness[i];
            }

            sumOfFitnesses += currentPopulationFitness[i];
        }

        if (historyLength > 0) {
            historyAvgFitness[0] = sumOfFitnesses / (double) populationSize;
            historyMaxFitness[0] = maxFitness;
            historyMinFitness[0] = minFitness;
        }
    }

    /**
     * Evaluates an individual in the population.
     * <p/>
     * Choromosome index parameter is used to update fitness related data
     * of the individual. Ex. currentPopulationFitness
     *
     * @param chromosomeNo index of the chromosome in the current population to be evaluated.
     * @param chromosome   chromosome of the individual to be evaluated.
     * @return fitness of the individual.
     */
    public double evaluateIndividual(int chromosomeNo, boolean[] chromosome) {
        // This method should be overriden by the child classes.
        // InvalidGAConfigurationException is thrown if not overrided.
        throw new InvalidGAConfigurationException("evaluateIndividual(int chromosomeNo, boolean[] chromosome) method in BinaryCodedGA should be overridden.");
    }

    /**
     * Evolves the population.
     *
     * @throws InvalidFitnessValueException if the fitness value of any individual is lower than zero.
     */
    public void evolve() throws InvalidFitnessValueException {
        for (int i = 0; i < chromosomeLength; i++) {
            nextPopulation[0][i] = currentPopulation[bestChromosomeNo][i];
            nextPopulation[1][i] = mutateChromosome(currentPopulation[bestChromosomeNo][i]);
        }

        for (int j = 2; j < populationSize; j += 2) {
            int k = selectChromosome();
            int i1 = selectChromosome();

            for (int k1 = 0; k1 < chromosomeLength; k1++) {
                nextPopulation[j][k1] = mutateChromosome(currentPopulation[k][k1]);
                nextPopulation[j + 1][k1] = mutateChromosome(currentPopulation[i1][k1]);
                crossoverChromosomes(nextPopulation[j], nextPopulation[j + 1]);
            }
        }

        for (int l = 0; l < populationSize; l++) {
            for (int j1 = 0; j1 < chromosomeLength; j1++) {
                currentPopulation[l][j1] = nextPopulation[l][j1];
            }

            currentPopulationFitness[l] = evaluateIndividual(l,
                    currentPopulation[l]);

            if (currentPopulationFitness[l] < 0) {
                throw new InvalidFitnessValueException("Fitness value can not be negative.");
            }

            if (l == 0) {
                maxFitness = currentPopulationFitness[l];
                bestChromosomeNo = l;
                minFitness = currentPopulationFitness[l];
                sumOfFitnesses = 0.0;
            }

            if (currentPopulationFitness[l] > maxFitness) {
                maxFitness = currentPopulationFitness[l];
                bestChromosomeNo = l;
            }

            if (currentPopulationFitness[l] < minFitness) {
                minFitness = currentPopulationFitness[l];
            }

            sumOfFitnesses += currentPopulationFitness[l];
        }

        if (historyLength > generationNo) {
            historyAvgFitness[generationNo] = sumOfFitnesses / (double) populationSize;
            historyMaxFitness[generationNo] = maxFitness;
            historyMinFitness[generationNo] = minFitness;
        }

        generationNo++;
    }

    /**
     * Selects a chromosome using roulette-selection.
     *
     * @return index of the selected chromosome in the current population.
     */
    protected int selectChromosome() {
        double f = (float) Math.random() * sumOfFitnesses;
        int i = -1;

        for (float f1 = 0.0F; (i < (populationSize - 1)) && (f1 < f);
             f1 += currentPopulationFitness[i]) {
            i++;
        }

        return i;
    }

    /**
     * Mutates a given bit in a chromosome randomly.
     * <p/>
     * If a randomly chosen number between zero and one is lower than
     * mutationProbability then the bit is mutated.
     *
     * @param bit bit to mutate
     * @return mutated bit
     * @see #mutationProbability
     */
    protected boolean mutateChromosome(boolean bit) {
        if (Math.random() < (double) mutationProbability) {
            return !bit;
        } else {
            return bit;
        }
    }

    /**
     * Applies crossover operator to given chromosomes.
     *
     * @param chromosome1 first parent chromosome
     * @param chromosome2 second parent chromosome
     */
    protected void crossoverChromosomes(boolean[] chromosome1,
                                        boolean[] chromosome2) {
        int i;
        boolean j;

        for (i = -1; (i < 0) || (i >= populationSize);
             i = (int) Math.random() * populationSize) {
            ;
        }

        for (; i < chromosomeLength; i++) {
            j = chromosome1[i];
            chromosome1[i] = chromosome2[i];
            chromosome2[i] = j;
        }
    }

    /**
     * Changes fitness of the given chromosome
     *
     * @param chromosomeNo index of the chromosome in the current population
     * @param newFitness   new fitness of the individual
     * @throws InvalidFitnessValueException if a fitness value lower than zero is tried to be used.
     */
    public void setChromosomeFitness(int chromosomeNo, double newFitness)
            throws InvalidFitnessValueException {
        if (newFitness < 0) {
            throw new InvalidFitnessValueException("BinaryCodedGA - setChro" +
                    "mosomeFitness() - Invalid fitness value: " + newFitness);
        }

        sumOfFitnesses -= currentPopulationFitness[chromosomeNo];

        currentPopulationFitness[chromosomeNo] = newFitness;

        if (currentPopulationFitness[chromosomeNo] > maxFitness) {
            maxFitness = currentPopulationFitness[chromosomeNo];
        }

        if (currentPopulationFitness[chromosomeNo] < minFitness) {
            minFitness = currentPopulationFitness[chromosomeNo];
        }

        sumOfFitnesses += currentPopulationFitness[chromosomeNo];
    }

    /**
     * Returns maximum fitness in the current generation
     *
     * @return maximum fitness in the current generation
     */
    public double getMaxFitness() {
        return maxFitness;
    }

    /**
     * Returns minimum fitness in the current generation
     *
     * @return minimum fitness in the current generation.
     */
    public double getMinFitness() {
        return minFitness;
    }

    /**
     * Returns average fitness in the current generation
     *
     * @return average fitness in the current generation.
     */
    public double getAvgFitness() {
        return avgFitness;
    }

    /**
     * Returns fitnesses of the individuals in the current generation
     *
     * @return fitnesses of the current population
     */
    public double[] getCurrentPopulationFitness() {
        return currentPopulationFitness;
    }

    /**
     * Returns individuals of the current generation
     *
     * @return current population
     */
    public boolean[][] getPopulation() {
        return currentPopulation;
    }

    /**
     * Returns the best individual in the current generation
     *
     * @return best individual
     */
    public boolean[] getFittestChromosome() {
        return currentPopulation[bestChromosomeNo];
    }

    /**
     * Returns the fitness history of best individuals over all generations.
     *
     * @return fitnesses of the best individuals for all previous generations
     */
    public double[] getMaxHistory() {
        return historyMaxFitness;
    }

    /**
     * Returns the fitness history of worst individuals over all generations.
     *
     * @return fitnesses of the worst individuals for all previous generations
     */
    public double[] getMinHistory() {
        return historyMinFitness;
    }

    /**
     * Returns the history of the average fitnesses
     *
     * @return fitnesses of the average fitnesses for all previous generations
     */
    public double[] getAvgHistory() {
        return historyAvgFitness;
    }

    /**
     * Returns probability of the crossover operation
     *
     * @return probability of crossover
     */
    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    /**
     * Returns probability of the mutation operation
     *
     * @return probability of mutation
     */
    public double getMutationProbability() {
        return mutationProbability;
    }

    /**
     * Changes probability of the crossover operation
     *
     * @param newProbability new crossover probability
     */
    public void setCrossoverProbability(double newProbability) {
        if ((newProbability >= 0) && (newProbability <= 1)) {
            crossoverProbability = newProbability;
        }
    }

    /**
     * Changes probability of the mutation operation
     *
     * @param newProbability new mutation probability
     */
    public void setMutationProbability(double newProbability) {
        if ((newProbability >= 0) && (newProbability <= 1)) {
            mutationProbability = newProbability;
        }
    }

    /**
     * Returns population size
     *
     * @return population size
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * Returns the length of chromosomes.
     *
     * @return number of bits in the chromosome
     */
    public int getChromosomeLength() {
        return chromosomeLength;
    }

    /**
     * Prints given individual
     *
     * @param chromosome individual to print
     */
    public void printIndividual(boolean[] chromosome) {
        for (int i = 0; i < chromosome.length; i++) {
            System.out.print(chromosome[i] ? "1" : "0");
        }

        System.out.println();
    }

    /**
     * Serializes given individual to disk with the given file name.
     *
     * @param chromosome individual to serialize
     * @param fileName   name of the file where the serialized individual will be written
     * @param isBinary   specifies whether the serialization done with Java's own
     *                   serialization method or with a simple text representation.
     *                   If set to true, then Java serialization will be used.
     * @throws IOException if serialization fails
     */
    public static void serializeIndividual(boolean[] chromosome,
                                           String fileName, boolean isBinary) throws IOException {
        if (isBinary) {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(chromosome);
            out.flush();
            out.close();
        } else {
        }
    }

    /**
     * Deserializes given individual from disk with the given file name.
     *
     * @param fileName name of the file where the serialized individual will be written
     * @param isBinary specifies whether the serialization done with Java's own
     *                 serialization method or with a simple text representation.
     *                 If set to true, then Java serialization will be used.
     * @return deserialized individual
     * @throws ClassNotFoundException if deserialization fails
     * @throws IOException            if deserialization fails
     */
    public static boolean[] deSerializeIndividual(String fileName,
                                                  boolean isBinary) throws IOException, ClassNotFoundException {
        boolean[] individual = null;

        if (isBinary) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
                individual = (boolean[]) in.readObject();
            }
        }

        return individual;
    }
}
