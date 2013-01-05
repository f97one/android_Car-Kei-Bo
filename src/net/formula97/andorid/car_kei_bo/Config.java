/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author kazutoshi
 *
 */
public class Config extends PreferenceActivity {

	/**
	 *
	 */
	public Config() {
		// TODO 自動生成されたコンストラクター・スタブ
	}

	/* (非 Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自動生成されたメソッド・スタブ
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.config);
	}

}
