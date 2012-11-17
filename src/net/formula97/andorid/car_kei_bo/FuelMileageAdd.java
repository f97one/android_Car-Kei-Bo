/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * @author kazutoshi
 *
 */
public class FuelMileageAdd extends Activity implements OnClickListener {

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
	Button button_editDate;
	Button button_editTime;
	TextView textView_oilUnit;
	TextView textView_distanceUnit;
	TextView textView_moneyUnit;
	DatePickerDialog dpDialog;
	TimePickerDialog tpDialog;

	DatePickerDialog.OnDateSetListener dpListener;
	TimePickerDialog.OnTimeSetListener tpListener;

	private int CAR_ID;
	private int RECORD_ID;
	private String CAR_NAME;
	private boolean UPDATE_MODE;
	private double refuelRcordDate = 0;

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	Cursor cSpinnerCarList;

	private Calendar currentDateTime;

	private DateManager dmngr = new DateManager();

	private static String TEXT_BLANK = "";

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
		button_editDate = (Button)findViewById(R.id.button_editDate);
		button_editTime = (Button)findViewById(R.id.button_editTime);
		textView_oilUnit = (TextView)findViewById(R.id.textView_oilUnit);
		textView_distanceUnit = (TextView)findViewById(R.id.textView_distanceUnit);
		textView_moneyUnit = (TextView)findViewById(R.id.textView_moneyUnit);

		// 渡された引数を解析してグローバル変数に格納
		Intent i = getIntent();
		setCAR_ID(i.getIntExtra("CAR_ID", 0));
		setRECORD_ID(i.getIntExtra("RECORD_ID", 0));
		setCAR_NAME(i.getStringExtra("CAR_NAME"));
		setUPDATE_MODE(i.getBooleanExtra("UPDATE_MODE", false));

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
		setUnitLabel(db, getCAR_ID());

