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
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author kazutoshi
 *
 */
public class FuelMileageAdd extends Activity {

	// ウィジェット類の宣言
	Spinner spinner_carName;
	EditText editText_amountOfOil;
	EditText EditText_odometer;
	EditText editText_unitPrice;
	EditText editText_dateOfRefuel;
	Button button_addRefuelRecord;
	Button button_cancelAddRefuelRecord;
	TextView textView_oilUnit;
	TextView textView_distanceUnit;
	TextView textView_moneyUnit;

	private int CAR_ID;
	private String CAR_NAME;

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	Cursor spinnerCarList;

	/* (非 Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fuelmileageadd);

		// ウィジェット類
		spinner_carName = (Spinner)findViewById(R.id.spinner_carName);
		editText_amountOfOil = (EditText)findViewById(R.id.editText_amountOfOil);
		EditText_odometer = (EditText)findViewById(R.id.EditText_odometer);
		editText_unitPrice = (EditText)findViewById(R.id.editText_unitPrice);
		editText_dateOfRefuel = (EditText)findViewById(R.id.editText_dateOfRefuel);
		button_addRefuelRecord = (Button)findViewById(R.id.button_addRefuelRecord);
		button_cancelAddRefuelRecord = (Button)findViewById(R.id.button_cancelAddRefuelRecord);
		textView_oilUnit = (TextView)findViewById(R.id.textView_oilUnit);
		textView_distanceUnit = (TextView)findViewById(R.id.textView_distanceUnit);
		textView_moneyUnit = (TextView)findViewById(R.id.textView_moneyUnit);

		// 渡された引数を解析してグローバル変数に格納
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

		closeCursor();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();

		closeCursor();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();

		/*
		 * ボタンの配置を画面幅の1/2にする処理
		 *
		 * onCreate()ではなくこちらに書くのは、最終的な画面設定が行われるのがこちらという
		 * Androidのくせによるものである。
		 */
		// 画面幅を取得
		int displayWidth = getWindowManager().getDefaultDisplay().getWidth();

		// ボタンの幅を、取得した画面幅の1/2にセット
		button_addRefuelRecord.setWidth(displayWidth / 2);
		button_cancelAddRefuelRecord.setWidth(displayWidth / 2);

		// DBをReadableで開く
		//  ※注：Androidの仕様によれば、ReadableでもDBへの書き込みができるため、
		//        これで問題はない。
		db = dbman.getReadableDatabase();

		// スピナーにクルマの一覧をセットし、引数で渡されたCAR_IDのクルマを初期値にする
		setSpinner(db, getCAR_ID());

		// 体積、価格、距離の単位をDBから取得してセット
		textView_distanceUnit.setText(dbman.getDistanceUnitById(db, getCAR_ID()));
		textView_moneyUnit.setText(dbman.getPriceUnitById(db, getCAR_ID()));
		textView_oilUnit.setText(dbman.getVolumeUnitById(db, getCAR_ID()));

	}

	/**
	 * スピナーにDBから取得したクルマの一覧をセットする。
	 * @param sqlitedb
	 * @param focusCarId
	 */
	private void setSpinner(SQLiteDatabase sqlitedb, int focusCarId) {
		// TODO 自動生成されたメソッド・スタブ
		spinnerCarList = dbman.getCarNameList(sqlitedb);
		String[] from = {"CAR_NAME"};
		int[] to = {R.id.tv_spinner_carname};

		// SimpleCursorAdapterで値をセットする
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.spinnerelement_fuelmileageadd,
				spinnerCarList,
				from,
				to);
		spinner_carName.setAdapter(sca);

		// 選択位置を引数にあった値にセットする
		spinner_carName.setSelection(focusCarId -1);
	}

	/**
	 * @return cAR_ID
	 */
	public int getCAR_ID() {
		return CAR_ID;
	}

	/**
	 * @param cAR_ID セットする cAR_ID
	 */
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

	private void closeCursor() {
		spinnerCarList.close();
	}
}
