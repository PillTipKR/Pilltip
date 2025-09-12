package com.pilltip.pilltip.view.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.messaging.FirebaseMessaging
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.MainComposable.BottomBar
import com.pilltip.pilltip.composable.MainComposable.BottomTab
import com.pilltip.pilltip.composable.MainComposable.DosageCard
import com.pilltip.pilltip.composable.MainComposable.DosagePage
import com.pilltip.pilltip.composable.MainComposable.FeatureButton
import com.pilltip.pilltip.composable.MainComposable.LogoField
import com.pilltip.pilltip.composable.MainComposable.MainSearchField
import com.pilltip.pilltip.composable.MainComposable.SmallTabCard
import com.pilltip.pilltip.composable.MainComposable.formatDate
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.QuestionnaireComposable.DottedDivider
import com.pilltip.pilltip.composable.QuestionnaireComposable.InfoRow
import com.pilltip.pilltip.composable.QuestionnaireComposable.QuestionnaireToggleSection
import com.pilltip.pilltip.composable.SearchComposable.shareText
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.HandleBackPressToExitApp
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.model.search.SensitiveViewModel
import com.pilltip.pilltip.ui.theme.backgroundColor
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.questionnaire.Logic.toKoreanGender
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PillMainPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel,
    sensitiveViewModel: SensitiveViewModel,
    initialTab: BottomTab = BottomTab.Home
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val systemUiController = rememberSystemUiController()
    val scrollState = rememberScrollState()

    val baseColor = Color(0xFFF1F6FE)
    val endColor = Color.White
    val steps = 12

    val gradientColors = remember {
        List(steps) { i ->
            lerp(baseColor, endColor, i / (steps - 1).toFloat())
        }
    }
    HandleBackPressToExitApp(navController)
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "토큰: $token")
                searchHiltViewModel.sendFcmToken(token)
            } else {
                Log.w("FCM", "토큰 받기 실패", task.exception)
            }
        }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    Scaffold(
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .drawBehind {
                    val gradient = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = size.height
                    )
                    drawRect(brush = gradient)
                }
        ) {
            when (selectedTab) {
                BottomTab.Home -> HomePage(navController, searchHiltViewModel)
                BottomTab.Interaction -> navController.navigate("DURPage")
                BottomTab.Chart -> MyQuestionnairePage(navController, searchHiltViewModel)
                BottomTab.Calendar -> CalenderPage(navController, searchHiltViewModel)
                BottomTab.MyPage -> MyPage(
                    navController,
                    searchHiltViewModel,
                    sensitiveViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    val systemUiController = rememberSystemUiController()
    val permission = UserInfoManager.getUserData(LocalContext.current)?.permissions
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by remember { mutableStateOf(false) }
    var isFriendSheetVisible by remember { mutableStateOf(false) }
    val inviteUrl by searchHiltViewModel.inviteUrl.collectAsState()
    var link by remember { mutableStateOf("") }
    var nickname = UserInfoManager.getUserData(context)?.nickname

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color(0xFFF1F6FE),
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    LogoField(
        onClick = { navController.navigate("NotificationPage") }
    )
    MainSearchField(
        onClick = { navController.navigate("SearchPage") }
    )
    HeightSpacer(14.dp)
    Row(
        modifier = Modifier.padding(horizontal = 22.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 5.dp,
                    spotColor = gray600,
                    ambientColor = Color(0x14000000),
                    clip = false,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .weight(1f)
                .height(243.11111.dp)
                .background(color = Color.White, shape = RoundedCornerShape(size = 12.dp))
                .padding(start = 16.dp, top = 20.dp, end = 20.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .noRippleClickable {
                        navController.navigate("SearchPage")
                    }
            ) {
                Text(
                    text = "빠른 약 검색",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800
                    )
                )
                HeightSpacer(6.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "내 복약 찾아보기",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = Color(0xFF408AF1),
                        )
                    )
                    WidthSpacer(1.dp)
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_main_blue_right_arrow),
                        contentDescription = "우측 arrow"
                    )
                }

            }
            Image(
                painter = painterResource(id = R.drawable.ic_main_doc_with_pill),
                contentDescription = "logo",
                modifier = Modifier
                    .scale(1.02f)
                    .offset(x = 5.dp, y = 4.dp)
            )
        }
        WidthSpacer(12.dp)
        Column(
            modifier = Modifier
                .height(243.11111.dp)
                .weight(1f)
        ) {
            SmallTabCard(
                HeaderText = "잊지말고 드세요",
                SubHeaderText = "복약 관리",
                ImageField = R.drawable.img_main_manage,
                onClick = {
                    navController.navigate("MyDrugInfoPage")
                }
            )
            HeightSpacer(12.dp)
            SmallTabCard(
                HeaderText = "간편한 내 기록 관리",
                SubHeaderText = "스마트 문진표",
                ImageField = R.drawable.ic_main_shield,
                onClick = {
                    navController.navigate("PillMainPage/Chart") {
                        popUpTo("PillMainPage") {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
    HeightSpacer(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FeatureButton(
            imageResource = R.drawable.ic_main_friend,
            description = "친구 추가",
            onClick = {
                scope.launch {
                    try {
                        searchHiltViewModel.fetchInviteUrl()
                        delay(300)

                        inviteUrl?.let { url ->
                            link = url
                            isFriendSheetVisible = true
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("초대 링크", url))
                        } ?: Toast.makeText(context, "링크를 불러오지 못했어요", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        FeatureButton(
            imageResource = R.drawable.ic_main_cute_baby,
            description = "가족 관리",
            onClick = {
                navController.navigate("ChildrenPage")
//                Toast.makeText(context, "법적 검토 중인 기능이에요\n업데이트를 기대해주세요!", Toast.LENGTH_SHORT).show()
            }
        )
        FeatureButton(
            imageResource = R.drawable.ic_main_qrcode,
            description = "문진표 QR",
            onClick = {
                if (permission == true) navController.navigate("QRScanPage")
                else {
                    isSheetVisible = true
                }
            }
        )
        FeatureButton(
            imageResource = R.drawable.ic_main_review,
            description = "최신 리뷰",
            onClick = {
                Toast.makeText(context, "정식 오픈 뒤 서비스 예정이에요\n업데이트를 기대해주세요!", Toast.LENGTH_SHORT).show()
            }
        )
    }
    HeightSpacer(40.dp)
    val selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val logData by searchHiltViewModel.dailyDosageLog.collectAsState()
    val dateText = formatDate(selectedDate)

    LaunchedEffect(selectedDate) {
        searchHiltViewModel.fetchDailyDosageLog(selectedDate)
    }

    logData?.let { data ->
        val items = listOf(DosagePage.Overall(dateText, data.percent)) +
                data.perDrugLogs.map {
                    DosagePage.PerDrug(it.medicationName, it.percent)
                }

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { items.size }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (val item = items[page]) {
                is DosagePage.Overall -> DosageCard(
                    title = item.dateText,
                    percent = item.percent,
                    onClick = { navController.navigate("NotificationPage") }
                )

                is DosagePage.PerDrug -> DosageCard(
                    title = item.medicationName,
                    percent = item.percent,
                    onClick = { navController.navigate("NotificationPage") }
                )
            }
        }
    }

    if (isSheetVisible) {
        LaunchedEffect(Unit) {
            bottomSheetState.show()
        }
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    isSheetVisible = false
                }
            },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 8.dp, bottom = 11.dp)
                        .width(48.dp)
                        .height(5.dp)
                        .background(Color(0xFFE2E4EC), RoundedCornerShape(12.dp))
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeightSpacer(12.dp)
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
                    contentDescription = "민감정보 동의",
                    modifier = Modifier
                        .padding(1.4.dp)
                        .width(28.dp)
                        .height(28.dp)
                )
                HeightSpacer(15.dp)
                Text(
                    text = "민감정보 필요",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 25.2.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1A1A1A)
                    )
                )
                HeightSpacer(8.dp)
                Text(
                    text = "해당 기능은 민감정보가 필요해요!\n필팁의 강력한 기능들을 경험해보세요",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(400),
                        color = gray500,
                        textAlign = TextAlign.Center
                    )
                )
                HeightSpacer(12.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NextButton(
                        mModifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "뒤로가기",
                        buttonColor = gray100,
                        textColor = gray700,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                isSheetVisible = false
                            }
                        }
                    )
                    WidthSpacer(12.dp)
                    NextButton(
                        mModifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "입력하기",
                        buttonColor = primaryColor,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                                navController.navigate("EssentialPage")
                            }.invokeOnCompletion {
                                isSheetVisible = false
                            }
                        }
                    )
                }
            }
        }
    }
    if (isFriendSheetVisible) {
        LaunchedEffect(Unit) {
            bottomSheetState.show()
        }
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    isFriendSheetVisible = false
                }
            },
            sheetState = bottomSheetState,
            containerColor = Color.White,
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 8.dp, bottom = 11.dp)
                        .width(48.dp)
                        .height(5.dp)
                        .background(Color(0xFFE2E4EC), RoundedCornerShape(12.dp))
                )
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeightSpacer(12.dp)
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
                    contentDescription = "초대링크 수신 성공",
                    modifier = Modifier
                        .padding(1.4.dp)
                        .width(28.dp)
                        .height(28.dp)
                )
                HeightSpacer(15.dp)
                Text(
                    text = "초대링크 생성 성공!",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 25.2.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1A1A1A)
                    )
                )
                HeightSpacer(8.dp)
                Text(
                    text = "링크를 공유하러 가볼까요?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(400),
                        color = gray500,
                        textAlign = TextAlign.Center
                    )
                )
                HeightSpacer(12.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NextButton(
                        mModifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "뒤로가기",
                        buttonColor = gray100,
                        textColor = gray700,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                isFriendSheetVisible = false
                            }
                        }
                    )
                    WidthSpacer(12.dp)
                    NextButton(
                        mModifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "공유하기",
                        buttonColor = primaryColor,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                                shareText(context, "Pilltip | 당신만의 개인맞춤 의약관리\n ${nickname}님께서 친구 요청을 보내셨어요! ", link)
                            }.invokeOnCompletion {
                                isFriendSheetVisible = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyQuestionnairePage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = gray050,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    val questionnaire = searchHiltViewModel.questionnaireState.value
    val localHeight = LocalConfiguration.current.screenHeightDp
    val permission = UserInfoManager.getUserData(LocalContext.current)?.permissions
    val gender = UserInfoManager.getUserData(LocalContext.current)?.gender
    val birthDate = UserInfoManager.getUserData(LocalContext.current)?.birthDate
    val age = UserInfoManager.getUserData(LocalContext.current)?.age
    var pregnant = UserInfoManager.getUserData(LocalContext.current)?.pregnant
    LaunchedEffect(permission) {
        if (permission == true) {
            searchHiltViewModel.loadQuestionnaire()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(localHeight.dp)
            .background(color = gray050)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        Text(
            text = "내 문진표",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
        )
        when {
            permission != true -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HeightSpacer(150.dp)
                    Image(
                        painter = painterResource(R.drawable.ic_questionnaire_none),
                        contentDescription = "문진표 없음"
                    )
                    HeightSpacer(10.dp)
                    Text(
                        text = "필팁의 스마트 문진표를 사용하려면\n민감정보 수집 추가 동의가 필요해요!",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 22.4.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = gray600,
                            textAlign = TextAlign.Center,
                        )
                    )
                    HeightSpacer(20.dp)
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = primaryColor,
                                shape = RoundedCornerShape(size = 12.dp)
                            )
                            .height(45.dp)
                            .background(
                                color = primaryColor,
                                shape = RoundedCornerShape(size = 12.dp)
                            )
                            .padding(
                                start = 22.dp,
                                top = 14.dp,
                                end = 22.dp,
                                bottom = 14.dp
                            )
                            .noRippleClickable {
                                navController.navigate("EssentialPage")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "민감정보 제공 동의하기",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(600),
                                color = Color.White,
                            )
                        )
                    }
                }
            }

            questionnaire == null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                val editable = searchHiltViewModel.editableQuestionnaire.value ?: return

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                            clip = false
                        )
                        .background(
                            gray050,
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .padding(start = 20.dp, top = 30.dp, end = 20.dp, bottom = 30.dp),
                ) {
                    HeightSpacer(12.dp)
                    InfoRow("이름", questionnaire.realName)
                    HeightSpacer(12.dp)
                    InfoRow("생년월일", "$birthDate (만 ${age}세)")
                    HeightSpacer(12.dp)
                    InfoRow("성별", gender?.toKoreanGender() ?: "알 수 없음")
                    HeightSpacer(12.dp)
                    InfoRow("전화번호", questionnaire.phoneNumber.toString())
                    HeightSpacer(12.dp)
                    InfoRow("주소", questionnaire.address)
                    if (gender == "FEMALE") {
                        HeightSpacer(12.dp)
                        InfoRow("임신여부", if (pregnant == true) "O" else "X")
                    }
                    HeightSpacer(24.dp)
                    DottedDivider()
                    HeightSpacer(24.dp)
                    QuestionnaireToggleSection(
                        title = "복약 정보",
                        items = editable.medicationInfo,
                        getName = { it.medicationName },
                        getSubmitted = { it.submitted },
                        onToggle = { index -> searchHiltViewModel.toggleMedication(index) }
                    )
                    HeightSpacer(12.dp)
                    DottedDivider()
                    HeightSpacer(24.dp)
                    QuestionnaireToggleSection(
                        title = "알러지 정보",
                        items = editable.allergyInfo,
                        getName = { it.allergyName },
                        getSubmitted = { it.submitted },
                        onToggle = { index -> searchHiltViewModel.toggleAllergy(index) }
                    )
                    HeightSpacer(12.dp)
                    DottedDivider()
                    HeightSpacer(24.dp)
                    QuestionnaireToggleSection(
                        title = "기저질환",
                        items = editable.chronicDiseaseInfo,
                        getName = { it.chronicDiseaseName },
                        getSubmitted = { it.submitted },
                        onToggle = { index -> searchHiltViewModel.toggleChronicDisease(index) }
                    )
                    HeightSpacer(12.dp)
                    DottedDivider()
                    HeightSpacer(24.dp)
                    QuestionnaireToggleSection(
                        title = "수술 이력",
                        items = editable.surgeryHistoryInfo,
                        getName = { it.surgeryHistoryName },
                        getSubmitted = { it.submitted },
                        onToggle = { index -> searchHiltViewModel.toggleSurgeryHistory(index) }
                    )
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .height(10.dp)
                ) {
                    val triangleHeight = size.height
                    val triangleWidth = triangleHeight / 0.866f

                    val triangleCount = (size.width / triangleWidth).toInt()
                    val adjustedTriangleWidth = size.width / triangleCount

                    val path = Path().apply {
                        moveTo(0f, 0f)
                        var x = 0f
                        for (i in 0 until triangleCount) {
                            lineTo(x + adjustedTriangleWidth / 2f, triangleHeight)
                            lineTo(x + adjustedTriangleWidth, 0f)
                            x += adjustedTriangleWidth
                        }
                        lineTo(size.width, 0f)
                        close()
                    }

                    val paint = android.graphics.Paint().apply {
                        style = android.graphics.Paint.Style.FILL
                        color = "#F9FAFB".toColorInt()
                        isAntiAlias = true
                        setShadowLayer(3f, 0f, 4f, android.graphics.Color.argb(10, 0, 0, 0))
                    }

                    this.drawContext.canvas.nativeCanvas.apply {
                        save()
                        drawPath(path.asAndroidPath(), paint)
                        restore()
                    }
                }
                NextButton(
                    mModifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 22.dp)
                        .height(58.dp),
                    text = "제출하기"
                ) {
                    searchHiltViewModel.submitEditedQuestionnaire()
                    navController.navigate("QRScanPage")
                }
            }
        }
    }
}