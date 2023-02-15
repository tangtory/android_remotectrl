package com.lotte.mart.agent.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lotte.mart.agent.room.entity.ErrorLogEntity


@Dao
interface ErrorLogDao {
    //insert 구문들
    @Insert
    fun insert(errorLogEntity: ErrorLogEntity)

    @Delete
    fun delete(errorLogEntity: ErrorLogEntity)
//
//    @Insert
//    fun insert(presetGroupEntity01: PresetGroupEntity, presetGroupEntity02: PresetGroupEntity)
//
//    @Insert()
//    fun insert(vararg presetGroupEntity: PresetGroupEntity)
//
//    @Insert
//    fun insert(presetGroupEntity: List<PresetGroupEntity>)
//
//    //update 구문들
//    @Update
//    fun update(presetGroupEntity: PresetGroupEntity)
//
//    @Update
//    fun update(presetGroupEntity01: PresetGroupEntity, presetGroupEntity02: PresetGroupEntity)
//
//    @Update()
//    fun update(vararg presetGroupEntity: PresetGroupEntity)
//
//    @Update
//    fun update(presetGroupEntity: List<PresetGroupEntity>)
//
//    //delete 구문들
//    @Delete
//    fun delete(presetGroupEntity: PresetGroupEntity)
//
//    @Delete
//    fun delete(presetGroupEntity01: PresetGroupEntity, presetGroupEntity02: PresetGroupEntity)
//
//    @Delete
//    fun delete(vararg presetGroupEntity: PresetGroupEntity)
//
//    @Delete
//    fun delete(presetGroupEntity: List<PresetGroupEntity>)


    /**
     * 프리셋그룹 전체 데이터 조회
     */
    @Query("SELECT * FROM S_ERROR_LOG LIMIT :LIMIT")
    fun getAll(LIMIT :Int): List<ErrorLogEntity>
}