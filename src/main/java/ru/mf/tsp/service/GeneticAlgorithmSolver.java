package ru.mf.tsp.service;

import ru.mf.tsp.dto.AlgorithmResult;
import ru.mf.tsp.model.RoutingMatrix;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class GeneticAlgorithmSolver {
    private final int POPULATION_SIZE = 100;
    private final int MAX_GENERATIONS = 500;
    private final double MUTATION_RATE = 0.02;
    private final double ELITISM_RATE = 0.1;
    private final Random rand = new Random();

    public AlgorithmResult solve(RoutingMatrix matrix) {
        long startTime = System.currentTimeMillis();
        int n = matrix.size;


        int[][] population = initializePopulation(n);
        double[] fitness = new double[POPULATION_SIZE];
        double[] convergence = new double[MAX_GENERATIONS];

        int[] bestTour = null;
        double bestFitness = Double.MAX_VALUE;

        for (int gen = 0; gen < MAX_GENERATIONS; gen++) {
            for (int i = 0; i < POPULATION_SIZE; i++) {
                fitness[i] = calculateFitness(population[i], matrix);

                if (fitness[i] < bestFitness) {
                    bestFitness = fitness[i];
                    bestTour = population[i].clone();
                }
            }

            convergence[gen] = bestFitness;


            int[][] newPopulation = new int[POPULATION_SIZE][n];


            int eliteCount = (int)(POPULATION_SIZE * ELITISM_RATE);
            int[] eliteIndices = selectElite(fitness, eliteCount);
            for (int i = 0; i < eliteCount; i++) {
                newPopulation[i] = population[eliteIndices[i]].clone();
            }


            for (int i = eliteCount; i < POPULATION_SIZE; i++) {
                // Селекция родителей
                int parent1 = tournamentSelection(fitness, 5);
                int parent2 = tournamentSelection(fitness, 5);


                int[] child = orderedCrossover(population[parent1], population[parent2]);


                if (rand.nextDouble() < MUTATION_RATE) {
                    child = swapMutation(child);
                }

                newPopulation[i] = child;
            }

            population = newPopulation;
        }


        if (bestTour != null) {
            int[] tourWithReturn = Arrays.copyOf(bestTour, bestTour.length + 1);
            tourWithReturn[bestTour.length] = bestTour[0];
            bestTour = LocalSearch.twoOpt(tourWithReturn, matrix);
            bestFitness = LocalSearch.calculateDistance(bestTour, matrix);
        }

        long time = System.currentTimeMillis() - startTime;

        return new AlgorithmResult(
                "Генетический алгоритм",
                bestTour,
                bestFitness,
                time,
                convergence,
                MAX_GENERATIONS
        );
    }

    private int[][] initializePopulation(int n) {
        int[][] population = new int[POPULATION_SIZE][n];

        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = generateRandomTour(n);
        }

        return population;
    }

    private int[] generateRandomTour(int n) {
        int[] tour = new int[n];
        for (int i = 0; i < n; i++) tour[i] = i;


        for (int i = n - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = tour[i];
            tour[i] = tour[j];
            tour[j] = temp;
        }

        return tour;
    }

    private double calculateFitness(int[] tour, RoutingMatrix matrix) {
        double total = 0;
        for (int i = 0; i < tour.length - 1; i++) {
            total += matrix.getDistance(tour[i], tour[i + 1]);
        }
        total += matrix.getDistance(tour[tour.length - 1], tour[0]);
        return total;
    }

    private int[] selectElite(double[] fitness, int count) {
        Integer[] indices = new Integer[fitness.length];
        for (int i = 0; i < indices.length; i++) indices[i] = i;

        Arrays.sort(indices, Comparator.comparingDouble(i -> fitness[i]));

        int[] result = new int[count];
        for (int i = 0; i < count; i++) result[i] = indices[i];
        return result;
    }

    private int tournamentSelection(double[] fitness, int tournamentSize) {
        int best = rand.nextInt(fitness.length);
        double bestFitness = fitness[best];

        for (int i = 1; i < tournamentSize; i++) {
            int candidate = rand.nextInt(fitness.length);
            if (fitness[candidate] < bestFitness) {
                best = candidate;
                bestFitness = fitness[candidate];
            }
        }

        return best;
    }

    private int[] orderedCrossover(int[] parent1, int[] parent2) {
        int n = parent1.length;
        int[] child = new int[n];
        Arrays.fill(child, -1);

        int start = rand.nextInt(n);
        int end = rand.nextInt(n - start) + start;


        for (int i = start; i <= end; i++) {
            child[i] = parent1[i];
        }

        int childPos = (end + 1) % n;
        for (int i = 0; i < n; i++) {
            int city = parent2[(end + 1 + i) % n];
            if (!contains(child, city)) {
                child[childPos] = city;
                childPos = (childPos + 1) % n;
            }
        }

        return child;
    }

    private boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) return true;
        }
        return false;
    }

    private int[] swapMutation(int[] tour) {
        int n = tour.length;
        int i = rand.nextInt(n);
        int j = rand.nextInt(n);

        while (i == j) j = rand.nextInt(n);

        int temp = tour[i];
        tour[i] = tour[j];
        tour[j] = temp;

        return tour;
    }
}
