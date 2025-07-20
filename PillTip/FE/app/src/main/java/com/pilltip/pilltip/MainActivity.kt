package com.pilltip.pilltip

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility
import com.kakao.vectormap.KakaoMapSdk
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.model.search.ReviewViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.model.search.SensitiveViewModel
import com.pilltip.pilltip.model.search.UserProfileViewModel
import com.pilltip.pilltip.model.signUp.ServerAuthAPI
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.model.signUp.TokenManager
import com.pilltip.pilltip.nav.NavGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var serverAuthAPI: ServerAuthAPI
    override fun onCreate(savedInstanceState: Bundle?) {
        val kakaoKey = BuildConfig.KAKAO_KEY
        Log.d("KakaoKey", kakaoKey)
        KakaoSdk.init(this, kakaoKey)
        KakaoMapSdk.init(this, kakaoKey)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val searchHiltViewModel: SearchHiltViewModel = hiltViewModel()
            val signUpViewModel: SignUpViewModel = hiltViewModel()
            val logViewModel: LogViewModel = viewModel()
            val sensitiveViewModel : SensitiveViewModel = hiltViewModel()
            val reviewViewModel : ReviewViewModel = hiltViewModel()
            val userProfileViewModel : UserProfileViewModel = hiltViewModel()
            val context = LocalContext.current
            Log.d("KeyHash", Utility.getKeyHash(this))

            FirebaseApp.initializeApp(context)
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            FirebaseAuth.getInstance().firebaseAuthSettings // 디버그 모드에서, PlayIntegrity 통과 못할 시 강제 리캡챠로 진행하도록 수정.
                .forceRecaptchaFlowForTesting(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }

            val startDestination by produceState(initialValue = "SplashPage", context, serverAuthAPI) {
                value = withContext(Dispatchers.IO) {
                    val accessToken = TokenManager.getAccessToken(context)
                    Log.d("accessToken: ", accessToken.toString())
                    if (accessToken != null && isAccessTokenValid(serverAuthAPI, accessToken, context)) {
                        "PillMainPage"
                    } else {
                        "SplashPage"
                    }
                }
            }
            NavGraph(
                startPage = startDestination,
                signUpViewModel = signUpViewModel,
                searchHiltViewModel = searchHiltViewModel,
                logViewModel = logViewModel,
                sensitiveViewModel = sensitiveViewModel,
                reviewViewModel = reviewViewModel,
                userProfileViewModel = userProfileViewModel
            )
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

suspend fun isAccessTokenValid(api: ServerAuthAPI, accessToken: String, context: Context): Boolean {
    return try {
        val response = api.getMyInfo("Bearer $accessToken")
        if (response.isSuccessful && response.body()?.status == "success") {
            response.body()?.data?.let {
                UserInfoManager.saveUserData(context, it)
                Log.d("UserData : ", UserInfoManager.getUserData(context).toString())
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}