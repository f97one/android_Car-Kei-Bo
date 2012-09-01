/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author kazutoshi
 *  DB操作関連メソッドを定義するクラス
 *
 *    クエリ系のメソッドのうち、Cursor型で定義していないメソッドについては、
 *    必要な値を仮変数に格納した後、Cursor#close()で閉じるようにしている。
 *    # でないと「Cursor閉じろやｺﾞﾙｧ!!」とか怒られる。が、動作上は問題はなさそうだが....
 *
 */
public class DbManager extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "fuel_mileage.db";
	private static final int DB_VERSION = 1;

	// テーブルの名称を定義
	private static final String LUB_MASTER = "LUB_MASTER";
	private static final String COSTS_MASTER = "COSTS_MASTER";
	private static final String CAR_MASTER = "CAR_MASTER";

	public DbManager(Context context) {
		super(context, DATABASE_NAME, null, DB_VERSION);
		// TODO 自動生成されたコンストラクター・スタブ
		// ここにdbファイルとバージョンを定義する
	}

	/**
	 * CAR_IDに対応する燃費記録を、LUB_MASTERに記録する。
	 * @param db SQLiteDatabase型、燃費を記録するDBインスタンス
	 * @param carId int型、燃費を記録するクルマのCAR_ID
	 * @param amountOfOil long型、給油量
	 * @param odometer int型、給油を行った時の走行メーター値
	 * @param unitPrice int型、給油を行った時の給油単価
	 * @param comments String型、給油時のコメントを入力
	 * @param gregolianDay Calendar型、給油を行った日時
	 * @return long型、insertに成功すればそのときのrowIdを、失敗すれば-1を返す。なお、失敗時はSQLExceptionを投げる
	 */
	protected long addMileageById (SQLiteDatabase db, int carId, double amountOfOil, int odometer, int unitPrice, String comments, Calendar gregolianDay) {
		long result = 0;

		// レコードを追加する
		ContentValues value = new ContentValues();
		value.put("CAR_ID", carId);
		// 入力されたCalendarをユリウス通日に変換する
		DateManager dm = new DateManager();
		double julianDay = dm.toJulianDay(gregolianDay);

		// 価格、距離、体積の各単位をセット
		value.put("REFUEL_DATE", julianDay);
		value.put("LUB_AMOUNT", amountOfOil);
		value.put("UNIT_PRICE", unitPrice);
		value.put("ODOMETER", odometer);
		value.put("COMMENTS", comments);

		// トランザクション開始
		db.beginTransaction();
		try {
			// 失敗したら例外を投げるinsertOrThrowでレコードをINSERT
			result = db.insertOrThrow(LUB_MASTER, null, value);

			// 例外が投げられなければ、トランザクション成功をセット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(DATABASE_NAME, "Car record insert failed, ");
		} finally {
			// トランザクション終了
			// INSERTに失敗した場合は、endTransaction()を呼んだところでロールバックされる
			db.endTransaction();
		}

		return result;
	}

	/**
	 * クルマのレコードを追加する。
	 *   ....引数多いな、オイ(^^;)
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carName String型、追加するクルマの名前
	 * @param isDefaultCar boolean型、デフォルトのクルマにセットするか否か
	 * @param priceUnit String型、価格の単位を格納
	 * @param distanceUnit String型、距離の単位を格納
	 * @param volumeUnit String型、体積の単位を格納
	 * @return long型、insertに成功すればそのときのrowIdを、失敗すれば-1を返す。なお、失敗時はSQLExceptionを投げる
	 */
	protected long addNewCar(SQLiteDatabase db, String carName, boolean isDefaultCar,
			String priceUnit, String distanceUnit, String volumeUnit) {
		// insertOrThrow()の戻り値を格納する変数を、0で初期化する
		long result = 0;

		// レコードを追加する
		ContentValues value = new ContentValues();
		value.put("CAR_NAME", carName);
		// defaultCarにtrueが渡されている場合は1を、そうでない場合は0をputする
		if (isDefaultCar == true) {
			value.put("DEFAULT_FLAG", 1);
		} else {
			value.put("DEFAULT_FLAG", 0);
		}

		// 価格、距離、体積の各単位をセット
		value.put("PRICEUNIT", priceUnit);			// 単価
		value.put("DISTANCEUNIT", distanceUnit);	// 距離
		value.put("VOLUMEUNIT", volumeUnit);		// 体積
		value.put("FUELMILEAGE_LABEL", distanceUnit + "/" + volumeUnit);	// 燃費
		value.put("RUNNINGCOST_LABEL", priceUnit + "/" + distanceUnit);		// ランニングコスト

		// トランザクション開始
		db.beginTransaction();
		try {
			// 失敗したら例外を投げるinsertOrThrowでレコードをINSERT
			result = db.insertOrThrow(CAR_MASTER, null, value);

			// 例外が投げられなければ、トランザクション成功をセット
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(DATABASE_NAME, "Car record insert failed, ");
		} finally {
			// トランザクション終了
			// INSERTに失敗した場合は、endTransaction()を呼んだところでロールバックされる
			db.endTransaction();
		}

		return result;
	}

	/**
	 * デフォルトカーフラグを変更する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、フラグを変更するするクルマのcarId
	 * @return int型、Updateに成功したレコード数
	 */
	protected int changeDefaultCar(SQLiteDatabase db, int carId) {
		// クエリを格納する変数の定義
		ContentValues cv = new ContentValues();
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		int result;

		// 安全のためトランザクションを開始する
		db.beginTransaction();
		try {
			// まず、すべてのデフォルトカーフラグを降ろす
			cv.put("DEFAULT_FLAG", 0);
			result = db.update(CAR_MASTER, cv, null, null);

			// 次に、carIdで指定されたレコードのみに
			cv.clear();
			cv.put("DEFAULT_FLAG", 1);
			result = db.update(CAR_MASTER, cv, where, args);

			// トランザクションの正常終了を宣言
			db.setTransactionSuccessful();
		} finally {
			// トランザクションを終了する。
			// 例外発生とかでトランザクションが正常に完結しなかった場合（＝setTransactionSuccessful()が呼ばれていない）は、
			// endTransaction()を呼んだところでロールバックされる。
			db.endTransaction();
		}

		return result;
	}

	/**
	 * デフォルトフラグを一律下げる。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return int型、Updateに成功したレコード数
	 */
	protected int clearAllDefaultFlags(SQLiteDatabase db) {
		// クエリを格納する変数の定義
		ContentValues cv = new ContentValues();
		int result;

		// 安全のためトランザクションを開始する
		db.beginTransaction();
		try {
			cv.put("DEFAULT_FLAG", 0);
			result = db.update(CAR_MASTER, cv, null, null);

			// トランザクションの正常終了を宣言
			db.setTransactionSuccessful();
		} finally {
			// トランザクションを終了する。
			// 例外発生とかでトランザクションが正常に完結しなかった場合（＝setTransactionSuccessful()が呼ばれていない）は、
			// endTransaction()を呼んだところでロールバックされる。
			db.endTransaction();
		}

		return result;
	}

	/**
	 * クルマのCAR_IDからクルマのレコードを削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteCarById(SQLiteDatabase db, int carId) {
		int result;

		// deleteメソッドに渡す値
		String table = CAR_MASTER;
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};

		// 削除したレコード数を格納する。
		// 通常「1」しか入っていないはず....。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * クルマのCAR_IDに対応するランニングコスト記録を削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteCostsByCarId(SQLiteDatabase db, int carId) {
		int result;

		// deleteメソッドに渡す値
		String table = COSTS_MASTER;
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};

		// 削除したレコード数を格納する。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * クルマのCAR_IDに対応する給油記録を削除する。
	 * @param db SQLiteDatabase型、レコード削除対象のDBインスタンス
	 * @param carId int型、削除するクルマのCAR_ID
	 * @return int型、削除したレコード数
	 */
	protected int deleteLubsByCarId(SQLiteDatabase db, int carId) {
		int result;

		// deleteメソッドに渡す値
		String table = LUB_MASTER;
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};

		// 削除したレコード数を格納する。
		result = db.delete(table, where, args);

		return result;
	}

	/**
	 * 入力されたクルマのCAR_IDから、CAR_NAMEを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、チェックするクルマのcarId
	 * @return String型、チェックする車のcarIdに対応する名前
	 */
	protected String findCarNameById(SQLiteDatabase db, int carId) {
		// 戻り値を格納する変数
		String sRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_ID = ?";
		// CAR_IDはintだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(carId)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		sRet = q.getString(0);
		q.close();

		return sRet;
	}

	/**
	 * 入力されたクルマの名前から、CAR_IDを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carName String型、チェックするクルマの名前
	 * @return int型、チェックする車の名前に対応するcarId
	 */
	protected int getCarId(SQLiteDatabase db, String carName) {
		// 戻り値を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_ID"};
		String where = "CAR_NAME = ?";
		String[] args = {carName};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		iRet = q.getInt(0);
		q.close();

		return iRet;
	}

	/**
	 * クルマリストのリストビューに差し込むデータを取得する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return Cursor型、Cursorオブジェクトをそのまま返すので、Cursor#close()は行わない。
	 */
	protected Cursor getCarList(SQLiteDatabase db) {
		// クエリを格納する変数の定義
		Cursor q;
		String[] columns = {"CAR_ID AS _id", "CAR_NAME", "CURRENT_FUEL_MILEAGE", "FUELMILEAGE_LABEL", "CURRENT_RUNNING_COST", "RUNNINGCOST_LABEL"};
		//String where = "CAR_ID = ?";
		//String[] args = {String.valueOf(1)};
		//String groupBy = ""
		//String having = ""
		String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, null, null, null, null, orderBy);
		q.moveToFirst();

		return q;
	}

	/**
	 * CAR_IDに対応するクルマの名称を返す。
	 * @param db SQLiteDatabase型、
	 * @param carId
	 * @return
	 */
	protected String getCarNameById (SQLiteDatabase db, int carId) {
		// 戻り値を格納する変数
		String sRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_ID = ?";
		// DEFAULT_FLAG=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(carId)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		sRet = q.getString(0);
		Log.i(CAR_MASTER, "Found specified car : " + sRet + " , related to CAR_ID : " + String.valueOf(carId));
		q.close();

		return sRet;
	}

	/**
	 * 燃費追加等でスピナーにセットするデータを取得する。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return Cursor型、Cursorオブジェクトをそのまま返すので、Cursor#close()は行わない。
	 */
	protected Cursor getCarNameList(SQLiteDatabase db) {
		// クエリを格納する変数の定義
		Cursor q;
		String[] columns = {"CAR_ID AS _id", "CAR_NAME"};
		//String where = "CAR_ID = ?";
		//String[] args = {String.valueOf(1)};
		//String groupBy = ""
		//String having = ""
		String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, null, null, null, null, orderBy);
		q.moveToFirst();

		return q;
	}

	/**
	 * デフォルトカーフラグのあるCAR_IDを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return int型、デフォルトカーフラグのあるクルマのcarId
	 */
	protected int getDefaultCarId(SQLiteDatabase db) {
		// 戻り値を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_ID"};
		String where = "DEFAULT_FLAG = ?";
		// DEFAULT_FLAG=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(1)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		iRet = q.getInt(0);
		q.close();

		return iRet;
	}

	/**
	 * デフォルトカーフラグのあるCAR_NAMEを返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return String型、デフォルトカーフラグのあるクルマの名前
	 */
	protected String getDefaultCarName(SQLiteDatabase db) {
		// 戻り値を格納する変数
		String sRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "DEFAULT_FLAG = ?";
		// DEFAULT_FLAG=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(1)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();
		sRet = q.getString(0);
		Log.i(CAR_MASTER, "Found default car : " + sRet);
		q.close();

		return sRet;
	}

	/**
	 * CAR_IDのクルマに対応する距離の単位を返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の距離の単位
	 */
	protected String getDistanceUnitById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"DISTANCEUNIT"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応する燃費の表示ラベルを返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の燃費の表示ラベル
	 */
	protected String getFuelmileageLabelById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"FUELMILEAGE_LABEL"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応する価格の単位を返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の価格の単位
	 */
	protected String getPriceUnitById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"PRICEUNIT"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応するランニングコストの表示ラベルを返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車のランニングコストの表示ラベル
	 */
	protected String getRunningcostLabelById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"RUNNINGCOST_LABEL"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_IDのクルマに対応する体積の単位を返す。
	 * @param db SQLiteDatabase型、検索するDBインスタンス
	 * @param carId int型、検索するクルマのCAR_ID
	 * @return String型、その車の体積の単位
	 */
	protected String getVolumeUnitById(SQLiteDatabase db, int carId) {
		Cursor q;

		String[] columns = {"VOLUMEUNIT"};
		String where = "CAR_ID = ?";
		String[] args = {String.valueOf(carId)};
		//String groupBy = ""
		//String having = ""
		//String orderBy = "CAR_ID";

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		String unit = q.getString(0);
		q.close();

		return unit;
	}

	/**
	 * CAR_MASTERに有効なレコードがあるかを調べる。
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return boolean型、有効なレコードがあればtrue、なければfalse
	 */
	protected boolean hasCarRecords(SQLiteDatabase db) {
		// getCount()を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(CAR_MASTER, null, null, null, null, null, null);
		q.moveToFirst();
		iRet = q.getCount();
		q.close();

		if (iRet == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * COSTS_MASTERに有効なレコードがあるかを調べる
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return boolean型、有効なレコードがあればtrue、なければfalse
	 */
	protected boolean hasCostsRecords(SQLiteDatabase db) {
		// getCount()を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(COSTS_MASTER, null, null, null, null, null, null);
		q.moveToFirst();
		iRet = q.getCount();
		q.close();

		if (iRet == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * LUB_MASTERに有効なレコードがあるかを調べる
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @return boolean型、有効なレコードがあればtrue、なければfalse
	 */
	protected boolean hasLubRecords(SQLiteDatabase db) {
		// getCount()を格納する変数
		int iRet;

		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(LUB_MASTER, null, null, null, null, null, null);
		q.moveToFirst();
		iRet = q.getCount();
		q.close();

		if (iRet == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 重複チェックその２
	 *   すでにデフォルトカーフラグがセットされているか否かをチェックする。
	 *   @param db SQLiteDatabase型、操作するDBインスタンス
	 *   @return boolean型、trueだとチェック済み、falseはチェックなし
	 */
	protected boolean isExistDefaultCarFlag(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		//String[] columns = {"DEFAULT_FLAG"};
		String where = "DEFAULT_FLAG = ?";
		String[] args = {"1"};

		q = db.query(CAR_MASTER, null, where, args, null, null, null);
		q.moveToFirst();

		int defaultFlag = q.getCount();
		q.close();

		if (defaultFlag == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 重複チェックその２
	 *   やっていることは上と同じ。
	 *   @param db SQLiteDatabase型、操作するDBインスタンス
	 *   @param carId int型、チェックするcarIdを指定する
	 *   @return boolean型、trueだとチェック済み、falseはチェックなし
	 */
	protected boolean isExistDefaultCarFlag(SQLiteDatabase db, int carId) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"DEFAULT_FLAG"};
		String where = "CAR_ID = ?";
		// CAR_IDはintだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(carId)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		int defaultFlag = q.getInt(0);
		q.close();

		if (defaultFlag == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 重複チェックその１
	 *   同一名称の車がないかをチェックする。
	 *   @param db SQLiteDatabase型、操作するDBインスタンス
	 *   @param carName String型、重複がないか調べるクルマの名前
	 *   @return boolean型、trueだと重複があり、falseだと重複はない。
	 */
	protected boolean isExistSameNameCar(SQLiteDatabase db, String carName) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_NAME = ?";
		String[] args = {carName};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);
		q.moveToFirst();

		// 検索結果の総数を調査
		int count = q.getCount();
		q.close();

		// 総数が0（＝検索結果がない）の場合はfalseを、そうでない場合はtrueを返す
		if (count == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * DBがない場合にコンストラクタからコールされ、テーブルをDDLに従い作成する。
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// テーブル作成
		String create_lub_master;
		String create_costs_master;
		String create_car_master;

		// DDL
		//   LUB_MASTERテーブル
		create_lub_master = "CREATE TABLE IF NOT EXISTS LUB_MASTER " +
				"(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"REFUEL_DATE REAL, " +
				"CAR_ID INTEGER, " +
				"LUB_AMOUNT REAL DEFAULT 0, " +
				"UNIT_PRICE REAL DEFAULT 0, " +
				"ODOMETER INTEGER, " +
				"COMMENTS TEXT);";

		//   COSTS_MASTERテーブル
		create_costs_master = "CREATE TABLE IF NOT EXISTS COSTS_MASTER " +
				"(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"CAR_ID INTEGER, " +
				"REFUEL_DATE REAL, " +
				"RUNNING_COST REAL DEFAULT 0);";

		//   CAR_MASTERテーブル
		create_car_master = "CREATE TABLE IF NOT EXISTS CAR_MASTER " +
				"(CAR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"CAR_NAME TEXT, " +
				"DEFAULT_FLAG INTEGER DEFAULT 0, " +
				"CURRENT_FUEL_MILEAGE INTEGER DEFAULT 0, " +
				"CURRENT_RUNNING_COST INTEGER DEFAULT 0, " +
				"PRICEUNIT TEXT, " +
				"DISTANCEUNIT TEXT, " +
				"VOLUMEUNIT TEXT, " +
				"FUELMILEAGE_LABEL TEXT, " +
				"RUNNINGCOST_LABEL TEXT);";

		// DDLをexecSQLで実行する
		db.execSQL(create_lub_master);
		db.execSQL(create_costs_master);
		db.execSQL(create_car_master);
	}

	/**
	 * DBのバージョンが既存のDBより大きい場合、コンストラクタからコールされるが、
	 * 初期リリースのためダミー処理を書いている。
	 * アプリケーションのバージョンアップを行う場合は、ここの処理を抜本的に
	 * 書き直す必要あり。
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param oldVersion int型、DBの古いバージョン番号、コンストラクタが決める？
	 * @param newVersion int型、DBの新しいバージョン番号、コンストラクタが決める？
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自動生成されたメソッド・スタブ
		//   本来はALTER TABLEなどでテーブル構造を編集するべきだが、
		//   テスト用にいったんドロップしてテーブルを作り直す処理としておく
		Log.w("Car-Kei-Bo", "Updating database, which will destroy all old data.(for testing)");

		// テーブルをいったんすべてドロップ
		db.execSQL("DROP TABLE IF EXISTS LUB_MASTER;");
		db.execSQL("DROP TABLE IF EXISTS COSTS_MASTER;");
		db.execSQL("DROP TABLE IF EXISTS CAR_MASTER;");

		// onCreate()で作成しなおす
		onCreate(db);
	}

	/**
	 * データベースを再編成する。
	 * @param db SQLiteDatabase型、再編成対象のDBインスタンス
	 */
	protected void reorgDb(SQLiteDatabase db) {
		db.execSQL("vacuum");
	}

	/**
	 * 指定したクルマのCAR_IDに対する、すべての給油記録を返す。
	 * @param db SQLiteDatabase型、操作するDBインスタンス
	 * @param carId int型、検索対象のクルマのCAR_ID
	 * @param invertOrder trueにすると日付を降順に、falseにすると日付を昇順にオーダーする
	 * @return Cursor型、検索結果
	 */
	protected Cursor getRefuelRecordsById(SQLiteDatabase db, int carId, boolean invertOrder) {
		Cursor q;
		String order;

		// 検索結果は登録時刻順で返すが、そのソート結果を決める
		if (invertOrder) {
			order = "DESC";
		} else {
			order = "";
		}

		// テーブルをまたぐのでrawQueryでSQL文を直接たたいている。
		q = db.rawQuery("SELECT LUB_MASTER.RECORD_ID as _id, datetime(LUB_MASTER.REFUEL_DATE) as DATE_OF_REFUEL," +
				" LUB_MASTER.LUB_AMOUNT, CAR_MASTER.VOLUMEUNIT FROM LUB_MASTER, CAR_MASTER" +
				" WHERE LUB_MASTER.CAR_ID = CAR_MASTER.CAR_ID" +
				" AND LUB_MASTER.CAR_ID = " + String.valueOf(carId) +
				" ORDER BY LUB_MASTER.REFUEL_DATE " + order + ";", null);
		q.moveToFirst();

		return q;
	}
}
