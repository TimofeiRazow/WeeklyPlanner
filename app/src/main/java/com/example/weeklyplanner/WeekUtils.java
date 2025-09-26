package com.example.weeklyplanner;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeekUtils {
    private static final String TAG = "WeekUtils";

    /**
     * Получает начало недели (понедельник 00:00:00)
     */
    public static Date getWeekStart(Calendar week) {
        Calendar weekStart = (Calendar) week.clone();

        // Устанавливаем на понедельник текущей недели
        int dayOfWeek = weekStart.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        weekStart.add(Calendar.DAY_OF_YEAR, -daysToSubtract);

        // Устанавливаем время на 00:00:00.000
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d(TAG, "Week start calculated: " + sdf.format(weekStart.getTime()));

        return weekStart.getTime();
    }

    /**
     * Получает конец недели (воскресенье 23:59:59)
     */
    public static Date getWeekEnd(Calendar week) {
        Calendar weekEnd = (Calendar) week.clone();

        // Устанавливаем на понедельник текущей недели
        int dayOfWeek = weekEnd.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
        weekEnd.add(Calendar.DAY_OF_YEAR, -daysToSubtract);

        // Добавляем 6 дней чтобы получить воскресенье
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);

        // Устанавливаем время на 23:59:59.999
        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
        weekEnd.set(Calendar.MINUTE, 59);
        weekEnd.set(Calendar.SECOND, 59);
        weekEnd.set(Calendar.MILLISECOND, 999);

        return weekEnd.getTime();
    }
}