package com.pilltip.pilltip.model

import android.content.Context
import com.pilltip.pilltip.model.signUp.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getAccessToken(context)
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}

class ProfileIdInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val selectedProfileId = UserInfoManager.getUserData(context)
            ?.userList
            ?.firstOrNull { it.isSelected }
            ?.userId

        selectedProfileId?.let {
            requestBuilder.addHeader("X-Profile-Id", it.toString())
        }

        return chain.proceed(requestBuilder.build())
    }
}
