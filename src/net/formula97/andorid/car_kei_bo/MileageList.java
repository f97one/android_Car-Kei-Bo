/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * @author kazutoshi
 *
 */
/**
 * @author kazutoshi
 *
 */
public class MileageList extends Activity implements OnClickListener {

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
	TextView tv_element_totalAmountOfOil;
	TextView tv_value_totalAmountOfOil;
	TextView tv_unit_totalAmountOfOil;

	private DbManager dbman = new DbManager(this);
	//private DateManager dmngr = new DateManager();
	public static SQLiteDatabase db;

	Cursor cMileageList;
	Cursor cLvRow;
	Cursor selectedRow = null;

	int currentRecordId = 0;

	AlertDialog.Builder adbuilder;

	String priceUnit;
	String distanceUnit;
	String volumeUnit;

	boolean hasRecord;

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
		tv_element_totalAmountOfOil = (TextView)findViewById(R.id.tv_element_totalAmountOfOil);
		tv_value_totalAmountOfOil = (TextView)findViewById(R.id.tv_value_totalAmountOfOil);
		tv_unit_totalAmountOfOil = (TextView)findViewById(R.id.tv_unit_totalAmountOfOil);

		// 渡された引数を解析してグローバル変数に格納
		Intent i = getIntent();
		setCAR_ID(i.getIntExtra("CAR_ID", 0));
		setCAR_NAME(i.getStringExtra("CAR_NAME"));

		Log.d(getResources().toString(), "CAR_ID : " + String.valueOf(getCAR_ID()));
		Log.d(getResources().toString(), "CAR_NAME : " + getCAR_NAME());

		tv_element_CarName2.setText(getCAR_NAME());

