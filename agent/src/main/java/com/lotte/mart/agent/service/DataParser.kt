package com.lotte.mart.agent.service

import com.google.gson.Gson
import com.lotte.mart.agent.data.*
import java.lang.Exception

/**
 * EMS로그 전송 전문 파서
 */
class DataParser(json: String) {

    //요청 에러 여부
    var requestError : Boolean = false
    //응답 에러 여부
    var responseError : Boolean = false
    var json : String = json
    lateinit var response : Response

    data class Builder(val header: Header){
        lateinit var json :String
        fun request(msg: Input) = apply {
            var request = Request()
            request.header = header
            request.requestBody = RequestBody(msg)
            json = Gson().toJson(request)
        }
        fun build() = DataParser(json)
    }

    companion object {
        val TAG = DataParser::class.java.simpleName
    }

    init {
    }

    fun isRequestError() : Boolean {
        return requestError
    }

    fun isResponseError() : Boolean {
        return responseError
    }

    fun requestMessage(header: Header, msg:Input): String{
        var request = Request()
        request.header = header
        request.requestBody = RequestBody(msg)
        return Gson().toJson(request)
    }

    fun setResponse(res : String): Boolean{
        var test = "{\n" +
                "\"Header\":{\n" +
                "\t\"Class\":\"T901\",\n" +
                "\t\"Func\":\"EmsMsg\",\n" +
                "\t\"SaleDt\":\"20210111\",\n" +
                "\t\"StrCd\":\"0631\",\n" +
                "\t\"PosNo\":\"\",\n" +
                "\t\"TrnsNo\":\"\",\n" +
                "\t\"Seq\":\"\",\n" +
                "\t\"SleEmpNo\":\"\",\n" +
                "\t\"RespCd\":\"3001\",\n" +
                "\t\"TranState\":\"None\"\n" +
                "\t}\n" +
                "}"
        try {
            response = Gson().fromJson(test, Response::class.java)
            return true
        } catch (e:Exception){
            //parsing error
            responseError = true
        }

        return false
    }
}