package com.eservice.iot.model;

/**
 * @Description: java类作用描述
 * @Author: ZT
 * @CreateDate: 2019/3/14 12:42
 */
public class VerifyResult {
    String face_image;
    String scene_image;
    double similarity;
    boolean same_person;
    String time;

    public String getFace_image() {
        return face_image;
    }

    public void setFace_image(String face_image) {
        this.face_image = face_image;
    }

    public String getScene_image() {
        return scene_image;
    }

    public void setScene_image(String scene_image) {
        this.scene_image = scene_image;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public boolean isSame_person() {
        return same_person;
    }

    public void setSame_person(boolean same_person) {
        this.same_person = same_person;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
