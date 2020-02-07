package com.example.spammer.Models;

public class Result {
    private String result;
    private String postNumber;
    private String groupId;
    private String commentNumber;

    public Result(String result, String postNumber) {
        this.result = result;
        this.postNumber = postNumber;
    }

    public Result(String result, String postNumber, String groupId, String commentNumber) {
        this.result = result;
        this.postNumber = postNumber;
        this.groupId = groupId;
        this.commentNumber = commentNumber;
    }

    public String getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(String commentNumber) {
        this.commentNumber = commentNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Result(String result, String postNumber, String groupId) {
        this.result = result;
        this.postNumber = postNumber;
        this.groupId = groupId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPostNumber() {
        return postNumber;
    }

    public void setPostNumber(String postNumber) {
        this.postNumber = postNumber;
    }
}
