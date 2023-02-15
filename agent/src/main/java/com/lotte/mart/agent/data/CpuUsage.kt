package com.lotte.mart.agent.data

data class CpuUsage(
    var total : Int,
    var used : Double
){


    companion object {
        fun parse(core : Int, data:List<String>) : CpuUsage? {
            var t = 100 * core
            var u = 0.0
            for (value in data) {
                if(value.toDoubleOrNull() != null) {
                    u += value.toDouble()
                }
            }

            return CpuUsage(t, u)
        }
    }
}