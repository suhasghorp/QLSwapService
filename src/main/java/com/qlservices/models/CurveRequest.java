package com.qlservices.models;

import io.smallrye.common.constraint.NotNull;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;
import java.time.LocalDate;
import java.util.List;

/*
"curve":{
    "name":"USD-LIBOR-3M",
    "date":"09-25-2020",
    "type":"projection",
    "currency":"USD",
    "tenor":"3M",
    "quotes":[
      {"name":"DEPOSIT.1D", "rate":0.02359},
      {"name":"DEPOSIT.1W", "rate":0.0237475},
      {"name":"DEPOSIT.1M", "rate":0.02325},
      {"name":"DEPOSIT.2M", "rate":0.0232475},
      {"name":"DEPOSIT.3M", "rate":0.0230338},
      {"name":"FUTURE.2M", "rate":97.92},
      {"name":"FUTURE.5M", "rate":98.005},
      {"name":"FUTURE.8M", "rate":98.185},
      {"name":"FUTURE.11M", "rate":98.27},
      {"name":"FUTURE.14M", "rate":98.33},
      {"name":"SWAP.2Y", "rate":0.01879},
      {"name":"SWAP.3Y", "rate":0.01835},
      {"name":"SWAP.5Y", "rate":0.01862},
      {"name":"SWAP.7Y", "rate":0.0194},
      {"name":"SWAP.10Y", "rate":0.02065},
      {"name":"SWAP.15Y", "rate":0.02204},
      {"name":"SWAP.30Y", "rate":0.02306}
    ]
  }
 */

public class CurveRequest {

    @NotNull
    @JsonbProperty("name")
    public String curveName;

    @NotNull
    @JsonbProperty("type")
    public String curveType;

    @NotNull
    @JsonbProperty("currency")
    public String currency;

    @NotNull
    @JsonbProperty("tenor")
    public String tenor;

    @NotNull
    @JsonbProperty("quotes")
    public List<Quote> quotes;

    public CurveRequest(){}

    public CurveRequest(String curveName, String curveType, String currency, String tenor, List<Quote> quotes) {
        this.curveName = curveName;
        this.curveType = curveType;
        this.currency = currency;
        this.tenor = tenor;
        this.quotes = quotes;
    }
}
