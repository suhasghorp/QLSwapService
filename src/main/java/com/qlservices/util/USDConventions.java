package com.qlservices.util;

import org.quantlib.*;

public class USDConventions {

    public int DEPOSIT_FIXING_DAYS(){return 2;}
    public Calendar DEPOSIT_CALENDAR(){return new UnitedStates();}
    public BusinessDayConvention DEPOSIT_BUSINESS_DAY_CONVENTION(){return BusinessDayConvention.ModifiedFollowing;}
    public boolean DEPOSIT_END_OF_MONTH(){return false;}
    public DayCounter DEPOSIT_DAY_COUNTER(){return new Actual360();}

    public int LENGTH_IN_MONTHS(){ return 3;}
    public Calendar FUTURE_CALENDAR(){return new UnitedStates();}
    public BusinessDayConvention FUTURE_BUSINESS_DAY_CONVENTION(){return BusinessDayConvention.ModifiedFollowing;}
    public boolean FUTURE_END_OF_MONTH(){return false;}
    public DayCounter FUTURE_DAY_COUNTER(){return new Actual360();}

    public Calendar SWAP_FIXED_CALENDAR(){return new UnitedStates();}
    public Calendar SWAP_FLOATING_CALENDAR(){return new UnitedStates();}
    public Frequency SWAP_FIXED_FREQUENCY(){return Frequency.Semiannual;}
    public Frequency SWAP_FLOATING_FREQUENCY(){return Frequency.Quarterly;}
    public BusinessDayConvention SWAP_FIXED_CONVENTION(){return  BusinessDayConvention.ModifiedFollowing;}
    public BusinessDayConvention SWAP_FLOATING_CONVENTION(){return BusinessDayConvention.ModifiedFollowing;}
    public DayCounter SWAP_FIXED_DAY_COUNTER(){return new Thirty360();}
    public DayCounter SWAP_FLOATING_DAY_COUNTER(){return new Actual360();}
    public org.quantlib.IborIndex SWAP_FLOATING_INDEX(){return new USDLibor(new Period("3M"));}

    public USDConventions() {}

    public DayCounter CURVE_DAY_COUNTER(){
        return new Actual360();
    }


}
