package net.formula97.andorid.car_kei_bo;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class AddMyCar extends Activity {

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	private static final boolean FLAG_DEFAULT_ON = true;
	private static final boolean FLAG_DEFAULT_OFF = false;

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
		dbman.close();
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

	/*
	 * 「クルマを追加」ボタンを押したときの処理
	 *   GUIに紐付けるときは、publicメソッドにしなきゃいけないようだ
	 */
	public void onClickAddCar(View v) {
		String carName;
		boolean defaultFlags;

		db = dbman.getWritableDatabase();

		// TextViewに入力された値を取得
		//   getText()はCaheSequence型になるので、Stringにキャストする
		SpannableStringBuilder sp = (SpannableStringBuilder) textview_addCarName.getText();
		carName = sp.toString();
		Log.w("CarList", "New Car name = " + carName);

		// チェックボックスの状態を取得
		if (checkbox_setDefault.isChecked()) {
			/*
			 * チェックボックスにチェックがあれば、
			 *   1.デフォルトフラグがすでにセットされているかを調べ、
			 *   2.セットされていればいったんすべてのデフォルトフラグを下げる
			 */
			if (dbman.isExistDefaultCarFlag(db)) {
				int iRet = dbman.clearAllDefaultFlags(db);
				// デフォルトフラグを下げたことをログに出力する
				Log.w("CAR_MASTER", "Default Car flags cleared, " + String.valueOf(iRet) + "rows updated.");
			}
			defaultFlags = FLAG_DEFAULT_ON;
		} else {
			defaultFlags = FLAG_DEFAULT_OFF;
		}

		// クルマデータをCAR_MASTERに追加
		long lRet = dbman.addNewCar(db, carName, defaultFlags);
		Log.i("CAR_MASTER", "Car record inserted, New Car Name = " + carName + " , New row ID = " + String.valueOf(lRet) );

		dbman.close();
	}

	/*
	 * 「キャンセル」を押したときの処理
	 */
	public void onClickCancel(View v) {
		// 入力されている値を消す
		//   「消す」=「空の値をセット」ということらしい
		textview_addCarName.setText("");
		// チェックされていない状態にする
		checkbox_setDefault.setChecked(false);
	}

}