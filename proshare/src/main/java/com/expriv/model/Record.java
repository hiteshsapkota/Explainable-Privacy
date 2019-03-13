package com.expriv.model;

public class Record {
    private int id;
    private String image_id;
    private String image_path;
    private String user_name;
    private int sharing_decision;
    private int display_status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;

    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public int getSharing_decision() {
        return sharing_decision;
    }

    public void setSharing_decision(int sharing_decision) {
        this.sharing_decision = sharing_decision;
    }


    public int getDisplay_status() {
        return display_status;
    }

    public void setDisplay_status(int display_status) {
        this.display_status = display_status;
    }


    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }
}
