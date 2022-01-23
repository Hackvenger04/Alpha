package com.example.hackvengers.feed;

import java.io.Serializable;

public class FeedObject implements Serializable {
    private String eventName, eventDetails, eventLink, orgName, eventPoster, orgImage,category;

    public FeedObject(String orgName,String orgImage, String eventName, String eventPoster, String eventDetails, String eventLink,String category){
        this.orgName = orgName;
        this.orgImage = orgImage;
        this.eventName = eventName;
        this.eventPoster = eventPoster;
        this.eventDetails = eventDetails;
        this.eventLink = eventLink;
        this.category=category;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public String getEventLink() {
        return eventLink;
    }

    public void setEventLink(String eventLink) {
        this.eventLink = eventLink;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEventPoster() {
        return eventPoster;
    }

    public void setEventPoster(String eventPoster) {
        this.eventPoster = eventPoster;
    }

    public String getOrgImage() {
        return orgImage;
    }

    public void setOrgImage(String orgImage) {
        this.orgImage = orgImage;
    }
}


