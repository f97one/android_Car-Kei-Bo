/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * クルマリストを表示するActivity
 * @author kazutoshi
 */
public class CarList extends Activity implements OnClickListener {

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	Cursor cCarList = null;
	Cursor selectedRow = null;

	// ウィジェットを扱うための定義
	TextView textView_CarListTitleContainer;
	TextView tv_label_value_defaultcar;
	TableLayout TableLayout1;
	ListView listView_CarList;
	Button button_addFuelRecord;

	// DBから取得したデフォルト値を格納する変数
	private int defaultCarID;
	private String defaultCarName;

	// ListViewのカレント値を格納する変数
	private int currentCarID;
	private String currentCarName;

	private String externalFile;

	/**
	 * SDカードとやりとりするためのファイル名を取得する。
	 * @return String型、externalFileフィールドの値を返す
	 */
	String getExternalFile() {
		Log.d("getExternalFile", "returned external file name = " + externalFile);
		return externalFile;
	}

	/**
	 * SDカードとやりとりするためのファイル名をセットする。
	 * @param externalFile String型、externalFileフィールドにセットする値
	 */
	void setExternalFile(String externalFile) {
		this.externalFile = externalFile;
		Log.d("setExternalFile", "set external file name = " + getExternalFile());
	}

	/**
	 * 明示的コンストラクタ
	 *   Activityの場合、onCreate()がコンストラクタの役割を果たすので、
	 *   特に処理を書かなくても成立する。
	 */
	public CarList() {
	}

	/**
	 * Activity初期化処理、ここではウィジェット類のIDを取得している。
	 * @param savedInstanceState Bundle型、インスタンスを取得したときの状態
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.carlist);

		// ウィジェット
		//   プログラムから扱うための定数を検索してセット
		textView_CarListTitleContainer = (TextView) findViewById(R.id.textView_CarListTitleContainer);
		tv_label_value_defaultcar = (TextView) findViewById(R.id.tv_label_value_defaultcar);
		button_addFuelRecord = (Button) findViewById(R.id.button_addFuelRecord);
		listView_CarList = (ListView) findViewById(R.id.listView_CarList);
	}

	/**
	 * Menuキーを押した段階で呼び出される処理。
	 * @param menu 項目を配置したメニューを表示
	 * @return trueにするとメニューを表示、falseだと表示しない
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// MenuInflater型のオブジェクトを、getMenuInflater()で初期化
		MenuInflater inflater = getMenuInflater();

		// res/menu/menu.xmlの記述に従い、メニューを展開する
		inflater.inflate(R.menu.optionsmenu, menu);
		return true;
	}

	/**
	 * [Menu]キーを押したとき、システムから呼ばれる。
	 *   XMLで定義しているメニューに応じた処理を行わせる。
	 * @param item MenuItem型、選択されたメニューアイテムを格納
	 * @return boolean型、trueにするとアイテム有効、falseは無効
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		 * switch文でそれぞれのメニューに対するアクションへ分岐する。
		 * メニュー項目を増やしたら、アクションを追加すること。
		 * ....て、別クラスにすれば修正箇所を集約できると思うが、できんのか？
		 */

		// 別画面呼び出しのためのインテント宣言
		Intent configActivity = new Intent(this, Config.class); // 設定画面
		Intent addCarActivity = new Intent(this, AddMyCar.class); // 「クルマを追加」画面
		Intent carListActivity = new Intent(this, CarList.class); // 「クルマリスト」画面

