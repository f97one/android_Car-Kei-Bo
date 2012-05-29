package net.formula97.andorid.car_kei_bo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
	 * ISO 8601形式の日付、および時刻フォーマットを持つ「文字列」を返す
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
}
