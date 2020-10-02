package com.qlservices.util;

import org.quantlib.*;

public class EURConventions {

    public static final DayCounter CURVE_DAY_COUNTER = new Actual360();

    public static final int DEPOSIT_FIXING_DAYS = 2;
    public static final Calendar DEPOSIT_CALENDAR = new TARGET();
    public static final BusinessDayConvention DEPOSIT_BUSINESS_DAY_CONVENTION = BusinessDayConvention.ModifiedFollowing;
    public static final boolean DEPOSIT_END_OF_MONTH = false;
    public static final DayCounter DEPOSIT_DAY_COUNTER = new Actual360();

    public static final int LENGTH_IN_MONTHS = 3;
    public static final Calendar FUTURE_CALENDAR = new TARGET();
    public static final BusinessDayConvention FUTURE_BUSINESS_DAY_CONVENTION = BusinessDayConvention.ModifiedFollowing;
    public static final boolean FUTURE_END_OF_MONTH = false;
    public static final DayCounter FUTURE_DAY_COUNTER = new Actual360();

    public static final Calendar SWAP_FIXED_CALENDAR = new TARGET();
    public static final Frequency SWAP_FIXED_FREQUENCY = Frequency.Annual;
    public static final BusinessDayConvention SWAP_FIXED_CONVENTION = BusinessDayConvention.ModifiedFollowing;
    public static final DayCounter SWAP_FIXED_DAY_COUNTER = new Actual360();
    public static final org.quantlib.IborIndex SWAP_FLOATING_INDEX = new EURLibor(new Period("6M"));

}

