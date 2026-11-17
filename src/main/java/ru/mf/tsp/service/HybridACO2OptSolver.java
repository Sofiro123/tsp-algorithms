package ru.mf.tsp.service;

import ru.mf.tsp.dto.AlgorithmResult;
import ru.mf.tsp.model.RoutingMatrix;

public class HybridACO2OptSolver {

    public AlgorithmResult solve(RoutingMatrix matrix) {
        long startTime = System.currentTimeMillis();


        ACOSolver acoSolver = new ACOSolver();
        AlgorithmResult acoResult = acoSolver.solve(matrix);


        int[] tourWithReturn;
        if (acoResult.tour != null) {
            tourWithReturn = new int[acoResult.tour.length + 1];
            System.arraycopy(acoResult.tour, 0, tourWithReturn, 0, acoResult.tour.length);
            tourWithReturn[acoResult.tour.length] = acoResult.tour[0];
        } else {
            tourWithReturn = new int[0];
        }

        int[] improvedTour = LocalSearch.twoOpt(tourWithReturn, matrix);
        double improvedDistance = LocalSearch.calculateDistance(improvedTour, matrix);

        long time = System.currentTimeMillis() - startTime;

        return new AlgorithmResult(
                "Гибридный ACO + 2-opt",
                improvedTour,
                improvedDistance,
                time,
                acoResult.convergenceHistory,
                acoResult.iterations
        );
    }
}
