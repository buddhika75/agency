package com.divudi.bean.common;

import com.divudi.data.Sex;
import com.divudi.data.Title;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author Dr. M H B Ariyaratne
 */
@Named(value = "commonFunctionsController")
@ApplicationScoped
public class CommonFunctionsController {


    public boolean checkToDateAreInSameDay(Date firstDate, Date secondDate) {
        Date startOfDay = getStartOfDay(firstDate);
        Date endOfDay = getEndOfDay(firstDate);
        if (startOfDay.before(secondDate) && endOfDay.after(secondDate)) {
            return true;
        } else {
            return false;
        }
    }

    public Date getAddedDate(Date date, int range) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, range);
        Date returnDate = cal.getTime();
        return returnDate;
    }

    public Long getDayCountTillNow(Date date) {
        if (date == null) {
            return 0l;
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        Long inDays = (cal1.getTimeInMillis() - cal2.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        return inDays;
    }

    public Long getDayCount(Date frm, Date to) {
        if (frm == null) {
            return 0l;
        }
        if (to == null) {
            to = new Date();
        }
        to = getEndOfDay(to);
        frm = getStartOfDay(frm);
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(to);
        cal2.setTime(frm);
        Long inDays = (cal1.getTimeInMillis() - cal2.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        return inDays;
    }

    public Date guessDob(String docStr) {
        try {
            int years = Integer.valueOf(docStr);
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("IST"));
            now.add(Calendar.YEAR, -years);
            return now.getTime();
        } catch (Exception e) {
            return new Date();
        }
    }

    public Date guessDobFromMonth(String docStr) {
        try {
            int month = Integer.valueOf(docStr);
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("IST"));
            now.add(Calendar.MONTH, -month);
            return now.getTime();
        } catch (Exception e) {
            return new Date();
        }
    }

    public String calculateAge(Date dob, Date toDate) {
        if (dob == null || toDate == null) {
            return "";
        }
        long ageInDays;
        ageInDays = (toDate.getTime() - dob.getTime()) / (1000 * 60 * 60 * 24);
        if (ageInDays < 0) {
            return "";
        }
        if (ageInDays < 60) {
            return ageInDays + " days";
        } else if (ageInDays < 366) {
            return (ageInDays / 30) + " months";
        } else {
            return (ageInDays / 365) + " years";
        }
    }

    public long calculateAgeInDays(Date dob, Date toDate) {
        if (dob == null || toDate == null) {
            return 0l;
        }
        long ageInDays;
        ageInDays = (toDate.getTime() - dob.getTime()) / (1000 * 60 * 60 * 24);
        if (ageInDays < 0) {
            ageInDays = 0;
        }
        return ageInDays;
    }

    public long calculateDurationTime(Date dob, Date toDate) {
        if (dob == null || toDate == null) {
            return 0;
        }
        long durationHours;
        durationHours = (toDate.getTime() - dob.getTime()) / (1000 * 60 * 60);
        return durationHours;
    }

    public long calculateDurationMin(Date dob, Date toDate) {
        if (dob == null || toDate == null || dob.getTime() > toDate.getTime()) {
            return 0;
        }
        long dMin;
        dMin = (toDate.getTime() - dob.getTime()) / (1000 * 60);
        return dMin;
    }

    public Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        ////// // System.out.println("calendar.getTime() = " + calendar.getTime());
        return calendar.getTime();
    }

    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        ////// // System.out.println("calendar.getTime() = " + calendar.getTime());
        return calendar.getTime();
    }

    public static Date getStartOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, 1, 0, 0, 0);
        return calendar.getTime();
    }

    public static Date getFirstDayOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 1);
        //////// // System.out.println("First : " + cal.getTime());
        return cal.getTime();
    }

    public static Date getLastDayOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, 31);
        //////// // System.out.println("Last : " + cal.getTime());
        return cal.getTime();
    }

    public Date getEndOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        //      int day = calendar.get(Calendar.DATE);
//        if (month == Calendar.DECEMBER) {
//            calendar.set(year + 1, 1, 1, 0, 0, 0);
//        } else {
//            calendar.set(year, month + 1, 1, 0, 0, 0);
//        }
        calendar.set(year, month, 1, 0, 0, 0);
        //System.err.println("DDDD " + calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        //System.err.println("FFFF " + calendar.getTime());
        return calendar.getTime();
    }

    public Date getBeginningOfMonth(Date date) {
        if (date == null) {
            date = new Date();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MINUTE, -1);
        return calendar.getTime();
    }

    public Date getFirstDayOfWeek(Date date) {
        // get today and clear time of day
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

// get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        //      //////// // System.out.println("Start of this week:       " + cal.getTime());
        //       //////// // System.out.println("... in milliseconds:      " + cal.getTimeInMillis());

// start of the next week
//        cal.add(Calendar.WEEK_OF_YEAR, 1);
//        //////// // System.out.println("Start of the next week:   " + cal.getTime());
//        //////// // System.out.println("... in milliseconds:      " + cal.getTimeInMillis());
        return cal.getTime();
    }

    public Date getLastDayOfWeek(Date date) {
        // get today and clear time of day
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

// get start of this week in milliseconds
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        //   //////// // System.out.println("Start of this week:       " + cal.getTime());
        //     //////// // System.out.println("... in milliseconds:      " + cal.getTimeInMillis());

        cal.add(Calendar.DATE, 7);

// start of the next week
//        cal.add(Calendar.WEEK_OF_YEAR, 1);
//        //////// // System.out.println("Start of the next week:   " + cal.getTime());
//        //////// // System.out.println("... in milliseconds:      " + cal.getTimeInMillis());
        return cal.getTime();
    }

    public static Date removeTime(Date date) {
        Calendar cal;
        cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //      System.err.println("Only Date " + cal.getTime());
        return cal.getTime();
    }

    public Boolean checkAgeSex(Date dob, Sex sex, Title title) {
        Boolean result = true;
        Date toDate = Calendar.getInstance().getTime();

        long age;

        if ((toDate.getTime() - dob.getTime()) / (1000 * 60 * 60 * 24) == 0) {
            return false;
        }

        age = ((toDate.getTime() - dob.getTime()) / (1000 * 60 * 60 * 24)) / 365;

        if (title == Title.Baby || title == Title.Baby_Of) {
            if (age > 6) {
                result = false;
            }
        } else if ((title == Title.Master)) {
            if (age > 13) {
                result = false;
            }
        }

        if (title == Title.Mrs
                || title == Title.Mrs
                || title == Title.Ms
                || title == Title.Miss
                || title == Title.DrMrs
                || title == Title.DrMiss) {

            if (sex == Sex.Male) {
                result = false;
            }
        }

        if (title == Title.Mr
                || title == Title.Master
                || title == Title.Dr) {
            if (sex == Sex.Female) {
                result = false;
            }
        }

        return result;
    }

    /**
     * Creates a new instance of CommonFunctionsController
     */
    public CommonFunctionsController() {
    }

}
