package com.pilltip.pilltip.view.auth


import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.AppBar
import com.pilltip.pilltip.composable.AuthComposable.AgeField
import com.pilltip.pilltip.composable.AuthComposable.BodyProfile
import com.pilltip.pilltip.composable.AuthComposable.LoginButton
import com.pilltip.pilltip.composable.AuthComposable.ProfileGenderPick
import com.pilltip.pilltip.composable.AuthComposable.ProfileStepDescription
import com.pilltip.pilltip.composable.AuthComposable.RoundTextField
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.DoubleLineTitleText
import com.pilltip.pilltip.composable.Guideline
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.HighlightingLine
import com.pilltip.pilltip.composable.LabelText
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.PlaceholderTextField
import com.pilltip.pilltip.composable.SingleLineTitleText
import com.pilltip.pilltip.composable.TagButton
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.buttonModifier
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.HandleBackPressToExitApp
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.signUp.KaKaoLoginViewModel
import com.pilltip.pilltip.model.signUp.LoginType
import com.pilltip.pilltip.model.signUp.PhoneAuthViewModel
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.model.signUp.TokenManager
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.gray900
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.auth.logic.InputType
import com.pilltip.pilltip.view.auth.logic.OtpInputField
import com.pilltip.pilltip.view.auth.logic.TermBottomSheet
import com.pilltip.pilltip.view.auth.logic.containsSequentialNumbers
import kotlinx.coroutines.delay

@Composable
fun SplashPage(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()
    HandleBackPressToExitApp(navController)
    SideEffect {
        systemUiController.isNavigationBarVisible = false
    }

    LaunchedEffect(Unit) {
        visible = true
        delay(3000)
        navController.navigate("SelectPage") {
            systemUiController.isNavigationBarVisible = true
            popUpTo("SplashPage") { inclusive = true }
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = primaryColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.logo_splash),
            contentDescription = "Pilltip_Logo",
            modifier = Modifier.alpha(alpha)
        )
    }
}

@Composable
fun SelectPage(
    navController: NavController,
    signUpViewModel: SignUpViewModel,
    kakaoViewModel: KaKaoLoginViewModel = hiltViewModel(),
) {
    HandleBackPressToExitApp(navController)
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    var termsOfService by remember { mutableStateOf(false) }

    val user by kakaoViewModel.user
    val token = kakaoViewModel.getAccessToken()

    LaunchedEffect(user) {
        if (user != null && token != null) {
            signUpViewModel.socialLogin(
                token = token,
                provider = "KAKAO",
                onSuccess = { accessToken, refreshToken ->
                    signUpViewModel.fetchMyInfo(accessToken) { userData ->
                        TokenManager.saveTokens(context, accessToken, refreshToken)
                        Log.d("Login", "로그인 성공! 액세스토큰: $accessToken")
                        UserInfoManager.saveUserData(context, userData)
                        Toast.makeText(context, "이미 가입하셨군요!\n${userData.nickname}님, 반가워요!", Toast.LENGTH_SHORT)
                            .show()
                        navController.navigate("PillMainPage") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onFailure = { error ->
                    Log.e("SocialLogin", "가입되어 있지 않은 회원입니다. 회원가입 진행합니다: ${error?.message}")
                    kakaoViewModel.user.value?.let { user ->
                        signUpViewModel.updateLoginType(LoginType.SOCIAL)
                        signUpViewModel.updateToken(token ?: "")
                        signUpViewModel.updateProvider("kakao")
                        termsOfService = true
                    }
                }
            )
        }
    }


    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    Column(
        modifier = WhiteScreenModifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeightSpacer(214.dp)
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.logo_pilltip_blue_pill),
            contentDescription = "필팁 알약 로고"
        )
        HeightSpacer(13.dp)
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.logo_pilltip_typo),
            contentDescription = "필팁 로고 typography"
        )
        HeightSpacer(12.dp)
        Text(
            text = "당신만의 AI 의약 관리",
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray600
        )
        HeightSpacer(117.dp)
        Image(
            painter = painterResource(id = R.drawable.ic_select_page_fast_signin),
            contentDescription = "빠른 회원가입"
        )
        Spacer(modifier = Modifier.height(10.dp))
        LoginButton(
            text = "카카오로 시작하기",
            sourceImage = R.drawable.ic_kakao_login,
            borderColor = Color(0xFFFEE500),
            backgroundColor = Color(0xFFFEE500),
            fontColor = Color.Black,
            onClick = {
                kakaoViewModel.kakaoLogin(context)
            }
        )
