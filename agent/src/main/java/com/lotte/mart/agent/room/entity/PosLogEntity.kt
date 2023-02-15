package com.lotte.mart.agent.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "S_TRAN_LOG2")
data class PosLogEntity(
    @PrimaryKey @ColumnInfo(name = "SEQ_NO") var SEQ_NO: Int?
    , @NotNull @ColumnInfo(name = "SALE_DATE") var SALE_DATE: String?
    , @NotNull @ColumnInfo(name = "BUSI_TYPE") var BUSI_TYPE: String?
    , @NotNull @ColumnInfo(name = "STR_CD") var STR_CD: String?
    , @NotNull @ColumnInfo(name = "SYS_ID") var SYS_ID: String?
    , @NotNull @ColumnInfo(name = "PROC_CD") var PROC_CD: String?
    , @NotNull @ColumnInfo(name = "EVT_CD") var EVT_CD: String?
    , @NotNull @ColumnInfo(name = "CUR_LOC") var CUR_LOC: String?
    , @NotNull @ColumnInfo(name = "EVT_GBN") var EVT_GBN: String?
    , @NotNull @ColumnInfo(name = "EVT_STAT_GBN") var EVT_STAT_GBN: String?
    , @NotNull @ColumnInfo(name = "ERR_LVL") var ERR_LVL: String?
    , @NotNull @ColumnInfo(name = "APP_ERR_CMT") var APP_ERR_CMT: String?
    , @NotNull @ColumnInfo(name = "DET_CMT") var DET_CMT: String?
    , @NotNull @ColumnInfo(name = "CUR_DATE") var CUR_DATE: String?
    , @NotNull @ColumnInfo(name = "CUR_TIME") var CUR_TIME: String?
    , @NotNull @ColumnInfo(name = "SND_GB") var SND_GB: String?
    , @NotNull @ColumnInfo(name = "RCV_DATE") var RCV_DATE: String?
    , @NotNull @ColumnInfo(name = "SND_DATE") var SND_DATE: String?
)