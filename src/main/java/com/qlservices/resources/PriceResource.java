package com.qlservices.resources;

import com.qlservices.MarketData;
import com.qlservices.models.Fixing;
import com.qlservices.models.VanillaSwap;
import com.qlservices.services.CurveBuilderService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.quantlib.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PriceResource {
    private static final Logger LOG = Logger.getLogger(PriceResource.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

    @Inject
    CurveBuilderService curveBuilderService;

    @Inject
    MarketData marketData;

    @ConfigProperty(name = "environment")
    String env;

    @POST
    @Path("vanillaswap")
    public VanillaSwap price(VanillaSwap swap) throws Exception {
        if (env.equals("DEV")) LOG.info("Starting to price vanillaswap" + LocalDateTime.now().format(formatter));
        RelinkableYieldTermStructureHandle discountTermStructure = new RelinkableYieldTermStructureHandle();
        RelinkableYieldTermStructureHandle projectionTermStructure = new RelinkableYieldTermStructureHandle();

        YieldTermStructure discountCurve = curveBuilderService.buildDiscountCurve("DISC-CURVE", "USD");
        discountTermStructure.linkTo(discountCurve);
        DiscountingSwapEngine engine = new DiscountingSwapEngine(discountTermStructure);

        YieldTermStructure projectionCurve = curveBuilderService.buildProjectionCurve("PROJ-CURVE", "USD", "3M", discountCurve);
        projectionTermStructure.linkTo(projectionCurve);

        USDLibor index = new USDLibor(new Period(Frequency.Quarterly), projectionTermStructure);
        swap.setPricingEngine(engine, index);
        if (env.equals("DEV")) LOG.info("Pricing engine set " + LocalDateTime.now().format(formatter));

        Schedule floatingLegSchedule = swap.getFloatingSchedule();
        Calendar cal = new WeekendsOnly(); //UnitedStates();
        Date prevDate = cal.advance(floatingLegSchedule.previousDate(Settings.instance().getEvaluationDate()), -2, TimeUnit.Days);
        Optional<Double> fixing = getFixingForDate(prevDate, "USD", "3M");
        if (fixing.isPresent())
            index.addFixing(prevDate, fixing.get());
        if (env.equals("DEV")) LOG.info("Fixing added " + LocalDateTime.now().format(formatter));

        swap.netPresentValue = swap.npv();
        if (env.equals("DEV")) LOG.info("Swap NPV: " + swap.netPresentValue + " at " + LocalDateTime.now().format(formatter));
        swap.fairRate = swap.fairRate();
        if (env.equals("DEV")) LOG.info("Swap fairrate: " + swap.fairRate + " at " + LocalDateTime.now().format(formatter));

        if (swap.fullResults) {
            if (env.equals("DEV")) LOG.info("Generating full results " + LocalDateTime.now().format(formatter));

            double shift = 0.0001;
            discountTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(discountCurve), new QuoteHandle(new SimpleQuote(shift))));
            projectionTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(projectionCurve), new QuoteHandle(new SimpleQuote(shift))));
            double npvUp = swap.npv();
            if (env.equals("DEV")) LOG.info("Swap UP NPV calculated " + LocalDateTime.now().format(formatter));

            discountTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(discountCurve), new QuoteHandle(new SimpleQuote(-shift))));
            projectionTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(projectionCurve), new QuoteHandle(new SimpleQuote(-shift))));
            double npvDown = swap.npv();
            if (env.equals("DEV")) LOG.info("Swap DOWN NPV calculated " + LocalDateTime.now().format(formatter));

            double dv01 = (npvDown - npvUp) / 2.0;
            swap.dv01 = swap.swapType == org.quantlib.VanillaSwap.Type.Payer ? dv01 : -1.0 * dv01;
            if (env.equals("DEV")) LOG.info("Swap dv01 calculated " + swap.dv01 + " at " + LocalDateTime.now().format(formatter));

            projectionTermStructure.linkTo(projectionCurve);
            if (env.equals("DEV")) LOG.info("Starting KRDs " + LocalDateTime.now().format(formatter));
            List<com.qlservices.models.Quote> quotes = marketData.getProjectionMarketData(marketData.getEvaluationJavaDate(), "USD", "3M");
            double sumBuckets = 0.0;
            for (com.qlservices.models.Quote quote : quotes) {
                double value = quote.simpleQuote.value();
                quote.simpleQuote.setValue(value + 0.0001);
                double upNPV = swap.npv();
                quote.simpleQuote.setValue(value - 0.0001);
                double downNPV = swap.npv();
                double bucketedDV01 = (upNPV - downNPV) / 2.0;
                quote.simpleQuote.setValue(value);
                swap.bucketedDV01.put(quote.tenor, (Math.abs(bucketedDV01) < 0.001 ? 0.0 : bucketedDV01));
                sumBuckets += bucketedDV01;
                if (env.equals("DEV")) LOG.info(quote.tenor + " KRD calculated " + " at " + LocalDateTime.now().format(formatter));
            }
        }
        return swap;
    }

    public Optional<Double> getFixingForDate(Date dt, String currency, String tenor) throws Exception{
        if (env.equals("DEV")) LOG.info("Looking for fixing " + LocalDateTime.now().format(formatter));
        Optional<Double> ret = Optional.empty();
        List<Fixing> fixings = marketData.getFixingsMarketData(currency, tenor);
        Optional<Fixing> fixing = fixings.stream().filter(fix -> fix.date.serialNumber() == dt.serialNumber()).findFirst();
        if (fixing.isPresent())
            ret = Optional.of(fixing.get().rate);
        if (env.equals("DEV")) LOG.info("Found fixing " + LocalDateTime.now().format(formatter));
        return ret;
    }
}
