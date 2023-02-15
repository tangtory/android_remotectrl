package com.lotte.mart.vncserver.data

//메모리 정보 클래스
data class MemUsage(
    var total : Double,
    var used : Double,
    var free : Double
){
    companion object {
        val SPLIT_MEM: String = "Mem"
        val SPLIT_TOTAL: String = "total"
        val SPLIT_USED: String = "used"
        val SPLIT_FREE: String = "free"

        //메모리 정보 계산
        fun parse(data:String) : MemUsage? {

            if(!data.contains(SPLIT_MEM))
                return null

            var details = data.split(" ")
            var t = 0.0
            var u = 0.0
            var f = 0.0
            var value = -1.0
            for (detail in details) {
                var ret = detail.replace("k", "").toDoubleOrNull()
                if (ret != null) {
                    value = ret
                } else {
                    if(value > -1) {
                        if(detail.contains(SPLIT_TOTAL)){
                            t = value
                            value = -1.0
                        } else if(detail.contains(SPLIT_USED)){
                            u = value
                            value = -1.0
                        } else if(detail.contains(SPLIT_FREE)){
                            f = value
                            value = -1.0
                        }
                    }
                }
            }

            return MemUsage(t, u, f)
        }

        fun parse(data:List<String>) : MemUsage? {
            var t = data[0]
            var f = data[1]
            var u = data[2]

            return MemUsage(t.toDouble(), u.toDouble(), f.toDouble())
        }
    }
}