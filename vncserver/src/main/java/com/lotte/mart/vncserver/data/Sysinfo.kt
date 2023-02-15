package com.lotte.mart.vncserver.data

import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlin.math.roundToInt

data class Sysinfo(
    @SerializedName("Cpu")
    var Cpu : String,
    @SerializedName("Memory")
    var Memory : String,
    @SerializedName("Battery")
    var Battery : String,
    @SerializedName("Usercount")
    var Usercount : String
){
    companion object {
        val SPLIT_CORE : String = "core"
        val SPLIT_CPU: String = "cpu"
        val SPLIT_MEMORY : String = "memory"
        val SPLIT_BATTERY : String = "battery"

        fun parse(data:String, userCount : String) : Sysinfo{
            var details = data.split(System.lineSeparator())

            val idxCore = details.indexOf(SPLIT_CORE)
            val idxCpu = details.indexOf(SPLIT_CPU)
            val idxMemory = details.indexOf(SPLIT_MEMORY)
            val idxBattery = details.indexOf(SPLIT_BATTERY)

            var coreData = details.subList(idxCore + 1, idxCpu)
            var cpuData = details.subList(idxCpu + 1, idxMemory)
            var memData = details.subList(idxMemory + 1, idxBattery)
            var bttrData = details.subList(idxBattery + 1, details.size - 1)

            Log.d("Sysinfo", "idxCore $idxCore")
            Log.d("Sysinfo", "coreData $coreData")
            Log.d("Sysinfo", "idxCpu $idxCpu")
            Log.d("Sysinfo", "cpuData $cpuData")
            Log.d("Sysinfo", "idxMemory $idxMemory")
            Log.d("Sysinfo", "memData $memData")
            Log.d("Sysinfo", "idxBattery $idxBattery")
            Log.d("Sysinfo", "bttrData $bttrData")

//            var cpu = CpuUsage.parse(coreData.size, cpuData)
            var memory = MemUsage.parse(memData[0])
            var battery = bttrData[0]
//            val c = ((cpu!!.used / cpu.total)*100).toInt()
            val c = cpuData[0].toDouble().roundToInt()
            val m = ((memory!!.used / memory.total)* 100).roundToInt()
            return Sysinfo(c.toString(), m.toString(), battery, userCount)
        }
    }
}