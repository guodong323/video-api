package com.huangk.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Image {
    @Id
    private Long id;

    private String coverPath;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }
}
