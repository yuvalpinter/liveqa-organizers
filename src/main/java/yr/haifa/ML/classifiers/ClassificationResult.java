// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package yr.haifa.ML.classifiers;

public class ClassificationResult extends RegressionResult {
    public ClassificationResult(boolean iLabel) {
        super();
        label = iLabel;
    }



    public ClassificationResult(boolean iLabel, Double iScore) {
        super(iScore);
        label = iLabel;
    }



    public ClassificationResult(boolean iLabel, Double iScore, Double iConfidence) {
        super(iScore, iConfidence);
        label = iLabel;
    }



    public boolean label() {
        return label;
    }



    public int labelSign() {
        return (label ? 1 : -1);
    }



    private final boolean label;
}
