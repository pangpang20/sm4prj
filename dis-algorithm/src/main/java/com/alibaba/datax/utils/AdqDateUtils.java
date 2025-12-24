package com.alibaba.datax.utils;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/** 
 * 时间工具类
* @author liudaizhong.liu
* @date 2019年11月6日 下午5:28:39 
* @desc 
*/
public class AdqDateUtils extends org.apache.commons.lang3.time.DateUtils {
	/**
     * 日期格式，年份，例如：2004，2008 
     */  
    public static final String DATE_FORMAT_YYYY = "yyyy";  
    // ==格式到年月 ==   
    /** 
     * 日期格式，年份和月份，例如：200707，200808 
     */  
    public static final String DATE_FORMAT_YYYYMM = "yyyyMM";  
    /** 
     * 日期格式，年份和月份，例如：200707，2008-08 
     */  
    public static final String DATE_FORMAT_YYYY_MM = "yyyy-MM";  

    /** 
     * 日期格式，年月日，例如：20050630，20080808 
     */  
    public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";  
    /** 
     * 日期格式，年月日，用横杠分开，例如：2006-12-25，2008-08-08 
     */  
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";  
    /** 
     * 日期格式，年月日，用斜杠分开，例如：2006/12/25，2008/08/08 
     */
    public static final String DATE_FORMAT_SLASH_YYYY_MM_DD = "yyyy/MM/dd";

    /** 
     * 日期格式，年月日时分，例如：20001230 12:00，20080808 20:08 
     */  
    public static final String DATE_TIME_FORMAT_YYYYMMDD_HH_MI = "yyyyMMdd HH:mm";  
    /** 
     * 日期格式，年/月/日时分，例如：2000/12/30 12:00，2008/08/08 20:08 
     */  
    public static final String DATE_TIME_FORMAT_SLASH_YYYY_MM_DD_HH_MI = "yyyy/MM/dd HH:mm";  
    /** 
     * 日期格式，年月日时分，例如：2000-12-30 12:00，2008-08-08 20:08 
     */  
    public static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI = "yyyy-MM-dd HH:mm";  
    // ==格式到年月日 时分秒==   
    /** 
     * 日期格式，年月日时分秒，例如：20001230120000，20080808200808 
     */  
    public static final String DATE_TIME_FORMAT_YYYYMMDDHHMISS = "yyyyMMddHHmmss";  
    /** 
     * 日期格式，年/月/日 时分秒，年月日用横杠分开，时分秒用冒号分开 
     * 例如：2005/05/10 23：20：00，2008/08/08 20:08:08 
     */  
    public static final String DATE_TIME_FORMAT_SLASH_YYYY_MM_DD_HH_MI_SS = "yyyy/MM/dd HH:mm:ss";  
    /** 
     * 日期格式，年月日时分秒，年月日用横杠分开，时分秒用冒号分开 
     * 例如：2005-05-10 23：20：00，2008-08-08 20:08:08 
     */  
    public static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS = "yyyy-MM-dd HH:mm:ss";  
    // ==格式到年月日 时分秒 毫秒==   
    /** 
     * 日期格式，年月日时分秒毫秒，例如：20001230120000123，20080808200808456 
     */
    public static final String DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS = "yyyyMMddHHmmssSSS";


    /** 
     * 格式化Date时间 
     * @param time Date类型时间 
     * @param timeFromat String类型格式 
     * @return 格式化后的字符串
     */
    public static String parseDateToStr(Date time, String timeFromat) {
        DateFormat dateFormat = new SimpleDateFormat(timeFromat);
        return dateFormat.format(time);
    }

    /** 
     * 格式化String时间 
     * @param time String类型时间 
     * @param timeFromat String类型格式 
     * @return 格式化后的Date日期
     */
    public static Date parseStrToDate(String time, String timeFromat) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        Date date = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat(timeFromat);
            date = dateFormat.parse(time);
        } catch (Exception e) {
        }
        return date;
    }


    /** 
     * 通用所有时间格式类型的字符串转成时间类型
     * 当strTime为2008-9时返回为2008-9-1 00:00格式日期时间，无法转换返回null. 
     * @param strTime 时间字符
     * @return 时间
     */
    public static Date strToDate(String strTime) {
        if (strTime == null || strTime.trim().length() <= 0)
            return null;

        Date date = null;
        List<String> list = new ArrayList<>(8);
        list.add(DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS);
        list.add(DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS);  
        list.add(DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI);  
        list.add(DATE_TIME_FORMAT_YYYYMMDD_HH_MI);  
        list.add(DATE_TIME_FORMAT_YYYYMMDDHHMISS);  
        list.add(DATE_FORMAT_YYYY_MM_DD);  
        list.add(DATE_FORMAT_YYYYMMDD);  
        list.add(DATE_FORMAT_YYYY_MM);  
        list.add(DATE_FORMAT_YYYYMM);  
        list.add(DATE_FORMAT_YYYY);  
        list.add(DATE_FORMAT_SLASH_YYYY_MM_DD);  
        list.add(DATE_TIME_FORMAT_SLASH_YYYY_MM_DD_HH_MI);
        list.add(DATE_TIME_FORMAT_SLASH_YYYY_MM_DD_HH_MI_SS);

        for (Iterator<String> iter = list.iterator(); iter.hasNext(); ) {
            String format = iter.next();
            if (strTime.contains("-") && !format.contains("-"))
                continue;
            if (!strTime.contains("-") && format.contains("-"))
                continue;  
            if(strTime.length()>format.length())  
                continue;  
            date = parseStrToDate(strTime, format);  
            if (date != null)  
                break;  
        }  
  
        return date;  
    }
}
