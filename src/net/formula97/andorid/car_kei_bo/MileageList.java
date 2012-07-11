/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author kazutoshi
 *
 */
public class MileageList extends Activity {

	private int CAR_ID;
	private String CAR_NAME;

	// ウィジェット類の定義
	TextView tv_element_CarName2;		// クルマの名前
	TextView tv_value_FuelMileage2;		// クルマのトータル燃費
	TextView tv_unit_fuelMileage2;		// クルマの燃費単位
	TextView tv_value_RunningCosts2;		// クルマのトータルランニングコスト
	TextView tv_unit_runningCosts2;		// クルマのランニングコスト単位
	Button btn_add_mileage;				// 燃費記録追加ボタン
	ListView lv_mileagelist;			// 燃費記録を表示するListView

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	Cursor cMileageList;

	/**
	 *
	 */
	public MileageList() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fuelmileagelist);

		// ウィジェット類の取得
		tv_element_CarName2 = (TextView)findViewById(R.id.tv_element_CarName2);
		tv_value_FuelMileage2 = (TextView)findViewById(R.id.tv_value_FuelMileage2);
		tv_unit_fuelMileage2 = (TextView)findViewById(R.id.tv_unit_fuelMileage2);
		tv_value_RunningCosts2 = (TextView)findViewById(R.id.tv_value_RunningCosts2);
		tv_unit_runningCosts2 = (TextView)findViewById(R.id.tv_unit_runningCosts2);
		btn_add_mileage = (Button)findViewById(R.id.btn_add_mileage);
		lv_mileagelist = (ListView)findViewById(R.id.lv_mileagelist);

		// 渡された引数を解析してグローバル変数に格納
		Intent i = new Intent();
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
	}

	public int getCAR_ID() {
		return CAR_ID;
	}

	public void setCAR_ID(int cAR_ID) {
		CAR_ID = cAR_ID;
	}

	/**
	 * @return cAR_NAME
	 */
	public String getCAR_NAME() {
		return CAR_NAME;
	}

	/**
	 * @param cAR_NAME セットする cAR_NAME
	 */
	public void setCAR_NAME(String cAR_NAME) {
		CAR_NAME = cAR_NAME;
	}

}
