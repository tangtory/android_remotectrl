package com.lotte.mart.agent.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lotte.mart.agent.room.entity.AgentLogEntity

@Dao
interface AgentLogDao {
    @Insert
    fun insert(agentLogEntity: AgentLogEntity)

    @Query("DELETE FROM S_AGENT_LOG WHERE CUR_DATE < :DATE")
    fun deleteOverDate(DATE: String)

    @Query("SELECT * FROM S_AGENT_LOG LIMIT :LIMIT")
    fun getAll(LIMIT :Int): List<AgentLogEntity>

    @Query("SELECT * FROM S_AGENT_LOG WHERE SEQ_NO >= :SEQ_NO AND CAST((CUR_DATE|| CUR_TIME) as unsigned) >= :DATETIME LIMIT :LIMIT")
    fun getLogs(SEQ_NO:Long, DATETIME: String, LIMIT :Int): List<AgentLogEntity>
}