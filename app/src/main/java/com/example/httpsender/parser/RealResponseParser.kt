package com.example.httpsender.parser

import com.example.httpsender.entity.Response
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.AbstractParser
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type

/**
 * 输入T,输出T,并对code统一判断
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 *
 * 如果使用协程发送请求，wrappers属性可不设置，设置了也无效
 *
 * [RealResponseParser]、[ResponseParser]两者的区别是，前者返回Response<T>，后者仅返回T
 */
//@Parser(name = "RealResponse", wrappers = [PageList::class])
open class RealResponseParser<T> : AbstractParser<Response<T>> {

    protected constructor() : super()

    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): Response<T> {
        val data: Response<T> = response.convertTo(Response::class, mType)
        var t = data.data //获取data字段
        if (t == null && mType === String::class.java) {
            /*
             * 考虑到有些时候服务端会返回：{"errorCode":0,"errorMsg":"关注成功"}  类似没有data的数据
             * 此时code正确，但是data字段为空，直接返回data的话，会报空指针错误，
             * 所以，判断泛型为String类型时，重新赋值，并确保赋值不为null
             */
            @Suppress("UNCHECKED_CAST")
            t = data.msg as T
        }
        if (data.code != 0 || t == null) { //code不等于0，说明数据不正确，抛出异常
            throw ParseException(data.code.toString(), data.msg, response)
        }
        return data
    }
}