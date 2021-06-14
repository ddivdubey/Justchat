package com.escalon.JustChat.Model;


public class GroupChat {

    private  String sender;
    private String message,messageid;
    private  String username,imageurl;

    public GroupChat(String sender, String message, String username, String imageurl) {
        this.sender = sender;
        this.message = message;
        this.username = username;
        this.imageurl = imageurl;
    }

    public GroupChat() {
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