//        HeightSpacer(16.dp)
//        LoginButton(
//            text = "구글로 시작하기 - 개발 중",
//            sourceImage = R.drawable.ic_google_login,
//            borderColor = gray500,
//            backgroundColor = Color.White,
//            fontColor = Color.Black,
//            onClick = {
//
//            }
//        )
        HeightSpacer(16.dp)
        LoginButton(
            text = "아이디로 시작하기",
            sourceImage = R.drawable.ic_id_login,
            borderColor = Color(0xFF408AF1),
            backgroundColor = Color.White,
            fontColor = primaryColor,
            onClick = {
                navController.navigate("IDPage")
                signUpViewModel.updateLoginType(LoginType.IDPW)
                signUpViewModel.updateToken("")
                signUpViewModel.updateProvider("")
            }
        )
        HeightSpacer(14.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "기존 계정이 있으신가요?",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF949BA8),
                    textAlign = TextAlign.Center,
                )
            )
            WidthSpacer(10.dp)
            Text(
                text = "로그인",
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.noRippleClickable { navController.navigate("LoginPage") }
            )
        }

    }

    if (termsOfService) {
        TermBottomSheet(
            signUpViewModel,
            navController,
            onDismiss = { termsOfService = false }
        )
    }
}

@Composable
fun LoginPage(
    navController: NavController,
    viewModel: SignUpViewModel,
    kakaoViewModel: KaKaoLoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var id by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val user by kakaoViewModel.user
    val token = kakaoViewModel.getAccessToken()

    LaunchedEffect(user) {
        if (user != null && token != null) {
            viewModel.socialLogin(
                token = token,
                provider = "KAKAO",
                onSuccess = { accessToken, refreshToken ->
                    viewModel.fetchMyInfo(accessToken) { userData ->
                        TokenManager.saveTokens(context, accessToken, refreshToken)
                        Log.d("Login", "로그인 성공! 액세스토큰: $accessToken")
                        UserInfoManager.saveUserData(context, userData)
                        Toast.makeText(context, "${userData.nickname}님, 반가워요!", Toast.LENGTH_SHORT)
                            .show()
                        navController.navigate("PillMainPage") {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                },
                onFailure = { error ->
                    Log.e("SocialLogin", "실패: ${error?.message}")
                }
            )
        }
    }

    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 22.dp)
    ) {
        BackButton(
            navigationTo = ({ navController.navigate("SelectPage") }),
            horizontalPadding = 0.dp
        )
        HeightSpacer(56.dp)
        Text(
            text = "안녕하세요\n필팁입니다 :)",
            style = TextStyle(
                fontSize = 28.sp,
                lineHeight = 39.2.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = gray900,
                letterSpacing = 0.28.sp,
            )
        )
        HeightSpacer(12.dp)
        Text(
            text = "로그인 후 이용 부탁드려요!",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray500,
            )
        )
        HeightSpacer(40.dp)
        Text(
            text = "아이디",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(12.dp)
        RoundTextField(
            text = id,
            textChange = { id = it },
            placeholder = "아이디 입력",
            isLogin = true
        )
        HeightSpacer(20.dp)
        Text(
            text = "비밀번호",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(12.dp)
        RoundTextField(
            text = password,
            textChange = { password = it },
            placeholder = "비밀번호 입력",
            isLogin = true
        )
        HeightSpacer(28.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "아이디 찾기",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray500,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.noRippleClickable {
                    Toast.makeText(context, "업데이트를 기대해주세요!", Toast.LENGTH_SHORT).show()
//                    navController.navigate("FindMyInfoPage/FIND_ID") {
//                        popUpTo(
//                            "LoginPage"
//                        ) { inclusive = false }
//                    }
                }
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_login_vertical_divider),
                contentDescription = "디바이더",
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Text(
                text = "비밀번호 찾기",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray500,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.noRippleClickable {
                    Toast.makeText(context, "업데이트를 기대해주세요!", Toast.LENGTH_SHORT).show()
//                    navController.navigate("FindMyInfoPage/FIND_PW") {
//                        popUpTo(
//                            "LoginPage"
//                        ) { inclusive = false }
//                    }
                }
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_login_vertical_divider),
                contentDescription = "디바이더",
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Text(
                text = "회원가입",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray500,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.noRippleClickable {
                    navController.navigate("SelectPage") {
                        popUpTo(
                            0
                        ) { inclusive = false }
                    }
                }
            )
        }
        HeightSpacer(16.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                thickness = 1.5.dp,
                color = Color(0xFFE2E4EC),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "간편 로그인",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFAFB8C1),
                ),
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(
                thickness = 1.5.dp,
                color = Color(0xFFE2E4EC),
                modifier = Modifier.weight(1f)
            )
        }
        HeightSpacer(20.dp)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(52.dp)
                    .background(
                        color = Color(0xFFFDE500),
                        shape = RoundedCornerShape(size = 100.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_kakao_login),
                    contentDescription = "카카오 로고",
                    modifier = Modifier
                        .size(18.dp)
                        .noRippleClickable {
                            kakaoViewModel.kakaoLogin(context)
                        }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            text = "로그인",
            buttonColor = if (id.isNotEmpty() && password.isNotEmpty()) Color(0xFF348ADF) else Color(
                0xFFCADCF5
            ),
            onClick = {
                viewModel.login(
                    loginId = id,
                    password = password,
                    onSuccess = { accessToken, refreshToken ->
                        viewModel.fetchMyInfo(accessToken) { userData ->
                            TokenManager.saveTokens(context, accessToken, refreshToken)
                            UserInfoManager.saveUserData(context, userData)
                            Toast.makeText(
                                context,
                                "${userData.nickname}님, 반가워요!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("PillMainPage") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onFailure = { error ->
                        Toast.makeText(context, "아이디 또는 비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                        Log.e("Login", "로그인 실패", error)
                    }
                )
            }
        )
    }
}

@Composable
fun FindMyInfoPage(
    navController: NavController,
    mode: String
) {
    var step by remember { mutableStateOf(1) }
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isNameFocus by remember { mutableStateOf(false) }
    var isPhoneFocus by remember { mutableStateOf(false) }

    val nameOffsetY by animateDpAsState(
        targetValue = if (step == 1) 0.dp else 80.dp, // 아래로 이동
        animationSpec = tween(durationMillis = 500),
        label = "nameOffset"
    )

    Column(
        modifier = WhiteScreenModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(horizontalPadding = 22.dp) {
            navController.navigate("LoginPage") {
                popUpTo("SelectPage") {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
        HeightSpacer(74.dp)
        SingleLineTitleText(
            if (step == 1) "이름을 입력해주세요" else "휴대폰 번호를 입력해주세요",
            padding = 22.dp
        )
        HeightSpacer(56.dp)
        Box(modifier = Modifier.fillMaxWidth()) {
            if (step >= 1) {
                Column(
                    modifier = Modifier
                        .offset(y = nameOffsetY)
                        .fillMaxWidth()
                ) {
                    LabelText(labelText = if (name.isNotEmpty()) "이름" else "")
                    PlaceholderTextField(
                        placeHolder = "이름",
                        inputText = name,
                        inputType = InputType.TEXT,
                        onTextChanged = { name = it },
                        onFocusChanged = { isNameFocus = it }
                    )
                    HeightSpacer(14.dp)
                    HighlightingLine(text = phoneNumber, isFocused = isNameFocus)
                    HeightSpacer(14.dp)
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = step == 2,
                enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { -20 }),
                exit = fadeOut(tween(300)) + slideOutVertically(targetOffsetY = { -20 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    LabelText(labelText = if (phoneNumber.isNotEmpty()) "휴대폰 번호" else "")
                    PlaceholderTextField(
                        placeHolder = "휴대폰 번호",
                        inputText = phoneNumber,
                        inputType = InputType.NUMBER,
                        onTextChanged = { phoneNumber = it },
                        onFocusChanged = { isPhoneFocus = it }
                    )
                    HeightSpacer(14.dp)
                    HighlightingLine(text = phoneNumber, isFocused = isPhoneFocus)
                    HeightSpacer(14.dp)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            text = if (step == 1) "다음" else "확인",
            buttonColor = if ((step == 1 && name.isNotEmpty()) || (step == 2 && phoneNumber.isNotEmpty()))
                Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (step == 1 && name.isNotEmpty()) {
                    step = 2
                } else if (step == 2 && phoneNumber.isNotEmpty()) {
                    if (mode == "FIND_ID") {
                        navController.navigate("ShowFoundIdPage/${name}/${phoneNumber}")
                    } else if (mode == "FIND_PW") {
                        navController.navigate("VerifyPwResetCodePage/${name}/${phoneNumber}")
                    }
                }
            }
        )
    }
}

@Composable
fun ShowFoundIdPage(
    navController: NavController
) {
    Column(
        modifier = WhiteScreenModifier,
    ) {
        BackButton(horizontalPadding = 22.dp) {
            navController.navigate("LoginPage") { popUpTo(0) { inclusive = true } }
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
                contentDescription = "아이디 찾기",
                modifier = Modifier.size(70.dp)
            )
        }
        HeightSpacer(24.dp)
        Text(
            text = "고객님의 아이디는",
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 30.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = Color(0xFF010913),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "000000",
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 30.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = primaryColor,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "입니다!",
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 30.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NextButton(
                mModifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
                    .padding(start = 22.dp, bottom = 46.dp, end = 5.dp)
                    .height(58.dp),
                text = "비밀번호 찾기",
                buttonColor = Color(0xFFF3F4F8),
                onClick = {

                }
            )
            NextButton(
                mModifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
                    .padding(start = 5.dp, bottom = 46.dp, end = 22.dp)
                    .height(58.dp),
                text = "다음",
                buttonColor = primaryColor,
                onClick = {

                }
            )
        }
    }
}

/**
 * 아이디 입력 페이지입니다.
 * @param SignUpViewModel로, SignIn Data를 관리합니다.
 * @param navController Navigation Controller입니다.
 * @author 김기윤
 */
@Composable
fun IdPage(
    navController: NavController,
    viewModel: SignUpViewModel
) {
    val context = LocalContext.current
    var ID by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }

    var containsEngNum by remember { mutableStateOf(false) }
    var containsKorean by remember { mutableStateOf(false) }
    var containsSpeical by remember { mutableStateOf(false) }
    val engNumRegex = Regex("[a-zA-Z0-9]")
    val koreanRegex = Regex("[\uAC00-\uD7AF\u1100-\u11FF\u3130-\u318F]+")
    val specialCharRegex = Regex("[!@#$%^&*(),.?\":{}|<>]")
    containsEngNum = engNumRegex.containsMatchIn(ID)
    containsKorean = koreanRegex.containsMatchIn(ID)
    containsSpeical = specialCharRegex.containsMatchIn(ID)

    val isEnglishAndNumberValid =
        ID.matches(Regex(".*[a-zA-Z].*")) && ID.matches(Regex(".*[0-9].*")) && !containsKorean
    val isLengthValid = ID.length >= 8 && ID.length <= 20
    val isSpecialCharValid = !containsSpeical && ID.isNotEmpty()
    val isAllConditionsValid = isEnglishAndNumberValid && isLengthValid && isSpecialCharValid
    var isDuplicate by remember { mutableStateOf<Boolean?>(null) }
    val focusRequester = FocusRequester()
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navigationTo = ({ navController.navigate("SelectPage") }))
        HeightSpacer(56.dp)
        DoubleLineTitleText("아이디를", "입력해주세요")
        HeightSpacer(42.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            BasicTextField(
                value = ID,
                cursorBrush = SolidColor(if (isFocused) Color(0xFF397CDB) else Color(0xFFBFBFBF)),
                onValueChange = {
                    val filtered = it.filter { ch -> ch.isLetterOrDigit() }
                    if (filtered != it) {
                        ID = filtered
                    } else {
                        ID = it
                    }
                    isDuplicate = true
                    isChecked = false
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier
                    .height(22.dp)
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(22.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (ID.isEmpty()) {
                            Text(
                                text = "아이디",
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFFBFBFBF)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (ID.isNotEmpty()) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                    contentDescription = "x_marker",
                    modifier = Modifier
                        .padding(start = 21.43.dp, end = 9.dp, top = 2.5.dp, bottom = 1.3.dp)
                        .clickable {
                            ID = ""
                        }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(
            text = ID,
            isFocused = isFocused,
            isAllConditionsValid = isAllConditionsValid
        )
        HeightSpacer(32.dp)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            text = "가이드",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.W700,
                color = Color(0xFFAFB8C1)
            )
        )
        Guideline(
            description = "영문, 숫자 조합을 사용해주세요",
            isValid = isEnglishAndNumberValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "최소 8자리 이상, 20자 미만으로 구성해주세요",
            isValid = isLengthValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "특수문자는 사용할 수 없어요",
            isValid = isSpecialCharValid
        )
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            text = if (isChecked) "다음" else "아이디 중복 확인",
            buttonColor = if (isAllConditionsValid) Color(0xFF348ADF) else Color(0xFFCADCF5),
            onClick = {
                viewModel.updateloginId(ID)
                if (!isChecked) {
                    viewModel.checkLoginIdDuplicate(ID) { isSuccess, isAvailable ->
                        isDuplicate = isAvailable?.not()
                        if (isSuccess && isDuplicate == true) {
                            Toast.makeText(context, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show()
                        } else if (!isSuccess) {
                            Toast.makeText(context, "중복 확인 중 오류가 발생했어요.", Toast.LENGTH_SHORT).show()
                        }
                        if (isSuccess && isDuplicate == false) isChecked = true
                    }
                } else if (isChecked && !isDuplicate!! && isAllConditionsValid) {
                    // 중복 확인 완료 + 사용 가능 + 유효성 검증 완료
                    navController.navigate("PasswordPage")
                }
            }
        )
    }
}

/**
 * 비밀번호 입력 페이지입니다.
 * @param SignUpViewModel로, SignIn Data를 관리합니다.
 * @param navController Navigation Controller입니다.
 * @author 김기윤
 */
@Composable
fun PasswordPage(
    navController: NavController,
    viewModel: SignUpViewModel,
) {
    var password by remember { mutableStateOf("") }
    var reenteredPassword by remember { mutableStateOf("") }

    val focusRequester = FocusRequester()
    var isFocused by remember { mutableStateOf(false) }
    var isFocusedReentered by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val containsKorean by remember { mutableStateOf(false) }
    val isEnglishAndNumberValid =
        password.matches(Regex(".*[a-zA-Z].*")) && password.matches(Regex(".*[0-9].*")) && !containsKorean
    val isLengthValid = password.length >= 8
    val isSequentialNumbersValid = !containsSequentialNumbers(password)
    val keyboardController = LocalSoftwareKeyboardController.current

    val isAllConditionsValid = isEnglishAndNumberValid && isLengthValid && isSequentialNumbersValid
    var isVisible1 by remember { mutableStateOf(true) }
    var isVisible2 by remember { mutableStateOf(true) }
    var termsOfService by remember { mutableStateOf(false) }

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(navigationTo = ({ navController.navigate("IdPage") }))
        HeightSpacer(56.dp)
        DoubleLineTitleText("비밀번호를", "입력해주세요")
        HeightSpacer(40.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            BasicTextField(
                value = password,
                onValueChange = {
                    val filtered = it.filter { ch -> ch.isLetterOrDigit() }
                    if (filtered != it) {
                        password = filtered
                    } else {
                        password = it
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                visualTransformation = if (isVisible1 == true) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier
                    .height(22.dp)
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(21.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (password.isEmpty()) {
                            Text(
                                text = "비밀번호",
                                style = TextStyle(
                                    fontFamily = pretendard,
                                    fontSize = 18.sp,
                                    color = Color(0x99818181)
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    imageVector = ImageVector.vectorResource(R.drawable.btn_login_visiblility),
                                    contentDescription = "비밀번호 확인",
                                    modifier = Modifier.noRippleClickable {
                                        isVisible1 = !isVisible1
                                    }
                                )
                            }
                        }
                        innerTextField()
                    }
                }

            )
            if (password.isNotEmpty()) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                    contentDescription = "x_marker",
                    modifier = Modifier
                        .padding(start = 21.43.dp, end = 9.dp, top = 2.5.dp, bottom = 2.5.dp)
                        .clickable {
                            password = ""
                        }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(
            text = password,
            isFocused = isFocused,
            isAllConditionsValid = isAllConditionsValid
        )
        HeightSpacer(32.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
        ) {
            BasicTextField(
                value = reenteredPassword,
                onValueChange = {
                    val filtered = it.filter { ch -> ch.isLetterOrDigit() }
                    if (filtered != it) {
                        reenteredPassword = filtered
                    } else {
                        reenteredPassword = it
                    }
                },
                visualTransformation = if (isVisible2 == true) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
                modifier = Modifier
                    .height(22.dp)
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isFocusedReentered = focusState.isFocused
                    },
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(21.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (reenteredPassword.isEmpty()) {
                            Text(
                                text = "비밀번호 재확인",
                                style = TextStyle(
                                    fontFamily = pretendard,
                                    fontSize = 18.sp,
                                    color = Color(0x99818181)
                                )
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    imageVector = ImageVector.vectorResource(R.drawable.btn_login_visiblility),
                                    contentDescription = "비밀번호 확인",
                                    modifier = Modifier.noRippleClickable {
                                        isVisible2 = !isVisible2
                                    }
                                )
                            }
                        }
                        innerTextField()
                    }
                }
            )
            if (reenteredPassword.isNotEmpty()) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                    contentDescription = "x_marker",
                    modifier = Modifier
                        .padding(start = 21.43.dp, end = 9.dp, top = 2.5.dp, bottom = 2.5.dp)
                        .clickable {
                            reenteredPassword = ""
                        }
                )
            }
        }
        HeightSpacer(14.dp)
        HighlightingLine(
            text = reenteredPassword,
            isFocused = isFocusedReentered,
            isAllConditionsValid = password == reenteredPassword
        )
        HeightSpacer(32.dp)
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            text = "가이드",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.W700,
                color = Color(0xFFAFB8C1),
            )
        )
        Guideline(
            description = "영문, 숫자 조합을 사용해주세요",
            isValid = isEnglishAndNumberValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "최소 8자리 이상으로 구성해주세요",
            isValid = isLengthValid
        )
        HeightSpacer(12.dp)
        Guideline(
            description = "연속된 숫자는 사용할 수 없어요",
            isValid = isSequentialNumbersValid
        )
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            text = "다음",
            buttonColor =
                if (isAllConditionsValid && password == reenteredPassword) Color(0xFF348ADF)
                else Color(0xFFCADCF5),
            onClick = {
                if (isAllConditionsValid && password == reenteredPassword) {
                    viewModel.updatePassword(password)
                    if (!termsOfService)
                        termsOfService = true
                }
            }
        )
    }
    if (termsOfService) {
        TermBottomSheet(
            viewModel,
            navController,
            onDismiss = { termsOfService = false }
        )
    }
}

