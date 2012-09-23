package net.formula97.andorid.car_kei_bo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.text.format.Time;
import android.util.Log;

/**
 * 日付処理に関するメソッドを管理するクラス
 * @author f97one
 *
 */
public class DateManager {

	// 便宜上表記が必要なときに使用する、特殊な境界線上の時刻表記
	static final String START_HOUR = "00:00:00";
	static final String END_HOUR = "23:59:59";

	public DateManager() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	/**
	 * ISO 8601形式の日付、および時刻フォーマットを持つ「文字列」を返す。
	 * @param gcd Calendar型（日付）
	 * @param withTime  boolean型、時刻が必要なときはtrue、不要なときはfalse
	 * @return ISO 8601形式の文字列
	 */
	public String getISO8601Date(Calendar gcd, boolean withTime) {
		// SimpleDateFormatが使いたいので、Calendar型の引数をDate型に変換する
		Date dd = gcd.getTime();

		// withTime引数の値によって、日付の書式を決める
		String dateFormat;

		if (withTime) {
			dateFormat = "yyyy-MM-dd HH:mm:ss";
		} else {
			dateFormat = "yyyy-MM-dd";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		// 整形済み日付をStringにして返す
		return sdf.format(dd).toString();
	}

	/**
	 * ISO 8601形式の日付、および時刻フォーマットを持つ「文字列」を返す。
	 * @param julianDay double型、ユリウス通日表記の日付
	 * @return ISO 8601形式の文字列
	 */
	public String getISO8601Date(double julianDay) {
		// ユリウス通日に対応するDate型オブジェクトを取得する
		//   milliSecOfDayは1日をミリ秒単位にしたものを、
		//   originDateは1970年1月1日 00:00:00 UTCを、それぞれあらわす。
		//   ※ちなみにこの方法を使えば、Calendar→ユリウス通日への変換も2、3行で記述できるんだが....。
		int milliSecOfDay = 86400000;
		double originDate = 2440587.5;

		long dateFromJ = (long)((julianDay - originDate) * milliSecOfDay);

		// 端末のタイムゾーン設定を取得する
		String currentTZ = Time.getCurrentTimezone();
		Log.d("getISO8601Date", "Current Time zone is " + currentTZ);
		TimeZone current = TimeZone.getTimeZone(currentTZ);
		int offsetInMillis = current.getRawOffset();
		int offsetHour = offsetInMillis / 1000 /60 / 60;

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dateFromJ);
		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - offsetHour);

