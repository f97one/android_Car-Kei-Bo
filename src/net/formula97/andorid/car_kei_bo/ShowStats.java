/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author kazutoshi
 *
 */
public class ShowStats extends Activity {

	private double[][] statDaysRange;
	private int CAR_ID;
	private String CAR_NAME;

	private static int STARTDAY_INDEX = 0;
	private static int ENDDAY_INDEX = 1;

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	/* (非 Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showstats);

		// Activityに渡されたCAR_IDを取り出す
		Intent i = getIntent();
		setCAR_ID(i.getIntExtra("CAR_ID", 0));
		setCAR_NAME(i.getStringExtra("CAR_NAME"));
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();

		// DBをReadableで開く
		//  ※注：Androidの仕様によれば、ReadableでもDBへの書き込みができるため、
		//        これで問題はない。
		db = dbman.getReadableDatabase();

		// プリファレンスの値呼び出し
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		// 統計範囲の日時を配列で取得する
		//   格納値の実際はint型数値だが、String-Arrayに列挙しているため、
		//   StringからIntへ変換している
		int statRangeValue = Integer.parseInt(pref.getString("StatRangeValue", "0"));
		Log.i("onResume", "StatRangeValue is " + String.valueOf(statRangeValue));

		statDaysRange = getStatDaysRange(db, getCAR_ID(), statRangeValue);

	}

	/**
	 * 指定したCAR_IDのクルマについて、統計可能な範囲を返す。
	 * @param db SQLiteDatabase型、統計範囲を取得するデータベースのインスタンス
	 * @param carId int型、クルマのCAR_ID
	 * @param statRangeValue int型、統計範囲とするカウンター値
	 * @return double[][]型、統計範囲のユリウス通日
	 */
	private double[][] getStatDaysRange(SQLiteDatabase db, int carId, int statRangeValue){
		// 最低限の値で初期化
		int dimcounter = 1;

		// 指定したCAR_IDの給油記録のうち、最古、および最新の給油日時を取得する
		double oldestRefuelJDay = dbman.getOldestRefuelDateById(db, carId);		// 最古
		double latestRefuelJDay = dbman.getLatestRefuelDateById(db, carId);		// 最新

		// 最新の給油記録をCalendarに変換する
		DateManager dmngr = new DateManager();
		Calendar gcd = dmngr.jd2Calendar(latestRefuelJDay);

		// statRangeValueが0でない場合は、上位配列を確保する数をループで求める
		if (statRangeValue == 0) {
			while (dmngr.toJulianDay(gcd) >= oldestRefuelJDay) {
				// 上位配列の数を1加算
				dimcounter++;
				// 月を1減算
				gcd.set(Calendar.MONTH, gcd.get(Calendar.MONTH) - 1);
			}
		} else {
			// statRangeValueが0でない場合は、与えられた範囲の値を用いる
			dimcounter = statRangeValue + 1;
		}

		Log.i("onResume", "index of stat. range is " + String.valueOf(dimcounter));

		// 配列を初期化
		double[][] daysRange = new double[dimcounter][];
		// gcdの値を元に戻す
		gcd.clear();
		gcd = dmngr.jd2Calendar(latestRefuelJDay);

		// cgdのMONTHを-1していき、latestRefuelJDayを超えるまで繰り返す。
		// 繰り返し回数がその前の処理で判明しているので、forを使う。
		for (int loopcounter = 0; loopcounter < dimcounter; loopcounter++) {
			Log.i("onResume", "Loop counter value is now "+ String.valueOf(loopcounter));

			daysRange[loopcounter] = new double[2];
			daysRange[loopcounter][0] = dmngr.getFirstMomentOfMonth(gcd);
			daysRange[loopcounter][1] = dmngr.getLastMomentOfMonth(gcd);

			// 月を1減算
			gcd.set(Calendar.MONTH, gcd.get(Calendar.MONTH) - 1);
		}

		// 最終的な配列を返す
		return daysRange;
	}

	/**
	 * CAR_IDを取得します。
	 * @return CAR_ID
	 */
	public int getCAR_ID() {
	    return CAR_ID;
	}

	/**
	 * CAR_IDを設定します。
	 * @param CAR_ID CAR_ID
	 */
	public void setCAR_ID(int CAR_ID) {
	    this.CAR_ID = CAR_ID;
	}

	/**
	 * CAR_NAMEを設定します。
	 * @param CAR_NAME CAR_NAME
	 */
	public void setCAR_NAME(String CAR_NAME) {
	    this.CAR_NAME = CAR_NAME;
	}

	/**
	 * CAR_NAMEを取得します。
	 * @return CAR_NAME
	 */
	public String getCAR_NAME() {
	    return CAR_NAME;
	}

}
