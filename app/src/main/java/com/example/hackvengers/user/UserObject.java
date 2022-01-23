package com.example.hackvengers.user;

import java.io.Serializable;

public class UserObject implements Serializable {

    private final String uid;
    private final String phoneNumber;
    private String name;
    private String status;
    private String profileImageUri;
    private String chatID;

    boolean isUser,
            isOrganizer,
            isMentor;

    String mentorKey,organizerKey;


    private boolean isSelected = false;


    public UserObject() {
        this.uid = "";
        this.name = "";
        this.phoneNumber = "";
        this.status = "";
        this.profileImageUri = "";
        this.chatID = "";

    }

    public UserObject(String uid, String name, String phoneNumber, String chatID) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.chatID = chatID;
    }




    public UserObject(String uid, String name, String phoneNumber, String status, String profileImageUri, String chatID) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.profileImageUri = profileImageUri;
        this.chatID = chatID;
    }

    public UserObject(String uid, String phoneNumber, String name, String status, String profileImageUri, String chatID, boolean isUser, boolean isOrganizer, boolean isMentor) {
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.status = status;
        this.profileImageUri = profileImageUri;
        this.chatID = chatID;
        this.isUser = isUser;
        this.isOrganizer = isOrganizer;
        this.isMentor = isMentor;
    }

    public UserObject(String uid, String name, String phoneNumber, String status, String profileImageUri, String chatID, boolean isUser, boolean isOrganizer, boolean isMentor, String mentorKey, String organizerKey) {
        this.uid = uid;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.status = status;
        this.profileImageUri = profileImageUri;
        this.chatID = chatID;
        this.isUser = isUser;
        this.isOrganizer = isOrganizer;
        this.isMentor = isMentor;
        this.mentorKey = mentorKey;
        this.organizerKey = organizerKey;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public String getChatID() {
        return chatID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isMentor() {
        return isMentor;
    }

    public boolean isOrganizer() {
        return isOrganizer;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setMentor(boolean mentor) {
        isMentor = mentor;
    }

    public void setOrganizer(boolean organizer) {
        isOrganizer = organizer;
    }

    public String getOrganizerKey() {
        return organizerKey;
    }

    public String getMentorKey() {
        return mentorKey;
    }

    public void setOrganizerKey(String organizerKey) {
        this.organizerKey = organizerKey;
    }

    public void setMentorKey(String mentorKey) {
        this.mentorKey = mentorKey;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
