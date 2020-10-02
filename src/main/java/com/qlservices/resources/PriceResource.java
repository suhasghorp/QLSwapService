package com.qlservices.resources;

import com.qlservices.MarketData;
import com.qlservices.models.Fixing;
import com.qlservices.models.VanillaSwap;
import com.qlservices.services.CurveBuilderService;
import org.jboss.logging.Logger;
import org.quantlib.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/price")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PriceResource {
    private static final Logger LOG = Logger.getLogger(PriceResource.class);

    @Inject
    CurveBuilderService curveBuilderService;

    @Inject
    MarketData marketData;

    @POST
    @Path("vanillaswap")
    public VanillaSwap price(VanillaSwap swap) throws Exception {

        RelinkableYieldTermStructureHandle discountTermStructure = new RelinkableYieldTermStructureHandle();
        RelinkableYieldTermStructureHandle projectionTermStructure = new RelinkableYieldTermStructureHandle();

        YieldTermStructure discountCurve = curveBuilderService.buildDiscountCurve("DISC-CURVE", "USD");
        discountTermStructure.linkTo(discountCurve);
        DiscountingSwapEngine engine = new DiscountingSwapEngine(discountTermStructure);

        YieldTermStructure projectionCurve = curveBuilderService.buildProjectionCurve("PROJ-CURVE", "USD", "3M", discountCurve);
        projectionTermStructure.linkTo(projectionCurve);

        LOG.info("Projection curve built");

        USDLibor index = new USDLibor(new Period(Frequency.Quarterly), projectionTermStructure);
        swap.setPricingEngine(engine, index);
        LOG.info("pricing engine set");
        Schedule floatingLegSchedule = swap.getFloatingSchedule();
        Calendar cal = new WeekendsOnly(); //UnitedStates();
        Date prevDate = cal.advance(floatingLegSchedule.previousDate(Settings.instance().getEvaluationDate()), -2, TimeUnit.Days);
        Optional<Double> fixing = getFixingForDate(prevDate, "USD", "3M");
        if (fixing.isPresent())
            index.addFixing(prevDate, fixing.get());
        LOG.info("fixcing added");
        swap.netPresentValue = swap.npv();
        LOG.info("swap npv:" + swap.netPresentValue);
        swap.fairRate = swap.fairRate();

        if (swap.fullResults) {
            double shift = 0.0001;
            discountTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(discountCurve), new QuoteHandle(new SimpleQuote(shift))));
            projectionTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(projectionCurve), new QuoteHandle(new SimpleQuote(shift))));
            //swap.npv();
            double npvUp = swap.npv();

            discountTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(discountCurve), new QuoteHandle(new SimpleQuote(-shift))));
            projectionTermStructure.linkTo(new ZeroSpreadedTermStructure(new YieldTermStructureHandle(projectionCurve), new QuoteHandle(new SimpleQuote(-shift))));
            //swap.npv();
            double npvDown = swap.npv();
            double dv01 = (npvDown - npvUp) / 2.0;
            swap.dv01 = swap.swapType == org.quantlib.VanillaSwap.Type.Payer ? dv01 : -1.0 * dv01;
            LOG.info("swap DV01: " + swap.dv01);

            projectionTermStructure.linkTo(projectionCurve);
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
            }
            LOG.info("Sum of Buckets:" + sumBuckets);
        }

        return swap;
    }

    public Optional<Double> getFixingForDate(Date dt, String currency, String tenor) throws Exception{
        Optional<Double> ret = Optional.empty();
        List<Fixing> fixings = marketData.getFixingsMarketData(currency, tenor);
        Optional<Fixing> fixing = fixings.stream().filter(fix -> fix.date.serialNumber() == dt.serialNumber()).findFirst();
        if (fixing.isPresent())
            ret = Optional.of(fixing.get().rate);
        return ret;
    }
}
