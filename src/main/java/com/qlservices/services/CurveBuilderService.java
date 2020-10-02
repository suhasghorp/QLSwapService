package com.qlservices.services;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.qlservices.MarketData;
import com.qlservices.models.Quote;
import com.qlservices.util.EURConventions;
import com.qlservices.util.USDConventions;
import org.jboss.logging.Logger;
import org.quantlib.*;

@ApplicationScoped
public class CurveBuilderService {

    @Inject
    MarketData marketData;

    private static final Logger LOG = Logger.getLogger(CurveBuilderService.class);

    public YieldTermStructure buildDiscountCurve(String curveName, String currency) throws Exception {
        org.quantlib.Date qlToday = Settings.instance().getEvaluationDate();
        LocalDate javaToday = marketData.getEvaluationJavaDate();
        LOG.info("Building discount curve ");
        RelinkableYieldTermStructureHandle discountCurve = null;
        RateHelperVector rateHelpers = new RateHelperVector();
        Calendar cal = null;
        OvernightIndex overnightIndex = null;
        if (currency.equals("USD")) {
            cal = new UnitedStates();
            overnightIndex = new FedFunds();
        } else {
            cal = new TARGET();
            overnightIndex = new Eonia();
        }
        Date settlementDate = cal.advance(qlToday, 2, TimeUnit.Days);
        int settlementDays = settlementDate.serialNumber() - qlToday.serialNumber();

        //build discount curve
        List<Quote> quotes = marketData.getDiscountMarketData(marketData.getEvaluationJavaDate(), currency);
        for (Quote quote : quotes) {
            rateHelpers.add(new OISRateHelper(settlementDays, new Period(quote.tenor), new QuoteHandle(new SimpleQuote(quote.rate)), overnightIndex));
        }
        YieldTermStructure curve = new PiecewiseLogLinearDiscount(settlementDate, rateHelpers, overnightIndex.dayCounter());
        curve.enableExtrapolation();
        LOG.info("Finished building discount curve");
        return curve;
    }

    public YieldTermStructure buildProjectionCurve(String curveName, String currency, String tenor, YieldTermStructure discountCurve) throws Exception {
        LOG.info("building projection curve");
        RelinkableYieldTermStructureHandle discountTermStructure = new RelinkableYieldTermStructureHandle();
        YieldTermStructure projectionCurve = null;
        org.quantlib.Date qlToday = Settings.instance().getEvaluationDate();
        Calendar cal = null;

        if (currency.equals("USD")) {
            cal = new UnitedStates();
        } else {
            cal = new TARGET();
        }
        Date settlementDate = cal.advance(qlToday, 2, TimeUnit.Days);

        //build projection curve
        RateHelperVector rateHelpers = new RateHelperVector();
        //discountTermStructure.linkTo(buildDiscountCurve(curveName, currency));
        discountTermStructure.linkTo(discountCurve);
        List<Quote> quotes = marketData.getProjectionMarketData(marketData.getEvaluationJavaDate(), currency,tenor);
        for (Quote quote : quotes){
            String quoteType = quote.quoteName;
            String ten = quote.tenor;
            if (currency.equals("USD")) {
                rateHelpers.add(getUSDRateHelper(quoteType, ten, quote.simpleQuote, discountTermStructure));
            } else if (currency.equals("EUR")){
                rateHelpers.add(getEURRateHelper(quoteType, ten, quote.simpleQuote, discountTermStructure));
            }
        }

        if (currency.equals("USD")){
            projectionCurve = new PiecewiseLinearZero(settlementDate, rateHelpers, new USDConventions().CURVE_DAY_COUNTER());
            projectionCurve.enableExtrapolation();
        } else if (currency.equals("EUR")){
            projectionCurve = new PiecewiseLinearZero(settlementDate, rateHelpers, EURConventions.CURVE_DAY_COUNTER);
            projectionCurve.enableExtrapolation();
        }
        LOG.info("finsihed building projection curve");
        return projectionCurve;
    }

