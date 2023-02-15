package com.lotte.mart.agent.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.lotte.mart.agent.room.entity.PosLogEntity


@Dao
interface PosLogDao {
    /**
     * 프리셋그룹 전체 데이터 조회
     */
    @Query("SELECT * FROM S_TRAN_LOG2 LIMIT :LIMIT")
    fun getAll(LIMIT:Int): List<PosLogEntity>

    /**
     * 프리셋그룹 전체 데이터 조회
     */
    @Query("SELECT * FROM S_TRAN_LOG2 WHERE SEQ_NO >= :SEQ_NO AND CAST((CUR_DATE|| CUR_TIME) as unsigned) >= :DATETIME LIMIT :LIMIT")
    fun getLogs(SEQ_NO:Long, DATETIME: Long, LIMIT:Int): List<PosLogEntity>
//    /**
//     * 프리셋그룹 이름으로 데이터 조회
//     * @param PRS_GRP_NM  프리셋 이름
//     * @return PresetGroupEntity
//     */
//    @Query("SELECT * FROM TB_PSPM_PRSGRP_M where PRS_GRP_NM = :PRS_GRP_NM ")
//    fun getPrsGrpNm(PRS_GRP_NM: String): List<PresetGroupEntity>
//    /**
//     * 프리셋그룹 번호로 데이터 조회
//     * @param PRS_GRP_NO  프리셋 그룹 번호
//     * @return PresetGroupEntity
//     */
//    @Query("SELECT * FROM TB_PSPM_PRSGRP_M where PRS_GRP_NO = :PRS_GRP_NO ")
//    fun getPrsGrpNo(PRS_GRP_NO: String): List<PresetGroupEntity>
//
//    /**
//     * 프리셋그룹 이름 업데이트
//     * @param PRS_GRP_NM  프리셋 그룹 이름
//     * @param PRS_GRP_NO  프리셋 그룹 번호
//     * @return PresetGroupEntity
//     */
//    @Query("UPDATE TB_PSPM_PRSGRP_M SET PRS_GRP_NM = :PRS_GRP_NM WHERE PRS_GRP_NO = :PRS_GRP_NO")
//    fun setPrsGrpNo(PRS_GRP_NM: String, PRS_GRP_NO: Int)
}