		return getISO8601Date(cal, true);
	}

	/**
	 * 渡された日時をユリウス通日に変換する
	 * @param gcd Calendar型（日付）
	 * @return double型、SQLiteが認識可能なユリウス通日
	 */
	public double toJulianDay(Calendar gcd) {
		double jDate;							// ユリウス通日
		int jYear, jMonth, jDay;				// ユリウス通日計算の元となる年、月、日
		double jHour, jMinute, jSecond;		// 同  時間、分、秒
		int a, b;								// グレゴリオ暦における、うるう年補正値

		// ユリウス通日を求める上で必要な「年」と「月」のセット
		//   グレゴリオ暦の「月」が2より大きい（＝Calendar.MONTHが1より大きい）場合は、
		//   そのままjYear, jMonthに年と月をセット
		if (gcd.get(Calendar.MONTH) > 1) {
			jYear = gcd.get(Calendar.YEAR);
			jMonth = gcd.get(Calendar.MONTH) + 1;
		} else {
			// グレゴリオ暦の「月」が2以下の場合は、jYearを-1、jMonthを+12する
			jYear = gcd.get(Calendar.YEAR) - 1;
			jMonth = gcd.get(Calendar.MONTH) + 13;
		}
		// 日、時間、分、秒をセット
		jDay = gcd.get(Calendar.DAY_OF_MONTH);
		// 時間、分、秒については、doubleで計算する必要があるのでdoubleでキャストする。
		jHour = (double)gcd.get(Calendar.HOUR_OF_DAY);
		jMinute = (double)gcd.get(Calendar.MINUTE);
		jSecond = (double)gcd.get(Calendar.SECOND);

		// うるう年補正値の計算
		a = (int) (jYear / 100);
		b = (int) (2 - a + (int)(a / 4));

		// グレゴリオ暦→ユリウス通日への変換公式は、以下のとおり。
		// JD = INT(365.25 y) + INT(30.6001 ( m + 1) ) + DD + (hh/24) + 1720994.5 + B
		//   ※ここでは、分、秒を考慮するため、jHourにjMinute/60とjSecond/3600を加算している。
		String tag = "toJulianDay";
		Log.d(tag, "Year : " + Math.floor(365.25 * jYear));
		Log.d(tag, "Month : " + Math.floor(30.6001 * (jMonth + 1)));
		Log.d(tag, "Day : " + jDay);
		double jh = (jHour + (jMinute / 60) + (jSecond / 3600)) / 24;
		Log.d(tag, "Hour : " + String.valueOf(jh));

		jDate = (int)(365.25 * jYear) +
				(int)(30.6001 * (jMonth + 1)) +
				jDay + jh +
				1720994.5 + b;

		return jDate;
	}

	/**
	 * ISO 8601形式の日時文字列をユリウス通日に変換する。
	 * @param iso8601Date String型、変換元のISO 8601形式の日時文字列
	 * @return double型、SQLiteが認識可能なユリウス通日
	 */
	public double toJulianDay(String iso8601Date) {
		double ret = 0;

		// ISO 8601形式の日時を分解
		Log.d("toJulianDay", "Input string = " + iso8601Date);
		String[] elementDate = iso8601Date.split("[-: ]");

		int year = Integer.parseInt(elementDate[0]);
		int month = Integer.parseInt(elementDate[1]);
		int day = Integer.parseInt(elementDate[2]);
		int hour = Integer.parseInt(elementDate[3]);
		int minute = Integer.parseInt(elementDate[4]);
		int second = Integer.parseInt(elementDate[5]);

		Log.d("toJulianDay", "year = " + String.valueOf(year));
		Log.d("toJulianDay", "month = " + String.valueOf(month));
		Log.d("toJulianDay", "day = " + String.valueOf(day));
		Log.d("toJulianDay", "hour = " + String.valueOf(hour));
		Log.d("toJulianDay", "minute = " + String.valueOf(minute));
		Log.d("toJulianDay", "second = " + String.valueOf(second));

		Calendar currentDay = Calendar.getInstance();
		currentDay.set(year, month, day, hour, minute, second);

		// 本当は
		//ret = currentDay.getTimeInMillis() / 86400000 + 2440587.5;
		// とやれば終わるのだが、計算結果の一貫性を保つためtoJulianDay(Calendar)を
		// 呼び出す。
		ret = toJulianDay(currentDay);

		return ret;
	}

	/**
	 * 現在日時を返す。
	 * @return Calendar型、現在のロケールにおける現在日時
	 */
	public Calendar getNow() {
		Calendar nowDateTime = Calendar.getInstance();
		return nowDateTime;
	}

	/**
	 * ユリウス通日をCalendarオブジェクトに変換する。
	 * @param julianDay 変換元のユリウス通日
	 * @return Calendar型、ユリウス通日から換算したCalendarオブジェクト、
	 *         ただし、ミリ秒以下は「000」になっている
	 */
	public Calendar jd2Calendar(double julianDay) {
		// GMT+0:00とされているカサブランカのタイムゾーン文字列
		//String strGmt = "Africa/Casablanca";

		String currentTZ = Time.getCurrentTimezone();
		//TimeZone gmtTz = TimeZone.getTimeZone(strGmt);
		TimeZone current = TimeZone.getTimeZone(currentTZ);

		// UTC(≒GMT)からの時差を取得する
		int rawOffsetInMillis = current.getRawOffset();
		// オフセットはミリ秒単位なので、時間単位に計算しなおす
		int rawOffsetHour = rawOffsetInMillis / 1000 / 60 / 60;
		Log.d("jd2Calendar", "Offset hour from GMT is " + String.valueOf(rawOffsetHour));

		//Calendar ret = Calendar.getInstance(gmtTz);
		Calendar ret = Calendar.getInstance();

		// ユリウス通日に対応するDate型オブジェクトを取得する
		//   milliSecOfDayは1日をミリ秒単位にしたものを、
		//   originDateは1970年1月1日 00:00:00 UTCを、それぞれあらわす。
		//   ※ちなみにこの方法を使えば、Calendar→ユリウス通日への変換も2、3行で記述できるんだが....。
		int milliSecOfDay = 86400000;
		double originDate = 2440587.5;

		// 都合上、long型にキャストする必要があるのだが、ミリ秒単位がどうも切り捨てられてるっぽい....
		long dayMilli = (long)((julianDay - originDate) * milliSecOfDay);
		ret.setTimeInMillis(dayMilli);
		//ret.setTimeZone(current);

		// 取得したオフセットだけ、時間を減算する
		ret.set(Calendar.HOUR_OF_DAY, ret.get(Calendar.HOUR_OF_DAY) - rawOffsetHour);

		return ret;
	}

	/**
	 * ISO 8601形式の日付文字列をCalendarオブジェクトに変換する。
	 * @param iso8601Date String型、変換元のISO 8601形式の日付文字列
	 * @return Calendar型、ISO 8601形式の日付文字列から換算したCalendarオブジェクト、
	 *         ただし、ミリ秒以下は「000」になっている
	 */
	public Calendar iso2Calendar(String iso8601Date) {
		Calendar ret = Calendar.getInstance();

		String[] elementDate = iso8601Date.split("[-: ]");

		int year = Integer.parseInt(elementDate[0]);
		int month = Integer.parseInt(elementDate[1]);
		int day = Integer.parseInt(elementDate[2]);
		int hour = Integer.parseInt(elementDate[3]);
		int minute = Integer.parseInt(elementDate[4]);
		int second = Integer.parseInt(elementDate[5]);

		ret.set(year, month, day, hour, minute, second);
		ret.set(Calendar.MILLISECOND, 0);

		return ret;
	}

}
