// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.yahoo.yrlhaifa.haifa_utils.utils.XmlDomUtils.XmlDomUtilitiesException;
import com.yahoo.yrlhaifa.liveqa.challenge.Constants;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.WrongUserResponseException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.common.ExceptionUtilities;

import static com.yahoo.yrlhaifa.haifa_utils.utils.XmlDomUtils.*;

/**
 * Parses an XML returned from a participant, which holds the participant's answer to a given question. <BR>
 * The XML might alternatively provide a human-readable string which explains why the participant did not answer the
 * question.
 *
 * Date: Jan 18, 2015
 * 
 * @author Asher Stern
 *
 */
public class ParticipantAnswerFromXmlString {
    public static final String ANSWER_BASE_ELEMENT_NAME = "answer";
    public static final String ANSWER_ANSWERED_YES_NO_ATTRIBUTE_NAME = "answered";
    public static final String ANSWER_REPORTED_TIME_MILLISECONDS_ATTRIBUTE_NAME = "time";
    public static final String ANSWER_WHY_NOT_ANSWERED_ELEMENT_NAME = "discard-reason";
    public static final String ANSWER_CONTENT_ELEMENT_NAME = "content";
    public static final String ANSWER_RESOURCES_ELEMENT_NAME = "resources";
    public static final String RESOURCES_LIST_SEPARATOR = ",";
    public static final String TITLE_FOCUS_ELEMENT_NAME = "title-foci";
    public static final String BODY_FOCUS_ELEMENT_NAME = "body-foci";
    public static final String QUESTION_SUMMARY_ELEMENT_NAME = "summary";



    public ParticipantAnswerFromXmlString(String xmlContents, int maximumAnswerContentLength,
                    int maximumSummaryLength) {
        super();
        this.xmlContents = xmlContents;
        this.maximumAnswerContentLength = maximumAnswerContentLength;
        this.maximumSummaryLength = maximumSummaryLength;
    }

    public static enum YesNoEnum {
        TRUE(true), FALSE(false), YES(true), NO(false);

        YesNoEnum(boolean yes) {
            this.yes = yes;
        }

        public boolean isYes() {
            return yes;
        }

        private final boolean yes;
    }


    public void create() throws WrongUserResponseException {
        try {
            Document document = createDocument();
            Element rootElement = document.getDocumentElement();
            Element answerElement = getChildElement(rootElement, ANSWER_BASE_ELEMENT_NAME);
            String yesNoString = answerElement.getAttribute(ANSWER_ANSWERED_YES_NO_ATTRIBUTE_NAME);
            answered = interpretYesNoString(yesNoString);
            if (answered) {
                // Read answer time attribute
                retrieveAnswerTime(answerElement);

                // Read answer contents
                retrieveAnswer(answerElement);
            } else // participant decided to ignore this answer.
            {
                retrieveWhyNotAnswered(answerElement);
            }
        } catch (WrongUserResponseException e) {
            throw e;
        } catch (SAXException | IOException | ParserConfigurationException | XmlDomUtilitiesException e) {
            // Note: the error is participant's error. Not ours. Program should continue, while discarding the
            // participant's answer.
            logger.warn("Failed to read an XML that was returned from a participant.\n"
                            + "This error is on participant\'s side, not system\'s side. Participant\'s answer will be discarded, and the challenge continues.\n"
                            + "Exception(s) message(s):\n" + ExceptionUtilities.getMessages(e));
            throw new WrongUserResponseException("Failed to read an XML that was returned from a participant.", e);
        }

    }


    public boolean isAnswered() {
        return answered;
    }

    public ParticipantAnswer getAnswer() {
        return answer;
    }

    public ReasonWhyQuestionDiscardByParticipant getWhyNotAnswered() {
        return whyNotAnswered;
    }



    //////////////////// PROTECTED & PRIVATE ////////////////////

    private void retrieveAnswerTime(final Element answerElement) throws WrongUserResponseException {
        // Read answer time attribute
        String timeString = answerElement.getAttribute(ANSWER_REPORTED_TIME_MILLISECONDS_ATTRIBUTE_NAME);
        if (timeString != null) {
            if (timeString.length() > 0) {
                try {
                    answerTimeMilliseconds = Long.parseLong(timeString);
                } catch (NumberFormatException e) {
                    throw new WrongUserResponseException("Wrong answer time attribute. (\"" + timeString
                                    + "\"). Expected an integer number.", e);
                }

            }
        } // end if { if {
    }

