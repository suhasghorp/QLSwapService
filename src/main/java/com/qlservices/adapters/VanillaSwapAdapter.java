package com.qlservices.adapters;

import com.qlservices.models.VanillaSwap;
import com.qlservices.util.USDConventions;
import com.qlservices.util.Utils;
import org.jboss.logging.Logger;
import org.quantlib.DateGeneration;

import javax.json.*;
import javax.json.bind.adapter.JsonbAdapter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VanillaSwapAdapter implements JsonbAdapter<VanillaSwap, JsonObject> {
    private static final Logger LOG = Logger.getLogger(VanillaSwapAdapter.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    @Override
    public JsonObject adaptToJson(VanillaSwap vanillaSwap) throws Exception {
        JsonObjectBuilder builder =  Json.createObjectBuilder()
                .add("id", vanillaSwap.ID)
                .add("npv", roundAvoid(vanillaSwap.netPresentValue,6))
                .add ("fair rate", roundAvoid(vanillaSwap.fairRate,6));
        if (vanillaSwap.fullResults){
            builder.add ("DV01", roundAvoid(vanillaSwap.dv01,6));
            JsonObjectBuilder bucketsBuilder = Json.createObjectBuilder();
            vanillaSwap.bucketedDV01.forEach((K,V) -> bucketsBuilder.add(K,roundAvoid(V,6)));
            builder.add("Bucketed DV01", bucketsBuilder.build());
        }
        return builder.build();
    }

    private static double roundAvoid(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    @Override
    public VanillaSwap adaptFromJson(JsonObject jsonObject) throws Exception {
        LOG.info(jsonObject.toString());
        //System.loadLibrary("QuantLibJNI");
        VanillaSwap swap = new VanillaSwap();
        swap.ID = jsonObject.getString("ID");
        swap.swapType = jsonObject.getString("swapType").toUpperCase().equals("PAYER")? org.quantlib.VanillaSwap.Type.Payer : org.quantlib.VanillaSwap.Type.Receiver;
        swap.nominal = jsonObject.getJsonNumber("nominal").doubleValue();
        swap.startDate = Utils.javaDateToQLDate(LocalDate.parse(jsonObject.getString("startDate"),formatter));
        swap.maturityDate = Utils.javaDateToQLDate(LocalDate.parse(jsonObject.getString("maturityDate"),formatter));
        swap.fixedLegRate = jsonObject.getJsonNumber("fixedLegRate").doubleValue();
        swap.floatingLegSpread = jsonObject.getJsonNumber("floatingLegSpread").doubleValue();
        swap.fullResults = jsonObject.containsKey("fullResults") && jsonObject.getBoolean("fullResults") == true ? true : false;
        if (jsonObject.containsKey("convention")){
            if (jsonObject.getString("convention").equals("USD")){
                swap.fixedLegFrequency = new USDConventions().SWAP_FIXED_FREQUENCY();
                swap.fixedLegCalendar = new USDConventions().SWAP_FIXED_CALENDAR();
                swap.fixedLegConvention = new USDConventions().SWAP_FIXED_CONVENTION();
                swap.fixedLegDateGenerationRule = DateGeneration.Rule.Backward;
                swap.fixedLegDayCount = new USDConventions().SWAP_FIXED_DAY_COUNTER();

                swap.floatingLegFrequency = new USDConventions().SWAP_FLOATING_FREQUENCY();
                swap.floatingLegCalendar = new USDConventions().SWAP_FLOATING_CALENDAR();
                swap.floatingLegConvention = new USDConventions().SWAP_FLOATING_CONVENTION();
                swap.floatingLegDateGenerationRule = DateGeneration.Rule.Backward;
                swap.floatingLegDayCount = new USDConventions().SWAP_FLOATING_DAY_COUNTER();

            } else {

                swap.fixedLegFrequency = Utils.getFrequency(jsonObject.getString("fixedLegFrequency"));
                swap.fixedLegCalendar = Utils.getCalendar(jsonObject.getString("fixedLegCalendar"));
                swap.fixedLegConvention = Utils.getBusDayConvention(jsonObject.getString("fixedLegConvention"));
                swap.fixedLegDateGenerationRule = jsonObject.getString("fixedLegDateGenerationRule").equals("BACKWARD")? DateGeneration.Rule.Backward : DateGeneration.Rule.Forward;
                swap.fixedLegDayCount = Utils.getDayCounter(jsonObject.getString("fixedLegDayCount"));

                swap.floatingLegFrequency = Utils.getFrequency(jsonObject.getString("floatingLegFrequency"));
                swap.floatingLegCalendar = Utils.getCalendar(jsonObject.getString("floatingLegCalendar"));
                swap.floatingLegConvention = Utils.getBusDayConvention(jsonObject.getString("floatingLegConvention"));
                swap.floatingLegDateGenerationRule = jsonObject.getString("floatingLegDateGenerationRule").equals("BACKWARD")? DateGeneration.Rule.Backward : DateGeneration.Rule.Forward;
                swap.floatingLegDayCount = Utils.getDayCounter(jsonObject.getString("floatingLegDayCount"));
            }
        }



        return swap;
    }
}
