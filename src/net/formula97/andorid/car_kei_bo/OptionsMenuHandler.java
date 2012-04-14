package net.formula97.andorid.car_kei_bo;

import android.app.Activity;
import android.os.Bundle;

public class OptionsMenuHandler extends Activity {

	/* (非 Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);


		switch (savedInstanceState.describeContents()) {
		case R.id.optionsmenu_closeAPP:
			opMenuCloseAPP();
		case R.id.optionsmenu_call_preference:
			opMenuCallPreference();
		case R.id.optionsmenu_addcar:
			opMenuAddCar();
		case R.id.optionsmenu_carlist:
			opMenuCarList();
		}


	}

	public void opMenuCloseAPP() {
		// アプリを終了させる
		finish();
	}

	public void opMenuCallPreference() {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void opMenuAddCar() {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void opMenuCarList() {
		// TODO 自動生成されたメソッド・スタブ

	}


}