/**
 * 전화번호 입력 페이지입니다.
 * @param SignUpViewModel로, SignIn Data를 관리합니다.
 * @param navController Navigation Controller입니다.
 * @author 김기윤
 */
@SuppressLint("DefaultLocale")
@Composable
fun PhoneAuthPage(
    navController: NavController,
    viewModel: SignUpViewModel,
    phoneViewModel: PhoneAuthViewModel = hiltViewModel(),
) {
    var phoneNumber by remember { mutableStateOf("") }
    val verificationId by phoneViewModel.verificationId.collectAsState()
    val code by phoneViewModel.code.collectAsState()
    val timeRemaining by phoneViewModel.timeRemaining.collectAsState()
    val timerText = remember(timeRemaining) {
        val min = timeRemaining / 60
        val sec = timeRemaining % 60
        String.format("%02d:%02d", min, sec)
    }
    var isDuplicate by remember { mutableStateOf<Boolean?>(null) }
    var isChecked by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val activity = context as? Activity
    val localWitdh = LocalConfiguration.current.screenWidthDp

    val isAutoVerified by phoneViewModel.isAutoVerified.collectAsState()
    val isPhoneValid = phoneNumber.length >= 11
    val isReadyToVerify = isChecked && verificationId == null && isPhoneValid
    val isCodeEntered = verificationId != null && code.length == 6

    val buttonText = when {
        !isChecked -> "중복 확인"
        isReadyToVerify -> "인증 요청"
        isCodeEntered -> "인증하기"
        else -> "다음"
    }

    LaunchedEffect(phoneNumber) {
        isChecked = false
        isDuplicate = null
    }

    LaunchedEffect(isAutoVerified) {
        if (isAutoVerified) {
            viewModel.updatePhone(phoneNumber)
            navController.navigate("ProfilePage")
        }
    }

    Column(
        modifier = WhiteScreenModifier
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (verificationId != null) {
            HeightSpacer(130.dp)
            SingleLineTitleText("문자로 받은")
            SingleLineTitleText("인증번호를 입력해주세요")
            HeightSpacer(24.dp)
            Text(
                text = if (timeRemaining > 0) "남은 시간 $timerText" else "만료",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight.Medium,
                color = gray500
            )
            HeightSpacer(40.dp)
            OtpInputField(
                otpText = code,
                onOtpTextChange = { phoneViewModel.updateCode(it) },
                modifier = Modifier.width((localWitdh - 48).dp)
            )
        } else {
            HeightSpacer(130.dp)
            SingleLineTitleText("휴대폰 번호를 알려주세요")
            HeightSpacer(56.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    LabelText(labelText = if (phoneNumber.isNotEmpty()) "휴대폰 번호" else "")
                    PlaceholderTextField(
                        placeHolder = "휴대폰 번호",
                        inputText = phoneNumber,
                        inputType = InputType.NUMBER,
                        onTextChanged = {
                            phoneNumber = it
                            phoneViewModel.updatePhoneNumber(it)
                        },
                        onFocusChanged = {
                            isFocused = it
                        }
                    )
                }
            }
            HeightSpacer(14.dp)
            HighlightingLine(text = phoneNumber, isFocused = isFocused)
            HeightSpacer(14.dp)
            Text(
                text = "입력된 정보는 회원가입에만 사용돼요.",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray700
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            buttonColor = if (
                (verificationId == null && phoneNumber.length >= 11) ||
                (verificationId != null && code.length == 6)
            ) Color(0xFF397CDB) else Color(0xFFCADCF5),
            text = buttonText,
            onClick = {
                if (!isChecked) {
                    viewModel.checkPhoneNumberDuplicate(phoneViewModel.toString()) { isSuccess, isAvailable ->
                        isDuplicate = isAvailable?.not()
                        if (isSuccess && isDuplicate == true) {
                            Toast.makeText(context, "이미 사용 중인 번호예요.", Toast.LENGTH_SHORT).show()
                        } else if (!isSuccess) {
                            Toast.makeText(context, "중복 확인 중 오류가 발생했어요.", Toast.LENGTH_SHORT).show()
                        } else if (isDuplicate == false) {
                            isChecked = true
                            Toast.makeText(context, "사용 가능한 번호예요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (isChecked && verificationId == null) {
                    activity?.let {
                        phoneViewModel.requestVerification(
                            activity = it,
                            onSent = {},
                            onFailed = {
                                Toast.makeText(context, "인증 요청 실패", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } else if (isCodeEntered) {
                    phoneViewModel.verifyCodeInput(
                        onSuccess = {
                            viewModel.updatePhone(phoneNumber)
                            navController.navigate("ProfilePage")
                        },
                        onFailure = {
                            Toast.makeText(context, "인증 실패", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun ProfilePage(
    navController: NavController,
    viewModel: SignUpViewModel
) {
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(0) }
    var month by remember { mutableStateOf(0) }
    var day by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    val isFormValid = nickname.isNotBlank()
            && gender.isNotBlank()
            && year > 0 && month > 0 && day > 0
            && height.length >= 2
            && weight.length >= 2

    Column(
        modifier = WhiteScreenModifier.padding(horizontal = 24.dp)
    ) {
        HeightSpacer(50.dp)
        AppBar(
            horizontalPadding = 0.dp,
            LNB = R.drawable.btn_left_gray_arrow,
            LNBDesc = "뒤로가기 버튼",
            LNBClickable = { navController.navigate("SelectPage") },
            TitleText = "프로필 등록",
        )
        HeightSpacer(36.dp)
        ProfileStepDescription("닉네임")
        HeightSpacer(12.dp)
        RoundTextField(
            text = nickname,
            textChange = { nickname = it },
            placeholder = "닉네임을 입력해주세요",
            isLogin = false
        )
        HeightSpacer(28.dp)
        ProfileStepDescription("성별")
        HeightSpacer(12.dp)
        ProfileGenderPick(select = { gender = it })
        HeightSpacer(28.dp)
        ProfileStepDescription("연령")
        HeightSpacer(12.dp)
        AgeField(
            ageChange = { selectedYear, selectedMonth, selectedDay ->
                year = selectedYear
                month = selectedMonth
                day = selectedDay
            },
            displayYear = year,
            displayMonth = month,
            displayDay = day,
        )
        HeightSpacer(28.dp)
        Row {
            ProfileStepDescription("연령")
            WidthSpacer(4.dp)
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_profile_question),
                contentDescription = "물음표 아이콘을 누를 시 간단한 알림 문구를 표출합니다"
            )
        }
        HeightSpacer(12.dp)
        BodyProfile { selectedHeight, selectedWeight ->
            height = selectedHeight
            weight = selectedWeight
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "등록된 정보는 나중에 수정할 수 있어요!",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = primaryColor,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            buttonColor = if (isFormValid) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (height.length >= 2 && weight.length >= 2) {
                    viewModel.updateBirthDate(
                        year,
                        month,
                        day
                    )
                    viewModel.updateNickname(nickname)
                    viewModel.updateGender(gender)
                    viewModel.updateHeight(height.toInt())
                    viewModel.updateWeight(weight.toInt())
                    navController.navigate("InterestPage")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestPage(
    navController: NavController,
    viewModel: SignUpViewModel
) {
    val selectedKeywords = remember { mutableStateListOf<String>() }
    val allKeywords = listOf(
        "복약", "운동", "수면", "스트레스", "면역력",
        "복약관리", "만성질환", "피부질환", "소화",
        "체중감량", "알러지", "금연", "문진표",
        "정신건강", "백신", "약물부작용", "가족건강",
        "개인정보", "보안"
    )

    val context = LocalContext.current

    Column(
        modifier = WhiteScreenModifier,
    ) {
        HeightSpacer(50.dp)
        AppBar(
            horizontalPadding = 20.dp,
            LNB = R.drawable.btn_left_gray_arrow,
            LNBDesc = "뒤로가기 버튼",
            LNBClickable = { navController.navigate("ProfilePage") },
            TitleText = "관심사 선택",
        )
        HeightSpacer(56.dp)
        DoubleLineTitleText(upperTextLine = "관심있는 키워드를", lowerTextLine = "선택해보세요", padding = 20.dp)
        HeightSpacer(12.dp)
        Text(
            text = "최대 5개까지 선택할 수 있어요",
            fontSize = 12.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = gray500,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        HeightSpacer(40.dp)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            allKeywords.forEach { keyword ->
                val isSelected = keyword in selectedKeywords
                TagButton(
                    keyword = keyword,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedKeywords.remove(keyword)
                        } else if (selectedKeywords.size < 5) {
                            selectedKeywords.add(keyword)
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            text = "저장하기",
            mModifier = buttonModifier,
            buttonColor = if (selectedKeywords.isNotEmpty()) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (selectedKeywords.isNotEmpty()) {
                    viewModel.updateInterest(interest = selectedKeywords.joinToString(","))
                    viewModel.logSignUpData()
                    viewModel.completeSignUp(
                        onSuccess = { accessToken, refreshToken ->
                            TokenManager.saveTokens(context, accessToken, refreshToken)
                            viewModel.submitTerms(
                                token = accessToken,
                                onSuccess = {
                                    viewModel.fetchMyInfo(accessToken) { userData ->
                                        TokenManager.saveTokens(context, accessToken, refreshToken)
                                        UserInfoManager.saveUserData(context, userData)
                                        Toast.makeText(
                                            context,
                                            "${userData.nickname}님, 필팁에 오신 걸 환영해요!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("PillMainPage") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                },
                                onFailure = { error ->
                                    Toast.makeText(
                                        context,
                                        "약관 전송 실패: ${error?.message ?: "알 수 없는 오류"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate("SelectPage") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                context,
                                error?.message ?: "회원가입에 실패했습니다. 다시 시도해주세요.",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("SelectPage") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        )
    }
}



