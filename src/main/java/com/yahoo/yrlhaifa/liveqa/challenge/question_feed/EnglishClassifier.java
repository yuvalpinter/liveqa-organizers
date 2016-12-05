// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import yr.haifa.ML.classifiers.ClassificationResult;

public interface EnglishClassifier {

    ClassificationResult classify(String questionString);

}
