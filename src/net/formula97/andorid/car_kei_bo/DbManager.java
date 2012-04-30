/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author kazutoshi
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

	/* (非 Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// テーブル作成
		//   LUB_MASTERテーブル
		String create_lub_master = "CREATE TABLE IF NOT EXIST LUB_MASTER ";
		create_lub_master += "(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
		create_lub_master += "DATE TEXT, CAR_ID INTEGER, ";
		create_lub_master += "LUB_AMOUNT REAL, ";
		create_lub_master += "UNIT_PRICE REAL, ";
		create_lub_master += "ODOMETER REAL, ";
		create_lub_master += "COMMENTS TEXT);";

		db.execSQL(create_lub_master);

		//   COSTS_MASTERテーブル
		String create_costs_master = "CREATE TABLE IF NOT EXIST COSTS_MASTER ";
		create_costs_master += "(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
		create_costs_master += "DATE TEXT, ";
		create_costs_master += "RUNNING_COST REAL);";

		db.execSQL(create_costs_master);

		//   CAR_MASTERテーブル
		String create_car_master = "CREATE TABLE IF NOT EXIST CAR_MASTER ";
		create_car_master += "(CAR_ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
		create_car_master += "CAR_NAME TEXT, ";
		create_car_master += "DEFAULT INTEGER);";

		db.execSQL(create_car_master);
	}

	/* (非 Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 自動生成されたメソッド・スタブ
		//   本来はALTER TABLEなどでテーブル構造を編集するべきだが、
		//   テスト用にいったんドロップしてテーブルを作り直す処理としておく
		Log.w("Car-Kei-Bo", "Updating database, which will destroy all old data.(for testing)");

		// テーブルをいったんすべてドロップ
		db.execSQL("DROP TABLE IF EXIST LUB_MASTER;");
		db.execSQL("DROP TABLE IF EXIST COSTS_MASTER;");
		db.execSQL("DROP TABLE IF EXIST CAR_MASTER;");

		// onCreate()で作成しなおす
		onCreate(db);
	}

	/*
	 * クルマのレコードを追加する
	 *
	 */
	public int addNewCar(SQLiteDatabase db, String carName, boolean isDefaultCar) {
		// insert()の戻り値を格納する変数を、0で初期化する
		int resultInsert = 0;

		// レコードを追加する
		ContentValues value = new ContentValues();
		value.put("CAR_NAME", carName);
		value.put("DEFAULT", isDefaultCar);
		db.insert(CAR_MASTER, null, value);

		return resultInsert;
	}

	/*
	 * 重複チェックその１
	 *   同一名称の車がないかをチェックする。
	 *   trueだと重複があり、falseだと重複はない。
	 *   SQL文にすると以下のとおり。
	 *     SELECT CAR_NAME FROM CAR_MASTER WHERE CAR_NAME = '$carName';
	 */
	protected boolean isExistSameNameCar(SQLiteDatabase db, String carName) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に下記に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_NAME = ?";
		String[] args = {carName};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);

		// 検索結果の総数を調査
		int count = q.getCount();

		// 総数が0（＝検索結果がない）の場合はfalseを、そうでない場合はtrueを返す
		if (count == 0) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * 重複チェックその２
	 *   すでにデフォルトカーフラグがセットされているか否かをチェックする。
	 *   trueだとチェック済み、falseはチェックなし
	 *
	 */
	protected boolean isExistDefaultCarFlag(SQLiteDatabase db, String carName) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に下記に書き直している。
		Cursor q;
		String[] columns = {"DEFAULT"};
		String where = "CAR_NAME = ?";
		String[] args = {carName};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);

		int defaultFlag = q.getInt(0);

		if (defaultFlag == 0) {
			return false;
		} else {
			return true;
		}
	}
}
