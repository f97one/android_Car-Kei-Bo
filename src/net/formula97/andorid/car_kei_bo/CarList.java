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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.preference.PreferenceActivity;

/**
 * @author kazutoshi
 *
 */
public class CarList extends Activity {

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	// ウィジェットを扱うための定義
    Button button_addFuelRecord;
    ListView listView_CarList;
    TextView tv_label_value_defaultcar;
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
        button_addFuelRecord = (Button)findViewById(R.id.button_addFuelRecord);
        listView_CarList = (ListView)findViewById(R.id.listView_CarList);
        tv_label_value_defaultcar = (TextView)findViewById(R.id.tv_label_value_defaultcar);

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

		// 設定画面を呼び出すためのインテント宣言
		Intent configActivity = new Intent(this, Config.class);

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
			return true;
		case R.id.optionsmenu_carlist:
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
		db.close();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();
		db.close();
	}

	/* (非 Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();

        // 編集可能な状態でDBを開く
		db = dbman.getWritableDatabase();

		// クルマリストとデフォルトカーの表示処理
        //ListView listView_CarList = (ListView)findViewById(R.id.listView_CarList);
        //TextView tv_label_value_defaultcar = (TextView)findViewById(R.id.tv_label_value_defaultcar);

        // クルマリストのArrayをつくる
        //   count()の結果が1レコード以上ないとAdapterが作成できないので、CAR_MASTERに１レコード以上あるかを調べ、
        //   あった場合のみAdapterをつくる
        if (dbman.hasCarRecords(db)) {
            Cursor cCarList = dbman.getCurrentMileageData(db);
	        cCarList.moveToFirst();
	        String[] from = {"A.CAR_NAME", "CURRENT_FUEL_MILEAGE", "CURRENT_RUNNING_CONSTS"};
	        int[] to = {R.id.ctv_element_CarName, R.id.tv_value_FuelMileage, R.id.tv_value_RunningCosts};

	        SimpleCursorAdapter sca = new SimpleCursorAdapter(getApplicationContext(), R.layout.listviewelement_carlist, cCarList, from, to);
	        listView_CarList.setAdapter(sca);

	        // デフォルトカーの名前を取得してセット

	        tv_label_value_defaultcar.setText(dbman.getDefaultCarId(db));
        }
	}

}
