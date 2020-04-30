package yzw.ahaqth.calculatehelper.tools;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {
    private static DateTimeFormatter yyyyMd_EEEE_Formatter ;
    private static DateTimeFormatter yyyyMd_Formatter;
    private static DateTimeFormatter yyyyM_Formatter;
    private static DateTimeFormatter yyyyMdHHmmss_Formatter;
    private static DateTimeFormatter HHmmss_Formatter;

    private final static Locale locale = Locale.CHINA;

    public static DateTimeFormatter getYyyyMd_EEEE_Formatter(){
        if(yyyyMd_EEEE_Formatter == null)
            yyyyMd_EEEE_Formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 EEEE",locale);
        return yyyyMd_EEEE_Formatter;
    }

    public static DateTimeFormatter getYyyyMd_Formatter(){
        if(yyyyMd_Formatter == null)
            yyyyMd_Formatter = DateTimeFormatter.ofPattern("yyyy年M月d日",locale);
        return yyyyMd_Formatter;
    }

    public static DateTimeFormatter getYyyyM_Formatter(){
        if(yyyyM_Formatter == null)
            yyyyM_Formatter = DateTimeFormatter.ofPattern("yyyy年M月",locale);
        return yyyyM_Formatter;
    }

    public static DateTimeFormatter getYyyyMdHHmmss_Formatter(){
        if(yyyyMdHHmmss_Formatter == null)
            yyyyMdHHmmss_Formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm:ss",locale);
        return yyyyMdHHmmss_Formatter;
    }

    public static DateTimeFormatter getHHmmss_Formatter(){
        if(HHmmss_Formatter == null)
            HHmmss_Formatter = DateTimeFormatter.ofPattern("HH:mm:ss",locale);
        return HHmmss_Formatter;
    }
}
