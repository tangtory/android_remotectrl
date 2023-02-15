package com.lotte.mart.agent.room.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lotte.mart.agent.room.dao.PosLogDao
import com.lotte.mart.agent.room.entity.PosLogEntity
import com.lotte.mart.agent.utils.AgentIniUtil

@Database(entities = [PosLogEntity::class], version = 18, exportSchema = true)
abstract class PosLogDatabase : RoomDatabase() {
//    abstract fun presetInfoDao(): PresetInfoDao
    abstract fun logDao(): PosLogDao

    companion object {
        private val DB_NAME = AgentIniUtil.getInstance().getPathPosLogDb("${AgentIniUtil.Constant.PATH_LOCAL_DB}AmsLogData.db") //"/storage/emulated/0/Download/AmsLogData.db"
        @Volatile
        private var instance: PosLogDatabase? = null

        fun getInstance(context: Context) =
             instance ?: synchronized(this) {
                    instance ?: buildDatabase(context).also {
                        instance = it
                    }
                    //instance!!.openHelper.readableDatabase
                }


        fun close(){
            synchronized(this) {
                if(instance != null) {
                    instance!!.close()
                    instance = null
                }
            }
        }

        private fun buildDatabase(context: Context): PosLogDatabase {
            return Room.databaseBuilder(context.applicationContext, PosLogDatabase::class.java, DB_NAME)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                .setJournalMode(JournalMode.TRUNCATE)
                .build()
        }
    }
}