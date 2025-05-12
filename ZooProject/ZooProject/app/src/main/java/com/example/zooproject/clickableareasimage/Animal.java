package com.example.zooproject.clickableareasimage;

public class Animal {
    private Integer sectionId;
    private String name;

    public Animal(Integer sectionId, String name) {
        this.sectionId = sectionId;
        this.name = name;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
