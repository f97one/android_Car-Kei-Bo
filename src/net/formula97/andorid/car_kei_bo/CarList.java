/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.preference.PreferenceActivity;

/**
 * @author kazutoshi
 *
 */
public class CarList extends Activity {

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	Cursor cCarList;

	// ウィジェットを扱うための定義
    TextView textView_CarListTitleContainer;
    TextView tv_label_value_defaultcar;
    TableLayout TableLayout1;
    ListView listView_CarList;
    Button button_addFuelRecord;

    /**
	 *
	 */
	public CarList() {
	}

	/* (非 Javadoc)
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

	}

	/* (非 Javadoc)
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

	/* (非 Javadoc)
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
		Intent configActivity = new Intent(this, Config.class);		// 設定画面
		Intent addCarActivity = new Intent(this, AddMyCar.class);	// 「クルマを追加」画面
		//Intent carListActivity = new Intent(this, CarList.class);	// 「クルマリスト」画面

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
		default:
			return false;
		}
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();
        cCarList.close();
		dbman.close();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();
        cCarList.close();
		dbman.close();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
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
	        String[] from = {"_id", "CAR_NAME", "CURRENT_FUEL_MILEAGE", "CURRENT_RUNNING_COST"};
	        int[] to = {R.id.tv_carID, R.id.tv_element_CarName, R.id.tv_value_FuelMileage, R.id.tv_value_RunningCosts};

	        SimpleCursorAdapter sca = new SimpleCursorAdapter(getApplicationContext(), R.layout.listviewelement_carlist, cCarList, from, to);
	        listView_CarList.setAdapter(sca);

	        // デフォルトカーの名前を取得してセット
	        tv_label_value_defaultcar.setText(dbman.getDefaultCarName(db));

        }

        dbman.close();
	}

}
