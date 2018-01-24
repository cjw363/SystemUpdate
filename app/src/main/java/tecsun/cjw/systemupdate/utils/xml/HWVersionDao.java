package tecsun.cjw.systemupdate.utils.xml;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

public class HWVersionDao {
	private final static String COL_HWVERSION = "hwversion";
	private static SystemCenterDBOpenHelper mDBOpenHelper;
	private SQLiteDatabase db;
	private AtomicInteger mOpenCounter = new AtomicInteger();  	  
    private static HWVersionDao instance;  	

    public static synchronized void initializeInstance(Context context) {  
        if (instance == null) {  
            instance = new HWVersionDao();  
            mDBOpenHelper = new SystemCenterDBOpenHelper(context);  
        }  
    }  
  
    public static synchronized HWVersionDao getInstance() {  
        if (instance == null) {  
            throw new IllegalStateException(HWVersionDao.class.getSimpleName() +  
                    " is not initialized, call initializeInstance(..) method first.");  
        }  
  
        return instance;  
    }  
  
    public synchronized SQLiteDatabase openDatabase() {  
        if(mOpenCounter.incrementAndGet() == 1) {  
            // Opening new database  
            db = mDBOpenHelper.getWritableDatabase();  
        }  
        return db;  
    }  
  
    public synchronized void closeDatabase() {  
        if(mOpenCounter.decrementAndGet() == 0) {  
            // Closing database  
            db.close();  
  
        }  
    } 
    
	
//    public HWVersionDao(Context context) {
//		mDBOpenHelper = new SystemCenterDBOpenHelper(context);
//		db = mDBOpenHelper.getReadableDatabase();
//	}

	public void insert(HwVersionModel hwVersionModel) {
		ContentValues cv = new ContentValues();// 实例化一个ContentValues用来装载待插入的数据
		cv.put(COL_HWVERSION, hwVersionModel.getHwVersion());
		db.insert(SystemCenterDBOpenHelper.TABLE_HWVERSION, null, cv);// 执行插入操作
	}

	public void delete() {
		String whereClause = "id=?";// 删除的条件
		String[] whereArgs = { "1" };// 删除的条件参数
		db.delete(SystemCenterDBOpenHelper.TABLE_HWVERSION, whereClause, whereArgs);// 执行删除
	}

	public void update(HwVersionModel hwVersionModel) {
		ContentValues cv = new ContentValues();// 实例化ContentValues
		cv.put(COL_HWVERSION, hwVersionModel.getHwVersion());	
		String whereClause = "id=?";// 修改条件
		String[] whereArgs = { "1" };// 修改条件的参数
		db.update(SystemCenterDBOpenHelper.TABLE_HWVERSION, cv, whereClause, whereArgs);// 执行修改
	}

	public HwVersionModel query() {
		HwVersionModel hwVersionModel = null;
		Cursor c = db.query(SystemCenterDBOpenHelper.TABLE_HWVERSION, null, null, null, null, null, null);// 查询并获得游标
		if (c.moveToFirst()) {// 判断游标是否为空
			for (int i = 0; i < c.getCount(); i++) {
				c.move(i);// 移动到指定记录
				// String username = c.getString(c.getColumnIndex("username"));
				// String password = c.getString(c.getColumnIndex("password"));
				String hwVersion = c.getString(c.getColumnIndex(COL_HWVERSION));
				hwVersionModel = new HwVersionModel(hwVersion);				
			}
		}
		return hwVersionModel;
	}

	public int getCount(){
		Cursor c = db.query(SystemCenterDBOpenHelper.TABLE_HWVERSION, null, null, null, null, null, null);// 查询并获得游标
		return c.getCount();
	}
	
	public boolean tabbleIsExist(String tableName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = mDBOpenHelper.getReadableDatabase();
			String sql = "select count(*) as c from " + SystemCenterDBOpenHelper.DATABASENAME + " where type ='table' and name ='"
					+ tableName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}
}
