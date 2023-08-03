package com.test

/**
 * @Author wangziyu1
 * @Date 2022/6/24 18:37
 * @Version 1.0
 */

import okhttp3.Interceptor.Chain

import javax.validation.constraints.NotNull
import java.io.IOException
import okhttp3.Response


class HttpDemo {


//  class OkhttpInterceptor(var maxRentry: Int) extends Nothing {
//    @NotNull
//    @throws[IOException]
//    def intercept(@NotNull chain: Nothing): Unit = {
//      /* 递归 2次下发请求，如果仍然失败 则返回 null ,但是 intercept must not return null.
//                  * 返回 null 会报 IllegalStateException 异常
//                  * */ retry(chain, 0) //这个递归真的很舒服
//
//    }
//
//    def retry(chain: Chain, retryCent: Int): Unit = {
//      val request = chain.request
//      var response: Response = null
//      try //                System.out.println("第" + (retryCent + 1) + "次执行发http请求.");
//        response = chain.proceed(request)
//      catch {
//        case e: Exception =>
//          if (maxRentry > retryCent)
//            retry(chain, retryCent + 1)
//      } finally
//        response
//    }
//  }
}
