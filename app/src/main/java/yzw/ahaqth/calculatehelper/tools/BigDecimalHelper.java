package yzw.ahaqth.calculatehelper.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalHelper {
    private static BigDecimal getBigDecimal(double value){
        return new BigDecimal(String.valueOf(value));
    }

    public static double round(double value, int flag){
        return getBigDecimal(value).setScale(flag, RoundingMode.HALF_UP).doubleValue();
    }

    public static double add(double d1,double d2){
        return getBigDecimal(d1).add(getBigDecimal(d2)).doubleValue();
    }

    public static double add(double d1,double d2,int flag){
        return round(add(d1,d2),flag);
    }

    public static double minus(double d1,double d2){
        return add(d1,-d2);
    }

    public static double minus(double d1,double d2,int flag){
        return add(d1,-d2,flag);
    }

    public static double multiply(double d1,double d2){
        return getBigDecimal(d1).multiply(getBigDecimal(d2)).doubleValue();
    }

    public static double multiply(double d1,double d2,int flag){
        return round(multiply(d1,d2),flag);
    }

    public static double multiplyOnFloor(double d1,double d2){
        return getBigDecimal(d1).multiply(getBigDecimal(d2)).setScale(0,RoundingMode.FLOOR).doubleValue();
    }

    public static double divide(double d1,double d2){
        return divide(d1,d2,6);
    }

    public static double divideOnFloor(double d1,double d2){
        return getBigDecimal(d1).divide(getBigDecimal(d2),0,RoundingMode.FLOOR).doubleValue();
    }

    public static double divide(double d1,double d2,int flag){
        return getBigDecimal(d1).divide(getBigDecimal(d2),flag,RoundingMode.HALF_UP).doubleValue();
    }

    public static double addAll(int flag,double...values){
        if(values.length == 0)
            return 0;
        double result = values[0];
        for(int i = 1;i<values.length;i++){
            result = add(result,values[i],flag);
        }
        return result;
    }

    public static double multiplyAll(int flag,double...values){
        if(values.length == 0)
            return 0;
        double result = values[0];
        for(int i = 1;i<values.length;i++){
            double value = values[i];
            if(value == 0)
                return 0;
            result = multiply(result,value,flag);
        }
        return result;
    }

    public static int compare(double d1,double d2){
        return getBigDecimal(d1).compareTo(getBigDecimal(d2));
    }
}
