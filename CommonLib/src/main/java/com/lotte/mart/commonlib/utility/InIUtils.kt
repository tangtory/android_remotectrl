package com.lotte.mart.commonlib.utility

import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.lang.Exception

/**
 * ini 유틸
 */
object InIUtils
{
    /**
     * ini 파일을 저장한다.
     * @param iniFilePath - ini 파일 경로
     * @param sectionName - sectionName 경로
     * @param optionName - optionName 경로
     * @param data - 저장할 data 내용
     */
    fun saveData(iniFilePath: String, optionName: String, data: String ) : Boolean
    {
        synchronized(this) {
            var isSucess = false
            try {
                val iniFile = File(iniFilePath)
                if (!iniFile.exists()) {
                    iniFile.createNewFile()
                }
                val jsonData = iniFile.readText()
                if(jsonData.isNotEmpty()){
                    val jsonObject = JSONObject(jsonData)
                    jsonObject.put(optionName,data)
                    iniFile.writeText(jsonObject.toString())
                    isSucess = true
                }else{
                    val jsonObject = JSONObject()
                    jsonObject.put(optionName,data)
                    iniFile.writeText(jsonObject.toString())
                    isSucess = true
                }
            } catch (e: Exception) {
                isSucess = false
                e.printStackTrace()
            }
            return isSucess
        }
    }



    /**
     * ini 파일을 불러온다. 값이 없을 경우 기본값 리턴
     * @param iniFilePath - ini 파일 경로
     * @param sectionName - sectionName 경로
     * @param optionName - optionName 경로
     */
    fun loadData(iniFilePath: String, optionName: String, default:String) : String
    {
        var result = ""
        try {
            val iniFile = File("$iniFilePath")
            if(!iniFile.exists())
            {
                iniFile.createNewFile()
            }

            val jsonData = iniFile.readText()
            if(jsonData.isNotEmpty()){
                val JSONObject = JSONObject(jsonData)
                result = JSONObject.getString(optionName)
            }

            return result
        } catch (e: JSONException){
            e.printStackTrace()
            return if(saveData(iniFilePath,optionName,default)) default else ""
        }
    }

    /**
     * ini 존재 여부 확인
     * @param iniFilePath - ini 파일 경로
     */
    fun isExist(iniFilePath: String): Boolean{
        val iniFile = File("$iniFilePath")
        return iniFile.exists()
    }
}