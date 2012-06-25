package net.formula97.andorid.car_kei_bo;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 「クルマを追加」画面を扱うクラス。
 * @author kazutoshi
 *
 */
public class AddMyCar extends Activity implements OnItemSelectedListener {

	private DbManager dbman = new DbManager(this);
	public static SQLiteDatabase db;

	private static final boolean FLAG_DEFAULT_ON = true;
	private static final boolean FLAG_DEFAULT_OFF = false;

	// ウィジェットを扱うための定義
	TextView textview_addCarName;
	CheckBox checkbox_setDefault;
	Button button_addCar;
	Button button_cancel_addCar;
	Spinner spinner_price_Unit;
	Spinner spinner_distanceUnit;
	Spinner spinner_volumeUnit;

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
		spinner_price_Unit = (Spinner)findViewById(R.id.spinner_priceUnit);
		spinner_distanceUnit = (Spinner)findViewById(R.id.spinner_distanceUnit);
		spinner_volumeUnit = (Spinner)findViewById(R.id.spinner_volumeUnit);

		// 各スピナーへonClickListenerを定義
		spinner_price_Unit.setOnItemSelectedListener(this);
		spinner_distanceUnit.setOnItemSelectedListener(this);
		spinner_volumeUnit.setOnItemSelectedListener(this);
    }

	/**
	 * ほかのActivityへ遷移するなどで一時的に処理を停止するときに、システムからコールされる。
	 * DBの閉じ忘れを防止するため、DBが開いていたらここでクローズする。
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO 自動生成されたメソッド・スタブ
		super.onPause();

		if (db.isOpen()) {
			dbman.close();
		}
	}

	/**
	 * 画面描画を行うときに必ずシステムからコールされる。
	 * 上記特徴を利用し、画面表示されるコントロール類の挙動を設定している。
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

		/*
		 * 「デフォルトカー」チェックの挙動決定
		 *   CAR_MASTERにまったくレコードがない場合、デフォルトカーチェックを入れていないと、
		 * クルマリストに戻ったときにデフォルトカーが特定できず異常終了する。
		 *   CAR_MASTERにレコードがまったくない場合、最初に追加されるクルマがデフォルトになるのは
		 * 暗黙の了解ともいえるため、
		 *   １　CAR_MASTERにレコードがない場合は、デフォルトカーチェックをオン
		 *   ２　CAR_MASTERにレコードがある場合は、デフォルトカーチェックをオフのままにする
		 * という処理を行う。
		 */
		db = dbman.getReadableDatabase();

		if (dbman.hasCarRecords(db)) {
			checkbox_setDefault.setChecked(FLAG_DEFAULT_OFF);
		} else {
			checkbox_setDefault.setChecked(FLAG_DEFAULT_ON);
		}

		dbman.close();
	}

	/**
	 * 「クルマを追加」ボタンを押したときの処理。
	 *   OnClickListenerをインターフェース実装していない関係で、GUIに紐付けしてボタン処理を書いている。
	 *   ただ、このやり方だとpublicメソッドにしなきゃいけないようだ。
	 * @param v View型、ボタンを押されたときのId？
	 */
	public void onClickAddCar(View v) {
		String carName;
		boolean defaultFlags;
		String volume = "";
		String distance = "";
		String price = "";

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

		// 各スピナーから値を取得する。
		price = (String) spinner_price_Unit.getSelectedItem();
		distance = (String)spinner_distanceUnit.getSelectedItem();
		volume = (String)spinner_volumeUnit.getSelectedItem();

		// クルマデータをCAR_MASTERに追加
		long lRet = dbman.addNewCar(db, carName, defaultFlags, price, distance, volume);
		Log.i("CAR_MASTER", "Car record inserted, New Car Name = " + carName + " , New row ID = " + String.valueOf(lRet) );

		dbman.close();

		// テキストボックスを空にし、デフォルトカーチェックをはずす
		textview_addCarName.setText("");
		checkbox_setDefault.setChecked(FLAG_DEFAULT_OFF);

		// トーストを表示する
		showToastmsg(carName);
	}

	/**
	 * 「キャンセル」を押したときの処理。
	 *   onClickAddCar()同様、OnClickListenerをインターフェース実装していない関係で、
	 *   GUIに紐付けしてボタン処理を書いている。
	 *   ただ、このやり方だとpublicメソッドにしなきゃいけないようだ。
	 * @param v View型、ボタンを押されたときのId？
	 */
	public void onClickCancel(View v) {
		// 入力されている値を消す
		//   「消す」=「空の値をセット」ということらしい
		textview_addCarName.setText("");
		// チェックされていない状態にする
		checkbox_setDefault.setChecked(FLAG_DEFAULT_OFF);
	}

	/**
	 * スピナーのアイテムを選択したときに発生するイベント
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * スピナーのアイテムを何も選択しなかったときに発生するイベント
	 * @param arg0
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * クルマの追加をトースト表示で通知する。
	 * @param carName String型、トースト中に表示するクルマの名前
	 */
	protected void showToastmsg(String carName) {
		// トースト表示の組み立てに使うString変数の宣言
		String line1, line2, line3;

		line1 = carName + " " + getString(R.string.toastmsg_addcar1);
		line2 = getString(R.string.toastmsg_addcar2);
		line3 = getString(R.string.toastmsg_addcar3);

		// トーストを作成する
		Toast.makeText(this, line1 + "\n" + line2 + "\n" + line3, Toast.LENGTH_LONG).show();
	}

}