package com.lotte.mart.agent.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "S_ERROR_LOG")
data class ErrorLogEntity(
    @ColumnInfo(name = "SEQ_NO") var SEQ_NO: Int?
    , @PrimaryKey @ColumnInfo(name = "LOG_KEY") var KEY: String
    , @ColumnInfo(name = "LOG_DATA") var LOG_DATA: String?
)