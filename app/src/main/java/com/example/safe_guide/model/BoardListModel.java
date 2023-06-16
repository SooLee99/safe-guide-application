package com.example.safe_guide.model;

public class BoardListModel {
    private String title;
    private String name;
    private String content;

    public BoardListModel(String tvTitle, String tvName, String tvContent) {
        this.title = tvTitle;
        this.name = tvName;
        this.content = tvContent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
