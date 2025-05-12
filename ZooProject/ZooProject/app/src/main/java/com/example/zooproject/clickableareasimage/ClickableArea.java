package com.example.zooproject.clickableareasimage;

public class ClickableArea<T> {

    private int x;
    private int y;
    private int width;
    private int height;

    private T item;

    public ClickableArea(int x, int y, int width, int height, T item){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.item = item;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public T getItem() {
        return item;
    }

    public void setLabel(T item) {
        this.item = item;
    }
}
