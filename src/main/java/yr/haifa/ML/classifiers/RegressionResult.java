// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package yr.haifa.ML.classifiers;

public class RegressionResult {
    public RegressionResult() {}



    public RegressionResult(Double iScore) {
        score = iScore;
    }



    public RegressionResult(Double iScore, Double iConfidence) {
        score = iScore;
        confidence = iConfidence;
    }



    public Double score() {
        return score;
    }



    public Double confidence() {
        return confidence;
    }



    private Double score = null;
    private Double confidence = null;
}
