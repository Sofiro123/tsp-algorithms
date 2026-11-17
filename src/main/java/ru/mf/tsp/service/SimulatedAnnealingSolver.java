package ru.mf.tsp.service;

import ru.mf.tsp.dto.AlgorithmResult;
import ru.mf.tsp.model.RoutingMatrix;

import java.util.Arrays;
import java.util.Random;

public class SimulatedAnnealingSolver {
    private final int MAX_ITERATIONS = 10000;
    private final double INITIAL_TEMPERATURE = 10000;
    private final double COOLING_RATE = 0.995;
    private final Random rand = new Random();

    public AlgorithmResult solve(RoutingMatrix matrix) {
        long startTime = System.currentTimeMillis();
        int n = matrix.size;


        int[] currentTour = LocalSearch.nearestNeighbor(matrix);
        double currentDistance = calculateTourLength(currentTour, matrix);

        int[] bestTour = currentTour.clone();
        double bestDistance = currentDistance;
        double[] convergence = new double[MAX_ITERATIONS];

        double temperature = INITIAL_TEMPERATURE;

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {

            int[] newTour = generateNeighbor(currentTour.clone());
            double newDistance = calculateTourLength(newTour, matrix);


            if (acceptanceProbability(currentDistance, newDistance, temperature) > rand.nextDouble()) {
                currentTour = newTour;
                currentDistance = newDistance;

  
                if (currentDistance < bestDistance) {
                    bestTour = currentTour.clone();
                    bestDistance = currentDistance;
                }
            }

            convergence[iter] = bestDistance;


            temperature *= COOLING_RATE;
        }

        int[] tourWithReturn = Arrays.copyOf(bestTour, bestTour.length + 1);
        tourWithReturn[bestTour.length] = bestTour[0];
        bestTour = LocalSearch.twoOpt(tourWithReturn, matrix);
        bestDistance = LocalSearch.calculateDistance(bestTour, matrix);

        long time = System.currentTimeMillis() - startTime;

        return new AlgorithmResult(
                "Имитация отжига (SA)",
                bestTour,
                bestDistance,
                time,
                convergence,
                MAX_ITERATIONS
        );
    }

    private int[] generateNeighbor(int[] tour) {
        int n = tour.length;
        int i = rand.nextInt(n);
        int j = rand.nextInt(n);

        while (i == j) j = rand.nextInt(n);


        int temp = tour[i];
        tour[i] = tour[j];
        tour[j] = temp;

        return tour;
    }

    private double acceptanceProbability(double currentDistance, double newDistance, double temperature) {
        if (newDistance < currentDistance) {
            return 1.0;
        }
        return Math.exp((currentDistance - newDistance) / temperature);
    }

    private double calculateTourLength(int[] tour, RoutingMatrix matrix) {
        double total = 0;
        int n = tour.length;

        for (int i = 0; i < n - 1; i++) {
            total += matrix.getDistance(tour[i], tour[i + 1]);
        }

        total += matrix.getDistance(tour[n - 1], tour[0]);

        return total;
    }
}
