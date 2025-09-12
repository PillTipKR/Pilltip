package com.pilltip.pilltip.model.signUp

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kakao.sdk.user.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class LoginType {
    SOCIAL, IDPW
}

@HiltViewModel
class KaKaoLoginViewModel @Inject constructor(
    private val kakaoAuthManager: KakaoAuthManager
) : ViewModel() {

    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user
    private var currentToken: String? = null

    fun kakaoLogin(context: Context, onResult: (Boolean) -> Unit = {}) {
        kakaoAuthManager.login(context) { token, error ->
            if (error != null) {
                Log.e("LoginVM", "로그인 실패", error)
                onResult(false)
            } else {
                if (token != null) {
                    currentToken = token.accessToken
                }
                kakaoAuthManager.getUserInfo { userInfo ->
                    _user.value = userInfo
                    onResult(true)
                }
            }
        }
    }

    fun getAccessToken(): String? = currentToken

    fun logout() {
        kakaoAuthManager.logout {
            _user.value = null
        }
    }

    fun unlink(context : Context){
        kakaoAuthManager.unlink(context)
        logout()
    }
}