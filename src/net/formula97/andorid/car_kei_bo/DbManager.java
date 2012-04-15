/**
 *
 */
package net.formula97.andorid.car_kei_bo;

import android.content.Context;
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

}
