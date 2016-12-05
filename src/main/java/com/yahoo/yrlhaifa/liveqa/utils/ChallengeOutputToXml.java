// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.yahoo.yrlhaifa.haifa_utils.ds.SeparatorStringBuilder;
import com.yahoo.yrlhaifa.haifa_utils.utils.FileUtils;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.QuestionWithAnswers;

/**
 * Generates XML for evaluation from challenge logs
 * 
 * @author Yuval Pinter
 *
 */
public class ChallengeOutputToXml {

    private final static Pattern UNPRINTABLE_CHARS = Pattern.compile("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F]");

    // TODO parametrize these
    private static final int QUESTIONS_TO_EXTRACT = -1;
    private static final boolean EXTRACT_RESOURCES_AS_LIST = false;

    private static final String BASE_DIR = "$BASE$/live-qa";
    private static final String RAW_TSTAMP = "0531-challenge";

    // private static final String QUESTIONS_LOG_FILE = BASE_DIR + "/logs-backup/questions-" + RAW_TSTAMP + ".txt";
    // private static final String ANSWERS_LOG_FILE = BASE_DIR + "/logs-backup/answers-" + RAW_TSTAMP + ".txt";
    private static final String QUESTIONS_LOG_FILE = BASE_DIR + "/" + RAW_TSTAMP + "-questions.tsv";
    private static final String ANSWERS_LOG_FILE = BASE_DIR + "/" + RAW_TSTAMP + "-answers.tsv";
    private static final String BAD_PARTICIPANTS = BASE_DIR + "/bad-participants.txt";
    private static final String BAD_QIDS = BASE_DIR + "/bad-challenge-qids.txt";
    private static final String XML_OUT =
                    BASE_DIR + "/" + RAW_TSTAMP + "-eval" + (QUESTIONS_TO_EXTRACT > 0 ? "-demo" : "") + ".xml";
    private static final String SEP_OUT =
                    BASE_DIR + "/" + RAW_TSTAMP + "-eval" + (QUESTIONS_TO_EXTRACT > 0 ? "-demo" : "") + "-pairs.xml";

