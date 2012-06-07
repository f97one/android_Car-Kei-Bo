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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * クルマリストを表示するActivity
 * @author kazutoshi
 *
 */
public class CarList extends Activity implements OnItemClickListener,
		OnItemLongClickListener, OnClickListener {

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	Cursor cCarList = null;

	// ウィジェットを扱うための定義
    TextView textView_CarListTitleContainer;
    TextView tv_label_value_defaultcar;
    TableLayout TableLayout1;
    ListView listView_CarList;
    Button button_addFuelRecord;

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
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);
        setContentView(R.layout.carlist);

        // ウィジェット
        //   プログラムから扱うための定数を検索してセット
        textView_CarListTitleContainer = (TextView)findViewById(R.id.textView_CarListTitleContainer);
        tv_label_value_defaultcar = (TextView)findViewById(R.id.tv_label_value_defaultcar);
        button_addFuelRecord = (Button)findViewById(R.id.button_addFuelRecord);
        listView_CarList = (ListView)findViewById(R.id.listView_CarList);

        // イベントリスナーのセット
        listView_CarList.setOnItemLongClickListener(this);
        listView_CarList.setOnItemClickListener(this);
        button_addFuelRecord.setOnClickListener(this);
	}

	/**
	 * Menuキーを押した段階で呼び出される処理。
	 * @param menu 項目を配置したメニューを表示
	 * @return trueにするとメニューを表示、falseだと表示しない
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO 自動生成されたメソッド・スタブ
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
		//Intent configActivity = new Intent(this, Config.class);		// 設定画面
		Intent addCarActivity = new Intent(this, AddMyCar.class);	// 「クルマを追加」画面
		//Intent carListActivity = new Intent(this, CarList.class);	// 「クルマリスト」画面

		switch (item.getItemId()) {
		case R.id.optionsmenu_closeAPP:
			// アプリを終了させる
			finish();
			return true;
//		case R.id.optionsmenu_call_preference:
//			// 設定画面を呼び出す
//			startActivity(configActivity);
//			return true;
		case R.id.optionsmenu_addcar:
			// 「クルマを追加」画面を呼び出す
			startActivity(addCarActivity);
			return true;
		case R.id.optionsmenu_carlist:
			// 「クルマリスト」画面を呼び出す
			//   CarListはこのクラスなので、呼び出しは行わずtrueのみ返す
			//startActivity(carListActivity);
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
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();

		// CursorとDBが閉じていなければそれぞれを閉じる
		if (db.isOpen()) {
			if (dbman.hasCarRecords(db)) {
				if (cCarList.isClosed() != true) {
	        		cCarList.close();
				}
			}
			Log.d("CarList#onPause()","SQLite database is closing.");
			dbman.close();
		}
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
		if (db.isOpen()) {
			if (dbman.hasCarRecords(db)) {
				if (cCarList.isClosed() != true) {
	        		cCarList.close();
				}
			}
			Log.d("CarList#onDestroy()","SQLite database is closing.");
			dbman.close();
		}
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
            Log.i("CAR_MASTER", "Got " + String.valueOf(cCarList.getCount()) + " records, including " + String.valueOf(cCarList.getColumnCount()) + " columns.");
            for (int i =0; i < cCarList.getColumnCount(); i++) {
            	Log.i("CAR_MASTER", "name of Column Index " + String.valueOf(i) + ":" + cCarList.getColumnName(i));
            }

	        // AdapterからListViewへ差し込むデータの整形
	        String[] from = {	"_id",
	        					"CAR_NAME",
	        					"CURRENT_FUEL_MILEAGE",
	        					"FUELMILEAGE_LABEL",
	        					"CURRENT_RUNNING_COST",
	        					"RUNNINGCOST_LABEL"};
	        int[] to = {R.id.tv_carID,
	        			 R.id.tv_element_CarName,
	        			 R.id.tv_value_FuelMileage,
	        			 R.id.tv_unit_fuelMileage,
	        			 R.id.tv_value_RunningCosts,
	        			 R.id.tv_unit_runningCosts};

	        SimpleCursorAdapter sca = new SimpleCursorAdapter(getApplicationContext(), R.layout.listviewelement_carlist, cCarList, from, to);
	        listView_CarList.setAdapter(sca);

	        // デフォルトカーの名前を取得してセット
	        tv_label_value_defaultcar.setText(dbman.getDefaultCarName(db));

//	        listView_CarList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//	        	public void onItemClick(AdapterView<?> parent, View v, int position,
//						long id) {
//					// TODO 自動生成されたメソッド・スタブ
//	        		// とりあえず、LogCatに流して挙動を観察
//	        		Log.d("onItemClick", "ListView item pressed.");
//	        		Log.d("onItemClick", "parent = " + parent.toString());
//	        		Log.d("onItemClick", "v = " + v.toString());
//	        		Log.d("onItemClick", "position = " + String.valueOf(position));
//	        		Log.d("onItemClick", "id = " + String.valueOf(id));
//				}
//	        });
//
//	        listView_CarList.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//				public boolean onItemLongClick(AdapterView<?> parent, View v,
//						int position, long id) {
//					// TODO 自動生成されたメソッド・スタブ
//					// とりあえず、LogCatに流して挙動を観察
//					Log.d("onItemLongClick", "ListView item long pressed.");
//					Log.d("onItemLongClick", "parent = " + parent.toString());
//					Log.d("onItemLongClick", "v = " + v.toString());
//					Log.d("onItemLongClick", "position = " + String.valueOf(position));
//					Log.d("onItemLongClick", "id = " + String.valueOf(id));
//
//					return false;
//				}
//			});

        }
	}

	/**
	 * ListViewのアイテムをタッチしたときの処理、そのクルマの燃費記録を表示するActivityを呼ぶ。
	 * @param parent AdapterView<>型、リストアダプタの親要素
	 * @param v View型、ビューの要素
	 * @param position int型、タッチされた要素の番号
	 * @param id long型、アイテムID
	 */
	//@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// TODO 自動生成されたメソッド・スタブ
		// とりあえず、LogCatに流して挙動を観察
		Log.d("onItemClick", "ListView item pressed.");
		Log.d("onItemClick", "parent = " + parent.toString());
		Log.d("onItemClick", "v = " + v.toString());
		Log.d("onItemClick", "position = " + String.valueOf(position));
		Log.d("onItemClick", "id = " + String.valueOf(id));
	}

	/**
	 * ListViewのアイテムを長押ししたときの処理
	 *   そのクルマに関するコンテキストメニューを表示し、それぞれの処理に振り分ける。
	 * @param parent AdapterView<>型、リストアダプタの親要素
	 * @param v View型、ビューの要素
	 * @param position int型、タッチされた要素の番号
	 * @param id long型、アイテムID
	 * @return
	 */
	//@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		// とりあえず、LogCatに流して挙動を観察
		Log.d("onItemLongClick", "ListView item long pressed.");
		Log.d("onItemLongClick", "parent = " + parent.toString());
		Log.d("onItemLongClick", "v = " + v.toString());
		Log.d("onItemLongClick", "position = " + String.valueOf(position));
		Log.d("onItemLongClick", "id = " + String.valueOf(id));
		//return false;
		return true;
	}

	public void onClick(View v) {
		// TODO 自動生成されたメソッド・スタブ
		// とりあえず、LogCatに流して挙動を観察
		Log.d("onClick", "Button pressed.");
		Log.d("onClick", "v = " + v.toString());

	}
}
