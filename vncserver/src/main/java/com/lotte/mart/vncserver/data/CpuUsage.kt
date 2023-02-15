package com.lotte.mart.vncserver.data

//CPU 정보 클래스
data class CpuUsage(
    var total : Int,
    var used : Double
){
    companion object {
        //CPU값 계산
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