    private void retrieveWhyNotAnswered(final Element answerElement) throws XmlDomUtilitiesException {
        String whyNotAnsweredString = "";
        Element whyNoAnsweredElement = getChildElement(answerElement, ANSWER_WHY_NOT_ANSWERED_ELEMENT_NAME, true);
        if (whyNoAnsweredElement == null) {
            whyNotAnsweredString = "";
        } else {
            whyNotAnsweredString = getTextOfElement(whyNoAnsweredElement, false);
            if (null == whyNotAnsweredString) {
                whyNotAnsweredString = "";
            }
        }
        whyNotAnswered = new ReasonWhyQuestionDiscardByParticipant(whyNotAnsweredString);
    }

    private void retrieveAnswer(final Element answerElement) throws XmlDomUtilitiesException {
        Element contentElement = getChildElement(answerElement, ANSWER_CONTENT_ELEMENT_NAME);
        String answerContent = getTextOfElement(contentElement, true);
        if (answerContent.length() > maximumAnswerContentLength) {
            logger.info("A too-long answer is being truncated. Original length = " + answerContent.length()
                            + ", maximum allowed length = " + maximumAnswerContentLength);
            answerContent = answerContent.substring(0, maximumAnswerContentLength);
        }

        List<String> resourcesList = null;
        Element resourcesElement = getChildElement(answerElement, ANSWER_RESOURCES_ELEMENT_NAME, true);
        if (resourcesElement != null) {
            String resourcesString = getTextOfElement(resourcesElement, false);
            if (resourcesString != null) {
                resourcesString = resourcesString.trim();
                if (resourcesString.length() > 0) {
                    String[] resourcesArray = resourcesString.split(RESOURCES_LIST_SEPARATOR);
                    if (resourcesArray.length > 0) {
                        resourcesList = new ArrayList<String>(resourcesArray.length);
                        for (String resource : resourcesArray) {
                            resourcesList.add(resource.trim());
                        }
                        resourcesList = Collections.unmodifiableList(resourcesList);
                    }
                }
            }
        }
        if (null == resourcesList) {
            resourcesList = Collections.emptyList();
        }

        Element titleFocusElement = getChildElement(answerElement, TITLE_FOCUS_ELEMENT_NAME, true);
        String titleFocusContent = titleFocusElement == null ? "" : getTextOfElement(titleFocusElement, false);
        if (titleFocusContent == null) {
            titleFocusContent = "";
        }
        Element bodyFocusElement = getChildElement(answerElement, BODY_FOCUS_ELEMENT_NAME, true);
        String bodyFocusContent = bodyFocusElement == null ? "" : getTextOfElement(bodyFocusElement, false);
        if (bodyFocusContent == null) {
            bodyFocusContent = "";
        }
        Element questionSummaryElement = getChildElement(answerElement, QUESTION_SUMMARY_ELEMENT_NAME, true);
        String questionSummaryContent =
                        questionSummaryElement == null ? "" : getTextOfElement(questionSummaryElement, false);
        if (questionSummaryContent == null) {
            questionSummaryContent = "";
        }
        if (questionSummaryContent.length() > maximumSummaryLength) {
            logger.info("A too-long question summary is being truncated. Original length = "
                            + questionSummaryContent.length() + ", maximum allowed length = " + maximumSummaryLength);
            questionSummaryContent = questionSummaryContent.substring(0, maximumSummaryLength);
        }

        answer = new ParticipantAnswer(answerContent, answerTimeMilliseconds, resourcesList, titleFocusContent,
                        bodyFocusContent, questionSummaryContent);
    }



    private Document createDocument() throws SAXException, IOException, ParserConfigurationException {
        InputStream stream = new ByteArrayInputStream(xmlContents.getBytes(Constants.WORKING_CHARSET));
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
        return document;
    }



    private static boolean interpretYesNoString(String str) throws WrongUserResponseException {
        boolean detected = false;
        boolean ret = false;
        for (YesNoEnum yesNo : YesNoEnum.values()) {
            if (yesNo.name().equalsIgnoreCase(str)) {
                ret = yesNo.isYes();
                detected = true;
                break;
            }
        }
        if (!detected) {
            throw new WrongUserResponseException(
                            "Given attirbute value for answered/not-answered is erroneous. Given attirbute value is \""
                                            + str + "\".");
        } else {
            return ret;
        }
    }



    private final String xmlContents;
    private final int maximumAnswerContentLength;
    private final int maximumSummaryLength;

    private boolean answered = false;
    private long answerTimeMilliseconds = 0;
    private ParticipantAnswer answer = null;
    private ReasonWhyQuestionDiscardByParticipant whyNotAnswered = null;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(ParticipantAnswerFromXmlString.class);
}
