package com.qlservices;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.qlservices.models.Fixing;
import com.qlservices.models.Quote;
import com.qlservices.util.Utils;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import org.bson.Document;
import org.jboss.logging.Logger;
import org.quantlib.Date;
import org.quantlib.Settings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MarketData {

    @Inject
    MongoClient mongoClient;

    private static final Logger LOG = Logger.getLogger(MarketData.class);
    public MarketData(){}

    @CacheResult(cacheName = "evluation-date-cache")
    public LocalDate getEvaluationJavaDate(){
        Date qlDate = Settings.instance().getEvaluationDate();
        return Utils.qlDateToJavaDate(qlDate);
    }

    @CacheResult(cacheName = "projection-curve-cache")
    public List<Quote> getProjectionMarketData(@CacheKey LocalDate date, @CacheKey String currency, @CacheKey String tenor) throws Exception {
        List<Quote> quotes = new ArrayList<>();

        /*String fileName = currency + "-LIBOR-" + tenor + ".csv";
        LOG.info("current dir:" + Paths.get(".").toAbsolutePath().normalize().toString());
        String marketDataPath = System.getProperty("market_data_path");
        LOG.info("using market data path: " + marketDataPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(marketDataPath + "/" + fileName)));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String quote = line.split(",")[0];
            String quoteType = quote.split("\\.")[0];
            String ten = quote.split("\\.")[1];
            double rate = Double.parseDouble(line.split(",")[1]);
            quotes.add(new Quote(quoteType,ten,rate));
        }
        br.close();*/

        MongoCollection projectionData = mongoClient.getDatabase("marketdata").getCollection("USD-LIBOR-3M");
        MongoCursor<Document> cursor = projectionData.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String instrument = document.getString("instrument");
                String quoteType = instrument.split("\\.")[0];
                String ten = instrument.split("\\.")[1];
                double rate = Double.parseDouble(document.getString("rate"));
                quotes.add(new Quote(quoteType,ten,rate));
            }
        } finally {
            cursor.close();
        }

        LOG.info("Loaded projection market data records:" + quotes.size());
        return quotes;
    }

    @CacheResult(cacheName = "discount-curve-cache")
    public List<Quote> getDiscountMarketData(@CacheKey LocalDate date, @CacheKey String currency) throws IOException {
        List<Quote> quotes = new ArrayList<>();
        /*
        String fileName = null;
        if (currency.equals("USD")) {
            fileName = currency + "-FedFunds.csv";
        } else if (currency.equals("EUR")){
            fileName = currency + "-EONIA.csv";
        }
        String marketDataPath = System.getProperty("market_data_path");
        LOG.info("using market data path: " + marketDataPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(marketDataPath + "/" + fileName)));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String ten = line.split(",")[0];
            double rate = Double.parseDouble(line.split(",")[1]);
            quotes.add(new Quote("OISSWAP",ten,rate));
        }
        br.close();*/

        MongoCollection projectionData = mongoClient.getDatabase("marketdata").getCollection("USD-FedFunds");
        MongoCursor<Document> cursor = projectionData.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                String ten = document.getString("tenor");
                double rate = Double.parseDouble(document.getString("rate"));
                quotes.add(new Quote("OISSWAP",ten,rate));
            }
        } finally {
            cursor.close();
        }
        LOG.info("Loaded discount market data records:" + quotes.size());
        return quotes;
    }

    @CacheResult(cacheName = "fixings-cache")
    public List<Fixing> getFixingsMarketData(@CacheKey String currency, @CacheKey String tenor) throws IOException {
        List<Fixing> fixings = new ArrayList<>();
        MongoCollection fixingsCollection = mongoClient.getDatabase("marketdata").getCollection("USD-FIXINGS-3M");
        MongoCursor<Document> cursor = fixingsCollection.find().iterator();
        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Fixing fixing = new Fixing();
                LocalDate dt = LocalDate.parse(document.getString("date"),DateTimeFormatter.ofPattern("MM-dd-yyyy"));
                double rate = Double.parseDouble(document.getString("fixing"));
                fixings.add(new Fixing(Utils.javaDateToQLDate(dt),rate));
            }
        } finally {
            cursor.close();
        }
        /*String fileName = currency + "-FIXINGS-" + tenor + ".csv";
        String marketDataPath = System.getProperty("market_data_path");
        LOG.info("using market data path: " + marketDataPath);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(marketDataPath + "/" + fileName)));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            LocalDate dt = LocalDate.parse(line.split(",")[0],DateTimeFormatter.ofPattern("MM-dd-yyyy"));
            double rate = Double.parseDouble(line.split(",")[1]);
            fixings.add(new Fixing(Utils.javaDateToQLDate(dt),rate));
        }
        br.close();*/

        LOG.info("Loaded fixings market data records:" + fixings.size());
        return fixings;
    }
}