    public static void main(String[] args) throws Exception {
        // init
        Set<String> badQids = new HashSet<>();
        for (String qid : FileUtils.iterateLines(BAD_QIDS)) {
            badQids.add(qid.replaceAll("YA:", ""));
        }
        Set<String> badParticipants = new HashSet<>();
        for (String part : FileUtils.iterateLines(BAD_PARTICIPANTS)) {
            badParticipants.add(part.toLowerCase());
        }
        BufferedWriter xmlOut = FileUtils.openWriter(new File(XML_OUT));
        BufferedWriter sepOut = FileUtils.openWriter(new File(SEP_OUT));

        Iterator<QuestionWithAnswers> scraper = null;
        if (QUESTIONS_LOG_FILE.endsWith(".tsv")) {
            scraper = new FromExcelLogScraper(QUESTIONS_LOG_FILE, ANSWERS_LOG_FILE, false);
        } else {
            scraper = new TextualLogScraper(QUESTIONS_LOG_FILE, ANSWERS_LOG_FILE, false);
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder mDocBuilder = docFactory.newDocumentBuilder();
        Document joinedAnswersDoc = mDocBuilder.newDocument();
        Element joinedRootElement = joinedAnswersDoc.createElement("liveqa");
        joinedAnswersDoc.appendChild(joinedRootElement);

        Document singleAnswersDoc = mDocBuilder.newDocument();
        Element singleRootElement = singleAnswersDoc.createElement("liveqa");
        singleAnswersDoc.appendChild(singleRootElement);

        // populate doc
        QuestionWithAnswers qwa;
        int totalQuestionsWritten = 0, totalAnswersWritten = 0, filteredQuestions = 0, filteredAnswers = 0, errors = 0;
        while (scraper.hasNext() && totalQuestionsWritten != QUESTIONS_TO_EXTRACT) {

            qwa = scraper.next();

            // add question
            Question question = qwa.getQuestion();
            Element qElement = joinedAnswersDoc.createElement("question-and-responses");
            String qid = question.getId();
            qElement.setAttribute("ID", qid);
            addSuccessorTextNode(joinedAnswersDoc, qElement, "category", question.getCategory());
            String questionText = question.getTitle();
            if (question.getBody() != null && !question.getBody().isEmpty()) {
                questionText += ("\t" + question.getBody());
            }
            addSuccessorTextNode(joinedAnswersDoc, qElement, "question", questionText);

            Element joinedRessElement = joinedAnswersDoc.createElement("responses");
            qElement.appendChild(joinedRessElement);

            List<Element> singleQaElements = new ArrayList<>();

            // add responses
            Map<String, ParticipantAnswer> answers = qwa.getAllAnswers();
            int anss = 0, filtAnss = 0;
            for (Entry<String, ParticipantAnswer> partAns : answers.entrySet()) {

                // dump response
                Element resElement = joinedAnswersDoc.createElement("response");
                String part = partAns.getKey();
                resElement.setAttribute("participant", part);
                ParticipantAnswer a = partAns.getValue();
                addSuccessorTextNode(joinedAnswersDoc, resElement, "answer", a.getAnswer());

                Element singleResElement = singleAnswersDoc.createElement("response");
                singleResElement.setAttribute("participant", part);
                addSuccessorTextNode(singleAnswersDoc, singleResElement, "answer", a.getAnswer());

                if (EXTRACT_RESOURCES_AS_LIST) {
                    Element rsrcsElement = joinedAnswersDoc.createElement("resources");
                    resElement.appendChild(rsrcsElement);
                    int i = 1;
                    for (String r : a.getReportedResources()) {
                        addSuccessorTextNode(joinedAnswersDoc, rsrcsElement, "resource-" + i++, r);
                    }
                    Element singleRsrcsElement = singleAnswersDoc.createElement("resources");
                    singleResElement.appendChild(singleRsrcsElement);
                    i = 1;
                    for (String r : a.getReportedResources()) {
                        addSuccessorTextNode(singleAnswersDoc, singleRsrcsElement, "resource-" + i++, r);
                    }
                } else {
                    String resourcesString =
                                    new SeparatorStringBuilder(",").appendAll(a.getReportedResources()).toString();
                    addSuccessorTextNode(joinedAnswersDoc, resElement, "resources", resourcesString);
                    addSuccessorTextNode(singleAnswersDoc, singleResElement, "resources", resourcesString);
                }

                if (!badParticipants.contains(part.toLowerCase())) {
                    joinedRessElement.appendChild(resElement);

                    Element qaElement = singleAnswersDoc.createElement("question-and-response");
                    qaElement.setAttribute("ID", qid + "-" + part);
                    addSuccessorTextNode(singleAnswersDoc, qaElement, "category", question.getCategory());
                    addSuccessorTextNode(singleAnswersDoc, qaElement, "question", questionText);
                    qaElement.appendChild(singleResElement);
                    singleQaElements.add(qaElement);

                    anss++;
                } else {
                    filtAnss++;
                }
            }

            if (!badQids.contains(qid.replaceAll("YA:", ""))) {
                joinedRootElement.appendChild(qElement);
                for (Element qaElement : singleQaElements) {
                    singleRootElement.appendChild(qaElement);
                }
                totalQuestionsWritten++;
                totalAnswersWritten += anss;
                filteredAnswers += filtAnss;
            } else {
                filteredQuestions++;
            }
        }

        // write to outs
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(joinedAnswersDoc);
        OutputStreamWriter osw = new OutputStreamWriter(bos, "utf-8");
        StreamResult result = new StreamResult(osw);
        transformer.transform(source, result);
        xmlOut.write(bos.toString());
        xmlOut.close();

        bos = new ByteArrayOutputStream();
        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        source = new DOMSource(singleAnswersDoc);
        osw = new OutputStreamWriter(bos, "utf-8");
        result = new StreamResult(osw);
        transformer.transform(source, result);
        sepOut.write(bos.toString());
        sepOut.close();

        System.out.println("Done! Written " + totalQuestionsWritten + " questions and " + totalAnswersWritten
                        + " answers.\n" + filteredQuestions + " filtered questions, " + filteredAnswers
                        + " filtered answers on written questions.\n" + errors + " read errors.");
    }

    private static void addSuccessorTextNode(Document doc, Element rootElement, String elementName, String value) {
        if (value == null) {
            value = "";
        }
        try {
            Element element = doc.createElement(elementName);
            element.appendChild(doc.createCDATASection(UNPRINTABLE_CHARS.matcher(value.trim()).replaceAll(" ")));
            rootElement.appendChild(element);
        } catch (DOMException e) {
            System.out.println(value);
        }
    }

}
