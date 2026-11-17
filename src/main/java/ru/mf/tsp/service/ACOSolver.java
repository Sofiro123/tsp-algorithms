package ru.mf.tsp.service;

import ru.mf.tsp.dto.AlgorithmResult;
import ru.mf.tsp.model.RoutingMatrix;

import java.util.Arrays;
import java.util.Random;

public class ACOSolver {
    private final int ANTS = 30;
    private final int MAX_ITERATIONS = 200;
    private final double ALPHA = 1.0;
    private final double BETA = 2.0;
    private final double RHO = 0.5;
    private final double Q = 100;

    public AlgorithmResult solve(RoutingMatrix matrix) {
        long startTime = System.currentTimeMillis();
        int n = matrix.size;


        double[][] pheromone = new double[n][n];
        double[][] heuristic = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                pheromone[i][j] = 0.1;
                if (i != j) {
                    heuristic[i][j] = 1.0 / matrix.getDistance(i, j);
                }
            }
        }

        int[] bestTour = null;
        double bestLength = Double.MAX_VALUE;
        double[] convergence = new double[MAX_ITERATIONS];
        Random rand = new Random();

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            int[][] antTours = new int[ANTS][n];
            double[] antLengths = new double[ANTS];

            for (int ant = 0; ant < ANTS; ant++) {
                antTours[ant] = constructTour(pheromone, heuristic, n, rand);
                antLengths[ant] = calculateTourLength(antTours[ant], matrix);

                if (antLengths[ant] < bestLength) {
                    bestLength = antLengths[ant];
                    bestTour = antTours[ant].clone();
                }
            }

            convergence[iter] = bestLength;


            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    pheromone[i][j] *= (1 - RHO);
                }
            }


            if (bestTour != null) {
                double delta = Q / bestLength;
                for (int i = 0; i < bestTour.length - 1; i++) {
                    int from = bestTour[i];
                    int to = bestTour[i + 1];
                    pheromone[from][to] += delta;
                    pheromone[to][from] += delta;
                }

                int from = bestTour[bestTour.length - 1];
                int to = bestTour[0];
                pheromone[from][to] += delta;
                pheromone[to][from] += delta;
            }
        }

        long time = System.currentTimeMillis() - startTime;

        return new AlgorithmResult(
                "Муравьиная колония (ACO)",
                bestTour,
                bestLength,
                time,
                convergence,
                MAX_ITERATIONS
        );
    }

    private int[] constructTour(double[][] pheromone, double[][] heuristic,
                                int n, Random rand) {
        boolean[] visited = new boolean[n];
        int[] tour = new int[n];
        int current = rand.nextInt(n);

        tour[0] = current;
        visited[current] = true;

        for (int i = 1; i < n; i++) {
            int next = selectNextCity(current, visited, pheromone, heuristic, n, rand);
            tour[i] = next;
            visited[next] = true;
            current = next;
        }

        return tour;
    }

    private int selectNextCity(int current, boolean[] visited,
                               double[][] pheromone, double[][] heuristic,
                               int n, Random rand) {
        double[] probabilities = new double[n];
        double sum = 0;

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                probabilities[i] = Math.pow(pheromone[current][i], ALPHA)
                        * Math.pow(heuristic[current][i], BETA);
                sum += probabilities[i];
            }
        }

        if (sum == 0) {
            for (int i = 0; i < n; i++) {
                if (!visited[i]) return i;
            }
        }

        double r = rand.nextDouble() * sum;
        double cumulative = 0;

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                cumulative += probabilities[i];
                if (cumulative >= r) {
                    return i;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (!visited[i]) return i;
        }

        return 0;
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
