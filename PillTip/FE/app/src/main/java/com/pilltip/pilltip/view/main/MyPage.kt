package com.pilltip.pilltip.view.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.MainComposable.DosageCard
import com.pilltip.pilltip.composable.MainComposable.DrugManagementRowTab
import com.pilltip.pilltip.composable.MainComposable.DrugSummaryCard
import com.pilltip.pilltip.composable.MainComposable.HealthCard
import com.pilltip.pilltip.composable.MainComposable.formatDate
import com.pilltip.pilltip.composable.MainComposable.toDisplayStrings
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.QuestionnaireComposable.DottedDivider
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.HandleBackPressToExitApp
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.model.search.SensitiveViewModel
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.model.signUp.TokenManager
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.gray900
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel,
    sensitiveViewModel: SensitiveViewModel
) {
    HandleBackPressToExitApp(navController)
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    LaunchedEffect(Unit) {
        sensitiveViewModel.loadPermissions()
        val userPregnant = UserInfoManager.getUserData(context)?.pregnant ?: false
        sensitiveViewModel.initPregnant(userPregnant)
    }
    val nickname = UserInfoManager.getUserData(context)?.nickname
    val gender = UserInfoManager.getUserData(context)?.gender

    val result by sensitiveViewModel.pregnant.collectAsState()
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by remember { mutableStateOf(false) }
    val currentApi = Build.VERSION.SDK_INT
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                Toast.makeText(context, "이미지를 선택했어요: $uri", Toast.LENGTH_SHORT).show()
            }
        }
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            } else {
                Toast.makeText(context, "갤러리 권한이 필요해요", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val logData by searchHiltViewModel.dailyDosageLog.collectAsState()
    val dateText = formatDate(selectedDate)
    val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
    val isNotificationGranted = ContextCompat.checkSelfPermission(
        context, notificationPermission
    ) == PackageManager.PERMISSION_GRANTED

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sensitiveViewModel.updateSinglePermission("phone", true)
            Toast.makeText(context, "알림 권한이 허용되었어요", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "알림 권한이 거부되었어요", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(selectedDate) {
        searchHiltViewModel.fetchDailyDosageLog(selectedDate)
    }
    val permissionState = sensitiveViewModel.permissionState
    val toggle = permissionState?.phonePermission ?: false
    val permission = UserInfoManager.getUserData(LocalContext.current)?.permissions

    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_mypage_profile),
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .background(color = gray200, shape = RoundedCornerShape(size = 46.15385.dp))
                    .noRippleClickable {
                        val permission = if (currentApi >= Build.VERSION_CODES.TIRAMISU)
                            Manifest.permission.READ_MEDIA_IMAGES
                        else
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                galleryLauncher.launch("image/*")
                            }

                            else -> {
                                permissionLauncher.launch(permission)
                            }
                        }
                    }
            )
            WidthSpacer(20.dp)
            Text(
                text = "$nickname 님",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = gray800
                )
            )
        }
        HeightSpacer(32.dp)
        logData?.let {
            DosageCard(
                title = dateText,
                percent = it.percent,
                horizontalPadding = 0.dp,
                onClick = { navController.navigate("NotificationPage") }
            )
        }
        HeightSpacer(32.dp)
        Text(
            text = "설정",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray600,
            )
        )
        HeightSpacer(20.dp)
        MyPageMenuItem(text = "내 복약정보 관리") {
            navController.navigate("MyDrugInfoPage")
        }
        MyPageMenuItem(text = "내 건강정보 관리") {
            if (permission == true)
                navController.navigate("MyHealthPage")
            else
                navController.navigate("EssentialPage")
        }
        MyPageMenuItem(text = "내 리뷰 관리") {
            Toast.makeText(context, "업데이트를 기대해주세요!", Toast.LENGTH_SHORT).show()
        }
        MyPageMenuItem(text = "내 친구 목록") {
            navController.navigate("FriendListPage")
        }
        if (gender == "FEMALE") {
            MyPageToggleItem(
                text = "임신 여부",
                isChecked = result,
                onCheckedChange = { newValue ->
                    sensitiveViewModel.updatePregnantStatus(
                        newValue,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                if (newValue) "${nickname}님! 임신 축하드려요!" else "임신 설정이 해제되었어요",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onError = {
                            Toast.makeText(
                                context,
                                it.message ?: "설정 중 오류가 발생했어요",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            )
        }

        HeightSpacer(48.dp)
        Text(
            text = "알림",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray600,
            )
        )
        HeightSpacer(20.dp)
        MyPageToggleItem(
            text = "푸시알람 동의",
            isChecked = toggle,
            onCheckedChange = { newValue ->
                if (newValue) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!isNotificationGranted) {
                            notificationPermissionLauncher.launch(notificationPermission)
                            return@MyPageToggleItem
                        }
                    }
                    sensitiveViewModel.updateSinglePermission("phone", true)
                    Toast.makeText(
                        context,
                        "푸시 알림 권한을 허용했어요! 앞으로 복약 알림을 보내드릴게요!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    sensitiveViewModel.updateSinglePermission("phone", false)
                    Toast.makeText(context, "앞으로 복약 알림을 비롯한 푸시알림을 받지 않아요", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
        MyPageMenuItem(text = "앱 이용 약관") { navController.navigate("EssentialInfoPage") }
        MyPageMenuItem(text = "로그아웃") {
            UserInfoManager.clear(context)
            TokenManager.clear(context)
            navController.navigate("SelectPage") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
        Text(
            text = "회원 탈퇴",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray500,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable {
                    if (!isSheetVisible) {
                        isSheetVisible = true
                    }
                }
        )
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
                    contentDescription = "회원탈퇴",
                    modifier = Modifier
                        .padding(1.4.dp)
                        .width(28.dp)
                        .height(28.dp)
                )
                HeightSpacer(15.dp)
                Text(
                    text = "정말 탈퇴하시겠어요?",
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
                    text = "보안을 위해 저장된 모든 정보가 파기되며,\n복구할 수 없습니다.",
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
                        text = "탈퇴하기",
                        buttonColor = gray100,
                        textColor = gray700,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                                searchHiltViewModel.deleteAccount(
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "그동안 필팁과 함께 해주셔서 감사합니다, 다시 뵙길 바라요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate("SelectPage") {
                                            popUpTo(0) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    onError = {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                )
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
                        text = "뒤로가기",
                        buttonColor = primaryColor,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                isSheetVisible = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyPageMenuItem(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            color = if (text == "로그아웃") Color(0xFFEB2C28) else gray900,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable {
                onClick()
            }
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun MyPageToggleItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray900,
            )
        )
        IosButton(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun MyDrugInfoPage(
    navController: NavController,
    viewModel: SearchHiltViewModel
) {
    var scrollState = rememberScrollState()
    var firstSelected by remember { mutableStateOf(false) }
    var secondSelected by remember { mutableStateOf(false) }
    val pillList by viewModel.pillSummaryList.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("최신순") }
    val pillDetail by viewModel.pillDetail.collectAsState()
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    LaunchedEffect(Unit) {
        viewModel.fetchDosageSummary()
    }
    BackHandler {
        navController.navigate("PillMainPage/MyPage") {
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    Column(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        BackButton(
            title = "복약정보 관리",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("PillMainPage/MyPage") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(4.dp),
//            modifier = Modifier.horizontalScroll(scrollState)
//        ) {
//            ProfileTagButton(
//                text = "처방약만",
//                selected = firstSelected,
//                onClick = { firstSelected = !firstSelected }
//            )
//            ProfileTagButton(
//                text = "복약 중인 약만",
//                selected = secondSelected,
//                onClick = { secondSelected = !secondSelected }
//            )
//            Image(
//                imageVector = ImageVector.vectorResource(R.drawable.ic_login_vertical_divider),
//                contentDescription = "디바이더",
//                modifier = Modifier
//                    .padding(horizontal = 6.dp)
//                    .width(1.dp)
//                    .background(gray200)
//                    .height(20.dp)
//            )
//            Box {
//                ProfileTagButton(
//                    text = sortOption,
//                    image = R.drawable.btn_blue_dropdown,
//                    selected = false,
//                    onClick = { expanded = true }
//                )
//
//                DropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false },
//                    modifier = Modifier.background(Color.White)
//                ) {
//                    DropdownMenuItem(text = { Text("최신순") }, onClick = {
//                        sortOption = "최신순"
//                        expanded = false
//                    })
//                    DropdownMenuItem(text = { Text("오래된 순") }, onClick = {
//                        sortOption = "오래된 순"
//                        expanded = false
//                    })
//                    DropdownMenuItem(text = { Text("가나다순") }, onClick = {
//                        sortOption = "가나다순"
//                        expanded = false
//                    })
//                }
//            }
//        }
        HeightSpacer(10.dp)
        if (pillList.isEmpty()) {
            Text(
                text = "등록된 복약 정보가 없어요.",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(vertical = 32.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(pillList) { pill ->
                    DrugSummaryCard(
                        pill = pill,
                        onDelete = { viewModel.deletePill(it.medicationId) },
                        onEdit = {
                            viewModel.fetchTakingPillDetail(pill.medicationId) { detail ->
                                navController.navigate("DosagePage/${detail.medicationId}/${detail.medicationName}")
                            }
                        },
                        onClick = { navController.navigate("MyDrugManagementPage/${pill.medicationId}") }
                    )
                }
            }
        }
    }
}

@Composable
fun MyDrugManagementPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel,
    medicationId: Long
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    LaunchedEffect(Unit) {
        searchHiltViewModel.fetchTakingPillDetail(medicationId)
    }
    val pillDetail by searchHiltViewModel.pillDetail.collectAsState()
    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
    ) {
        BackButton(
            title = "복약정보 조회",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("PillMainPage/MyPage") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
        BackHandler {
            navController.navigate("PillMainPage/MyPage") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
        HeightSpacer(22.dp)
        if (pillDetail != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 3.dp,
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                        clip = false
                    )
                    .background(
                        Color.White,
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = 0.dp,
                            bottomEnd = 0.dp
                        )
                    )
                    .padding(start = 20.dp, top = 30.dp, end = 20.dp, bottom = 30.dp)
            ) {
                Text(
                    text = pillDetail!!.alarmName,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                HeightSpacer(24.dp)
                DottedDivider()
                HeightSpacer(24.dp)
                DrugManagementRowTab("약품명", pillDetail!!.medicationName)
                HeightSpacer(12.dp)
                DrugManagementRowTab("복약 시작일", pillDetail!!.startDate)
                HeightSpacer(12.dp)
                DrugManagementRowTab("복약 종료일", pillDetail!!.endDate)
                HeightSpacer(12.dp)
                DrugManagementRowTab("1회당 복약량", pillDetail!!.dosageAmount.toString())
                HeightSpacer(24.dp)
                DottedDivider()
                HeightSpacer(24.dp)
                Text(
                    text = "복약시간",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray600,
                        textAlign = TextAlign.Justify,
                    )
                )
                HeightSpacer(18.dp)
                pillDetail!!.dosageSchedules.forEach { schedule ->
                    val (timeStr, alarmStr) = schedule.toDisplayStrings()
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeStr,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                color = gray800,
                                textAlign = TextAlign.Justify,
                            ),
                            modifier = Modifier.width(90.dp)
                        )
                        Text(
                            text = alarmStr,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                color = if (alarmStr == "[알람 O]") primaryColor else gray200,
                                textAlign = TextAlign.Justify,
                            )
                        )
                    }

                    HeightSpacer(12.dp)
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
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
                    color = android.graphics.Color.WHITE
                    isAntiAlias = true
                    setShadowLayer(3f, 0f, 4f, android.graphics.Color.argb(10, 0, 0, 0))
                }

                this.drawContext.canvas.nativeCanvas.apply {
                    save()
                    drawPath(path.asAndroidPath(), paint)
                    restore()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssentialInfoPage(
    navController: NavController,
    sensitiveViewModel: SensitiveViewModel,
    viewModel: SignUpViewModel,
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by remember { mutableStateOf(false) }
    var isEssential1Checked by remember { mutableStateOf(false) }
    var isEssential2Checked by remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    BackHandler {
        navController.navigate("PillMainPage/MyPage") {
            popUpTo(0) {
                inclusive = true
            }
        }
    }
    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 22.dp)
            .systemBarsPadding()
    ) {
        BackButton(
            title = "앱 이용 약관",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("PillMainPage/MyPage") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
        HeightSpacer(24.dp)
        MyPageMenuItem(text = "내 민감정보 삭제") {
            isSheetVisible = true
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
                    .padding(horizontal = 24.dp)
            ) {
//                Image(
//                    imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
//                    contentDescription = "회원탈퇴",
//                    modifier = Modifier
//                        .padding(1.4.dp)
//                        .width(28.dp)
//                        .height(28.dp)
//                )
                HeightSpacer(16.dp)
                Text(
                    text = "모든 민감정보를 삭제합니다",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 20.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = Color.Black,
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                HeightSpacer(8.dp)
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        imageVector =
                            if (!isEssential1Checked)
                                ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                            else
                                ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                        contentDescription = "checkBtn",
                        modifier = Modifier
                            .size(20.dp, 20.dp)
                            .noRippleClickable { isEssential1Checked = !isEssential1Checked }
                    )
                    WidthSpacer(8.dp)
                    Text(
                        text = "내 복약정보, 문진표 등 민감정보가 포함된\n모든 데이터가 파기되며, 복구가 불가능합니다",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF686D78),
                        ),
                        modifier = Modifier.noRippleClickable {
                            isEssential1Checked = !isEssential1Checked
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                        contentDescription = "description",
                        modifier = Modifier
                            .noRippleClickable {

                            }
                    )
                }
                HeightSpacer(4.dp)
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        imageVector =
                            if (!isEssential2Checked)
                                ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                            else
                                ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                        contentDescription = "checkBtn",
                        modifier = Modifier
                            .size(20.dp, 20.dp)
                            .noRippleClickable { isEssential2Checked = !isEssential2Checked }
                    )
                    WidthSpacer(8.dp)
                    Text(
                        text = "민감정보수집동의가 철회되며, 일부서비스를\n이용하시려면 다시 동의해야합니다.",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF686D78),
                        ),
                        modifier = Modifier.noRippleClickable {
                            isEssential2Checked = !isEssential2Checked
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                        contentDescription = "description",
                        modifier = Modifier
                            .noRippleClickable {
                            }
                    )
                }
                HeightSpacer(16.dp)
                NextButton(
                    mModifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(58.dp),
                    text = "모두 동의하고 삭제하기",
                    buttonColor = if (isEssential1Checked && isEssential2Checked) primaryColor else Color(
                        0xFFCADCF5
                    ),
                    onClick = {
                        if (isEssential1Checked && isEssential2Checked) {
                            scope.launch {
                                sensitiveViewModel.sensitivePermission = false
                                sensitiveViewModel.medicalPermission = false
                                sensitiveViewModel.updateSensitivePermissions()
                                sensitiveViewModel.deleteAllSensitiveInfo(
                                    onSuccess = {
                                        viewModel.fetchMyInfo(
                                            TokenManager.getAccessToken(context)
                                            .toString(),
                                            UserInfoManager
                                                .getUserData(context)
                                                ?.userList
                                                ?.find { it.isSelected }
                                                ?.userId) { userData ->
                                            UserInfoManager.saveUserData(context, userData)
                                            Toast.makeText(
                                                context,
                                                "모든 데이터가 파기처리 되었습니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onFailure = {
                                        Toast.makeText(
                                            context,
                                            "삭제 실패, 다시 시도해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }
                                )

                                bottomSheetState.hide()
                            }.invokeOnCompletion {
                                isSheetVisible = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MyHealthPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    LaunchedEffect(Unit) {
        searchHiltViewModel.fetchSensitiveInfo()
    }

    val sensitiveInfo by searchHiltViewModel.sensitiveInfo.collectAsState()
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }
    BackHandler {
        navController.navigate("PillMainPage/MyPage") {
            popUpTo(0) {
                inclusive = true
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
    ) {
        BackButton(
            title = "건강정보 관리",
            horizontalPadding = 22.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("PillMainPage/MyPage") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = gray050)
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            HealthCard(
                title = "기저질환 관리",
                descriptionHeader = "질환",
                description = sensitiveInfo?.chronicDiseaseInfo?.joinToString(", ")
                    ?: "등록된 데이터가 없어요",
                onClick = { navController.navigate("MyHealthDetailPage/chronicDisease") }
            )
            HeightSpacer(6.dp)
            HealthCard(
                title = "알러지 관리",
                descriptionHeader = "알러지",
                description = sensitiveInfo?.allergyInfo?.joinToString(", ") ?: "등록된 데이터가 없어요",
                onClick = { navController.navigate("MyHealthDetailPage/allergy") }
            )
            HeightSpacer(6.dp)
            HealthCard(
                title = "수술 이력 관리",
                descriptionHeader = "수술명",
                description = sensitiveInfo?.surgeryHistoryInfo?.joinToString(", ")
                    ?: "등록된 데이터가 없어요",
                onClick = { navController.navigate("MyHealthDetailPage/surgery") }
            )
        }
    }
}

@Composable
fun MyHealthDetailPage(
    type: String,
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    val sensitiveInfo by searchHiltViewModel.sensitiveInfo.collectAsState()
    val list = when (type) {
        "medication" -> sensitiveInfo?.medicationInfo ?: emptyList()
        "allergy" -> sensitiveInfo?.allergyInfo ?: emptyList()
        "chronicDisease" -> sensitiveInfo?.chronicDiseaseInfo ?: emptyList()
        "surgery" -> sensitiveInfo?.surgeryHistoryInfo ?: emptyList()
        else -> emptyList()
    }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
    ) {
        BackButton(
            title = when (type) {
                "allergy" -> "약물 알러지 이력"
                "chronicDisease" -> "기저질환 이력"
                else -> "수술 이력"
            },
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) { navController.popBackStack() }
        BackHandler { navController.popBackStack() }
        HeightSpacer(16.dp)
        list.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item)
            }
        }
    }
}
