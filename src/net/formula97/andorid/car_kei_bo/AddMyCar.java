package net.formula97.andorid.car_kei_bo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class AddMyCar extends Activity {

	// ウィジェットを扱うための定義
	TextView textview_addCarName;
	CheckBox checkbox_setDefault;
	Button button_addCar;
	Button button_cancel_addCar;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addcar);

        // ウィジェットを扱うための定義
        //   プログラムから扱うための定数を検索してセット
		textview_addCarName = (TextView)findViewById(R.id.textview_addCarName);
		checkbox_setDefault = (CheckBox)findViewById(R.id.checkBox_SetDefault);
		button_addCar = (Button)findViewById(R.id.button_addCar);
		button_cancel_addCar = (Button)findViewById(R.id.button_cancel_addCar);

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

		/*
		 * ボタンの配置を画面幅の1/2にする処理
		 *
		 * onCreate()ではなくこちらに書くのは、最終的な画面設定が行われるのがこちらという
		 * Androidのくせによるものである。
		 */

		// 画面幅を取得
		int displayWidth = getWindowManager().getDefaultDisplay().getWidth();

		// ボタンの幅を、取得した画面幅の1/2にセット
		button_addCar.setWidth(displayWidth / 2);
		button_cancel_addCar.setWidth(displayWidth / 2);
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
}