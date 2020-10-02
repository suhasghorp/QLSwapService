package com.qlservices;

import com.qlservices.util.Utils;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;
import org.quantlib.China;
import org.quantlib.Settings;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class StartUpLoader {
    @Inject
    MarketData marketData;

    private static final Logger LOG = Logger.getLogger(StartUpLoader.class);
    void onStart(@Observes StartupEvent ev) throws Exception {
        LocalDate evalDate = LocalDate.MAX;
        LOG.info("in OnStart event, loading JNI library");
        try {
            System.loadLibrary("QuantLibJNI");
        } catch (UnsatisfiedLinkError error){
            if (error.getMessage().contains("already loaded")){
                //do nothing
            } else {
                throw new Exception("Could not load QuantLib JNI so " + error.getMessage());
            }
        }
        try {
            evalDate = LocalDate.parse(System.getProperty("eval_date"), DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        } catch (Exception ex){
            LOG.info("-Deval_date=MM-dd-yyyy must be passed in on the cmd line");
            throw new Exception("-Deval_date=MM-dd-yyyy must be passed in on the cmd line\n" + ex.getMessage());
        }
        Settings.instance().setEvaluationDate(Utils.javaDateToQLDate(evalDate));
        LOG.info("in OnStart event, setting evaluation date to " + evalDate + " , will be used all over");
        //Settings.instance().setEvaluationDate(Utils.javaDateToQLDate(LocalDate.now()));

        LOG.info("in OnStart event, caching projection curve market data files");
        marketData.getProjectionMarketData(LocalDate.now(), "USD", "3M");
        LOG.info("in OnStart event, caching discount curve market data files");
        marketData.getDiscountMarketData(LocalDate.now(), "USD");
        LOG.info("in OnStart event, caching fixngs market data files");
        marketData.getFixingsMarketData("USD", "3M");
    }

}
