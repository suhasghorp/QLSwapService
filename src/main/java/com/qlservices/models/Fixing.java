package com.qlservices.models;

import org.quantlib.Date;

import java.time.LocalDate;

public class Fixing {
    public Date date;
    public double rate;
    public Fixing(){}

    public Fixing(Date date, double rate) {
        this.date = date;
        this.rate = rate;
    }
}
