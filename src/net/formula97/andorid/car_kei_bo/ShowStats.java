/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.math.BigDecimal;
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
import android.widget.SimpleAdapter;
import android.widget.Spinner;

/**
 * @author kazutoshi
 *
 */
public class ShowStats extends Activity implements OnItemSelectedListener {

	private double[][] statDaysRange;
	private ArrayList<HashMap<String,String>> statData;
	private int CAR_ID;
	private String CAR_NAME;

	private int intStatType;
	private int intStatPeriod;

	// 差込データで使うHashMapのキー名称
	private String hmPeriod = "PERIOD";
	private String hmValue = "VALUE";

	// 差込データで使う２次配列のインデックス値
	private static final int STARTDAY_INDEX = 0;
	private static final int ENDDAY_INDEX = 1;

	// 統計を行う種別を規定
	private static final int STATTYPE_FUEL_VOLUME = 2;	// 給油量
	private static final int STATTYPE_MILEAGE = 3;		// 燃費記録
	private static final int STATTYPE_RUNNINGCOSTS = 4;	// ランニングコスト

	// 統計範囲を規定
	private static final int STATPERIOD_3MONTHS = 2;
	private static final int STATPERIOD_6MONTHS = 5;
	private static final int STATPERIOD_12MONTHS = 11;
	private static final int STATPERIOD_ALL = 0;

	// DBインスタンス
	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	// 時刻関連処理インスタンス
	private DateManager dmngr = new DateManager();

	private SimpleAdapter saStatList;

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

		if (db.isOpen() != true) {
			db.close();
		}
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
			case STATPERIOD_3MONTHS:
				spinner_statPeriod.setSelection(0);
				break;
			case STATPERIOD_6MONTHS:
				spinner_statPeriod.setSelection(1);
				break;
			case STATPERIOD_12MONTHS:
				spinner_statPeriod.setSelection(2);
				break;
			default:
				spinner_statPeriod.setSelection(3);
				break;
		}

		// 各スピナーの現在値を取得してフィールドへ格納する
		setStatTypeFromSpinner();
		setStatPeriodFromSpinner();

		// 差込データを取得する
		statData = getStatDataArray(db, statDaysRange, getCAR_ID(), intStatType);

		setAdapters();
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
		int dimcounter = 0;

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
				gcd.add(Calendar.MONTH, -1);
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

			Log.d("getStatDaysRange", "Period of first: " + dmngr.getISO8601Date(daysRange[loopcounter][0]));
			Log.d("getStatDaysRange", "Period of last: " + dmngr.getISO8601Date(daysRange[loopcounter][1]));

			// 月を1減算
			gcd.add(Calendar.MONTH, -1);
			Log.d("getStatDaysRange", "Now " + dmngr.getISO8601Date(gcd, false));
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
		HashMap<String,String> map;

		// 配列の個数を取得
		int refuelDayListIndex = refuelDayList.length;
		Log.i("getStatDataArray", "Number of Refuel day index is " + String.valueOf(refuelDayListIndex));

		// 差込データ作成のループで使用する変数たち
		double subtotal = 0;
		double startJd = 0;
		double endJd = 0;
		String periodDay = null;
		String statUnit = null;

		// ListViewに差し込む値の単位
		String volUnit = dbman.getVolumeUnitById(db, carId);
		String priceUnit = dbman.getPriceUnitById(db, carId);
		String distanceUnit = dbman.getDistanceUnitById(db, carId);

		switch (statType) {
		case STATTYPE_FUEL_VOLUME:
			statUnit = volUnit;
			break;
		case STATTYPE_MILEAGE:
			statUnit = volUnit + "/" + priceUnit;
			break;
		case STATTYPE_RUNNINGCOSTS:
			statUnit = priceUnit + "/" + distanceUnit;
			break;
		}

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

			// 統計値を小数点２ケタまで表示するように加工
			//   ->小数点３ケタを四捨五入する
			BigDecimal bi = new BigDecimal(String.valueOf(subtotal));
			double biSubtotal = bi.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();

			// HashMapに値を追加した後、ArrayListに値を収める
			map  = new HashMap<String, String>();
			map.put(hmPeriod, periodDay);
			map.put(hmValue, String.valueOf(biSubtotal) + " " + statUnit);

			Log.d("getStatDataArray"," put a value " + periodDay + " to hmPeriod.");
			Log.d("getStatDataArray"," put a value " + String.valueOf(biSubtotal) + " " + statUnit + " to hmValue.");

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

	/**
	 * 統計種別スピナーの現在値から、統計種別IDを求めてフィールドへ格納する。
	 */
	private void setStatTypeFromSpinner() {
		int statTypeId;

		// 統計種別スピナーの現在値を取得
		//   文字列で取得したほうが分かりやすいのだが、文字列をswitch文の条件にできないので、
		//   ItemIdを取得するようにしている。
		//   ※注：文字列をswitch文の条件にするにはJava 7が必要だが、
		//         Android SDKがJava 7に対応していないため、使用できない。
		statTypeId = (int)spinner_statType.getSelectedItemId();
		Log.d("setStatTypeFromSpinner", "Stat Type Selector is now selected as follow, resId = " + String.valueOf(statTypeId));

		// 統計種別をセットする
		switch (statTypeId) {
		case 0:
			intStatType = STATTYPE_MILEAGE;
			break;
		case 1:
			intStatType = STATTYPE_FUEL_VOLUME;
			break;
		case 2:
			intStatType = STATTYPE_RUNNINGCOSTS;
			break;
		}
	}

	/**
	 * 統計範囲スピナーの現在値から、統計範囲IDを求めてフィールドへ格納する。
	 */
	private void setStatPeriodFromSpinner() {
		int statPeriodId;

		// 統計範囲スピナーの現在値を取得
		//   ItemIdにしている理由は、setStatTypeFromSpinner()に同じ。
		statPeriodId = (int)spinner_statPeriod.getSelectedItemId();
		Log.d("setStatPeriodFromSpinner", "Stat Period Selector is now selected as follow, resId = " + String.valueOf(statPeriodId));

		// 統計種別をセットする
		switch (statPeriodId) {
		case 0:
			intStatPeriod = STATPERIOD_3MONTHS;
			break;
		case 1:
			intStatPeriod = STATPERIOD_6MONTHS;
			break;
		case 2:
			intStatPeriod = STATPERIOD_12MONTHS;
			break;
		case 3:
			intStatPeriod = STATPERIOD_ALL;
			break;
		}
	}

	/* (非 Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView<?>, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// 統計種別と統計範囲をセットしなおす
		setStatTypeFromSpinner();
		setStatPeriodFromSpinner();

		// 統計範囲の配列を作成しなおす
		statDaysRange = getStatDaysRange(db, getCAR_ID(), intStatPeriod);

		// 差込データを取得し直す
		statData = getStatDataArray(db, statDaysRange, getCAR_ID(), intStatType);

		setAdapters();
	}

	/* (非 Javadoc)
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView<?>)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * ListViewにArrayListの値を差し込む。
	 */
	private void setAdapters() {
		// 差込データの位置定義
		String[] from = {hmPeriod, hmValue};
		int[] to = {R.id.tv_value_statPeriod, R.id.tv_value_statValue};

		saStatList = new SimpleAdapter(this, statData, R.layout.listviewelemnt_statlist, from, to);
		lv_statValue.setAdapter(saStatList);
	}

}
