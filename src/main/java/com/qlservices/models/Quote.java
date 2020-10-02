package com.qlservices.models;

import org.quantlib.QuoteHandle;
import org.quantlib.RelinkableQuoteHandle;
import org.quantlib.SimpleQuote;

public class Quote {
    public String quoteName;
    public String tenor;
    public double rate;
    public SimpleQuote simpleQuote;
    public QuoteHandle quoteHandle;


    public Quote(){}

    public Quote(String quoteName, String tenor, double rate) {
        this.quoteName = quoteName;
        this.tenor = tenor;
        this.rate = rate;
        this.simpleQuote = new SimpleQuote(rate);
        this.quoteHandle = new QuoteHandle(simpleQuote);
    }
}
