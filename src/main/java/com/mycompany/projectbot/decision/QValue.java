package com.mycompany.projectbot.decision;


public class QValue {
    private double value;
    private Integer weight;

    public QValue(double value, Integer weight) {
        this.value = value;
        this.weight = weight;
    }

    public void addValue(double value) {
        if (((this.weight * this.value) + value) != 0) {
            this.value = ((this.weight * this.value) + value) / (weight + 1);
        } else {
            this.value = 0;
        }

        this.weight++;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