		// AlertDialog周りの処理
		// ・ダイアログのタイトルは「Detail of refuel record（＝給油記録の詳細）」
		// ・端末のキャンセルキーでダイアログが消せるようにセット
		// ・ボタンはOKボタンのみ
		adbuilder = new AlertDialog.Builder(this);
		adbuilder.setTitle(R.string.adtitle_detail_of_refuel);
		adbuilder.setCancelable(true);
		adbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// AlertDialogでは、表示を消す以外のことをさせる予定はないため、
				// 何もせずそのまま抜ける。
			}
		});

	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (hasRecord) {
			closeCursor(cMileageList);
			//closeCursor(cLvRow);
		}
		closeDb(db);
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		if (hasRecord) {
			closeCursor(cMileageList);
			//closeCursor(cLvRow);
		}
		closeDb(db);
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		db = dbman.getReadableDatabase();

		hasRecord = dbman.hasLubRecords(db, getCAR_ID());

		// プリファレンスのSortOrderの値を読み出し、invertOrderに反映する。
		boolean invertOrder = getMileageOrder();

		// 各種単位の取得
		priceUnit = dbman.getPriceUnitById(db, getCAR_ID());
		distanceUnit = dbman.getDistanceUnitById(db, getCAR_ID());
		volumeUnit = dbman.getVolumeUnitById(db, getCAR_ID());

		cMileageList = dbman.getRefuelRecordsById(db, getCAR_ID(), invertOrder);

		String[] from = {
				//"_id",
				"DATE_OF_REFUEL",
				"LUB_AMOUNT",
				"VOLUMEUNIT"
				};
		int[] to = {
				//R.id.tv_value_number_refuel,
				R.id.tv_value_dateOfRefuel,
				R.id.tv_value_amountOfOil,
				R.id.tv_unit_amountOfOil
				};

		SimpleCursorAdapter sca = new SimpleCursorAdapter(getApplicationContext(), R.layout.listviewelemnt_mileagelist, cMileageList, from, to);
		lv_mileagelist.setAdapter(sca);

		// ListViewの要素を長押しした時のコンテキストメニューのリスナーをセット
		registerForContextMenu(lv_mileagelist);

		// ListViewの要素をクリックしたときのリスナーを宣言する
		// implementしてないので匿名メソッド
		lv_mileagelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// 取得したCursorから給油記録の詳細を取得する
				cLvRow = (Cursor)lv_mileagelist.getItemAtPosition(position);
	            for (int i =0; i < cLvRow.getColumnCount(); i++) {
	            	Log.i("onItemClick", "name of Column Index " + String.valueOf(i) + ":" + cLvRow.getColumnName(i) + " value = " + cLvRow.getString(i));
	            }

				int rowId = Integer.parseInt(cLvRow.getString(cLvRow.getColumnIndex("_id")));
				Log.d("onItemClick", "rowid is " + String.valueOf(rowId));

				// 給油記録の元ネタをDBから取得する
				Cursor cRefuelRecord = dbman.getRefuelRecordById(db, getCAR_ID(), rowId);
				Log.d("onItemClick", "returned rows = " + cRefuelRecord.getCount());
				for (int i = 0; i < cRefuelRecord.getColumnCount(); i++) {
					Log.i("onItemClick", "name of Column Index " + String.valueOf(i) + ":" + cRefuelRecord.getColumnName(i) + " value = " + cRefuelRecord.getString(i));
				}

				// AlertDialogに差し込むテキストの生成
				// 値の取得：計算用
				double dblDistance = cRefuelRecord.getDouble(5);	// 走行距離
				double dblOilAmount = cRefuelRecord.getDouble(3);	// 給油量
				double dblUnitprice = cRefuelRecord.getDouble(4);	// 給油単価

				// 給油時の燃費
				double dblMileage = dblDistance / dblOilAmount;
				BigDecimal bd1 = new BigDecimal(dblMileage);
				String strMileage = String.valueOf(bd1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

				// 給油時のランニングコスト
				double dblRunning = dblOilAmount * dblUnitprice / dblDistance;
				BigDecimal bd2 = new BigDecimal(dblRunning);
				String strRunningCost = String.valueOf(bd2.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

				// 値の取得
				String strRefuelDate = cLvRow.getString(1);				// 給油日時
				String strDistance = String.valueOf(dblDistance);		// 走行距離
				String strOilAmount = String.valueOf(dblOilAmount);		// 給油量
				String strUnitPrice = String.valueOf(dblUnitprice);		// 給油単価

				// AlertDialog内にセットするテキストの生成
				String adtext = getString(R.string.label_dateOfRefuel) + " : " + strRefuelDate + "\n" +
						getString(R.string.label_odometer) + " : " + strDistance + " " + distanceUnit + "\n" +
						getString(R.string.label_amountOfOil) + " : " + strOilAmount + " " + volumeUnit + "\n" +
						getString(R.string.label_unitPrice) + " : " + strUnitPrice + " " + priceUnit + "\n" +
						getString(R.string.label_fuelmileage) +  strMileage + " " + distanceUnit + "/" + volumeUnit + "\n" +
						getString(R.string.label_runningcost) +  strRunningCost + " " + priceUnit + "/" + distanceUnit;
				adbuilder.setMessage(adtext);

				// AlertDialogを表示する
				adbuilder.create();
				adbuilder.show();

				cRefuelRecord.close();
			}
		});

		btn_add_mileage.setOnClickListener(this);

		// 燃費とランニングコストの値を差し込む
		double txtMileage = dbman.getCurrentMileageById(db, getCAR_ID());
		double txtRunning = dbman.getCurrentRunningCostById(db, getCAR_ID());
		tv_value_FuelMileage2.setText(String.valueOf(txtMileage));
		tv_value_RunningCosts2.setText(String.valueOf(txtRunning));

		// 燃費とランニングコストの単位を差し込む
		tv_unit_fuelMileage2.setText(distanceUnit + "/" + volumeUnit);
		tv_unit_runningCosts2.setText(priceUnit + "/" + distanceUnit);

		setTotalAmountOfOil(getCAR_ID());
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

	/**
	 * ボタン等をクリックしたときの処理一式、onClickListenerが宣言されているウィジェットが対象。
	 * @param v View型、コールバックリスナーのあるビューのID
	 */
	@Override
	public void onClick(View v) {
		// コールバックリスナーのあるビューのIDを取得する
		int viewId = v.getId();

		switch (viewId) {
		case R.id.btn_add_mileage:
			// 燃費追加ボタンを押した時の処理
			Log.d("onClick", "btn_add_mileage pressed.");

			// 取得したCAR_IDとCAR_NAMEを引数にセットしてstartActivity
			Intent i = new Intent(getApplicationContext(), FuelMileageAdd.class);
			i.putExtra("CAR_ID", getCAR_ID());
			i.putExtra("CAR_NAME", getCAR_NAME());
			startActivity(i);

			break;
		default:
			break;
		}
	}

	/**
	 * 開かれているCursorオブジェクトを閉じる。
	 * @param c Cursor型、閉じる予定のCursorオブジェクト
	 */
	private void closeCursor(Cursor c) {
		if (c.isClosed() != true) {
			c.close();
			Log.d("closeCursor", "Cursor object closed, : " + c.toString());
		} else {
			Log.d("closeCursor", "Cursor object already closed.");
		}
	}

	/**
	 * 開かれているSQLiteDatabaseオブジェクトを閉じる。
	 * @param db SQLiteDatabase型、閉じる予定のSQLiteDatabaseオブジェクト
	 */
	private void closeDb(SQLiteDatabase db) {
		if (db.isOpen()) {
			db.close();

			Log.d("closeDb", "SQLiteDatabase is closed.");
		}
	}

	/**
	 * 累計給油量をTextViewへセットする
	 * @param carId int型、取得するクルマのCAR_ID
	 */
	private void setTotalAmountOfOil(int carId) {
		float totalFuel = dbman.getTotalOfRefuelById(db, carId);
		String unit = dbman.getVolumeUnitById(db, carId);

		tv_value_totalAmountOfOil.setText(String.valueOf(totalFuel));
		tv_unit_totalAmountOfOil.setText(unit);
	}

	/**
	 * プリファレンスから、燃費記録の表示順を決めるboolean値を読み出す。
	 * @return boolean型、trueなら降順、falseなら昇順を表す
	 */
	private boolean getMileageOrder() {
		boolean ret = false;

		// 目的のboolean設定値は、"MileageListSortOrder"という名前で保存されているので、
		// これをgetDefaultSharedPreferencesで読みだす
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		ret = pref.getBoolean("MileageListSortOrder", false);
		Log.d("getMileageOrder", "Mileage list sort order is " + new Boolean(ret).toString());

		return ret;
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO 自動生成されたメソッド・スタブ

		switch(item.getItemId()) {
		case R.id.ctxitem_edit_refuel_record:
			// 燃費記録を修正する処理
			break;
		case R.id.ctxitem_delete_refuel_record:
			// 燃費記録を削除する処理
			break;
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onContextMenuClosed(android.view.Menu)
	 */
	@Override
	public void onContextMenuClosed(Menu menu) {
		// TODO 自動生成されたメソッド・スタブ
		super.onContextMenuClosed(menu);

		// DBとCursorを閉じてActivityを再始動する
		closeCursor(cMileageList);
		closeDb(db);

		onResume();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreateContextMenu(menu, v, menuInfo);

		// 呼び出されたListViewの要素位置を取得する
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo)menuInfo;
		selectedRow = (Cursor)lv_mileagelist.getItemAtPosition(acmi.position);
		currentRecordId = selectedRow.getInt(selectedRow.getColumnIndex("_id"));

		getMenuInflater().inflate(R.menu.context_mileagelist, menu);
		menu.setHeaderTitle(getString(R.string.ctxmenutitle_mileagelist));

		// Cursorを閉じる。
		// ※副作用で、現在表示されている燃費記録リストが消える。
		selectedRow.close();
	}

	private void callEditMileage() {
		// TODO 修正するレコードのrowIdを引き渡してstartActivity
	}

	private void deleteMileage(SQLiteDatabase db) {
		// TODO 消去するレコードのrowIdを指定してDBから消去
	}
}
