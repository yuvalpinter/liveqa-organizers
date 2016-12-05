// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import com.yahoo.yrlhaifa.haifa_utils.ds.SeparatorStringBuilder;
import com.yahoo.yrlhaifa.haifa_utils.utils.StringUtils;
import com.yahoo.yrlhaifa.liveqa.challenge.Participant;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * An answer storage which prints the answers into a text file.
 *
 * <P>
 * Date: Feb 16, 2015
 * 
 * @author Asher Stern
 *
 */
public class TextFileAnswerStorage implements AnswerStorageOperator {
    public TextFileAnswerStorage(String fileName, boolean allowIncremental) throws IOException, StorageFatalException {
        super();
        this.fileName = fileName;
        this.allowIncremental = allowIncremental;
        if (!allowIncremental) {
            File file = new File(fileName);
            if (file.exists()) {
                throw new StorageFatalException("The given file " + file.getAbsolutePath() + " already exist.");
            }
        }
        writer = new PrintWriter(new FileWriter(fileName, allowIncremental), true);
    }

    @Override
    public void storeAnswers(Question question, Map<Participant, ParticipantResponse> mapParticipantToResponse)
                    throws StorageFatalException, StorageNonFatalException {
        try {
            StringBuilder sb = new StringBuilder();
            if (question != null) // should never be null
            {
                sb.append("Question id = ").append(question.getId()).append("\n");
            }
            boolean firstIteration = true;
            for (Participant participant : mapParticipantToResponse.keySet()) {
                if (participant != null) // should never be null
                {
                    if (firstIteration) {
                        firstIteration = false;
                    } else {
                        sb.append(separatorBetweenParticipants).append("\n");
                    }
                    sb.append("Participant unique id = ").append(participant.getUniqueSystemId()).append("\n");
                    ParticipantResponse response = mapParticipantToResponse.get(participant);
                    if (response != null) // should never be null
                    {
                        ParticipantAnswer answer = response.getAnswer();
                        if (answer != null) {
                            sb.append("Answer: ").append(answer.getAnswer()).append("\n");
                            sb.append("Reported answer time: ").append(answer.getReportedAnsweringTime()).append("\n");
                            sb.append("Resources: ").append(strResources(answer.getReportedResources())).append("\n");
                            sb.append("Title foci: ").append(spans(question.getTitle(), answer.getTitleFocusSpans()))
                                            .append("\n");
                            sb.append("Body foci: ").append(spans(question.getBody(), answer.getBodyFocusSpans()))
                                            .append("\n");
                            sb.append("Question summary: ").append(answer.getQuestionSummary()).append("\n");
                        }
                        ReasonWhyQuestionDiscardByParticipant reason = response.getWhyNotAnswered();
                        if (reason != null) {
                            sb.append("Discard reason: ").append(reason.getReason()).append("\n");
                        }
                    }
                }
            }
            sb.append(separator).append("\n");

            synchronized (this) {
                writer.println(sb.toString());

            }
        } catch (RuntimeException e) {
            throw new StorageNonFatalException("Failed to store answers.", e);
        }
    }

    @Override
    public void close() throws ChallengeCloseException {
        if (writer != null) {
            writer.close();
            logger.info("Answer storage text file has been closed.");
        }
    }


    private static String strResources(List<String> list) {
        if (null == list)
            return "";

        StringBuilder sb = new StringBuilder();
        boolean firstIteration = true;
        for (String resource : list) {
            if (firstIteration) {
                firstIteration = false;
            } else {
                sb.append(",");
            }
            sb.append(resource);
        }
        return sb.toString();
    }


    /**
     * 
     * @param text
     * @param span in "startchar-endchar" form
     * @return
     */
    private static String spans(String text, String spans) {
        if (spans == null) {
            return "";
        }
        int spanStart = -1, spanEnd = -1;
        SeparatorStringBuilder spanTexts = new SeparatorStringBuilder("\t");
        for (String span : spans.split(",")) {
            try {
                String[] spanParts = span.split("-");
                spanStart = Integer.parseInt(spanParts[0]);
                if (spanStart < spanEnd) { // previous end, that is
                    logger.error("Overlapping spans: " + spans);
                    return "";
                }
                spanEnd = Integer.parseInt(spanParts[1]);
                spanTexts.append(text.substring(spanStart, spanEnd));
            } catch (NumberFormatException nfe) {
                logger.error("Malformed number format in span: " + span);
                return "";
            } catch (IndexOutOfBoundsException ioobe) {
                if (spanStart > -1) {
                    logger.error("Span out of bounds: " + span + " for text: " + text);
                } else {
                    logger.error("Missing dash divider in span: " + span);
                }
                return "";
            }
        }
        return spanTexts.toString();
    }


    @SuppressWarnings("unused")
    private final String fileName;
    @SuppressWarnings("unused")
    private final boolean allowIncremental;

    private PrintWriter writer;
    private static final String separatorBetweenParticipants = StringUtils.generateStringOfCharacter('-', 50);
    private static final String separator = StringUtils.generateStringOfCharacter('=', 50);

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(TextFileAnswerStorage.class);
}
