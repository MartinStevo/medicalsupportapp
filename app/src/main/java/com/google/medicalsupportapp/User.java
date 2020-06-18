package com.google.medicalsupportapp;

public class User {

    private String id;
    private String username;
    private String doc;

    public User(String id, String username, String doc) {
        this.id = id;
        this.username = username;
        this.doc = doc;
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }
}
