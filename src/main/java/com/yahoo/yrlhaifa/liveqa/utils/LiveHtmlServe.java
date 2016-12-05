// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.yahoo.yrlhaifa.haifa_utils.ds.SeparatorStringBuilder;
import com.yahoo.yrlhaifa.haifa_utils.utils.Log4jInit;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.QuestionWithAnswers;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class LiveHtmlServe extends NanoHTTPD implements Closeable {

    private Set<String> qidsAlreadyReported = new HashSet<>();
    private Map<String, String> participantAliases = new HashMap<>();

    private static Thread mainThread = null;
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LiveHtmlServe.class);

    private String questionsFile;
    private String answersFile;
    private String htmlPage = "";

    public LiveHtmlServe(int port, String questionsFile, String answersFile) throws IOException {
        super(port);
        this.questionsFile = questionsFile;
        this.answersFile = answersFile;
        reload();
    }

    public LiveHtmlServe(int port) throws IOException {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getUri().equals("/main.css")) {
            return new Response(Status.OK, "text/css", getcss());
        }
        if (session.getUri().equals("/liveQA.png")) {
            return new Response(Status.OK, "img/png", getpng());
        }
        if (session.getUri().startsWith("/stop")) {
            logger.info("Interrupting main thread (" + mainThread.getName() + ").");
            mainThread.interrupt();
            return new Response(Status.OK, "text/html", "<html><body><center><h1>SHUTDOWN</h1></center></body></html>");
        }
        if (session.getUri().startsWith("/reset")) {
            reset();
        }
        if (session.getUri().startsWith("/re")) { // including reset
            try {
                reload();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (session.getUri().equalsIgnoreCase("/aliases")) {
            return new Response(Status.OK, "text/html", participantAliases.toString());
        }
        if (session.getUri().equalsIgnoreCase("/qids")) {
            return new Response(Status.OK, "text/html",
                            new SeparatorStringBuilder("<br>").appendAll(sort(qidsAlreadyReported)).toString());
        }
        return new Response(Status.OK, "text/html", getHomePageString());
    }

    private InputStream getpng() {
        try {
            return new FileInputStream(new File("data/liveQA.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Iterable<String> sort(Set<String> set) {
        ArrayList<String> l = Lists.newArrayList(set);
        Collections.sort(l);
        return l;
    }

    private String getcss() {
        try {
            byte[] cssFile = Files.readAllBytes(Paths.get("src/main/css/main.css"));
            return new String(cssFile, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getHomePageString() {
        return htmlPage;
    }

    @Override
    public void close() throws IOException {
        logger.info("Closing.");
    }

    private void reload() throws IOException {
        // TODO call this every few minutes
        // TODO make this much, much prettier (CSS etc.)
        htmlPage = "";
        TextualLogScraper scraper = new TextualLogScraper(questionsFile, answersFile, false);
        QuestionWithAnswers qwa;
        while (scraper.hasNext()) {
            qwa = scraper.next();
            Question question = qwa.getQuestion();
            String qid = question.getId();
            if (qidsAlreadyReported.contains(qid)) {
                continue;
            }
            qidsAlreadyReported.add(qid);
            qid = qid.replace("YA:", "");

            // create html page
            SeparatorStringBuilder ssb = new SeparatorStringBuilder("<br>\n");
            ssb.append("<div class=\"question_answers\">");
            ssb.append("<div class=\"question\">");
            ssb.append("<qid><h3>Question</h3><h4>ID</h4><a href = \"https://answers.yahoo.com/question/index?qid="
                            + qid + "\">" + qid + "</a></qid>");
            ssb.append("<cat><h4>Category</h4>" + question.getCategory() + "</cat>");
            ssb.append("<qtitle><h4>Title</h4>" + question.getTitle() + "</qtitle>");
            if (!question.getBody().trim().isEmpty()) {
                ssb.append("<qbody><h4>Body</h4>" + question.getBody() + "</qbody>");
            }
            ssb.append("</div>"); // question

            // TODO 2 column view (question on left, answers on right)
            ssb.append("<div class=\"answers\">");
            ssb.append("<ans_h><h3>Answers</h3></ans_h>");

            for (Entry<String, ParticipantAnswer> e : qwa.getAllAnswers().entrySet()) {
                // TODO only pick some of the answers
                String participant = e.getKey();
                if (!participantAliases.containsKey(participant)) {
                    String alias = generateAlias();
                    participantAliases.put(participant, alias);
                }
                ssb.append("<div class=\"answer\">");
                ssb.append("<part><h4>Participant Alias</h4>" + participantAliases.get(participant) + "</part>");
                ssb.append("<ans><h4>Answer</h4>" + e.getValue().getAnswer() + "</ans>");
                ssb.append("<resources><h4>Resources</h4>" + new SeparatorStringBuilder(",")
                                .appendAll(e.getValue().getReportedResources()).toString() + "</resources>");
                ssb.append("</div>"); // answer
            }
            ssb.append("</div>"); // answers
            ssb.append("</div>"); // question_answers
            ssb.append("<br>");
            htmlPage = ssb.toString() + htmlPage; // order from latest to earliest
        }
        htmlPage += "</body></html>\n";
        htmlPage = getHeader() + htmlPage;
    }

    private String getHeader() {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">"
                        + "<html><head><title>LiveQA 2015 Question-Answer feed</title>"
                        + "<meta http-equiv=\"refresh\" content=\"180\">" // TODO refresh as param?
                        + "<link rel=\"stylesheet\" href=\"main.css\">"
                        + "</head><body><br><img src=\"liveQA.png\" alt=\"logo\" height=\"134\" width=\"191\" class=\"displayed\"></img><br>\n";
    }

    private String generateAlias() {
        // TODO make much more interesting
        byte[] bytes = new byte[20];
        new Random().nextBytes(bytes);
        return bytes.toString();
    }

    /**
     * All QIDs will be displayed next time the page is loaded
     */
    private void reset() {
        qidsAlreadyReported = new HashSet<>();
    }

    public static void main(String[] args) {
        try {
            Log4jInit.init();

            int port = Integer.parseInt(args[0]);

            try (LiveHtmlServe server = new LiveHtmlServe(port, args[1], args[2])) {
                mainThread = Thread.currentThread();
                executeInstance(server);
            }

        } catch (Throwable t) {
            t.printStackTrace(System.out);
        }
    }

    private static void executeInstance(LiveHtmlServe server) {
        try {
            server.start();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }

        System.out.println("Server started, Hit Enter to stop.\n");

        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.in.read();
                } catch (Throwable ignored) {
                }
            }
        });
        readThread.setDaemon(true);
        readThread.start();
        try {
            readThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        server.stop();
        System.out.println("Server stopped.\n");
    }

}
