package com.lotte.mart.agent.room.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lotte.mart.agent.room.dao.AgentLogDao
import com.lotte.mart.agent.room.dao.ErrorLogDao
import com.lotte.mart.agent.room.entity.AgentLogEntity
import com.lotte.mart.agent.room.entity.ErrorLogEntity
import com.lotte.mart.agent.utils.AgentIniUtil

@Database(entities = [AgentLogEntity::class, ErrorLogEntity::class], version = 1, exportSchema = true)
abstract class AgentLogDatabase : RoomDatabase() {
    abstract fun agentLogDao(): AgentLogDao
    abstract fun errorLogDao(): ErrorLogDao

    companion object {
        private val DB_NAME = AgentIniUtil.getInstance().getPathAgentLogDb("${AgentIniUtil.Constant.PATH_LOCAL_DB}AgentLogData.db")
        @Volatile
        private var instance: AgentLogDatabase? = null

        fun getInstance(context: Context) =
              instance ?: synchronized(AgentLogDatabase::class.java){
                  instance ?: buildDatabase(context).also {
                      instance = it
                  }
                  }

        fun close(){
            synchronized(this) {
                if(instance != null) {
                    instance!!.close()
                    instance = null
                }
            }
        }

        private fun buildDatabase(context: Context): AgentLogDatabase {
            return Room.databaseBuilder(context.applicationContext, AgentLogDatabase::class.java, DB_NAME)
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