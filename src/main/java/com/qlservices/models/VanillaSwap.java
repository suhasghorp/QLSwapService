package com.qlservices.models;

import com.qlservices.adapters.VanillaSwapAdapter;
import org.quantlib.*;

import javax.json.bind.annotation.JsonbTypeAdapter;
import java.util.LinkedHashMap;
import java.util.Map;


@JsonbTypeAdapter(VanillaSwapAdapter.class)
public class VanillaSwap {
    public String ID;
    public boolean fullResults = false;
    public org.quantlib.VanillaSwap.Type swapType;
    public double nominal;
    public org.quantlib.Date startDate, maturityDate;
    public org.quantlib.Frequency fixedLegFrequency,floatingLegFrequency;
    public org.quantlib.Calendar fixedLegCalendar,floatingLegCalendar;
    public org.quantlib.BusinessDayConvention fixedLegConvention,floatingLegConvention;
    public DateGeneration.Rule fixedLegDateGenerationRule,floatingLegDateGenerationRule;
    public double fixedLegRate;
    public org.quantlib.DayCounter fixedLegDayCount,floatingLegDayCount;
    public double floatingLegSpread;
    public double dv01, netPresentValue, fairRate;
    public org.quantlib.VanillaSwap qlSwap;
    public Schedule fixedLegSchedule, floatingLegSchedule;
    public Map<String,Double> bucketedDV01 = new LinkedHashMap<>();


    public VanillaSwap(){}

    public void setPricingEngine(DiscountingSwapEngine engine, IborIndex floatingLegIborIndex){
        fixedLegSchedule = new Schedule(startDate,maturityDate,new Period(fixedLegFrequency),
                fixedLegCalendar,fixedLegConvention, fixedLegConvention,fixedLegDateGenerationRule,false);
        floatingLegSchedule = new Schedule(startDate,maturityDate,new Period(floatingLegFrequency),
                floatingLegCalendar,floatingLegConvention, floatingLegConvention,floatingLegDateGenerationRule,false);
        qlSwap = new org.quantlib.VanillaSwap(swapType,nominal,
                fixedLegSchedule,fixedLegRate,fixedLegDayCount,
                floatingLegSchedule,floatingLegIborIndex,floatingLegSpread,floatingLegDayCount);
        qlSwap.setPricingEngine(engine);
    }

    public Schedule getFloatingSchedule(){
        return floatingLegSchedule;
    }

    public Schedule getFixedSchedule(){
        return fixedLegSchedule;
    }

    public double fairRate(){
        return qlSwap.fairRate();
    }

    public double npv(){
        return qlSwap.NPV();
    }

}
