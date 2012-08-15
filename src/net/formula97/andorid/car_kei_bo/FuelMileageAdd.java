/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
	EditText editText_timeOfRefuel;
	EditText editText_comments;
	Button button_addRefuelRecord;
	Button button_cancelAddRefuelRecord;
	TextView textView_oilUnit;
	TextView textView_distanceUnit;
	TextView textView_moneyUnit;

	private int CAR_ID;
	private String CAR_NAME;

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	Cursor cSpinnerCarList;

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
		editText_timeOfRefuel = (EditText)findViewById(R.id.editText_timeOfRefuel);
		editText_comments = (EditText)findViewById(R.id.editText_comments);
		button_addRefuelRecord = (Button)findViewById(R.id.button_addRefuelRecord);
		button_cancelAddRefuelRecord = (Button)findViewById(R.id.button_cancelAddRefuelRecord);
		textView_oilUnit = (TextView)findViewById(R.id.textView_oilUnit);
		textView_distanceUnit = (TextView)findViewById(R.id.textView_distanceUnit);
		textView_moneyUnit = (TextView)findViewById(R.id.textView_moneyUnit);

		// 渡された引数を解析してグローバル変数に格納
		Intent i = getIntent();
		setCAR_ID(i.getIntExtra("CAR_ID", 0));
		setCAR_NAME(i.getStringExtra("CAR_NAME"));

		Log.d("onCreate", "got CAR_ID : " + String.valueOf(CAR_ID));
		Log.d("onCreate", "gor CAR_NAME : " + CAR_NAME);
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();

		closeCursor();
		closeDB(db);
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();

		closeCursor();
		closeDB(db);
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
		setSpinner(db, getCAR_NAME());

		// 体積、価格、距離の単位をDBから取得してセット
		textView_distanceUnit.setText(dbman.getDistanceUnitById(db, getCAR_ID()));
		textView_moneyUnit.setText(dbman.getPriceUnitById(db, getCAR_ID()));
		textView_oilUnit.setText(dbman.getVolumeUnitById(db, getCAR_ID()));

		// スピナーにコールバックリスナーを定義
		spinner_carName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO 自動生成されたメソッド・スタブ

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		// ボタンにコールバックリスナーを定義
		//   燃費記録追加
		button_addRefuelRecord.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		//   キャンセル
		button_cancelAddRefuelRecord.setOnClickListener(new View.OnClickListener() {

			/**
			 * EditTextの中身をクリアし、スピナーの初期値を引き渡されたCAR_NAMEに変更する。
			 * @param v View型、ボタンのView
			 */
			@Override
			public void onClick(View v) {
				String blank = "";

				// EditTextに空の値をセットする
				editText_amountOfOil.setText(blank);
				editText_dateOfRefuel.setText(blank);
				editText_unitPrice.setText(blank);
				EditText_odometer.setText(blank);
				editText_comments.setText(blank);
				editText_timeOfRefuel.setText(blank);

				// スピナーに値をセットしなおす前に、開かれているCursorをいったん閉じる
				closeCursor();
				setSpinner(db, CAR_NAME);
			}
		});
	}

	/**
	 * スピナーにDBから取得したクルマの一覧をセットする。
	 * @param sqlitedb SQLiteDatabase型、クルマリストを取得するDBインスタンス
	 * @param focusCarName String型、初期値にするクルマの名前
	 */
	private void setSpinner(SQLiteDatabase sqlitedb, String focusCarName) {
		cSpinnerCarList = dbman.getCarNameList(sqlitedb);
		String[] from = {"CAR_NAME"};
		int[] to = {R.id.tv_spinner_carname};

		// SimpleCursorAdapterで値をセットする
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.spinnerelement_fuelmileageadd,
				cSpinnerCarList,
				from,
				to);
		spinner_carName.setAdapter(sca);
		// ２回目以降の値セットがうまくいかないことの回避策、らしい
		sca.notifyDataSetChanged();

		// 選択位置を引数にあった値にセットする
		int pos =  getOffsetByName(focusCarName, cSpinnerCarList);
		Log.d("setSpinner", "got spinner position : " + String.valueOf(pos));
		spinner_carName.setSelection(pos);
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

	/**
	 * CursorオブジェクトのcSpinnerCarListを閉じる。
	 */
	private void closeCursor() {
		if (cSpinnerCarList.isClosed() != true ) {
			cSpinnerCarList.close();
			Log.d("CloseCursor", "cSpinnerCarList is closed.");
		} else {
			Log.d("closeCursor", "cSpinnerCarList is already closed.");
		}
	}

	/**
	 * データベースを閉じる。
	 * @param db SQLiteDatabase型、開かれているDBインスタンス
	 */
	private void closeDB(SQLiteDatabase db) {
		if (db.isOpen()) {
			db.close();
			Log.d("closeDB", "SQLiteDatabase is closed.");
		} else {
			Log.d("closeDB", "SQLiteDatabase is already closed.");
		}
	}

	/**
	 * 指定したクルマの名前から、Cursor表示位置（＝オフセット値）を取得する。
	 * @param carName String型、オフセット値を特定するクルマの名前
	 * @param cCarList Cursor型、クルマリストを格納したCursorオブジェクト
	 * @return int型、クルマに対応したCursorのオフセット値
	 */
	private int getOffsetByName(String carName, Cursor cCarList) {
		// 戻り値にするオフセットを、暫定で0としておく
		int offset = 0;
		int cnt = 0;
		String car;

		Log.d("getOffsetByName", "Inspecting as follow : " + carName);

		// Cursorを巻き戻す
		cCarList.moveToFirst();

		// cCarListがCursorの最後に到達するまで、Cursorを繰り上げながら検査する
		// getCount()を上限回数としたforステートメントでもよさそうだったが....
		do {
			car = cCarList.getString(1);
			Log.d("getOffsetByName", "got car name : " + car);

			if (car.equals(carName)) {
				// 指定した名前に合致したら、ループカウンタをオフセットとしてセットする
				offset = cnt;
				Log.d("getOffsetByName", "matched with " + carName + ", offset : " + String.valueOf(offset));
			}

			// ループカウンタをインクリメントして次の行へ移動
			cnt++;
			cCarList.moveToNext();
		}  while ( cCarList.isAfterLast() != true );

		Log.d("getOffsetByName", "offset result : " + String.valueOf(offset));
		Log.d("getOffsetByName", "counter result : " + String.valueOf(cnt));

		// 調べたオフセット値を返す
		return offset;
	}

}
