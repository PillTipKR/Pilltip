package com.pilltip.pilltip.view.questionnaire

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.AuthComposable.RoundTextField
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.QuestionnaireComposable.DottedDivider
import com.pilltip.pilltip.composable.QuestionnaireComposable.InfoRow
import com.pilltip.pilltip.composable.QuestionnaireComposable.InformationBox
import com.pilltip.pilltip.composable.QuestionnaireComposable.QuestionnaireToggleSection
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.buttonModifier
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.AllergyInfo
import com.pilltip.pilltip.model.search.ChronicDiseaseInfo
import com.pilltip.pilltip.model.search.SensitiveViewModel
import com.pilltip.pilltip.model.search.SurgeryHistoryInfo
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.model.signUp.TokenManager
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import com.pilltip.pilltip.view.auth.logic.EssentialTerms
import com.pilltip.pilltip.view.auth.logic.OptionalTerms
import com.pilltip.pilltip.view.questionnaire.Logic.toKoreanGender
import kotlinx.coroutines.launch

@Composable
fun QuestionnairePage(
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = WhiteScreenModifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .zIndex(1f)
                .align(Alignment.TopStart)
        ) {
            BackButton(
                horizontalPadding = 22.dp,
                verticalPadding = 0.dp
            ) {
                val backStackEntryExists = navController.backQueue.any {
                    it.destination.route == "DetailPage"
                }

                if (backStackEntryExists) {
                    navController.navigate("DetailPage") {
                        popUpTo("DetailPage") {
                            inclusive = false
                        }
                    }
                } else {
                    navController.navigate("PillMainPage") {
                        popUpTo("PillMainPage") {
                            inclusive = false
                        }
                    }
                }
            }

            BackHandler {
                val backStackEntryExists = navController.backQueue.any {
                    it.destination.route == "DetailPage"
                }

                if (backStackEntryExists) {
                    navController.navigate("DetailPage") {
                        popUpTo("DetailPage") {
                            inclusive = false
                        }
                    }
                } else {
                    navController.navigate("PillMainPage") {
                        popUpTo("PillMainPage") {
                            inclusive = false
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp)
                .padding(top = 80.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .border(
                        width = 1.4.dp,
                        color = primaryColor,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .background(Color.White, RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.logo_pilltip_typo),
                    contentDescription = "필팁 문진표 로고",
                    modifier = Modifier
                        .width(30.dp)
                        .height(10.dp)
                )
            }

            HeightSpacer(18.dp)
            InformationBox(
                header = "스마트 문진표",
                headerSize = 30,
                headerColor = primaryColor,
                desc = "손으로 적는 기존의 문진은 그만\n이제 간편한 문진의 시작"
            )

            HeightSpacer(56.dp)
            InformationBox(
                header = "언제 어느 곳에서든",
                desc = "내 정보를 바탕으로\n자동으로 문진표를 작성해요"
            )

            HeightSpacer(56.dp)
            InformationBox(
                header = "간편한 정보 선택",
                desc = "문진표 제출 시, 제공할 정보를\n간편하게 선택할 수 있어요"
            )

            HeightSpacer(56.dp)
            InformationBox(
                header = "개인정보 걱정마세요",
                desc = "문진표는 일정 기간이 지나면\n자동 삭제되니 유출 걱정없어요"
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    clip = false
                )
                .align(Alignment.BottomCenter)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            NextButton(
                mModifier = buttonModifier,
                text = "작성하기",
                onClick = {
                    navController.navigate("EssentialPage")
                }
            )
        }
    }
}

@Composable
fun EssentialPage(
    navController: NavController,
    sensitiveViewModel: SensitiveViewModel
) {
    var isEssentialChecked by remember { mutableStateOf(false) }
    var isOptionalChecked by remember { mutableStateOf(false) }
    var isEssentialExpanded by remember { mutableStateOf(false) }
    var isOptionalExpanded by remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    val essentialRotation by animateFloatAsState(
        targetValue = if (isEssentialExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "essential_arrow_rotation"
    )

    val optionalRotation by animateFloatAsState(
        targetValue = if (isOptionalExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "optional_arrow_rotation"
    )

    Column(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        BackButton(
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.popBackStack()
        }
        BackHandler {
            navController.popBackStack()
        }
        HeightSpacer(62.dp)
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
            contentDescription = "방패 이미지",
            modifier = Modifier
                .size(32.dp)
                .padding(1.dp)
        )
        HeightSpacer(12.dp)
        Text(
            text = "정보 보호를 위해 동의가 필요해요",
            fontSize = 24.sp,
            lineHeight = 33.8.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = gray800
        )
        HeightSpacer(12.dp)
        Text(
            text = "모든 정보는 강력히 암호화되어 안전하게 보관돼요",
            fontSize = 14.sp,
            lineHeight = 19.6.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = gray400,
        )
        HeightSpacer(34.dp)
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector =
                    if (!isEssentialChecked)
                        ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                    else
                        ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                contentDescription = "checkBtn",
                modifier = Modifier
                    .size(20.dp, 20.dp)
                    .noRippleClickable { isEssentialChecked = !isEssentialChecked }
            )
            WidthSpacer(8.dp)
            Text(
                text = "[필수] 서비스 이용약관",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF686D78),
                ),
                modifier = Modifier.noRippleClickable {
                    isEssentialChecked = !isEssentialChecked
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                contentDescription = "description",
                modifier = Modifier
                    .noRippleClickable {
                        isEssentialExpanded = !isEssentialExpanded
                        isOptionalExpanded = false
                    }
                    .graphicsLayer(rotationZ = essentialRotation)
            )
        }
        AnimatedVisibility(visible = isEssentialExpanded) {
            EssentialTerms()
        }
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector =
                    if (!isOptionalChecked)
                        ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                    else
                        ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                contentDescription = "checkBtn",
                modifier = Modifier
                    .size(20.dp, 20.dp)
                    .noRippleClickable { isOptionalChecked = !isOptionalChecked }
            )
            WidthSpacer(8.dp)
            Text(
                text = "[필수] 의료법에 관한 정보 수집 및 이용 동의서",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF686D78),
                ),
                modifier = Modifier.noRippleClickable { isOptionalChecked = !isOptionalChecked }
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                contentDescription = "grayCheck",
                modifier = Modifier
                    .noRippleClickable {
                        isOptionalExpanded = !isOptionalExpanded
                        isEssentialExpanded = false
                    }
                    .graphicsLayer(rotationZ = optionalRotation)
            )
        }
        AnimatedVisibility(visible = isOptionalExpanded) {
            OptionalTerms()
        }
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            buttonColor = if (isEssentialChecked && isOptionalChecked) primaryColor else Color(
                0xFFCADCF5
            ),
            text = "동의하기",
            onClick = {
                if (isEssentialChecked && isOptionalChecked) {
                    sensitiveViewModel.sensitivePermission = true
                    sensitiveViewModel.medicalPermission = true
                    navController.navigate("NameAddressPage")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameAddressPage(
    navController: NavController,
    sensitiveViewModel: SensitiveViewModel,
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(sensitiveViewModel.realName) }
    var address by remember { mutableStateOf(sensitiveViewModel.address) }
    var isValid = name.isNotEmpty() && address.isNotEmpty()
    val systemUiController = rememberSystemUiController()
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetVisible by remember { mutableStateOf(false) }
    val detailExists = navController.backQueue.any {
        it.destination.route == "DetailPage"
    }
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
        BackHandler {
            isSheetVisible = true
        }
        BackButton(
            title = "필수 정보 입력",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            isSheetVisible = true
        }
        HeightSpacer(36.dp)
        Text(
            text = "실명",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(12.dp)
        RoundTextField(
            text = name,
            textChange = { name = it },
            placeholder = "실명을 입력해주세요",
            isLogin = false
        )
        HeightSpacer(26.dp)
        Text(
            text = "주소",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(12.dp)
        RoundTextField(
            text = address,
            textChange = { address = it },
            placeholder = "주소를 입력해주세요",
            isLogin = false
        )

        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            buttonColor = if (isValid) primaryColor else Color(0xFFCADCF5),
            text = "다음"
        ) {
            if (isValid) {
                sensitiveViewModel.realName = name
                sensitiveViewModel.address = address
                navController.navigate("AreYouPage/알러지")
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
                    contentDescription = "경고",
                    modifier = Modifier
                        .padding(1.4.dp)
                        .width(28.dp)
                        .height(28.dp)
                )
                HeightSpacer(15.dp)
                Text(
                    text = "정말 중단하시겠어요?",
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
                    text = "진행 중인 데이터가 파기되고,\nAI 맞춤 안내를 받을 수 없어요",
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
                        text = "중단하기",
                        buttonColor = gray100,
                        textColor = gray700,
                        onClick = {
                            scope.launch {
                                bottomSheetState.hide()
                                sensitiveViewModel.resetAll()
                                if (detailExists) navController.popBackStack(
                                    "DetailPage",
                                    inclusive = false
                                )
                                else navController.popBackStack("PillMainPage", inclusive = false)
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
                        text = "계속 하기",
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
fun AreYouPage(
    navController: NavController,
    title: String,
    onYesClicked: () -> Unit,
    onNoClicked: () -> Unit
) {
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
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        BackButton(horizontalPadding = 0.dp, verticalPadding = 0.dp) {
            navController.popBackStack()
        }
        BackHandler {
            navController.popBackStack()
        }
        HeightSpacer(62.dp)
        Text(
            text = "Q.",
            fontSize = 26.sp,
            lineHeight = 33.8.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = primaryColor
        )
        HeightSpacer(4.dp)
        Text(
            text = title,
            fontSize = 26.sp,
            lineHeight = 33.8.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = Color(0xFF121212)
        )
        HeightSpacer(100.dp)
        Row {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(185.dp)
                    .background(
                        color = primaryColor050,
                        shape = RoundedCornerShape(size = 24.dp)
                    )
                    .padding(start = 17.dp, top = 22.dp, end = 17.dp, bottom = 30.dp)
                    .noRippleClickable { onYesClicked() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.btn_blue_circle),
                    contentDescription = "맞아요"
                )
                HeightSpacer(14.dp)
                Text(
                    text = "맞아요",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
            }
            WidthSpacer(16.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(185.dp)
                    .background(
                        color = Color(0xFFFFF3F3),
                        shape = RoundedCornerShape(size = 24.dp)
                    )
                    .padding(start = 17.dp, top = 22.dp, end = 17.dp, bottom = 30.dp)
                    .noRippleClickable { onNoClicked() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.btn_red_xmark),
                    contentDescription = "아니에요"
                )
                HeightSpacer(14.dp)
                Text(
                    text = "아니에요",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = Color(0xFFD51713),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


data class SelectedDrug(
    val id: Long,
    val name: String
)

@Composable
fun WritePage(
    navController: NavController,
    title: String,
    description: String,
    placeholder: String,
    mode: String,
    viewModel: SensitiveViewModel
) {
    val viewModelList = when (mode) {
        "allergy" -> viewModel.allergyInfo.map { it.allergyName }
        "etc" -> viewModel.chronicDiseaseInfo.map { it.chronicDiseaseName }
        else -> viewModel.surgeryHistoryInfo.map { it.surgeryHistoryName }
    }
    var textList by remember { mutableStateOf(viewModelList.toMutableList()) }
    val allFilled = textList.all { it.trim().isNotEmpty() }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    Box(
        modifier = WhiteScreenModifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            BackButton(horizontalPadding = 0.dp, verticalPadding = 0.dp) {
                navController.popBackStack()
            }
            BackHandler {
                navController.popBackStack()
            }

            HeightSpacer(62.dp)

            Text(
                text = "Q.",
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor
            )

            HeightSpacer(4.dp)

            Text(
                text = title,
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color(0xFF121212)
            )

            HeightSpacer(12.dp)

            Text(
                text = description,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 19.6.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray400,
                )
            )

            HeightSpacer(30.dp)
            if (textList.size < 10) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = primaryColor,
                                shape = RoundedCornerShape(size = 1000.dp)
                            )
                            .padding(1.dp)
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(size = 1000.dp)
                            )
                            .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 8.dp)
                            .noRippleClickable {
                                textList = textList.toMutableList().apply { add("") }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ 추가하기",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = primaryColor
                        )
                    }
                }
            }
            HeightSpacer(10.dp)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(textList.size) { index ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RoundTextField(
                            text = textList[index],
                            textChange = { newValue ->
                                textList =
                                    textList.toMutableList().also { it[index] = newValue }
                            },
                            placeholder = placeholder,
                            isLogin = false,
                            modifier = Modifier.weight(1f)
                        )
                        WidthSpacer(6.dp)
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.btn_red_xmark),
                            contentDescription = "삭제",
                            modifier = Modifier
                                .size(14.dp)
                                .noRippleClickable {
                                    textList = textList.toMutableList().apply { removeAt(index) }
                                }
                        )
                    }
                    HeightSpacer(12.dp)
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        NextButton(
            mModifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            text = "다음",
            buttonColor = if (allFilled) primaryColor else Color(0xFFCADCF5),
            onClick = {
                if (allFilled) {
                    val processedList = textList.map { it.trim() }

                    when (mode) {
                        "allergy" -> {
                            viewModel.allergyInfo =
                                processedList.map { AllergyInfo(it, submitted = true) }
                            navController.navigate("AreYouPage/기저질환")
                        }

                        "etc" -> {
                            viewModel.chronicDiseaseInfo =
                                processedList.map { ChronicDiseaseInfo(it, submitted = true) }
                            navController.navigate("AreYouPage/수술")
                        }

                        else -> {
                            viewModel.surgeryHistoryInfo =
                                processedList.map { SurgeryHistoryInfo(it, submitted = true) }
                            navController.navigate("SensitiveFinalPage")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun SensitiveFinalPage(
    navController: NavController,
    viewModel: SignUpViewModel,
    sensitiveViewModel: SensitiveViewModel
) {
    val context = LocalContext.current
    val realName = sensitiveViewModel.realName
    val address = sensitiveViewModel.address
    val phoneNumber = UserInfoManager.getUserData(context)?.phone
    val gender = UserInfoManager.getUserData(context)?.gender
    val birthDate = UserInfoManager.getUserData(context)?.birthDate
    val age = UserInfoManager.getUserData(context)?.age
    val allergyList = sensitiveViewModel.allergyInfo
    val chronicList = sensitiveViewModel.chronicDiseaseInfo
    val surgeryList = sensitiveViewModel.surgeryHistoryInfo
    val backStackEntryExists = navController.backQueue.any {
        it.destination.route == "DetailPage"
    }

    Box(
        modifier = WhiteScreenModifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "민감정보 확인",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray800,
                    )
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                HeightSpacer(10.dp)
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
                    InfoRow("이름", realName)
                    HeightSpacer(12.dp)
                    InfoRow("생년월일", "$birthDate (만 ${age}세)")
                    HeightSpacer(12.dp)
                    InfoRow("성별", gender?.toKoreanGender() ?: "알 수 없음")
                    HeightSpacer(12.dp)
                    InfoRow("전화번호", phoneNumber.toString())
                    HeightSpacer(12.dp)
                    InfoRow("주소", address)
                    HeightSpacer(24.dp)
                    DottedDivider()
                    HeightSpacer(24.dp)
                    QuestionnaireToggleSection(
                        title = "알러지 정보",
                        items = allergyList,
                        getName = { it.allergyName },
                        getSubmitted = { it.submitted },
                        onToggle = { },
                        enable = false
                    )
                    HeightSpacer(12.dp)
                    DottedDivider()
                    HeightSpacer(24.dp)
                    QuestionnaireToggleSection(
                        title = "기저질환",
                        items = chronicList,
                        getName = { it.chronicDiseaseName },
                        getSubmitted = { it.submitted },
                        onToggle = { },
                        enable = false
                    )
                    HeightSpacer(12.dp)
                    DottedDivider()
                    HeightSpacer(24.dp)
                    QuestionnaireToggleSection(
                        title = "수술 이력",
                        items = surgeryList,
                        getName = { it.surgeryHistoryName },
                        getSubmitted = { it.submitted },
                        onToggle = { },
                        enable = false
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
            }
            NextButton(
                mModifier = buttonModifier,
                text = "작성 완료"
            ) {
                sensitiveViewModel.updateSensitivePermissions()
                sensitiveViewModel.phoneNumber = phoneNumber.toString()
                sensitiveViewModel.submitSensitiveProfile(
                    onSuccess = {
                        viewModel.fetchMyInfo(
                            TokenManager.getAccessToken(context).toString(),
                            UserInfoManager
                                .getUserData(context)
                                ?.userList
                                ?.find { it.isSelected }
                                ?.userId) { userData ->
                            UserInfoManager.saveUserData(context, userData)
                            Toast.makeText(
                                context,
                                "민감정보 작성 완료!\n 필팁의 강력한 AI 기능을 이용해보세요!",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (backStackEntryExists) {
                                navController.navigate("DetailPage") {
                                    popUpTo("DetailPage") {
                                        inclusive = false
                                    }
                                }
                            } else {
                                navController.navigate("PillMainPage") {
                                    popUpTo("PillMainPage") {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    onFailure = {
                        Toast.makeText(context, "제출에 실패했어요. 다시 시도해주세요!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}


