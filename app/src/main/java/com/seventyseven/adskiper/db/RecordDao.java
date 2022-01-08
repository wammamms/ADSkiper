package com.seventyseven.adskiper.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.seventyseven.adskiper.model.Record;
import com.seventyseven.adskiper.utils.AppUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordDao {

    static final int MAX_THREAD = 5;
    static ExecutorService service = Executors.newFixedThreadPool(MAX_THREAD);
    private SQLiteDatabase db;

    public RecordDao(Context context) {
        this.db = com.seventyseven.adskiper.db.DbHelper.getDb(context);
    }

    public static void saveRecordByPackageName(final String packageName, final Context context){
        service.submit(new Runnable() {
            @Override
            public void run() {
                com.seventyseven.adskiper.db.RecordDao dao = new com.seventyseven.adskiper.db.RecordDao(context);
                Record record = dao.getRecordByPkgName(packageName);//当前记录
                if(record == null || !packageName.equals(record.getPackageName())){//TextUtils.isEmpty(record.getPackageName())
                    record = new Record();
                    record.setPackageName(packageName);
                    record.setAppName("");
                    record.setTimes(1);
                    record.setAppName(AppUtils.getAppName(context,packageName));
                    dao.add(record);
                    Log.d("db","  add "+record.getAppName()+record.getTimes());
                } else {
                    int times = record.getTimes();
                    times++;
                    record.setTimes(times);
                    dao.update(record);
                    Log.d("db","  update "+record.getAppName()+record.getTimes());
                }
            }
        });
    }

    public boolean add(Record record){
        if(record == null){
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(RecordTable.APP_NAME,record.getAppName());
        values.put(RecordTable.PACKAGE_NAME,record.getPackageName());
        values.put(RecordTable.TIMES,record.getTimes());
        long result = db.insert(RecordTable.NAME,null,values);
        return result != -1;
    }

    public boolean update(Record record){
        if(record == null){
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(RecordTable.APP_NAME,record.getAppName());
        values.put(RecordTable.PACKAGE_NAME,record.getPackageName());
        values.put(RecordTable.TIMES,record.getTimes());
        int result = db.update(RecordTable.NAME,
                values,
                RecordTable.PACKAGE_NAME+"=?",
                new String[]{record.getPackageName()});
        return result != -1;
    }

    public Record getRecordByPkgName(String pkgName){
        Cursor cursor = db.query(RecordTable.NAME,
                new String[]{RecordTable.PACKAGE_NAME,
                        RecordTable.APP_NAME,
                        RecordTable.TIMES},
                RecordTable.PACKAGE_NAME + "=?",
                new String[]{pkgName},null,null,null);
        if(cursor == null){
            return null;
        }
        Record record = new Record();
        while(cursor.moveToNext()){
            record.setAppName(cursor.getString(RecordTable.ID_APP_NAME));
            record.setPackageName(cursor.getString(RecordTable.ID_PACKAGE_NAME));
            record.setTimes(cursor.getInt(RecordTable.ID_TIMES));
            break;
        }
        Log.d("db"," getRecordByPackageName"+record.toString());
        return record;
    }

    public List<Record> getRecordList(){
        List<Record> list = new ArrayList<>();
        Cursor cursor = db.query(RecordTable.NAME,
                new String[]{RecordTable.PACKAGE_NAME,
                        RecordTable.APP_NAME,
                        RecordTable.TIMES},
                null,null,null,null, 
                RecordTable.TIMES+" desc");
        if(cursor == null){
            return list;
        }

        while(cursor.moveToNext()){
            Record record = new Record();
            record.setAppName(cursor.getString(RecordTable.ID_APP_NAME));
            record.setPackageName(cursor.getString(RecordTable.ID_PACKAGE_NAME));
            record.setTimes(cursor.getInt(RecordTable.ID_TIMES));
            list.add(record);
        }
        return list;
    }

    public int getClearCount(){
        int count = 0;
        Cursor cursor = db.query(RecordTable.NAME,
                new String[]{RecordTable.PACKAGE_NAME,
                        RecordTable.APP_NAME,
                RecordTable.TIMES},
                null,null,null,null, 
                RecordTable.TIMES+" desc");
        if(cursor == null){
            return count;
        }

        while(cursor.moveToNext()){
            int times = cursor.getInt(RecordTable.ID_TIMES);
            count += times;
        }
        return count;
    }

    public boolean cleanData(){
        db.delete(RecordTable.NAME,null,null);
        return true;
    }

}
