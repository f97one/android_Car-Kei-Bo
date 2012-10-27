/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.Spinner;

/**
 * @author kazutoshi
 *
 */
public class ShowStats extends Activity implements OnItemSelectedListener {

	private double[][] statDaysRange;
	private int CAR_ID;
	private String CAR_NAME;

	private static int STARTDAY_INDEX = 0;
	private static int ENDDAY_INDEX = 1;

	// 統計を行う種別を規定
	private static final int STATTYPE_FUEL_VOLUME = 0;	// 給油量
	private static final int STATTYPE_MILEAGE = 1;		// 燃費記録
	private static final int STATTYPE_RUNNINGCOSTS = 2;	// ランニングコスト


	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	private DateManager dmngr = new DateManager();

	// ウィジェット
	Spinner spinner_statType;
	Spinner spinner_statPeriod;
	ListView lv_statValue;

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

		// ウィジェットのリソースIDを取得
		spinner_statType = (Spinner)findViewById(R.id.spinner_statType);
		spinner_statPeriod = (Spinner)findViewById(R.id.spinner_statPeriod);
		lv_statValue = (ListView)findViewById(R.id.lv_statValue);

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
		//   プリファレンスに何も値がセットされていない場合は、統計範囲をすべて（＝0）
		//   としている
		int statRangeValue = Integer.parseInt(pref.getString("StatRangeValue", "0"));
		Log.i("onResume", "StatRangeValue is " + String.valueOf(statRangeValue));

		statDaysRange = getStatDaysRange(db, getCAR_ID(), statRangeValue);

		// スピナーにイベントリスナーをセット
		spinner_statPeriod.setOnItemSelectedListener(this);
		spinner_statType.setOnItemSelectedListener(this);

		// プリファレンスにある統計期間の初期値を反映
		// ハードコーディングしてしまっているが、上から
		//   過去3か月間
		//   過去6か月間
		//   過去12か月間
		//   すべて
		// を表す
		switch (statRangeValue) {
			case 2:
				spinner_statPeriod.setSelection(0);
				break;
			case 5:
				spinner_statPeriod.setSelection(1);
				break;
			case 11:
				spinner_statPeriod.setSelection(2);
				break;
			default:
				spinner_statPeriod.setSelection(3);
				break;
		}
	}

	/**
	 * 指定したCAR_IDのクルマについて、統計可能な範囲を返す。
	 * @param db SQLiteDatabase型、統計範囲を取得するデータベースのインスタンス
	 * @param carId int型、クルマのCAR_ID
	 * @param statRangeValue int型、統計範囲とするカウンター値
	 * @return double[][]型、統計範囲のユリウス通日
	 */
	// TODO 現状は「値を返す順」が降順になっているが、これを昇順に入れ替える
	//      方法を模索する。
	//      引数に boolean invertOrder とか
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

		// TODO このあたりに配列の降順->昇順変換処理を入れる
		//      当然のことながら、引数により条件分岐を入れる

		// 最終的な配列を返す
		return daysRange;
	}

	/**
	 * ListViewへ差し込むデータのもととなるArrayListを作成する。
	 * @param db SQLiteDatabase型、差込データを取得するDBインスタンス
	 * @param refuelDayList double[][]型、差込データ作成範囲のユリウス通日を配列化したもの
	 * @param carId int型、差込データを取得するクルマのCAR_ID
	 * @param statType int型、取得する統計データの種類
	 * @return
	 */
	private ArrayList<HashMap<String,String>> getStatDataArray(SQLiteDatabase db, double[][] refuelDayList, int carId, int statType) {
		// 戻り値の宣言
		ArrayList<HashMap<String,String>> result = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> map = new HashMap<String, String>();

		// 配列の個数を取得
		int refuelDayListIndex = refuelDayList.length;
		Log.i("getStatDataArray", "Number of Refuel day index is " + String.valueOf(refuelDayListIndex));

		// 差込データ作成のループで使用する変数たち
		double subtotal = 0;
		double startJd = 0;
		double endJd = 0;
		String periodDay = null;

		// HashMapのキー名称
		String hmPeriod = "PERIOD";
		String hmValue = "VALUE";

		// HashMapに値を追加する
		for (int i = 0; i < refuelDayListIndex; i++) {
			// 日付範囲を取得する
			startJd = refuelDayList[i][STARTDAY_INDEX];
			endJd = refuelDayList[i][ENDDAY_INDEX];

			// 給油量、燃費記録、ランニングコストの小計のうち、引数に応じたものを取得
			switch (statType) {
			case STATTYPE_FUEL_VOLUME:
				subtotal = dbman.getSubtotalOfRefuelById(db, carId, startJd, endJd);
				break;
			case STATTYPE_MILEAGE:
				// 取得値をfloat型にしていたので、doubleにキャストしている
				subtotal = (double)dbman.getSubtotalOfMileageById(db, carId, startJd, endJd);
				break;
			case STATTYPE_RUNNINGCOSTS:
				// 取得値をfloat型にしていたので、doubleにキャストしている
				subtotal = (double)dbman.getSubtotalOfRunningCostsById(db, carId, startJd, endJd);
				break;
			}

			// 給油期間をあらわす文字列（yyyy-MM）を取得（＝先頭から７文字取り出す）
			periodDay = dmngr.getISO8601Date(startJd).substring(0, 7);

			// HashMapに値を追加した後、ArrayListに値を収める
			map.put(hmPeriod, periodDay);
			map.put(hmValue, String.valueOf(subtotal));

			result.add(map);
		}

		return result;
	}


	//private void setAdapterToLV()

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

	/* (非 Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView<?>, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO 自動生成されたメソッド・スタブ

		switch (view.getId()) {
		case R.id.spinner_statType:
			// TODO 統計タイプを取得する処理を実装する

			break;

		case R.id.spinner_statPeriod:
			// TODO 統計期間を取得する処理を実装する


			break;

		}

		// TODO スピナーから得た値を、ListView作成処理に投げる処理を実装する

	}

	/* (非 Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView<?>)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
