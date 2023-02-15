package com.lotte.mart.agent.service

import android.content.Context
import com.lotte.mart.agent.room.database.AgentLogDatabase
import com.lotte.mart.agent.room.database.PosLogDatabase
import com.lotte.mart.agent.room.entity.AgentLogEntity
import com.lotte.mart.agent.room.entity.ErrorLogEntity
import com.lotte.mart.agent.room.entity.PosLogEntity
import com.lotte.mart.agent.data.Header
import com.lotte.mart.agent.data.Input
import com.lotte.mart.agent.utils.AgentIniUtil
import com.lotte.mart.agent.utils.Util
import com.lotte.mart.commonlib.log.Log
import com.lotte.mart.daemonlib.module.tcpip.TcpIpModule

/**
 * EMS로그 전송 서비스
 */
class EmsService(context: Context, port:Int) : Thread()  {
//    var _ip = ip
    var _port = port    //점서버 포트
    var _context = context
    var running = false

    companion object {
        //접속 서버 아이피
        private var server_ip = ""
        //접속 서버 아이피 인덱스(아이피 스위칭용)
        private var serverIndex = 0
        val TAG = EmsService::class.java.simpleName
        lateinit var firstServer:String     //첫번째 서버 아이피

        /**
         * 점서버 아이피 취득
         */
        fun getServerIp():String {
            if(server_ip.isNullOrEmpty()) {
                if(!Util.isExistFile(AgentIniUtil.Constant.FILE_SETTING_PATH)){
                    Log.i(TAG, "POS Config 설정 확인필요")
                }
                firstServer =  AgentIniUtil.getInstance().getServerIp() // 메인
                //1번 서버 값만 존재 할 경우
                if (firstServer.isNotEmpty()) {
                    server_ip = firstServer
                    serverIndex = 0
                }
                //그외 에러
                else {
                    server_ip = ""
                }
            }

            Log.i(TAG, "getServerIp, $server_ip")
            return server_ip
        }

        /**
         * 점서버 아이피 스위칭
         * - 접속 장애 시 접속 아이피 스위칭
         */
        fun serverSwitching(){
            //1번 서버 값만 존재 할 경우
            if(firstServer.isNotEmpty()) {
                server_ip = firstServer
            }
            //그외 에러
            else{
                server_ip = ""
            }

            Log.i(TAG, "ServerSwitching, ip : $server_ip, index : $serverIndex")
        }
    }

    /**
     * EMS로그 전송 서비스 시작
     */
    fun startService(){
        Log.i(TAG, "start EmsService, ${getServerIp()}, $_port")
        running = true
        this.start()
    }

    /**
     * EMS로그 전송 서비스 종료
     */
    fun stopService(){
        Log.i(TAG, "stop EmsService, ${getServerIp()}, $_port")
        running = false
        this.join(0)
    }

