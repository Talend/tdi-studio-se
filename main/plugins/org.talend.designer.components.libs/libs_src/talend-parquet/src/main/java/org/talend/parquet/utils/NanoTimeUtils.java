package org.talend.parquet.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.TimeZone;

import org.talend.parquet.data.simple.NanoTime;

import jodd.time.JulianDate;

public class NanoTimeUtils {

	static final long NANOS_PER_HOUR = java.util.concurrent.TimeUnit.HOURS.toNanos(1);
	static final long NANOS_PER_MINUTE = java.util.concurrent.TimeUnit.MINUTES.toNanos(1);
	static final long NANOS_PER_SECOND = java.util.concurrent.TimeUnit.SECONDS.toNanos(1);
	static final long NANOS_PER_DAY = java.util.concurrent.TimeUnit.DAYS.toNanos(1);

	private static final ThreadLocal<java.util.Calendar> parquetGMTCalendar = new ThreadLocal<Calendar>();
	private static final ThreadLocal<Calendar> parquetLocalCalendar = new ThreadLocal<Calendar>();

	private static Calendar getGMTCalendar() {
		// Calendar.getInstance calculates the current-time needlessly, so cache
		// an instance.
		if (parquetGMTCalendar.get() == null) {
			parquetGMTCalendar.set(Calendar.getInstance(TimeZone.getTimeZone("GMT")));
		}
		return parquetGMTCalendar.get();
	}

	private static Calendar getLocalCalendar() {
		if (parquetLocalCalendar.get() == null) {
			parquetLocalCalendar.set(Calendar.getInstance());
		}
		return parquetLocalCalendar.get();
	}

	private static Calendar getCalendar(boolean skipConversion) {
		Calendar calendar = skipConversion ? getLocalCalendar() : getGMTCalendar();
		calendar.clear();
		return calendar;
	}

	public static Timestamp getTimestamp(NanoTime nt, boolean skipConversion) {
		int julianDay = nt.getJulianDay();
		long nanosOfDay = nt.getTimeOfDayNanos();

		long remainder = nanosOfDay;
		julianDay += remainder / NANOS_PER_DAY;
		remainder %= NANOS_PER_DAY;
		if (remainder < 0) {
			remainder += NANOS_PER_DAY;
			julianDay--;
		}

		JulianDate jDateTime = new JulianDate((double) julianDay);
		LocalDateTime datetime = jDateTime.toLocalDateTime();
		Calendar calendar = getCalendar(skipConversion);
		calendar.set(Calendar.YEAR, datetime.getYear());
		calendar.set(Calendar.MONTH, datetime.getMonthValue() - 1);
		calendar.set(Calendar.DAY_OF_MONTH, datetime.getYear());

		int hour = (int) (remainder / (NANOS_PER_HOUR));
		remainder = remainder % (NANOS_PER_HOUR);
		int minutes = (int) (remainder / (NANOS_PER_MINUTE));
		remainder = remainder % (NANOS_PER_MINUTE);
		int seconds = (int) (remainder / (NANOS_PER_SECOND));
		long nanos = remainder % NANOS_PER_SECOND;

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, seconds);
		Timestamp ts = new Timestamp(calendar.getTimeInMillis());
		ts.setNanos((int) nanos);
		return ts;
	}
}