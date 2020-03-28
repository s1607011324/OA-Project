package com.oa.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CountDataTime {
    public static String getDatePoor(Date nowDate, Date endDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        if (hour==0){
            return day+"天整";
        }
        if (hour<=4&&hour>0) {
            return day+"天半";
        }
        if (day==0||hour >4) {
            return day+1+"天整";
        }
        System.out.println(day + "天" + hour + "小时" + min + "分钟");
        return day+"天"+hour+"时";
    }


    /**
     * 时分秒转换成年月
     * @param date
     * @return
     */
    public static Date getValidDate(Date date){
        Date dates = null;
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyy-MM-dd");
        try {
            String s = sdf1.format(date);
            dates = sdf1.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dates;
    }
}
