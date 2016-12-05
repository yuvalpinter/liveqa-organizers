// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.rep;

import java.util.Calendar;

/**
 * Represents a question to be sent to participants.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class Question {
    public Question(String id, String title, String body, String category, Calendar publishedDate) {
        super();
        this.id = id;
        this.title = title;
        this.body = body;
        this.category = category;
        this.publishedDate = publishedDate;

    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getCategory() {
        return category;
    }

    public Calendar getPublishedDate() {
        return publishedDate;
    }


    @Override
    public String toString() {
        return "Question [id=" + id + ", title=" + title + ", body=" + body + ", category=" + category + "]";
    }



    private final String id;
    private final String title;
    private final String body;
    private final String category;
    private final Calendar publishedDate;
}
