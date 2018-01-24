package tecsun.cjw.systemupdate.utils.xml;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//创建和更新数据库  
public class SystemCenterDBOpenHelper extends SQLiteOpenHelper {
	public static final String DATABASENAME = "systemcenter.db"; // 数据库名称
	public static final String TABLE_HWVERSION = "hwversion"; // 数据表名称
	private static final int DATABASEVERSION = 1;// 数据库版本

	public SystemCenterDBOpenHelper(Context context) {
		super(context, DATABASENAME, null, DATABASEVERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_HWVERSION +" (id integer primary key autoincrement,hwversion string)";
		db.execSQL(sql);// 执行有更改的sql语句
	}

	// 数据库版本或表结构改变会被调用
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_HWVERSION;
		db.execSQL(sql);
		onCreate(db);
	}

}