    private RateHelper getUSDRateHelper(String quoteType, String tenor, org.quantlib.Quote simpleQuote, RelinkableYieldTermStructureHandle discountCurve){
        RateHelper helper = null;

        if (quoteType.equals("Depos")){

            helper = new DepositRateHelper(new QuoteHandle(simpleQuote), new Period(tenor),
                    new USDConventions().DEPOSIT_FIXING_DAYS(),
                    new USDConventions().DEPOSIT_CALENDAR(),
                    new USDConventions().DEPOSIT_BUSINESS_DAY_CONVENTION(),
                    new USDConventions().DEPOSIT_END_OF_MONTH(),
                    new USDConventions().DEPOSIT_DAY_COUNTER()
            );

        } else if (quoteType.equals("Futures")){
            Date iborStartDate = IMM.nextDate(Settings.instance().getEvaluationDate().add(new Period(tenor)));

            helper = new FuturesRateHelper(new QuoteHandle(simpleQuote), iborStartDate,
                    new USDConventions().LENGTH_IN_MONTHS(),
                    new USDConventions().FUTURE_CALENDAR(),
                    new USDConventions().FUTURE_BUSINESS_DAY_CONVENTION(),
                    new USDConventions().FUTURE_END_OF_MONTH(),
                    new USDConventions().FUTURE_DAY_COUNTER()
            );

        } else if (quoteType.equals("Swaps")){
            helper = new SwapRateHelper(new QuoteHandle(simpleQuote), new Period(tenor),
                    new USDConventions().SWAP_FIXED_CALENDAR(),
                    new USDConventions().SWAP_FIXED_FREQUENCY(),
                    new USDConventions().SWAP_FIXED_CONVENTION(),
                    new USDConventions().SWAP_FIXED_DAY_COUNTER(),
                    new USDConventions().SWAP_FLOATING_INDEX(),
                    new QuoteHandle(),new Period(0, TimeUnit.Days), discountCurve
                    );

        }

        return helper;
    }

    private RateHelper getEURRateHelper(String quoteType, String tenor, org.quantlib.Quote simpleQuote, RelinkableYieldTermStructureHandle discountCurve){
        RateHelper helper = null;
        if (quoteType.equals("Depos")){
            helper = new DepositRateHelper(new QuoteHandle(simpleQuote), new Period(tenor),
                    EURConventions.DEPOSIT_FIXING_DAYS,
                    EURConventions.DEPOSIT_CALENDAR,
                    EURConventions.DEPOSIT_BUSINESS_DAY_CONVENTION,
                    EURConventions.DEPOSIT_END_OF_MONTH,
                    EURConventions.DEPOSIT_DAY_COUNTER
            );
        } else if (quoteType.equals("Futures")){
            Date iborStartDate = IMM.nextDate(Settings.instance().getEvaluationDate().add(new Period(tenor)));
            helper = new FuturesRateHelper(new QuoteHandle(simpleQuote), iborStartDate,EURConventions.LENGTH_IN_MONTHS,
                    EURConventions.FUTURE_CALENDAR,
                    EURConventions.FUTURE_BUSINESS_DAY_CONVENTION,
                    EURConventions.FUTURE_END_OF_MONTH,
                    EURConventions.FUTURE_DAY_COUNTER
            );
        } else if (quoteType.equals("Swaps")){
            helper = new SwapRateHelper(new QuoteHandle(simpleQuote), new Period(tenor),
                    EURConventions.SWAP_FIXED_CALENDAR,
                    EURConventions.SWAP_FIXED_FREQUENCY,
                    EURConventions.SWAP_FIXED_CONVENTION,
                    EURConventions.SWAP_FIXED_DAY_COUNTER,
                    EURConventions.SWAP_FLOATING_INDEX,
                    new QuoteHandle(),new Period(0, TimeUnit.Days), discountCurve);
        }
        return helper;
    }
}
