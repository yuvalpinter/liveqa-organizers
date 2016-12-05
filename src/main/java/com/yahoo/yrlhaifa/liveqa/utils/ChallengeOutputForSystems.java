// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.yahoo.yrlhaifa.haifa_utils.ds.SeparatorStringBuilder;
import com.yahoo.yrlhaifa.haifa_utils.utils.FileUtils;

public class ChallengeOutputForSystems {

    private static final String ANSWERER_SEPARATOR = "--------------------------------------------------";
    private static final String ANSWERS_FOR_QUESTION_SEPARATOR = "==================================================";

    private static final String BASE_DIR = "$BASE$/live-qa";
    private static final String RAW_TSTAMP = "0901-CHALLENGE";

    private static final String ID_MAP = BASE_DIR + "/trec-qid.txt";
    private static final String SCORES_MAP = BASE_DIR + "/qrels/qrels-primary.txt";

    private static final String ANSWERS_LOG_FILE = BASE_DIR + "/logs-backup/answers-" + RAW_TSTAMP + ".txt";

    public static void main(String[] args) throws Exception {

        // init
        Map<String, String> ids = new HashMap<>();
        Map<String, String> revIds = new HashMap<>();
        for (String kv : FileUtils.iterateLines(ID_MAP)) { // trec-id \t qid
            if (kv.isEmpty()) {
                continue;
            }
            String[] parts = kv.split("\\s+");
            ids.put(parts[1], parts[0]);
            revIds.put(parts[0], parts[1]);
        }

        Map<String, Map<String, String>> scores = new HashMap<>(); // qid => (participant => score)
        Map<String, BufferedWriter> outs = new HashMap<>(); // participant => file
        for (String qps : FileUtils.iterateLines(SCORES_MAP)) { // question \t participant \t score
            String[] parts = qps.split("\\s+");
            if (parts.length != 3) {
                continue;
            }
            String qid = revIds.get(parts[0]);
            Map<String, String> ps = scores.get(qid);
            if (ps == null) {
                ps = new HashMap<>();
                scores.put(qid, ps);
            }
            String participant = parts[1];
            ps.put(participant, parts[2]);

            if (!outs.containsKey(participant)) {
                BufferedWriter out = FileUtils.openWriter(new File(BASE_DIR + "/reports/" + participant));
                out.append("TREC ID\tQID\tSCORE\tANSWER");
                out.newLine();
                outs.put(participant, out);
            }
        }

        // ------
        Iterator<String> as = FileUtils.iterateLines(ANSWERS_LOG_FILE).iterator();

        // populate doc
        String qid = null, participant = null;
        SeparatorStringBuilder answer = null;
        String a = as.next();
        while (as.hasNext()) {

            if (a.startsWith("Question id = YA:")) {
                qid = a.substring(17);
                a = as.next();
            }

            while (!a.equals(ANSWERS_FOR_QUESTION_SEPARATOR)) {
                // loop over responses
                if (a.startsWith("Participant unique id = ")) {
                    participant = a.substring(24).replaceAll("\\s+", "");
                    a = as.next();
                }
                if (a.startsWith("Answer: ")) {
                    answer = new SeparatorStringBuilder("\\n");
                    answer.append(a.substring(8));
                    a = as.next();
                } else {
                    while (!a.equals(ANSWERER_SEPARATOR) && !a.equals(ANSWERS_FOR_QUESTION_SEPARATOR)) {
                        a = as.next();
                    }
                    if (a.equals(ANSWERER_SEPARATOR)) {
                        a = as.next();
                    }
                    continue;
                }
                while (!a.startsWith("Reported answer time: ")) {
                    answer.append(a);
                    a = as.next();
                }
                a = as.next(); // ignore reported time
                if (a.startsWith("Resources: ")) { // ignore
                    a = as.next();
                }

                // dump
                String trecId = ids.get(qid);
                BufferedWriter out = outs.get(participant);
                Map<String, String> qidScores = scores.get(qid);
                if (trecId != null && out != null && qidScores != null) {
                    String score = qidScores.get(participant);
                    if (score != null) {
                        out.append(trecId + "\t" + qid + "\t" + score + "\t" + answer.toString());
                        out.newLine();
                    }
                }

                // check if last for question
                if (a.equals(ANSWERER_SEPARATOR)) {
                    a = as.next();
                }
            }

            a = as.next();
            a = as.next();
        }

        // close
        for (BufferedWriter out : outs.values()) {
            out.flush();
            out.close();
        }

        System.out.println("Done!");
    }
}
