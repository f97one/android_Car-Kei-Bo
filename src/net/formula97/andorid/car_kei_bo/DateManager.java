package net.formula97.andorid.car_kei_bo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日付処理に関するメソッドを管理するクラス
 * @author f97one
 *
 */
public class DateManager {

	// 便宜上表記が必要なときに使用する、特殊な境界線上の時刻表記
	final String START_HOUR = "00:00:00";
	final String END_HOUR = "23:59:59";

	public DateManager() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	/**
	 * ISO 8601形式の日付、および時刻フォーマットを持つ「文字列」を返す
	 * @param d Date型（日付）
	 * @param withTime  boolean型、時刻が必要なときはtrue、不要なときはfalse
	 * @param dateFormat String型、日付フォーマットを一時的に格納する
	 * @return yyyy-MM-dd HH:mm:ss形式の文字列
	 */
	protected String getISO8601Date(Date d, boolean withTime) {
		String dateFormat;

		if (withTime) {
			dateFormat = "yyyy-MM-dd HH:mm:ss";
		} else {
			dateFormat = "yyyy-MM-dd";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		// 整形済み日付をStringにして返す
		return sdf.format(d).toString();
	}
}
