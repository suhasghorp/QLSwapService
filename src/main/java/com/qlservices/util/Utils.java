package com.qlservices.util;

import org.quantlib.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static final Map<Integer, Month> monthMap = new HashMap<>() {{
        put(1, Month.January);
        put(2, Month.February);
        put(3, Month.March);
        put(4, Month.April);
        put(5, Month.May);
        put(6, Month.June);
        put(7, Month.July);
        put(8, Month.August);
        put(9, Month.September);
        put(10, Month.October);
        put(11, Month.November);
        put(12, Month.December);
    }};

    public static final Map<Month,Integer> monthReversedMap = new HashMap<>() {{
        put(Month.January,1 );
        put(Month.February, 2);
        put(Month.March, 3);
        put(Month.April, 4);
        put(Month.May,5 );
        put(Month.June, 6);
        put(Month.July, 7);
        put(Month.August, 8);
        put(Month.September, 9);
        put(Month.October, 10);
        put(Month.November, 11);
        put(Month.December, 12);
    }};

    public static Date javaDateToQLDate(LocalDate dt) {
        return new Date(dt.getDayOfMonth(), monthMap.get(dt.getMonthValue()), dt.getYear());
    }

    public static LocalDate qlDateToJavaDate(Date qlDate){
        return LocalDate.of(qlDate.year(), monthReversedMap.get(qlDate.month()), qlDate.dayOfMonth());
    }

    public static org.quantlib.BusinessDayConvention getBusDayConvention(String conv) {
        BusinessDayConvention ret = BusinessDayConvention.Unadjusted;
        switch (conv.toUpperCase()) {
            case "FOLLOWING":
                ret = BusinessDayConvention.Following;
                break;
            case "MODIFIEDFOLLOWING":
                ret = BusinessDayConvention.ModifiedFollowing;
                break;
            case "PRECEDING":
                ret = BusinessDayConvention.Preceding;
                break;
            case "MODIFIEDPRECEDING":
                ret = BusinessDayConvention.ModifiedPreceding;
                break;
            case "UNADJUSTED":
                ret = BusinessDayConvention.Unadjusted;
                break;
            default:
                ret = BusinessDayConvention.Unadjusted;
                break;
        }
        ;
        return ret;
    }

    public static org.quantlib.Calendar getCalendar(String cal) {
        Calendar ret = new UnitedStates();
        switch (cal.toUpperCase()) {
            case "TARGET":
                ret = new TARGET();
                break;
            case "UNITEDSTATES":
                ret = new UnitedStates();
                break;
            case "UNITEDKINGDOM":
                ret = new UnitedKingdom();
                break;
            default:
                ret = new UnitedStates();
                break;
        }
        return ret;
    }

    public static Frequency getFrequency(String freq) {
        Frequency ret = Frequency.Annual;
        switch (freq.toUpperCase()) {
            case "DAILY":
                ret = Frequency.Daily;
                break;
            case "WEEKLY":
                ret = Frequency.Weekly;
                break;
            case "MONTHLY":
                ret = Frequency.Monthly;
                break;
            case "QUARTERLY":
                ret = Frequency.Quarterly;
                break;
            case "SEMIANNUAL":
                ret = Frequency.Semiannual;
                break;
            case "ANNUAL":
                ret = Frequency.Annual;
                break;
            default:
                ret = Frequency.Annual;
                break;
        }
        return ret;
    }

    public static DayCounter getDayCounter(String dcount) {
        DayCounter ret = new Actual360();
        switch (dcount.toUpperCase()) {
            case "ACTUAL360":
                ret = new Actual360();
                break;
            case "ACTUAL365FIXED":
                ret = new Actual365Fixed();
                break;
            case "ACTUALACTUAL":
                ret = new ActualActual();
                break;
            case "BUSINESS252":
                ret = new Business252();
                break;
            case "ONEDAYCOUNTER":
                ret = new OneDayCounter();
                break;
            case "SIMPLEDAYCOUNTER":
                ret = new SimpleDayCounter();
                break;
            case "THIRTY360":
                ret = new Thirty360();
                break;
            default:
                ret = new Actual360();
                break;
        }
        return ret;
    }
}
