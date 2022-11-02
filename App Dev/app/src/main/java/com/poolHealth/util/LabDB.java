package com.poolHealth.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.poolHealth.Models.ScanModel;
import com.poolHealth.Models.PoolReport;

import java.util.Calendar;


public class LabDB extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "pooltest.db";

    // Common Column
    private static final String COLUMN_SCN_NO = "scno";
    private static final String COLUMN_ID = "id";

    // ScanTable
    private static final String TABLE_SCN_DETAILS = "scn_details";
    // Columns for scan table

    private static final String COLUMN_SCN_NAME = "scnName";
    private static final String COLUMN_ADDEDDATE = "added_date";
    private static final String COLUMN_UPDATEDDATE = "updated_date";

    // Scan Vital Sign
    private static final String TABLE_CHEM_BALANCE = "vital_sign";
    // Columns of Vital Signs


    //  Pool Test Report
    private static final String TABLE_POOL_TEST = "pool_test";
    // Columns of Pool

    private static final String COLUMN_ALKALINITY = "alk";
    private static final String COLUMN_PH = "ph";
    private static final String COLUMN_FC = "fc";
    private static final String COLUMN_BROMINE = "bro";
    private static final String COLUMN_HARDNESS = "thard";

    // DATA TABLE
    private static final String TABLE_DATA = "pool_data";

    // FIELDS FOR pool_data
    private static final String COLUMN_DATA = "_data";

    public LabDB(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SCAN_TABLE = "CREATE TABLE " + TABLE_SCN_DETAILS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SCN_NAME + " INTEGER,"
                + COLUMN_ADDEDDATE + " TEXT,"
                + COLUMN_UPDATEDDATE + " TEXT)";
        db.execSQL(CREATE_SCAN_TABLE);
        String CREATE_CHEM_BALANCE_TABLE = "CREATE TABLE " + TABLE_CHEM_BALANCE + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SCN_NO + " INTEGER,"
                + COLUMN_ADDEDDATE + " TEXT,"
                + COLUMN_UPDATEDDATE + " TEXT)";
        db.execSQL(CREATE_CHEM_BALANCE_TABLE);

        String CREATE_POOL_REPORT_TABLE = "CREATE TABLE " + TABLE_POOL_TEST + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SCN_NO + " INTEGER,"
                + COLUMN_ALKALINITY + " REAL DEFAULT 0,"
                + COLUMN_PH + " REAL DEFAULT 0,"
                + COLUMN_FC + " REAL DEFAULT 0,"
                + COLUMN_BROMINE + " REAL DEFAULT 0,"
                + COLUMN_HARDNESS + " REAL DEFAULT 0,"
                + COLUMN_ADDEDDATE + " TEXT,"
                + COLUMN_UPDATEDDATE + " TEXT)";
        db.execSQL(CREATE_POOL_REPORT_TABLE);
        String CREATE_POOL_DATA_TABLE = "CREATE TABLE " + TABLE_DATA + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATA + " TEXT)";
        db.execSQL(CREATE_POOL_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int SaveScan(ScanModel ptdetail){
        int result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCN_NAME,ptdetail.getPlName());
        values.put(COLUMN_UPDATEDDATE, Calendar.getInstance().getTimeInMillis()/1000);
        if(ptdetail.getPlNo()!=0){
            db.update(TABLE_SCN_DETAILS,values,COLUMN_ID+"=?",new String[]{String.valueOf(ptdetail.getPlNo())});
            result = ptdetail.getPlNo();
        }else{
            values.put(COLUMN_ADDEDDATE, Calendar.getInstance().getTimeInMillis()/1000);
            db.insert(TABLE_SCN_DETAILS,null,values);
            result = getLastID(TABLE_SCN_DETAILS,db);
        }
        db.close();
        return result;
    }
    public ScanModel getScan(int scn_no){
        ScanModel scanModel = new ScanModel();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SCN_DETAILS, new String[] {
                        COLUMN_ID,
                        COLUMN_SCN_NAME,
                }, COLUMN_ID + "=?",
                new String[] { String.valueOf(scn_no) }, null, null, COLUMN_ID + " DESC", String.valueOf(1));
        if (cursor.moveToFirst()) {
            scanModel.setPlNo(Integer.parseInt(cursor.getString(0)));
            scanModel.setPlName(cursor.getString(1));
        }
        cursor.close();
        db.close();
        return scanModel;
    }




    public int SavePoolReport(PoolReport poolReport){
        int result = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCN_NO, poolReport.getScn_no());
        values.put(COLUMN_HARDNESS, poolReport.getTh());
        values.put(COLUMN_BROMINE, poolReport.getBro());
        values.put(COLUMN_FC, poolReport.getFC());
        values.put(COLUMN_PH, poolReport.getPh());
        values.put(COLUMN_ALKALINITY, poolReport.getAlk());
        values.put(COLUMN_UPDATEDDATE, Calendar.getInstance().getTimeInMillis()/1000);
        if(poolReport.getRow_id()!=0){
            db.update(TABLE_POOL_TEST,values,COLUMN_ID+"=?",new String[]{String.valueOf(poolReport.getRow_id())});
            result = poolReport.getRow_id();
        }else{
            values.put(COLUMN_ADDEDDATE, Calendar.getInstance().getTimeInMillis()/1000);
            db.insert(TABLE_POOL_TEST,null,values);
            result = getLastID(TABLE_POOL_TEST,db);
        }
        db.close();
        return result;
    }

    public PoolReport getLastPoolReport(int scn_no){
        PoolReport poolReport = new PoolReport();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_POOL_TEST, new String[] {
                COLUMN_ID,
                        COLUMN_HARDNESS,
                COLUMN_BROMINE,
                        COLUMN_FC,
                        COLUMN_PH,
                COLUMN_ALKALINITY,

                }, COLUMN_SCN_NO + "=?",
                new String[] { String.valueOf(scn_no) }, null, null, COLUMN_ID + " DESC", String.valueOf(1));
        if (cursor.moveToFirst()) {
            poolReport.setRow_id(Integer.parseInt(cursor.getString(0)));
            poolReport.setTh((float) Double.parseDouble(cursor.getString(1)));
            poolReport.setBro((float) Double.parseDouble(cursor.getString(2)));
            poolReport.setFC((float) Double.parseDouble(cursor.getString(3)));
            poolReport.setPh((float) Double.parseDouble(cursor.getString(4)));
            poolReport.setAlk((float) Double.parseDouble(cursor.getString(5)));
        }
        poolReport.setScn_no(scn_no);
        cursor.close();
        db.close();
        return poolReport;
    }


    private boolean isScanExist(int plno,SQLiteDatabase db){
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_SCN_DETAILS
                + " WHERE " + COLUMN_ID + "=" + plno , new String[] {});
        boolean exists = (cursor.getCount() > 0);
        return exists;
    }

    private int getLastID(String table,SQLiteDatabase db){
        int last_id =0;
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + table
                + " ORDER BY " + COLUMN_ID + " DESC LIMIT 1", new String[] {});
        boolean exists = (cursor.getCount() > 0);
        if(exists){
            cursor.moveToFirst();
            last_id = Integer.parseInt(cursor.getString(0));
        }
        cursor.close();
        return last_id;
    }
    public void setPoolData(String data){
        SQLiteDatabase db = this.getWritableDatabase();
        int last_id  = getLastID(TABLE_DATA,db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATA,data);
        if(last_id==0){
            // insert data
            db.insert(TABLE_DATA,null,values);
        }else{
            db.update(TABLE_DATA,values,COLUMN_ID+"=?",new String[]{String.valueOf(last_id)});
            // update data
        }
        db.close();
    }
    public String getPoolData(){
        String data = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DATA,new String[]{});
        if(cursor.moveToFirst()){
            data = cursor.getString(1);
        }
        cursor.close();
        db.close();
        return data;
    }
}
