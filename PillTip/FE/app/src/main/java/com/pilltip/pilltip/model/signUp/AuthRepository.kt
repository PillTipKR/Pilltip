package com.pilltip.pilltip.model.signUp

import android.util.Log
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: ServerAuthAPI
) {
    suspend fun sendSignUp(data: SignUpData): Pair<Boolean, SignUpTokenData?> {
        val rawPhone = data.phone
        val formattedPhone = formatPhoneForServer(rawPhone)
        val request = SignUpRequest(
            loginType = data.loginType.name,
            loginId = if (data.loginType == LoginType.IDPW) data.loginId else null,
            password = if (data.loginType == LoginType.IDPW) data.password else null,
            token = if (data.loginType == LoginType.SOCIAL) data.token else null,
            provider = if (data.loginType == LoginType.SOCIAL) data.provider else null,
            nickname = data.nickname,
            gender = data.gender,
            birthDate = data.birthDate,
            age = data.age,
            height = data.height,
            weight = data.weight,
            phone = formattedPhone,
            interest = data.interest,
        )
        Log.d("SignUp", "Request 객체 생성 완료")

        return try {
            val response = authApi.signUp(request)

            if (response.isSuccessful && response.body()?.status == "success") {
                Pair(true, response.body()?.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("SignUp", "응답 실패 - 코드: ${response.code()}, 바디: $errorBody")
                Pair(false, null)
            }
        } catch (e: Exception) {
            Log.e("SignUp", "네트워크 오류 발생", e)
            Pair(false, null)
        }
    }

    suspend fun submitTerms(token: String): Boolean {
        return try {
            val response = authApi.submitTerms("Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SubmitTerms", "Error submitting terms", e)
            false
        }
    }

    suspend fun login(loginId: String, password: String): Pair<Boolean, SignUpTokenData?> {
        val request = LoginRequest(loginId = loginId, password = password)
        return try {
            val response = authApi.login(request)
            if (response.isSuccessful && response.body()?.status == "success") {
                Pair(true, response.body()?.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("Login", "로그인 실패 - 코드: ${response.code()}, 바디: $errorBody")
                Pair(false, null)
            }
        } catch (e: Exception) {
            Log.e("Login", "네트워크 오류", e)
            Pair(false, null)
        }
    }

    suspend fun socialLogin(token: String, provider: String): Pair<Boolean, SignUpTokenData?> {
        val request = SocialLoginRequest(token = token, provider = provider)
        return try {
            val response = authApi.socialLogin(request)
            if (response.isSuccessful && response.body()?.status == "success") {
                Pair(true, response.body()?.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("SocialLogin", "실패 - 코드: ${response.code()}, 바디: $errorBody")
                Pair(false, null)
            }
        } catch (e: Exception) {
            Log.e("SocialLogin", "네트워크 오류", e)
            Pair(false, null)
        }
    }

    suspend fun getMyInfo(token: String, profileId: Long?): UserData? {
        return try {
            val response = authApi.getMyInfo("Bearer $token", profileId)
            if (response.isSuccessful) {
                response.body()?.data
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthMe", "실패 - 코드: ${response.code()}, 바디: $errorBody")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthMe", "네트워크 오류", e)
            null
        }
    }

    fun formatPhoneForServer(phone: String): String {
        return when {
            phone.length == 11 -> "${phone.substring(0,3)}-${phone.substring(3,7)}-${phone.substring(7)}"
            phone.length == 10 -> "${phone.substring(0,3)}-${phone.substring(3,6)}-${phone.substring(6)}"
            else -> phone
        }
    }

    suspend fun checkDuplicate(value: String, type: String): Pair<Boolean, Boolean?> {
        return try {
            val response = authApi.checkDuplicate(DuplicateCheckRequest(value, type))
            Log.d("중복 검사 결과: ", response.toString())
            if (response.isSuccessful) {
                Pair(true, response.body()?.data)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CheckDuplicate", "응답 실패 - 코드: ${response.code()}, 바디: $errorBody")
                Pair(false, null)
            }
        } catch (e: Exception) {
            Log.e("CheckDuplicate", "네트워크 오류 발생", e)
            Pair(false, null)
        }
    }
}