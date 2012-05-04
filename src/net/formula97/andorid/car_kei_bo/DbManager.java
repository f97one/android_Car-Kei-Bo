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
		String create_lub_master = "CREATE TABLE IF NOT EXISTS LUB_MASTER ";
		create_lub_master += "(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
		create_lub_master += "DATE TEXT, CAR_ID INTEGER, ";
		create_lub_master += "LUB_AMOUNT REAL, ";
		create_lub_master += "UNIT_PRICE REAL, ";
		create_lub_master += "ODOMETER REAL, ";
		create_lub_master += "COMMENTS TEXT);";

		db.execSQL(create_lub_master);

		//   COSTS_MASTERテーブル
		String create_costs_master = "CREATE TABLE IF NOT EXISTS COSTS_MASTER ";
		create_costs_master += "(RECORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
		create_costs_master += "DATE TEXT, ";
		create_costs_master += "RUNNING_COST REAL);";

		db.execSQL(create_costs_master);

		//   CAR_MASTERテーブル
		String create_car_master = "CREATE TABLE IF NOT EXISTS CAR_MASTER ";
		create_car_master += "(CAR_ID INTEGER PRIMARY KEY AUTOINCREMENT, ";
		create_car_master += "CAR_NAME TEXT, ";
		create_car_master += "DEFAULT_FLAG INTEGER);";

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
		db.execSQL("DROP TABLE IF EXISTS LUB_MASTER;");
		db.execSQL("DROP TABLE IF EXISTS COSTS_MASTER;");
		db.execSQL("DROP TABLE IF EXISTS CAR_MASTER;");

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
		// defaultCarにtrueが渡されている場合は1を、そうでない場合は0をputする
		if (isDefaultCar == true) {
			value.put("DEFAULT_FLAG", 1);
		} else {
			value.put("DEFAULT_FLAG", 0);
		}
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
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
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
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"DEFAULT_FLAG"};
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
	/*
	 * やっていることは上と同じだが、渡したCAR_IDにデフォルトフラグがあるかを調べ、
	 * フラグがたっていればtrue、そうでなければfalseを返す
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

		int defaultFlag = q.getInt(0);

		if (defaultFlag == 0) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * 入力されたクルマの名前から、CAR_IDを返す
	 */
	protected int getCarId(SQLiteDatabase db, String carName) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_ID"};
		String where = "CAR_NAME = ?";
		String[] args = {carName};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);

		return q.getInt(0);
	}

	/*
	 * 入力されたクルマのCAR_IDから、CAR_NAMEを返す
	 */
	protected String getCarName(SQLiteDatabase db, int carId) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_ID = ?";
		// CAR_IDはintだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(carId)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);

		return q.getString(0);
	}

	/*
	 * デフォルトカーフラグのあるCAR_NAMEを返す
	 */
	protected String getDefaultCarName(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_NAME"};
		String where = "CAR_ID = ?";
		// CAR_ID=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(1)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);

		return q.getString(0);
	}

	/*
	 * デフォルトカーフラグのあるCAR_IDを返す
	 */
	protected int getDefaultCarId(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;
		String[] columns = {"CAR_ID"};
		String where = "CAR_ID = ?";
		// CAR_ID=1を検索するのだが、query()がString[]であることを要求しているので、valueOf()でStringに変換する
		String[] args = {String.valueOf(1)};

		q = db.query(CAR_MASTER, columns, where, args, null, null, null);

		return q.getInt(0);
	}

	/*
	 * リストビューに表示する以下の内容を得る
	 *   ・クルマの名前
	 *   ・現在の燃費
	 *   ・現在のランニングコスト
	 */
	protected Cursor getCurrentMileageData(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		Cursor rq;
		String sql;

		// query()は複数テーブルをまたぐクエリができない？みたいなので、rawQuery()を使う。
		// 以下はそのためのSQL文を組み立てている。
		sql = "SELECT CAR_MASTER.CAR_NAME,  sum(LUB_MASTER.ODOMETER) / sum(LUB_MASTER.LUB_AMOUNT), " +
				"avg(COSTS_MASTER.RUNNING_COST) FROM CAR_MASTER, LUB_MASTER, COSTS_MASTER " +
				"WHERE LUB_MASTER.DATE = COSTS_MASTER.DATE " +
				"AND CAR_MASTER.CAR_ID = LUB_MASTER.CAR_ID " +
				"ORDER BY CAR_MASTER.CAR_ID";

		// rawQuery()にSQL文を投入
		rq = db.rawQuery(sql, null);

		return rq;
	}

	/*
	 * デフォルトカーフラグを変更する
	 */
	public int changeDefaultCar(SQLiteDatabase db, int carId) {
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

	/*
	 * CAR_MASTERに有効なレコードがあるかを調べる
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 */
	public boolean hasCarRecords(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(CAR_MASTER, null, null, null, null, null, null);

		if (q.getCount() == 0) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * LUB_MASTERに有効なレコードがあるかを調べる
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 */
	public boolean hasLubRecords(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(LUB_MASTER, null, null, null, null, null, null);

		if (q.getCount() == 0) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * COSTS_MASTERに有効なレコードがあるかを調べる
	 *   ここで言う「有効な」とは、レコードがあるか否かの話です(^^;
	 */
	public boolean hasCostsRecords(SQLiteDatabase db) {
		// クエリを格納する変数を定義
		// 検索フィールド名と検索値は配列にしないと怒られるので、配列に書き直している。
		Cursor q;

		q = db.query(COSTS_MASTER, null, null, null, null, null, null);

		if (q.getCount() == 0) {
			return false;
		} else {
			return true;
		}
	}
}