    override fun run() {
        Log.i(TAG, "Ems service is running")
        //EMS로그 전송 인터벌 값 취득
        var interval = AgentIniUtil.getInstance().getEmsLogInterval("30000")

        while(running) {
            try {
                if (Util.isExistAgentIni()) {
                    //마지막 POS 로그 업로드 키 취득(키값 기준으로 로그 전송 데이터 쿼리 실행)
                    val lastPosUploadKey = Util.getLastPosLogUploadKey()
                    //현재 POS로그 업로드중인 키 취득(값이 다를경우 미완료)
                    val posUploadingKey = Util.getPosLogUploadKey()

                    //POS로그 전송 단위 취득
                    val posProcessUnit = if (AgentIniUtil.getInstance().getPosProcessUnit()
                            .isEmpty()
                    ) "10" else AgentIniUtil.getInstance().getPosProcessUnit()
                    
                    //Agent로그 전송 단위 취득
                    val agentProcessUnit = if (AgentIniUtil.getInstance().getAgentProcessUnit("10")
                            .isEmpty()
                    ) "10" else AgentIniUtil.getInstance().getAgentProcessUnit("10")

                    //에러 로그 전송 단위 취득
                    val errorProcessUnit = if (AgentIniUtil.getInstance().getErrorProcessUnit("10")
                            .isEmpty()
                    ) "10" else AgentIniUtil.getInstance().getErrorProcessUnit("10")

                    //마지막 업로드 된 로그 키와 마지막 업로드 시도한 로그키 비교
                    val list: List<PosLogEntity> = if (posUploadingKey.isNotEmpty()) {
                        if (posUploadingKey == lastPosUploadKey) {
                            //다음 로그 전송
                            PosLogDatabase.getInstance(_context).logDao().getLogs(
                                posUploadingKey.split('-')[1].toLong() + 1,  //마지막 전송한 로그 시퀀스 +1
                                "${posUploadingKey.split('-')[2]}${posUploadingKey.split('-')[3]}".toLong(),
                                posProcessUnit.toInt()
                            )
                        } else {
                            //마지막 시도한 로그 다시 전송
                            PosLogDatabase.getInstance(_context).logDao().getLogs(
                                posUploadingKey.split('-')[1].toLong(),  //마지막 전송한 로그 시퀀스
                                "${posUploadingKey.split('-')[2]}${posUploadingKey.split('-')[3]}".toLong(),
                                posProcessUnit.toInt()
                            )
                        }
                    } else {
                        //업로딩 키값이 존재하지 않을 경우 처음부터 로그 취득
                        PosLogDatabase.getInstance(_context).logDao()
                            .getAll(posProcessUnit.toInt())
                    }
                    PosLogDatabase.close()
                    Log.i(TAG, "[POS] 로그 전송 리스트, ${list.size}, key: $posUploadingKey")

                    //취득한 로그 전송
                    for (data in list) {
                        val header = Header(
                            saleDt = Util.getCurrentDateTime("yyyyMMdd"),
                            strCd = Util.getStrCd(),
                            posNo = Util.getPosNo(),
                            tranNo = "0000",
                            seq = getSeq(data.SEQ_NO!!.toString()),
                            respCd = ""
                        )

                        val msg = Input().apply {
                            this.busiType = data.BUSI_TYPE
                            this.sysId = data.SYS_ID
                            this.procCd = data.PROC_CD
                            this.evtCd = data.EVT_CD
                            this.curLoc = data.CUR_LOC
                            this.evtGbn = data.EVT_GBN
                            this.evtStatGbn = data.EVT_STAT_GBN
                            this.errLvl = data.ERR_LVL
                            this.appErrCmt = data.APP_ERR_CMT
                            this.detCmt = data.DET_CMT
                            this.curDt = data.CUR_DATE
                            this.curTm = data.CUR_TIME
                        }

                        val dataParser = DataParser.Builder(header)
                            .request(msg)
                            .build()


                        val len = msgLength(dataParser.json.toByteArray().size + 10, false)
                        Log.d(TAG, "len, $len")
                        Log.d(TAG, "data, ${len + dataParser.json}")
                        try {
                            //로그 전송(tcp)
                            var tcp = TcpIpModule(getServerIp(), _port)
                            if (tcp.connect() == TcpIpModule.RESULTS.SUCCESS) {
                                Util.setPosLogUploadKey(
                                    "P-${data.SEQ_NO}-${data.CUR_DATE}-${data.CUR_TIME}"
                                )
                                val res = tcp.send(len + dataParser.json)
                                Log.d(TAG, "send result, $res")
                                if (!responseCheck(tcp)) {
                                    //에러 로그 저장
                                    AgentLogDatabase.getInstance(_context).errorLogDao().insert(
                                        ErrorLogEntity(
                                            null,
                                            Util.getPosLogUploadKey(),
                                            len + dataParser.json
                                        )
                                    )
                                    AgentLogDatabase.close()
                                    Log.i(TAG, "[POS] 응답 없음-에러로그 저장, ${data.SEQ_NO}")
                                }

                                //업로드 완료 로그 키 저장
                                Util.setLastPosLogUploadKey(
                                    Util.getPosLogUploadKey()
                                )
                                tcp!!.close()
                                Log.i(TAG, "[POS] 로그 전송 완료, ${data.SEQ_NO}")
                            } else {
                                serverSwitching()
                                Log.i(TAG, "[POS] 서버 연결 실패, ${data.SEQ_NO}")
                            }
                        } catch (e: Exception) {
                            //에러 로그 저장
                            AgentLogDatabase.getInstance(_context).errorLogDao().insert(
                                ErrorLogEntity(
                                    null,
                                    Util.getPosLogUploadKey(),
                                    len + dataParser.json
                                )
                            )
                            AgentLogDatabase.close()

                            Log.e(TAG, "[POS] 로그 전송 실패, ${data.SEQ_NO}", e)
                        } finally {
                            AgentLogDatabase.close()
                        }
                    }

                    //에이전트 로그 전송
                    //마지막 에이전트 로그 전송 키값 취득
                    val lastAgentUploadKey = Util.getLastAgentLogUploadKey()
                    //현재 에이전트 로그 업로드 키값 취득
                    val agentUploadingKey = Util.getAgentLogUploadKey()

                    //마지막 업로드 된 로그 키와 마지막 업로드 시도한 로그키 비교
                    var agentList: List<AgentLogEntity> = if (agentUploadingKey.isNotEmpty()) {
                        if (agentUploadingKey == lastAgentUploadKey) {
                            //다음 로그 전송
                            AgentLogDatabase.getInstance(_context).agentLogDao().getLogs(
                                agentUploadingKey.split('-')[1].toLong() + 1,  //마지막 전송한 로그 시퀀스 +1
                                "${agentUploadingKey.split('-')[2]}${agentUploadingKey.split('-')[3]}",
                                agentProcessUnit.toInt()
                            )
                        } else {
                            //마지막 시도한 로그 다시 전송
                            AgentLogDatabase.getInstance(_context).agentLogDao().getLogs(
                                agentUploadingKey.split('-')[1].toLong(),  //마지막 전송한 로그 시퀀스
                                "${agentUploadingKey.split('-')[2]}${agentUploadingKey.split('-')[3]}",
                                agentProcessUnit.toInt()
                            )
                        }
                    } else {
                        AgentLogDatabase.getInstance(_context).agentLogDao()
                            .getAll(agentProcessUnit.toInt())
                    }
                    AgentLogDatabase.close()

                    Log.i(TAG, "[AGENT] 로그 전송 리스트, ${agentList.size}, key: $agentUploadingKey")
                    for (data in agentList) {
                        val header = Header(
                            domain = "RcvAmsLog",
                            func =  "PosAmsLog",
                            saleDt = data.SALE_DATE!!,
                            strCd = data.STR_CD!!,
                            posNo = data.SYS_ID!!,
                            tranNo = "0000",
                            seq = getSeq(data.SEQ_NO!!.toString()),
                            respCd = ""
                        )

                        var msg = Input()
                        msg.busiType = data.BUSI_TYPE
                        msg.sysId = data.SYS_ID
                        msg.procCd = data.PROC_CD
                        msg.evtCd = data.EVT_CD
                        msg.curLoc = data.CUR_LOC
                        msg.evtGbn = data.EVT_GBN
                        msg.evtStatGbn = data.EVT_STAT_GBN
                        msg.errLvl = data.ERR_LVL
                        msg.appErrCmt = data.APP_ERR_CMT
                        msg.detCmt = data.DET_CMT
                        msg.curDt = data.CUR_DATE
                        msg.curTm = data.CUR_TIME
                        val dataParser = DataParser.Builder(header)
                            .request(msg)
                            .build()


                        val len = msgLength(dataParser.json.toByteArray().size + 10, false)
                        Log.d(TAG, "len, $len")
                        Log.d(TAG, "data, ${len + dataParser.json}")
                        try {
                            val tcp = TcpIpModule(getServerIp(), _port)
                            if (tcp.connect() == TcpIpModule.RESULTS.SUCCESS) {
                                //업로드 진행 로그 키 저장
                                Util.setAgentLogUploadKey(
                                    "A-${data.SEQ_NO}-${data.CUR_DATE}-${data.CUR_TIME}"
                                )
                                val res = tcp.send(len + dataParser.json)
                                Log.d(TAG, "send result, $res")
                                if (!responseCheck(tcp)) {
                                    //에러 로그 저장
                                    AgentLogDatabase.getInstance(_context).errorLogDao().insert(
                                        ErrorLogEntity(
                                            null,
                                            Util.getAgentLogUploadKey(),
                                            len + dataParser.json
                                        )
                                    )
                                    AgentLogDatabase.close()
                                    Log.i(TAG, "[AGNET] 응답 없음-에러로그 저장, ${data.SEQ_NO}")
                                }

                                //업로드 완료 로그 키 저장
                                Util.setLastAgentLogUploadKey(
                                    Util.getAgentLogUploadKey()
                                )
                                tcp.close()
                                Log.i(TAG, "[AGNET] 로그 전송 완료, ${data.SEQ_NO}")
                            } else {
                                serverSwitching()
                                Log.i(TAG, "[AGNET] 서버 연결 실패, ${data.SEQ_NO}")
                            }
                        } catch (e: Exception) {
                            //에러 로그 저장
                            AgentLogDatabase.getInstance(_context).errorLogDao().insert(
                                ErrorLogEntity(
                                    null,
                                    Util.getAgentLogUploadKey(),
                                    len + dataParser.json
                                )
                            )
                            AgentLogDatabase.close()
                            Log.e(TAG, "[AGNET] 로그 전송 실패, ${data.SEQ_NO}", e)
                        } finally {
                            AgentLogDatabase.close()
                        }
                    }

                    if (AgentIniUtil.getInstance().getErrorAutoRetry("1") == "1") {
                        //에러 로그 전송
                        val errList: List<ErrorLogEntity> =
                            AgentLogDatabase.getInstance(_context).errorLogDao()
                                .getAll(errorProcessUnit.toInt())
                        AgentLogDatabase.close()
                        Log.i(TAG, "[ERROR] 로그 전송 리스트, ${errList.size}")
                        for (data in errList) {
                            try {
                                val tcp = TcpIpModule(getServerIp(), _port)
                                if (tcp.connect() == TcpIpModule.RESULTS.SUCCESS) {
                                    val res = tcp.send(data.LOG_DATA!!)
                                    Log.d(TAG, "result, $res")
                                    if (!responseCheck(tcp)) {
                                        Log.i(TAG, "[ERROR] 오류 로그 전송 실패, ${data.KEY}")
                                    } else {
                                        AgentLogDatabase.getInstance(_context).errorLogDao()
                                            .delete(data)
                                        AgentLogDatabase.close()
                                        Log.i(TAG, "[ERROR] 오류 로그 전송 완료, ${data.KEY}")
                                    }

                                    tcp.close()
                                } else {
                                    serverSwitching()
                                    Log.i(TAG, "[ERROR] 서버 연결 실패, $agentUploadingKey")
                                }
                            } catch (e: Exception) {
                                //에러 로그 저장
                                Log.e(TAG, "[ERROR] 오류 로그 전송 에러, ${data.KEY}", e)
                            } finally {
                                AgentLogDatabase.close()
                            }
                        }
                    }
                } else {
                    if (Util.isExistAgentIni()) {
                        Log.i(TAG, "AgentWork.ini 파일 없음")
                    }
                    if (Util.isExistFile(AgentIniUtil.Constant.FILE_SETTING_PATH)) {
                        Log.i(TAG, "AgentConfig.ini 파일 없음")
                    }
                }
            } catch (e: InterruptedException){
                Log.i(TAG, "EmsService thread interrupted", e)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "EmsService 에러", e)
            } finally {
                AgentLogDatabase.close()
                PosLogDatabase.close()
            }
            sleep(interval.toLong())
        }
        Log.i(TAG, "EmsService 종료")
        AgentLogDatabase.close()
        PosLogDatabase.close()
    }

    /**
     * 응답 확인
     */
    private fun responseCheck(tcpIp :TcpIpModule):Boolean{
        if(!tcpIp.isConnected())
            return false

        val response = tcpIp.receive(5096)
        return when (response) {
            TcpIpModule.RESULTS.EXCEPTION_TIMEOUT -> {
                Log.d(TAG, "response timeout")
                false
            }
            TcpIpModule.RESULTS.EXCEPTION -> {
                Log.d(TAG, "response exception")
                false
            }
            else -> {
                Log.d(TAG, "response, $response")
                true
            }
        }
    }

    /**
     * 전송 전문 길이 계산
     */
    private fun msgLength(len:Int, zip:Boolean):String{
        val lenStr = len.toString()
        if(lenStr.length > 9)
            return ""

        val zerolen = 9-lenStr.length
        var zero = String.format("%0${zerolen}d", 0)
        zero = if(zip) {
            "$zero${lenStr}C"
        } else {
            "$zero${lenStr}N"
        }
        return zero
    }

    /**
     * 전송 전문 시퀀스 값 설정
     */
    private fun getSeq(seq:String):String{
        if(seq.length > 6)
            return ""

        var zerolen = 6-seq.length
        var zero = String.format("%0${zerolen}d", 0)
        zero = "$zero$seq"
        return zero
    }
}