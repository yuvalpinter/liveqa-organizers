// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.http_operation;

import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.QUESTION_BODY_PARAMETER_NAME;
import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.QUESTION_ID_PARAMETER_NAME;
import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.QUESTION_TITLE_PARAMETER_NAME;
import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.QUESTION_CATEGORY_PARAMETER_NAME;
import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.WORKING_CHARSET;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.message.BasicNameValuePair;

import com.yahoo.yrlhaifa.liveqa.challenge.ChallengeSystemUtilities;
import com.yahoo.yrlhaifa.liveqa.challenge.Constants;
import com.yahoo.yrlhaifa.liveqa.challenge.Participant;
import com.yahoo.yrlhaifa.liveqa.challenge.configuration.RequestGeneralParameters;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionOperationException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.WrongUserResponseException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.ParticipantAnswerFromXmlString;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.ParticipantResponse;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.ReasonWhyQuestionDiscardByParticipant;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.ResponseOperationInformation;


/**
 * Sends a given question to all the participants, concurrently, and store their answer in the given map.
 * <P>
 * This class makes use of HTTP-Post requests, and sends the questions concurrently to all the participating systems,
 * which are assumed to be HTTP servers.
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionOperationHttpRequestSender {
    public QuestionOperationHttpRequestSender(List<Participant> participants, Question question,
                    ConcurrentMap<Participant, ParticipantResponse> mapParticipantToAnswer,
                    RequestGeneralParameters timeParameters) {
        super();
        this.participants = participants;
        this.question = question;
        this.mapParticipantToAnswer = mapParticipantToAnswer;
        this.requestGeneralParameters = timeParameters;
    }


    public void sendRequestsAndCollectAnswers() throws QuestionOperationException, InterruptedException {
        if (mapParticipantToAnswer.size() > 0) {
            throw new QuestionOperationException("BUG: The given map from system-id to answers is not empty.");
        }

        CloseableHttpClient httpClient = HttpClients.custom().setMaxConnPerRoute(participants.size()).build();
        try {
            ExecutorService executor = Executors.newFixedThreadPool(participants.size());
            try {
                FutureRequestExecutionService requestExecutor = new FutureRequestExecutionService(httpClient, executor);
                logger.info("Sending requests using request-executor...");
                sendRequestsWithRequestExecutor(requestExecutor);
                logger.info("Sending requests using request-executor - done.");
            } finally {
                try {
                    executor.shutdownNow();
                } catch (RuntimeException e) {
                    // TODO Add more error handling
                    logger.error("Failed to shutdown executor. Program continues.", e);
                }
            }

        } finally {
            try {
                httpClient.close();
            } catch (IOException | RuntimeException e) {
                // TODO Add more error handling
                logger.error("Failed to close HTTP client. Program continues.", e);
            }
        }


        // Remove those who did not finish on time, but did write results.
        Set<Participant> didNotSucceed = new LinkedHashSet<Participant>();
        for (Participant participant : mapParticipantToAnswer.keySet()) {
            if (!(systemsSucceeded.contains(participant))) {
                didNotSucceed.add(participant);
            }
        }
        for (Participant toRemove : didNotSucceed) {
            mapParticipantToAnswer.remove(toRemove);
        }


        if (exception != null) {
            throw exception;
        }
    }

    private void sendRequestsWithRequestExecutor(final FutureRequestExecutionService requestExecutor)
                    throws QuestionOperationException, InterruptedException {
        final long timeOut = requestGeneralParameters.getTimeForAnswerMilliseconds()
                        + requestGeneralParameters.getExtraTimeForRequestResponseMilliseconds()
                        + requestGeneralParameters.getSlackTimeForRequestExecutorTimeOutMilliseconds();
        final long maximumAllowedDuration = requestGeneralParameters.getTimeForAnswerMilliseconds()
                        + requestGeneralParameters.getExtraTimeForRequestResponseMilliseconds();

        List<HttpRequestFutureTask<Participant>> futures =
                        new ArrayList<HttpRequestFutureTask<Participant>>(participants.size());
        for (Participant participant : participants) {
            HttpRequestFutureTask<Participant> future = requestExecutor.execute(createRequest(participant), null,
                            new AnswerResponseHandler(participant));
            futures.add(future);
        }

        systemsSucceeded = new LinkedHashSet<Participant>();
        final long loopStartTime = new Date().getTime();
        long extraAdd = 0;
        for (HttpRequestFutureTask<Participant> future : futures) {
            try {
                final long timePassed = (new Date().getTime() - loopStartTime);
                extraAdd += Constants.EXTRA_ADD_TIMEOUT_FOR_EACH_THREAD_MILLISECONDS;
                final long currentIterationTimeOut = extraAdd + Math.max(0, (timeOut - timePassed));
                if (logger.isDebugEnabled()) {
                    logger.debug("Getting a future with time-out of " + currentIterationTimeOut + " milliseconds");
                }
                Participant participantOfThisFuture = future.get(currentIterationTimeOut, TimeUnit.MILLISECONDS);
                logger.info("System " + participantOfThisFuture.getUniqueSystemId()
                                + " has finished the question processing.");
                if (!future.isDone()) {
                    logger.info("Processing by system: \"" + participantOfThisFuture.getUniqueSystemId()
                                    + "\" is not done, and is being cancelled now.");
                    future.cancel(true);
                } else {
                    if (!future.isCancelled()) {
                        if (future.taskDuration() <= maximumAllowedDuration) {
                            systemsSucceeded.add(participantOfThisFuture);
                            boolean inMap = false;
                            if (mapParticipantToAnswer.containsKey(participantOfThisFuture)) {
                                final ParticipantResponse answer = mapParticipantToAnswer.get(participantOfThisFuture);
                                if (answer != null) {
                                    inMap = true;
                                    ResponseOperationInformation responseOperationInformation =
                                                    new ResponseOperationInformation(future.startedTime(),
                                                                    future.endedTime(), future.taskDuration());
                                    answer.setResponseOperationInformation(responseOperationInformation);
                                }
                            }
                            if (!inMap) {
                                logger.info("A request-response for participant \""
                                                + participantOfThisFuture.getUniqueSystemId()
                                                + "\" has completed with no answer."); // Such a behavior might follow
                                                                                       // unsuccessful status code, or
                                                                                       // when the participant decides
                                                                                       // not to answer, while sending
                                                                                       // an HTTP response.
                                // logger.error("Unexpected behavior: A participant request-response ended successfully,
                                // but the answer was not put in the map. This is a bug. Program continues, however.");
                            }
                        } else {
                            logger.info("System \"" + participantOfThisFuture.getUniqueSystemId()
                                            + "\" has finished, but not in time (time out has not been reached, thanks to slack executor time. However, the required time constraints were not met).\n"
                                            + "It\'s answer (if exists) will be discarded."); // will be discarded by
                                                                                              // not including that
                                                                                              // system in the
                                                                                              // "systemSucceeded" set.
                        }
                    }
                }
            } catch (InterruptedException e) {
                // I was interrupted (someone called Thread.interrupt() on this thread. This has nothing to do with the
                // executor's threads).
                // I must stop. There is nothing special for cleanup, so let's just stop.
                throw e;
            } catch (CancellationException e) {
                // The task was cancelled. Nothing to worry about. If it has been cancelled, than it will not write into
                // the map. Just log it.
                logger.info("One of the requests was cancelled: " + e.getMessage() + ". Program continues.");
            } catch (ExecutionException e) {
                // An HTTP problem. Either IO or protocol problem. Never mind. This is not system-wide fatal problem.
                // Let's log it, and continue.
                logger.error("One of the requests failed to execute. Program continues.", e);
            } catch (TimeoutException e) {
                // A time-out has been reached. Again, nothing to worry about. I have to cancel the task.
                // Its output should be discarded, and I take care of it above (in the try block) implicitly, when I do
                // not include this
                // system-id in the "systemsSucceeded" set.
                // Even if the task writes its result into the map, it will be removed, and the removal happens after
                // the executor.shutdownNow(),
                // So we can be sure it is finally not in the map.
                String exceptionMessage = e.getMessage();
                if (null == exceptionMessage) {
                    exceptionMessage = "";
                } else
                    exceptionMessage = " <" + exceptionMessage + ">";
                logger.info("One of the requests has timed-out" + exceptionMessage + ". Program continues.");
                future.cancel(true);
            }

        }
    }

    private HttpPost createRequest(final Participant participant) {
        List<NameValuePair> requestParameters = new LinkedList<NameValuePair>();
        requestParameters.add(new BasicNameValuePair(QUESTION_ID_PARAMETER_NAME, question.getId()));
        requestParameters.add(new BasicNameValuePair(QUESTION_TITLE_PARAMETER_NAME, question.getTitle()));
        requestParameters.add(new BasicNameValuePair(QUESTION_BODY_PARAMETER_NAME, question.getBody()));
        requestParameters.add(new BasicNameValuePair(QUESTION_CATEGORY_PARAMETER_NAME, question.getCategory()));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(requestParameters, Constants.WORKING_CHARSET);
        HttpPost httpPost = new HttpPost(participant.getParticipantServerUrl());
        httpPost.setEntity(entity);

        return httpPost;
    }


    /**
     * The returned String is the system-id, which is the key of mapParticipantToAnswer.
     *
     *
     */
    private class AnswerResponseHandler implements ResponseHandler<Participant> {
        public AnswerResponseHandler(Participant participant) {
            super();
            this.participant = participant;
        }


        @Override
        public Participant handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            final String participantUniqueId = participant.getUniqueSystemId();

            try {
                handleResponseAndInterruption(response);
            } catch (InterruptedException e) // Note that this is rare. Typically, canceling a task would find the task
                                             // before the response handler starts, so this exception will not be
                                             // thrown.
            {
                logger.info("Request-response task for participant \"" + participantUniqueId
                                + "\" has been interrupted and will exit immediately.");
                if (e.getMessage() != null) {
                    logger.info("Interrupt message: " + e.getMessage());
                }
            }

            return participant;
        }

        protected void handleResponseAndInterruption(HttpResponse response)
                        throws ClientProtocolException, IOException, InterruptedException {
            final int statusCode = response.getStatusLine().getStatusCode();

            if (ChallengeSystemUtilities.httpStatusCodeOK(statusCode)) {
                try (InputStream content = response.getEntity().getContent()) {
                    try (Reader reader = new InputStreamReader(content, WORKING_CHARSET)) {
                        // Take care on future.cancel() from the executor's thread:
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }

                        final int answerMaximumLength = Constants.ANSWER_XML_LENGTH_IN_ADDITION_TO_ANSWER_LENGTH
                                        + requestGeneralParameters.getMaximumAnswerLength();
                        char[] answerBuffer = new char[answerMaximumLength + 1 + 1]; // I add +1 to see whether answer
                                                                                     // length exceeded maximum length,
                                                                                     // and also add an addition +1 to
                                                                                     // be on the safe side.
                        final int charactersRead = reader.read(answerBuffer, 0, answerMaximumLength + 1);
                        if (charactersRead > requestGeneralParameters.getMaximumAnswerLength()) {
                            // Note: this might result in a malformed XML.
                            // TODO This should be propagated to the participant
                            logger.info("Participant " + participant.getUniqueSystemId()
                                            + " returned answer longer than permitted. The answer is truncated to "
                                            + requestGeneralParameters.getMaximumAnswerLength() + " characters.");
                            // TODO check if same check required for summary
                        }
                        String answerString =
                                        new String(answerBuffer, 0, Math.min(charactersRead, answerMaximumLength));
                        if (logger.isDebugEnabled()) {
                            logger.debug("Answer string of participant " + participant.getUniqueSystemId()
                                            + " for question " + question.getId() + " is:\n" + answerString);
                        }

                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                        ParticipantResponse participantResponse = constructResponseFromXmlString(answerString);
                        ParticipantResponse responseInMap =
                                        mapParticipantToAnswer.putIfAbsent(participant, participantResponse);
                        if (null != responseInMap) {
                            // TODO Improve this error handling
                            logger.error("Unexpected error. The mapParticipantToAnswer has already contained value for the given system unique id.\n"
                                            + "Program does not stop, but this indicates a bug, and might indicate that all the results are incorrect.\n"
                                            + "The system is inherently corrupted !!!\n" + "Value tried to put = "
                                            + participantResponse + ".\n" + "Existing value = " + responseInMap);
                        }
                        // TODO perform check on foci well-formedness
                    }

                }
            } else {
                logger.warn("Unsuccessful http status code, for participant \"" + participant.getUniqueSystemId()
                                + "\". Status code = " + statusCode + ". Status line = " + response.getStatusLine());
            }
        }

        private ParticipantResponse constructResponseFromXmlString(final String answerString) {
            ParticipantResponse participantResponse = null;
            ParticipantAnswerFromXmlString fromXml = new ParticipantAnswerFromXmlString(answerString,
                            requestGeneralParameters.getMaximumAnswerLength(),
                            requestGeneralParameters.getMaximumSummaryLength());
            try {
                fromXml.create();
                if (fromXml.isAnswered()) {
                    participantResponse = new ParticipantResponse(fromXml.getAnswer());
                } else {
                    ReasonWhyQuestionDiscardByParticipant whyNoAnswered = fromXml.getWhyNotAnswered();
                    if (null == whyNoAnswered) {
                        // This is unexpected, and is not supposed to happen at all. However, to be on the safe side...
                        logger.error("Internal minor unexpected error of ParticipantAnswerFromXmlString returning null for both answer and discard-reason. Overriding here with an empty reason.");
                        whyNoAnswered = new ReasonWhyQuestionDiscardByParticipant("");
                    }
                    participantResponse = new ParticipantResponse(whyNoAnswered);
                }
            } catch (WrongUserResponseException userException) {
                participantResponse = new ParticipantResponse(userException);
            }
            return participantResponse;
        }

        private final Participant participant;

    }

    private final List<Participant> participants;
    private final Question question;
    private final ConcurrentMap<Participant, ParticipantResponse> mapParticipantToAnswer;
    private final RequestGeneralParameters requestGeneralParameters;

    /**
     * A fatal exception, which should stop the system.
     */
    private QuestionOperationException exception = null;

    private Set<Participant> systemsSucceeded = null;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(QuestionOperationHttpRequestSender.class);
}