		// スピナーにコールバックリスナーを定義
		spinner_carName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			/**
			 * スピナーの変化を検知したら、各種単位のラベルをクルマの設定に応じた値に書き換える
			 */
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long rowId) {
				// クルマの名前に応じたCAR_IDを特定する
				String carName = getCarNameFromSpinner();
				int carId = dbman.getCarId(db, carName);

				// ラベルを書き換える
				setUnitLabel(db, carId);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		// ボタンにコールバックリスナーを定義
		button_addRefuelRecord.setOnClickListener(this);		// 燃費記録追加
		button_cancelAddRefuelRecord.setOnClickListener(this);	// キャンセル
		button_editDate.setOnClickListener(this);				// 給油日の編集ボタン
		button_editTime.setOnClickListener(this);				// 給油時刻の編集ボタン

		// 現在日付と現在時刻をeditTextにセットする
		currentDateTime = dmngr.getNow();
		setDateToEdit(currentDateTime);
		setTimeToEdit(currentDateTime);

		// DateSetLintenerをセットする
		dpListener = new DatePickerDialog.OnDateSetListener() {

			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				// 日付をcurrentDateTimeにセットする
				currentDateTime.set(year, monthOfYear, dayOfMonth);

				// EditTextにセットされている日時を更新する
				setDateToEdit(currentDateTime);
				setTimeToEdit(currentDateTime);

				Log.d("onDateSet", "Current date has set to " + dmngr.getISO8601Date(currentDateTime, false));
			}


		};

		// TimePickerlistenerをセットする
		tpListener = new TimePickerDialog.OnTimeSetListener() {

			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				// 時刻をcurrentDateTimeにセットする
				currentDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				currentDateTime.set(Calendar.MINUTE, minute);
				// TimePickerDialogは秒を指定できないので、ユリウス通日を使用する関係上
				// 秒を0としてセットする。
				currentDateTime.set(Calendar.SECOND, 0);

				// EditTextにセットされている日時を更新する
				setDateToEdit(currentDateTime);
				setTimeToEdit(currentDateTime);

				Log.d("onTimeSet", "Current datetime has set to " + dmngr.getISO8601Date(currentDateTime, true));
			}
		};

		// UPDATE_MODEが有効の場合は、
		//   1.「燃費記録を追加」ボタンを「燃費記録を更新」に変更
		//   2.ウィジェットに編集するレコードをセット
		if (isUPDATE_MODE()) {
			button_addRefuelRecord.setText(R.string.label_btn_updatefuelmileagerecord);
			setModifyingRecords(db, getCAR_ID(), getRECORD_ID());
		}

	}

	/**
	 * スピナーにDBから取得したクルマの一覧をセットする。
	 * @param sqlitedb SQLiteDatabase型、クルマリストを取得するDBインスタンス
	 * @param focusCarName String型、初期値にするクルマの名前
	 */
	private void setSpinner(SQLiteDatabase sqlitedb, String focusCarName) {
		cSpinnerCarList = dbman.getCarNameList(sqlitedb);
		List<String> lstSpinner = new ArrayList<String>();

		// ArrayListに、Cursorオブジェクトになっている「取得したクルマ一覧」を入れなおす
		// 後で出てくるgetOffsetByNameと処理はほとんど同じなので、統合しようと思えばできるのだが....
		do {
			lstSpinner.add(cSpinnerCarList.getString(1));
			cSpinnerCarList.moveToNext();
		} while (cSpinnerCarList.isAfterLast() != true);

		// ArrayAdapterを定義してスピナーに値をセットする
		ArrayAdapter<String> aa = new ArrayAdapter<String> (
				this,
				android.R.layout.simple_spinner_item,
				lstSpinner);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_carName.setAdapter(aa);
		// ２回目以降の値セットがうまくいかないことの回避策、らしい
		aa.notifyDataSetChanged();

		// 選択位置を引数にあった値にセットする
		int pos =  getOffsetByName(focusCarName, cSpinnerCarList);
		Log.d("setSpinner", "got spinner position : " + String.valueOf(pos));
		spinner_carName.setSelection(pos);

		// Cursorを再利用することを考慮し、先頭まで巻き戻す
		cSpinnerCarList.moveToFirst();
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

	/**
	 * スピナーの現在選択位置から、DBへ追加するときのクルマの名前を取得する。
	 * @return String型、スピナーの現在選択位置にあるクルマの名前
	 */
	private String getCarNameFromSpinner() {
		String carName = (String)spinner_carName.getSelectedItem();
		Log.d("getCarNameFromSpinner", "now selected car is " + carName);

		return carName;
	}

	/**
	 * editTextに日付をセットする。フォーマットはロケール設定に従う。
	 * @param gcd Calendar型、指定日付にセットされたCalendarオブジェクト
	 */
	private void setDateToEdit(Calendar gcd) {
		Date dd = gcd.getTime();

		// AndroidのAPIに定義されているDateFormatでロケールを読み出し、
		// java.text.DateFormatに書き出す。
		Context ctx = getApplicationContext();
		java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(ctx);

		// EditTextにロケールに従ったフォーマットの日付をセット
		editText_dateOfRefuel.setText(df.format(dd));
	}

	/**
	 * editTextに時刻をセットする。フォーマットはロケール設定に従う。
	 * @param gcd Calendar型、指定時刻にセットされたCalendarオブジェクト
	 */
	private void setTimeToEdit(Calendar gcd) {
		Date dd = gcd.getTime();

		// AndroidのAPIに定義されているDateFormatでロケールを読み出し、
		// java.text.DateFormatに書き出す。
		Context ctx = getApplicationContext();
		java.text.DateFormat df = android.text.format.DateFormat.getTimeFormat(ctx);

		// EditTextにロケールに従ったフォーマットの日付をセット
		editText_timeOfRefuel.setText(df.format(dd));
	}

	/**
	 * 給油量、走行距離、単価の各種単位のラベルを、クルマのCAR_IDに応じたものにセットする。
	 * @param sqlitedb SQLiteDatabase型、各種ラベルを取得してくるDBインスタンス
	 * @param carId int型、クルマのCAR_ID
	 */
	private void setUnitLabel(SQLiteDatabase sqlitedb, int carId) {
		textView_distanceUnit.setText(dbman.getDistanceUnitById(sqlitedb, carId));
		textView_moneyUnit.setText(dbman.getPriceUnitById(sqlitedb, carId));
		textView_oilUnit.setText(dbman.getVolumeUnitById(sqlitedb, carId));
	}

	/**
	 * 入力された文字列がint型の数値として処理可能かを判断する。
	 * @param inputStr 判断に使用する文字列
	 * @return boolean型、int型として扱える場合はtrue、そうでない場合はfalse
	 */
	private boolean isValidInt(String inputStr) {
		boolean result = false;

		Log.d("isValidInt", "Input text is " + inputStr);

		if (inputStr.equals(TEXT_BLANK)) {
			result = false;
			Log.e("isValidInt", "Input String is nothing to display. Parsing to Integer failed.");
		}

		try {
			int testValue = Integer.parseInt(inputStr);
			result = true;
			Log.d("isValidInt", "Parsing to Integer successful. Output number is " + String.valueOf(testValue));
		} catch (NumberFormatException nfe) {
			result = false;
			Log.e("isValidInt", "NumberFormatException occured. Parsing to Integer failed.");
		} catch (Exception e) {
			result = false;
			Log.e("isValidInt", "Other Exception occured. Parsing to Integer failed.");
		}

		return result;
	}

	/**
	 * 入力された文字列がlong型の数値として処理可能かを判断する。
	 * @param inputStr 判断に使用する文字列
	 * @return boolean型、long型として扱える場合はtrue、そうでない場合はfalse
	 */
	private boolean isValidLong(String inputStr) {
		boolean result = false;

		Log.d("isValidLong", "Input text is " + inputStr);

		if (inputStr.equals(TEXT_BLANK)) {
			result = false;
			Log.e("isValidLong", "Input String is nothing to display. Parsing to Long failed.");
		}

		try {
			long testValue = Long.parseLong(inputStr);
			result = true;
			Log.d("isValidLong", "Parsing to Long successful. Output number is " + String.valueOf(testValue));
		} catch (NumberFormatException nfe) {
			result = false;
			Log.e("isValidLong", "NumberFormatException occured. Parsing to Long failed.");
		} catch (Exception e) {
			result = false;
			Log.e("isValidInt", "Other Exception occured. Parsing to Integer failed.");
		}
		return result;
	}

	/**
	 * 入力された文字列がdouble型の数値として処理可能かを判断する。
	 * @param inputStr 判断に使用する文字列
	 * @return boolean型、double型として扱える場合はtrue、そうでない場合はfalse
	 */
	private boolean isValidDouble(String inputStr) {
		boolean result = false;

		Log.d("isValidDouble", "Input text is " + inputStr);

		if (inputStr.equals(TEXT_BLANK)) {
			result = false;
			Log.e("isValidDouble", "Input String is nothing to display. Parsing to Double failed.");
		}

		try {
			double testValue = Double.parseDouble(inputStr);
			result = true;
			Log.d("isValidDouble", "Parsing to Double successful. Output number is " + String.valueOf(testValue));
		} catch (NumberFormatException nfe) {
			result = false;
			Log.e("isValidLong", "NumberFormatException occured. Parsing to Double failed.");
		} catch (Exception e) {
			result = false;
			Log.e("isValidInt", "Other Exception occured. Parsing to Integer failed.");
		}

		return result;
	}

	/**
	 * DBに追加した内容をトースト表示する。
	 * @param db
	 * @param carId
	 * @param gcd
	 * @param amountOfOil
	 * @param odometer
	 */
	private void showToastMsg(SQLiteDatabase db, int carId, Calendar gcd, double amountOfOil, double tripMeter) {
		String line1, line2, line3, line4;

		// CAR_IDからクルマの名前を特定する
		String carName = dbman.getCarNameById(db, carId);

		// 表示する文章を組み立てる
		if (isUPDATE_MODE()) {
			line1 = carName + getString(R.string.toastmsg_addmileage11);
		} else {
			line1 = carName + getString(R.string.toastmsg_addmileage1);
		}
		line2 = getString(R.string.toastmsg_addmileage2) + dmngr.getISO8601Date(gcd, true);
		line3 = getString(R.string.toastmsg_addmileage3) + String.valueOf(amountOfOil);
		line4 = getString(R.string.toastmsg_addmileage4) + String.valueOf(tripMeter);

		Toast.makeText(this, line1 + "\n" + line2 + "\n" + line3 + "\n" + line4, Toast.LENGTH_LONG).show();
	}

	/**
	 * OnClickListenerをセットされているウィジェットが、クリックイベントを検知した時に
	 * 呼び出される。
	 * @param v View型、リスナーを呼んだView
	 */
	@Override
	public void onClick(View v) {
		// リスナーを読んだViewのIDを取得する
		int viewId = v.getId();

		// プリファレンスから「前の画面へ戻る」設定を読み出す
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean returnPreviousView = sp.getBoolean("ReturnFromPreviousActivity", true);

		switch (viewId) {
		case R.id.button_addRefuelRecord:			// 燃費記録追加ボタン
			// 燃費記録を追加するにあたり、DBにセットするための値を取得する
			int targetCarId = dbman.getCarId(db, getCarNameFromSpinner());
			double amountOfOil, tripMeter, unitPrice;
			double runningCosts = 0;
			long ret = 0;
			long retRunningCost = 0;

			// EditTextに入力されている値を取り出す
			SpannableStringBuilder ssbAmountOfOil = (SpannableStringBuilder)editText_amountOfOil.getText();
			SpannableStringBuilder ssbUnitPrice = (SpannableStringBuilder)editText_unitPrice.getText();
			SpannableStringBuilder ssbComments = (SpannableStringBuilder)editText_comments.getText();
			SpannableStringBuilder ssbOdometer = (SpannableStringBuilder)EditText_odometer.getText();

			if (isValidDouble(ssbAmountOfOil.toString())) {
				amountOfOil = Double.parseDouble(ssbAmountOfOil.toString());
			} else {
				amountOfOil = 0;
			}
			if (isValidDouble(ssbUnitPrice.toString())) {
				unitPrice = Double.parseDouble(ssbUnitPrice.toString());
			} else {
				unitPrice = 0;
			}
			if (isValidDouble(ssbOdometer.toString())) {
				tripMeter = Double.parseDouble(ssbOdometer.toString());
			} else {
				tripMeter = 0;
			}

			String comments = ssbComments.toString();

			// 給油量、単価、トリップメーター値のいずれもが0より大きい場合のみ、給油記録を追加する。
			//if (amountOfOil <= 0 || unitPrice <= 0 || tripMeter <= 0) {
			if (amountOfOil <= 0 || unitPrice <= 0) {
				Log.w("onClick#R.id.button_addRefuelRecord", "Can't add mileage record(Maybe no value is set in one of the variables?)");
				Log.w("onClick#R.id.button_addRefuelRecord", "amountOfOil : " + String.valueOf(amountOfOil));
				Log.w("onClick#R.id.button_addRefuelRecord", "unitPrice : " + String.valueOf(unitPrice));
				Log.w("onClick#R.id.button_addRefuelRecord", "tripMeter : " + String.valueOf(tripMeter));

				ret = -1;
			} else {
				// ランニングコストを計算する
				runningCosts = getRunningCostValue(amountOfOil, unitPrice, tripMeter);

				// 日時は、currentDateTimeをgetInstance()した時の値をそのまま使う(^^;)
				// UPDATE_MODEの値で処理を変える
				if (isUPDATE_MODE()) {
					ret = dbman.updatedMileageByRecordId(db, getRECORD_ID(), amountOfOil, tripMeter, unitPrice, comments, currentDateTime);
					retRunningCost = dbman.updateRunningCostRecord(db, targetCarId, runningCosts, currentDateTime, refuelRcordDate);
				} else {
					ret = dbman.addMileageById(db, targetCarId, amountOfOil, tripMeter, unitPrice, comments, currentDateTime);
					retRunningCost = dbman.addRunningCostRecord(db, targetCarId, runningCosts, currentDateTime);
				}

				// ランニングコスト追加の成否をLog出力する
				if (retRunningCost > 0) {
					Log.i("onClick#R.id.button_addRefuelRecord", "Running cost value added successfuly, row ID = " + String.valueOf(retRunningCost));

					// トータルのランニングコストと燃費を再計算する
					dbman.updateCurrentFuelMileageById(db, targetCarId);
					dbman.updateCurrentRunningCostById(db, targetCarId);
				} else {
					Log.w("onClick#R.id.button_addRefuelRecord", "Running cost value could not added.");
				}
			}

			if (ret == -1 ) {
				Log.d("button_addRefuelRecord_Click", "Failed to add Mileage record.");
			} else {
				Log.d("button_addRefuelRecord_Click", "Adding Mileage record successful. rowId = " + String.valueOf(ret));
				showToastMsg(db, targetCarId, currentDateTime, amountOfOil, tripMeter);

				// 「前の画面へ戻る」設定が有効の場合はActivityを終了させ、
				// そうでない場合は入力値を初期化する。
				if (returnPreviousView) {
					finish();
				} else {
					resetUi();
				}
			}

			break;

		case R.id.button_cancelAddRefuelRecord:		// キャンセルボタン
			// 画面表示を初期化する。
			resetUi();
			break;

		case R.id.button_editDate:					// 給油日の編集ボタン
			// 年、月、日をそれぞれ取得する
			int year = currentDateTime.get(Calendar.YEAR);
			int month = currentDateTime.get(Calendar.MONTH);
			int dayOfMonth = currentDateTime.get(Calendar.DAY_OF_MONTH);

			dpDialog = new DatePickerDialog(this, dpListener, year, month, dayOfMonth);
			dpDialog.show();
			break;

		case R.id.button_editTime:					// 給油時刻の編集ボタン
			// 時、分をそれぞれ取得する。
			int hour = currentDateTime.get(Calendar.HOUR_OF_DAY);
			int minute = currentDateTime.get(Calendar.MINUTE);

			// TimePickerDialogには、「24時間制表記にするか」の設定があるため、
			// 第5引数に端末の設定を判断した結果をセットしている。
			tpDialog = new TimePickerDialog(this, tpListener, hour, minute, isSetting24hourFormat());
			tpDialog.show();
			break;

		default:

			break;
		}
	}

	/**
	 * 端末の「日付と時刻の設定」で、時刻表記が24時間制か否かを判断する。
	 * @return boolean型、24時間制の場合はtrue、12時間制の場合はfalse
	 */
	private boolean isSetting24hourFormat() {
		boolean result = false;

		// 12、または24をString型で返してくるため、Stringの比較で判断する。
		String str = Settings.System.getString(getApplicationContext().getContentResolver(), Settings.System.TIME_12_24);
		String hours24 = "24";

		// 端末の設定が24時間制だった場合は、trueをセット
		if (hours24.equals(str)) {
			result = true;
		}

		return result;
	}

	/**
	 * 画面表示を初期化する。
	 */
	private void resetUi() {
		// EditTextに空の値をセットする
		editText_amountOfOil.setText(TEXT_BLANK);
		editText_dateOfRefuel.setText(TEXT_BLANK);
		editText_unitPrice.setText(TEXT_BLANK);
		EditText_odometer.setText(TEXT_BLANK);
		editText_comments.setText(TEXT_BLANK);
		editText_timeOfRefuel.setText(TEXT_BLANK);

		// スピナーに値をセットしなおす前に、開かれているCursorをいったん閉じる
		closeCursor();
		setSpinner(db, CAR_NAME);

		// 現在日付と現在時刻をeditTextにセットする
		currentDateTime = dmngr.getNow();
		setDateToEdit(currentDateTime);
		setTimeToEdit(currentDateTime);
	}

	/**
	 * 給油実績から給油時のランニングコストを返す。
	 * @param amountOfOil double型、その時の給油量
	 * @param unitPrice doble型、その時の給油単価
	 * @param tripMeter double型、その時のトリップメーター値
	 * @return 給油実績から計算したランニングコスト値
	 */
	private double getRunningCostValue(double amountOfOil, double unitPrice, double tripMeter) {
		double ret = 0;
		double runningCost = amountOfOil * unitPrice / tripMeter;

		// 小数点2ケタで四捨五入する
		BigDecimal bd = new BigDecimal(runningCost);
		ret = bd.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();

		return ret;
	}

	/**
	 * @return UPDATE_MODE
	 */
	public boolean isUPDATE_MODE() {
		return UPDATE_MODE;
	}

	/**
	 * @param uPDATE_MODE セットする uPDATE_MODE
	 */
	public void setUPDATE_MODE(boolean uPDATE_MODE) {
		UPDATE_MODE = uPDATE_MODE;
	}

	/**
	 * @return rECORD_ID
	 */
	public int getRECORD_ID() {
		return RECORD_ID;
	}

	/**
	 * @param rECORD_ID セットする rECORD_ID
	 */
	public void setRECORD_ID(int rECORD_ID) {
		RECORD_ID = rECORD_ID;
	}

	/**
	 * 編集モードでよばれた時の、編集する給油レコードをウィジェットへセットする。
	 * @param db
	 * @param carId
	 * @param recordId
	 */
	private void setModifyingRecords(SQLiteDatabase db, int carId, int recordId) {
		// Cursorのインデックスに使う値に名前付けをしておく
		int REFUEL_DATE = 1;
		//int CAR_ID    = 2;
		int LUB_AMOUNT  = 3;
		int UNIT_PRICE  = 4;
		int TRIPMETER   = 5;
		int COMMENTS    = 6;

		// 対象レコードを取得
		Cursor record = dbman.getLUBRecordByRecordId(db, recordId);

		// 取得したCursorから、ウィジェットに値をセットする
		editText_amountOfOil.setText(String.valueOf(record.getFloat(LUB_AMOUNT)));		// 給油量
		EditText_odometer.setText(String.valueOf(record.getFloat(TRIPMETER)));			// トリップメーター
		editText_unitPrice.setText(String.valueOf(record.getFloat(UNIT_PRICE)));		// 単価
		editText_comments.setText(record.getString(COMMENTS));							// 給油時コメント

		// 給油日時はユリウス通日なので、Calendarに変換する必要があるが、
		// あとで給油日時を再利用する必要があるので、いったんフィールドに格納する
		refuelRcordDate = record.getDouble(REFUEL_DATE);
		Calendar refuelDate = dmngr.jd2Calendar(refuelRcordDate);
		currentDateTime = dmngr.jd2Calendar(refuelRcordDate);

		// EditTextへ給油日時をセットする
		setDateToEdit(refuelDate);
		setTimeToEdit(refuelDate);

		// Cursorを閉じる
		record.close();
	}
}