		switch (item.getItemId()) {
		case R.id.optionsmenu_closeAPP:
			// アプリを終了させる
			finish();
			return true;
		case R.id.optionsmenu_call_preference:
			// 設定画面を呼び出す
			startActivity(configActivity);
			return true;
		case R.id.optionsmenu_addcar:
			// 「クルマを追加」画面を呼び出す
			startActivity(addCarActivity);
			return true;
		case R.id.optionsmenu_carlist:
			// 「クルマリスト」画面を呼び出す
			//   CarListはこのクラスなので、呼び出しは行わずtrueのみ返す
			//startActivity(carListActivity);
			return true;
		case R.id.export_sd:
			// SDカードエクスポートのダイアログを表示する
			createExportMenu();
			return true;
		case R.id.import_sd:
			// SDカードインポートのダイアログを表示する
			createImportMenu();
			return true;
		default:
			return false;
		}
	}

	/**
	 * ActivityがほかのActivityに遷移するとき、システムから呼ばれる。
	 *   DBとCursorが開いていたら閉じる。
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		// CursorとDBが閉じていなければそれぞれを閉じる
		closeDbAndCursorIfOpen();
	}

	/**
	 * Activityが破棄されるとき、システムから呼ばれる。
	 *   DBとCursorが開いていたら閉じる。
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();

		// CursorとDBが閉じていなければそれぞれを閉じる
		closeDbAndCursorIfOpen();
	}

	/**
	 * Activityの初期化後、システムから呼ばれる。
	 *   最終的な画面描画の調整と、クルマリストの表示を行うのに使用。
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// 参照専用でDBを開く
		db = dbman.getReadableDatabase();

		// クルマリストとデフォルトカーの表示処理
		// クルマリストのArrayをつくる
		//   count()の結果が1レコード以上ないとAdapterが作成できないので、CAR_MASTERに１レコード以上あるかを調べ、
		//   あった場合のみAdapterをつくる
		if (dbman.hasCarRecords(db)) {
			// Adapterのもととなるレコードの取得
			cCarList = dbman.getCarList(db);
			Log.i("CAR_MASTER",
					"Got " + String.valueOf(cCarList.getCount()) + " records, including "
							+ String.valueOf(cCarList.getColumnCount()) + " columns.");
			for (int i = 0; i < cCarList.getColumnCount(); i++) {
				Log.i("CAR_MASTER", "name of Column Index " + String.valueOf(i) + ":" + cCarList.getColumnName(i));
			}

			// AdapterからListViewへ差し込むデータの整形
			String[] from = { "CAR_NAME",
					"CURRENT_FUEL_MILEAGE",
					"FUELMILEAGE_LABEL",
					"CURRENT_RUNNING_COST",
					"RUNNINGCOST_LABEL" };
			int[] to = { R.id.tv_element_CarName,
					R.id.tv_value_FuelMileage,
					R.id.tv_unit_fuelMileage,
					R.id.tv_value_RunningCosts,
					R.id.tv_unit_runningCosts };

			SimpleCursorAdapter sca = new SimpleCursorAdapter(getApplicationContext(),
					R.layout.listviewelement_carlist, cCarList, from, to);
			listView_CarList.setAdapter(sca);

			// コンテキストメニュー表示を車クルマリストに対して登録をする
			registerForContextMenu(listView_CarList);

			// イベントリスナーのセット
			button_addFuelRecord.setOnClickListener(this); // ボタン

			// 別画面呼び出し用に、デフォルト値を格納する
			defaultCarID = dbman.getDefaultCarId(db);
			defaultCarName = dbman.getDefaultCarName(db);

			// デフォルトカーの名前を取得してセット
			tv_label_value_defaultcar.setText(defaultCarName);

			// イベントリスナ（onItemClick）
			listView_CarList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position,
						long id) {
					// とりあえず、LogCatに流して挙動を観察
					Log.d("onItemClick", "ListView item pressed.");
					Log.d("onItemClick", "parent = " + parent.toString());
					Log.d("onItemClick", "v = " + v.toString());
					Log.d("onItemClick", "position = " + String.valueOf(position));
					Log.d("onItemClick", "id = " + String.valueOf(id));

					// 呼び出されたListViewの要素位置を取得する
					selectedRow = (Cursor) listView_CarList.getItemAtPosition(position);

					// カレント値を変数に格納
					currentCarID = selectedRow.getInt(selectedRow.getColumnIndex("_id"));
					currentCarName = selectedRow.getString(selectedRow.getColumnIndex("CAR_NAME"));

					selectedRow.close();

					// クルマの燃費記録一覧画面を呼び出す
					showMileageList(currentCarID, currentCarName);
				}
			});

		}
	}

	/**
	 * 「燃費記録を追加」ボタンを押すことで、デフォルトカーに燃費記録を追加する。
	 * @param v View型、クリックされたView
	 * @see android.view.View.OnClickListener#onClick(View)
	 */
	//@Override
	@Override
	public void onClick(View v) {
		// デフォルトカーについての燃費記録画面を表示する
		// とりあえず、LogCatに流して挙動を観察
		Log.d("onClick", "Button pressed.");
		Log.d("onClick", "v = " + v.toString());

		// 燃費記録追加画面を呼び出す
		addMileage(defaultCarID, defaultCarName);
	}

	/**
	 * コンテキストメニューの項目を選択した時の処理。
	 * @param item MenuItem型、選択されたメニュー項目
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.ctxitem_add_mileage:
			// 燃費記録追加画面を呼び出す
			addMileage(currentCarID, currentCarName);
			break;
		case R.id.ctxitem_delete_car:
			// クルマを削除する
			deleteCar(currentCarID, currentCarName);
			break;
		//		case R.id.ctxitem_edit_car_preference:
		//			// クルマの設定を変更する
		//			editCarPreference(currentCarID, currentCarName);
		//			break;
		case R.id.ctxitem_set_default_car:
			// デフォルトカーにする
			changeAsDefault(currentCarID, currentCarName);
			break;
		case R.id.ctxitem_show_mileage:
			// 燃費記録一覧を表示
			showMileageList(currentCarID, currentCarName);
			break;
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	/**
	 * コンテキストメニューを閉じたときの処理。
	 *   onCreateContextMenu()の最後でCursor selectedRowを閉じているが、
	 *   その副作用で画面表示されているクルマリストが消失してしまうため、
	 *   描画処理のあるonResume()をコールしている。
	 * @param menu Menu型、閉じられようとしているMenu
	 * @see android.app.Activity#onContextMenuClosed(Menu)
	 */
	@Override
	public void onContextMenuClosed(Menu menu) {
		// TODO 自動生成されたメソッド・スタブ
		super.onContextMenuClosed(menu);

		// DBとCursorを閉じてActivityを再始動する
		closeDbAndCursorIfOpen();
		onResume();
	}

	/**
	 * コンテキストメニューの生成を行う。
	 *   ここでは、XMLの記述に従いコンテキストメニューを展開している。
	 * @param menu ContextMenu型
	 * @param v View型
	 * @param menuInfo ContextMenuInfo型
	 * @see android.view.View.OnCreateContextMenuListener#onCreateContextMenu(ContextMenu, View, ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		// 呼び出されたListViewの要素位置を取得する
		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
		selectedRow = (Cursor) listView_CarList.getItemAtPosition(acmi.position);

		// カレント値を変数に格納
		currentCarID = selectedRow.getInt(selectedRow.getColumnIndex("_id"));
		currentCarName = selectedRow.getString(selectedRow.getColumnIndex("CAR_NAME"));

		// LocCatに流して挙動を観察
		Log.d("onCreateContextMenu", "ContextMenu created, v = " + String.valueOf(v.getId()));
		Log.d("onCreateContextMenu", "row number = " + currentCarID);
		Log.d("onCreateContextMenu", "Car Name = " + currentCarName);

		// XMLの記述に従い、コンテキストメニューを展開する
		getMenuInflater().inflate(R.menu.context_carlist, menu);
		menu.setHeaderTitle(getString(R.string.ctxmenutitle_carlist));

		// Cursorを閉じる。
		// ※副作用で、現在表示されているクルマリストが消える。
		selectedRow.close();
	}

	/**
	 * 燃費記録リストを表示するActivityを呼び出す。
	 * Activity呼び出しがメインなので、戻り値はvoidとした。
	 * @param carId int型、燃費リスト画面に引き渡すクルマのCAR_ID値。
	 * @param carName String型、燃費リスト画面に引き渡すクルマのCAR_NAME値。
	 */
	protected void showMileageList(int carId, String carName) {
		// 画面遷移の前に、DBとCursorを閉じる。
		closeDbAndCursorIfOpen();

		Log.d(getResources().toString(), "CAR_ID : " + String.valueOf(carId));
		Log.d(getResources().toString(), "CAR_NAME : " + carName);

		// 取得したCAR_IDとCAR_NAMEを引数にセットしてstartActivity
		Intent i = new Intent(getApplicationContext(), MileageList.class);
		i.putExtra("CAR_ID", carId);
		i.putExtra("CAR_NAME", carName);
		startActivity(i);

	}

	/**
	 * 燃費記録を追加するActivityを呼び出す。
	 * @param carId int型、呼び出すクルマのCAR_ID
	 * @param carName String型、呼び出すクルマのCAR_NAME
	 */
	protected void addMileage(int carId, String carName) {
		// 画面遷移の前に、DBとCursorを閉じる。
		closeDbAndCursorIfOpen();

		// 取得したCAR_IDとCAR_NAMEを引数にセットしてstartActivity
		Intent i = new Intent(getApplicationContext(), FuelMileageAdd.class);
		i.putExtra("CAR_ID", carId);
		i.putExtra("CAR_NAME", carName);
		startActivity(i);
	}

	/**
	 * 選択したクルマをデフォルトに切り替える。
	 * 再描画はonContextMenuClosed(int)の中に定義しているため、特にここでは何もしていない。
	 * @param carId int型、デフォルトに切り替えるクルマのCAR_ID
	 * @param carName String型、デフォルトに切り替えるクルマのCAR_NAME
	 */
	protected void changeAsDefault(int carId, String carName) {
		int iRet = dbman.changeDefaultCar(db, carId);

		Log.d("changeAsDefault", String.valueOf(iRet) + " row(s) updated.");
		Log.d("changeAsDefault", "Set as default car, CAR_ID = " + String.valueOf(carId));
		Log.d("changeAsDefault", "CAR_NAME = " + carName);
	}

	/**
	 * クルマの設定を変更する。
	 */
	protected void editCarPreference(int carId, String carName) {

	}

	/**
	 * クルマを削除する。
	 * このクラス内では、getReadableDatabase()でDBを開いているが、Androidにおいては
	 * ディスクフルでもない限り書き込みができる仕様なので、これで問題ない。
	 * @param carId int型、削除するクルマのCAR_ID
	 * @param carName String型、削除するクルマのCAR_NAME
	 * @see android.database.sqlite.SQLiteDatabase#delete(String, String, String[])
	 */
	protected void deleteCar(final int carId, final String carName) {
		// TODO 削除確認を行うポップアップダイアログを表示させる
		AlertDialog.Builder adbuilder = new AlertDialog.Builder(this);
		adbuilder.setTitle(carName);
		adbuilder.setMessage(getString(R.string.adbuilder_confirm_deletecar));
		// [back]キーでキャンセルができないようにする
		adbuilder.setCancelable(false);

		// 「はい」ボタンの処理
		adbuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// TODO 自動生成されたメソッド・スタブ
				int result;

				// 削除前に車の名前を取得しておく
				String carname = dbman.getCarNameById(db, carId);

				// クルマのレコードを削除する
				result = dbman.deleteCarById(db, carId);
				Log.d("deleteCar", "car record deleted, CAR_ID = " + String.valueOf(carId));
				Log.d("deleteCar", "car record deleted, CAR_NAME = " + carName);
				Log.d("deleteCar", "deleted records = " + String.valueOf(result));

				// TODO 給油記録とランニングコスト記録を削除するか否かの
				//      確認ダイアログを表示させる
				// 給油記録を削除
				result = dbman.deleteLubsByCarId(db, carId);
				Log.d("deleteCar", "lub record deleted, CAR_ID = " + String.valueOf(carId));
				Log.d("deleteCar", "lub record deleted, CAR_NAME = " + carName);
				Log.d("deleteCar", "deleted records = " + String.valueOf(result));

				// ランニングコスト記録を削除
				result = dbman.deleteCostsByCarId(db, carId);
				Log.d("deleteCar", "costs record deleted, CAR_ID = " + String.valueOf(carId));
				Log.d("deleteCar", "costs record deleted, CAR_NAME = " + carName);
				Log.d("deleteCar", "deleted records = " + String.valueOf(result));

				// DBを再編成する
				dbman.reorgDb(db);

				// レコードを消したというトーストを表示する。
				String line = carname + getString(R.string.adbuilder_toast_deleterecord);
				Toast.makeText(getApplicationContext(), line, Toast.LENGTH_LONG).show();

				// DBとCursorを閉じてActivityを再始動する
				closeDbAndCursorIfOpen();
				onResume();
			}
		});

		// 「キャンセル」ボタンの処理
		//   noなので「いいえ」かと思ったのだが....。
		//   何もせずに終了する。
		adbuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		// AlertDialogを表示する
		adbuilder.show();
	}

	/**
	 * CursorとDBが開いていたら閉じる。
	 * 引数なし、グローバル変数を使用。エレガントではないのはわかってはいるが。
	 */
	protected void closeDbAndCursorIfOpen() {
		if (db.isOpen()) {
			if (dbman.hasCarRecords(db)) {
				if (cCarList.isClosed() != true) {
					cCarList.close();
				}
			}
			Log.d(getApplication().toString(), "SQLite database is closing.");
			dbman.close();
		}

		//		if (selectedRow.isClosed() != true) {
		//			selectedRow.close();
		//		}
	}

	/**
	 * 「SDカードへのエクスポート」メニューを作成する。
	 */
	private void createExportMenu() {
		Log.d("createEnportMenu", "called export method.");
		AlertDialog.Builder adbuildr = new AlertDialog.Builder(this);

		// カスタムビューを使うためのView定義
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.import_dialog, null);
		final EditText editText_exportFilename = (EditText) view.findViewById(R.id.editText_exportFilename);

		adbuildr.setTitle(R.string.export_to_sd)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(view)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自動生成されたメソッド・スタブ
						setExternalFile(editText_exportFilename.getText().toString());
						writeToSD(getExternalFile());
					}

				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// テキストボックスの入力値をクリアしているが、
						// キャンセルボタンを押した瞬間にダイアログが閉じるので意味なし(^^;)
						editText_exportFilename.setText("");
					}
				})
				.show();

	}

	/**
	 * 「SDカードからのインポート」メニューを作成する。
	 */
	private void createImportMenu() {
		Log.d("createEnportMenu", "called export method.");

		AlertDialog.Builder adbuilder = new AlertDialog.Builder(this);

		adbuilder.setTitle(R.string.import_caution_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.import_caution_body)
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自動生成されたメソッド・スタブ

					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自動生成されたメソッド・スタブ

					}
				})
				.show();
	}

	/**
	 * SDカードにテーブルデータをエクスポートする。
	 * @param filename String型、エクスポートするファイル名
	 */
	private void writeToSD(String filename) {
		String fullpath = Environment.getExternalStorageDirectory().getPath() + "/" + filename;

		writeAllData(fullpath, DbManager.CAR_MASTER, false);
		writeAllData(fullpath, DbManager.LUB_MASTER, true);
		writeAllData(fullpath, DbManager.COSTS_MASTER, true);

	}

	/**
	 * テーブルからデータを取得し、指定したパスのファイルへ書き込む。
	 * @param fullpath
	 * @param targetTableName
	 * @param append
	 */
	private void writeAllData(String fullpath, String targetTableName, boolean append) {

		if (append == false) {
			writeDBVersionHeader(fullpath, db.getVersion());
		}

		try {
			File target = new File(fullpath);
			Cursor targetTable = dbman.getWholeRecords(targetTableName, db);

			int maxColumn = targetTable.getColumnCount();
			String[] headerWords = new String[maxColumn];
			headerWords = targetTable.getColumnNames().clone();

			BufferedWriter bw = new BufferedWriter(new FileWriter(target, true));
			StringBuilder sb = new StringBuilder();

			// 最初にヘッダ行をかく
			bw.write("Table name : " + targetTableName);
			bw.newLine();

			for (String str: headerWords) {
				sb.append(str).append(",");
			}
			String header = sb.append(headerWords[maxColumn - 1]).toString();
			Log.d("writeToSD", "header string = " + header);

			bw.write(header);
			bw.newLine();

			if (targetTable.isFirst() == false) {
				targetTable.moveToFirst();
			}

			while(targetTable.isAfterLast() == false) {
				StringBuilder sbRow = new StringBuilder();

				for (int i = 0; i < maxColumn -1; i++) {
					sbRow.append(targetTable.getString(i)).append(",");
				}
				String rowLine = sbRow.append(targetTable.getString(maxColumn -1)).toString();

				bw.write(rowLine);
				bw.newLine();

				targetTable.moveToNext();
			}

			bw.newLine();
			bw.close();
			if (targetTable.isClosed() == false) targetTable.close();

		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * 目標のファイルが書き込み可能かどうかをチェックする。
	 * @param targetFile File型、書き込みを開始する予定のファイル
	 * @return boolean型、目標のファイルが存在し、かつ書き込み可能である場合はtrue、そうでない場合はfalse
	 */
	private boolean isReadyToWriteFile(File targetFile) {
		if (targetFile.exists()) {
			if(targetFile.isFile() && targetFile.canWrite()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * DBのバージョンをファイルに書き込む。
	 * @param fullpath String型、書き込むファイルのフルパス
	 * @param dbVersion 書き込むDBのバージョン番号
	 */
	private void writeDBVersionHeader(String fullpath, int dbVersion) {
		try {
			File target = new File(fullpath);
			StringBuilder sb = new StringBuilder();
			BufferedWriter bw = new BufferedWriter(new FileWriter(target, false));
			bw.write("database version : " + String.valueOf(dbVersion));
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}
}