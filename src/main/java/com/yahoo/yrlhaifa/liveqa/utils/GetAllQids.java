// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import com.yahoo.yrlhaifa.haifa_utils.utils.FileUtils;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.QuestionWithAnswers;

public class GetAllQids {

    public static void main(String[] args) throws IOException {

        BufferedWriter out = FileUtils.openWriter(new File("path/first15runs-qids.txt"));
        TextualLogScraper tls =
                        new TextualLogScraper("path/questions-first15.txt",
                                        "path/answers-first15.txt", true);
        int total = 0;
        while (tls.hasNext()) {
            QuestionWithAnswers q = tls.next();
            out.write(q.getQuestion().getId().substring(3));
            out.newLine();
            total++;
        }
        out.flush();
        out.close();
        System.out.println("Total = " + total);

    }

}
