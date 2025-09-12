package com.pilltip.pilltip.view.main

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.MainComposable.DURText
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.QuestionnaireComposable.DottedDivider
import com.pilltip.pilltip.composable.SearchComposable.AutoCompleteList
import com.pilltip.pilltip.composable.SearchComposable.PillSearchField
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.buttonModifier
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.DurGptData
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.questionnaire.SelectedDrug
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun DURPage(
    navController: NavController,
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
        BackButton(
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("PillMainPage")
        }
        HeightSpacer(62.dp)
        Text(
            text = "Q.",
            style = TextStyle(
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
            )
        )
        HeightSpacer(6.dp)
        Text(
            text = "상충작용을 분석할\n약품을 두 개 선택해주세요.",
            style = TextStyle(
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color.Black,
            )
        )
        HeightSpacer(52.dp)
        Text(
            text = "약품명",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(12.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = gray200,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .height(51.dp)
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
                .noRippleClickable {
                    navController.navigate("DURSearchPage")
                }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "찾아보기",
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = gray600,
                )
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                    contentDescription = "약 검색 페이지 이동",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp)
        ){
            Row(verticalAlignment = Alignment.CenterVertically){
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_details_red_expert_pills),
                    contentDescription = "경고 문구"
                )
                Text(
                    text = "주의사항",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = Color(0xFFEF524F),
                    )
                )
            }
            HeightSpacer(10.dp)
            Text(
                text = "본 서비스는 식품의약품안전처 의약품안전사용서비스(DUR)를 기반으로 작동합니다.",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray400,
                )
            )
            HeightSpacer(10.dp)
            Text(
                text = " 해당 약품에 대해 정부 제공 데이터가 없을 시, 부정확 할 수 있으니 반드시 의사와 상의하세요",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray400,
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DURSearchPage(
    navController: NavController,
    searchViewModel: SearchHiltViewModel
) {
    var inputText by remember { mutableStateOf("") }
    val autoCompleted by searchViewModel.autoCompleted.collectAsState()
    val isLoading by searchViewModel.isAutoCompleteLoading.collectAsState()
    val context = LocalContext.current

    val selectedDrugs = remember { mutableStateListOf<SelectedDrug>() }

    LaunchedEffect(Unit) {
        snapshotFlow { inputText }
            .debounce(700)
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { debouncedText ->
                if (debouncedText.isNotBlank()) {
                    searchViewModel.fetchAutoComplete(debouncedText, reset = true)
                }
            }
    }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.isNavigationBarVisible = true
    }
    val showBottomSheet = selectedDrugs.isNotEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDFDFD))
                .padding(horizontal = 22.dp, vertical = 18.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            PillSearchField(
                initialQuery = "",
                navController = navController,
                nowTyping = { inputText = it },
                searching = { inputText = it },
                onNavigateToResult = { query ->
                    searchViewModel.fetchDrugSearch(query)
                    Log.d("Query: ", query)
                },
                from = "dur"
            )
            HeightSpacer(28.dp)
            if (inputText.isEmpty()) {
                Text(
                    text = "약품을 검색해보세요!",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray700,
                    )
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    AutoCompleteList(
                        horizontalPadding = 0.dp,
                        query = inputText,
                        searched = autoCompleted,
                        onClick = { },
                        onLoadMore = { searchViewModel.fetchAutoComplete(inputText) },
                        onAddClick = { selected ->
                            if (selectedDrugs.size < 2 && selectedDrugs.none { it.id == selected.id }) {
                                keyboardController?.hide()
                                selectedDrugs.add(
                                    SelectedDrug(
                                        id = selected.id,
                                        name = selected.value
                                    )
                                )
                            } else if (selectedDrugs.size >= 2) {
                                Toast.makeText(context, "의약품은 2개까지만 선택할 수 있어요", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    )

                    if (isLoading) {
                        HeightSpacer(40.dp)
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(32.dp),
                            color = primaryColor,
                            strokeWidth = 3.dp
                        )
                    }

                    if (!isLoading && autoCompleted.isEmpty()) {
                        HeightSpacer(40.dp)
                        Text(
                            text = "검색 결과가 없어요",
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 20.dp),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight.Medium,
                                color = gray500
                            )
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showBottomSheet,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                tonalElevation = 6.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .padding(top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .background(Color(0xFFD9D9D9), RoundedCornerShape(2.dp))
                    )
                    HeightSpacer(16.dp)
                    Text(
                        text = "선택 의약품",
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800
                    )
                    HeightSpacer(14.dp)
                    Box(
                        modifier = Modifier
                            .heightIn(max = 150.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedDrugs.forEach { drug ->
                                Row(
                                    modifier = Modifier
                                        .border(
                                            width = 1.dp,
                                            color = gray200,
                                            shape = RoundedCornerShape(size = 100.dp)
                                        )
                                        .height(30.dp)
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(size = 100.dp)
                                        )
                                        .padding(
                                            start = 12.dp,
                                            top = 8.dp,
                                            end = 12.dp,
                                            bottom = 8.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = drug.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontFamily = pretendard,
                                            fontWeight = FontWeight(500),
                                            color = gray700,
                                            textAlign = TextAlign.Center,
                                        ),
                                        modifier = Modifier.widthIn(max = 130.dp)
                                    )
                                    WidthSpacer(4.dp)
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.btn_tag_erase),
                                        contentDescription = "삭제",
                                        modifier = Modifier.noRippleClickable {
                                            selectedDrugs.remove(
                                                drug
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    NextButton(
                        mModifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(58.dp),
                        text = "분석하기",
                        buttonColor = if (selectedDrugs.size < 2) Color(0xFF348ADF) else primaryColor
                    ) {
                        if (selectedDrugs.size == 2) navController.navigate("DURLoadingPage/${selectedDrugs[0].id}/${selectedDrugs[1].id}")
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            )
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun DURLoadingPage(
    navController: NavController,
    searchViewModel: SearchHiltViewModel,
    firstDrug: Long,
    secondDrug: Long,
) {
    val durData by searchViewModel.durGptResult.collectAsState()
    val isLoading by searchViewModel.isDurGptLoading.collectAsState()

    LaunchedEffect(key1 = firstDrug, key2 = secondDrug) {
        searchViewModel.fetchDurAi(firstDrug, secondDrug)
    }
    Crossfade(targetState = isLoading || durData == null, label = "durLoadingTransition") { loading ->
        if (loading) {
            Column(
                modifier = WhiteScreenModifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val transition = rememberInfiniteTransition()
                val translateAnimation by transition.animateFloat(
                    initialValue = 360f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1200,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Canvas(modifier = Modifier.size(size = 60.dp)) {
                    val startAngle = 5f
                    val sweepAngle = 350f

                    rotate(translateAnimation) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    primaryColor,
                                    primaryColor.copy(0f)
                                ),
                                center = Offset(size.width / 2f, size.height / 2f)
                            ),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(6 / 2f, 6 / 2f),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                HeightSpacer(28.dp)
                Text(
                    text = "상충작용 분석 중",
                    style = TextStyle(
                        fontSize = 30.sp,
                        lineHeight = 40.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = gray800,
                        textAlign = TextAlign.Center,
                    )
                )
                HeightSpacer(8.dp)
                Text(
                    text = "잠시만 기다려주세요",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 19.6.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray400,
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp)
                ){
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_details_red_expert_pills),
                            contentDescription = "경고 문구"
                        )
                        Text(
                            text = "주의사항",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(600),
                                color = Color(0xFFEF524F),
                            )
                        )
                    }
                    HeightSpacer(10.dp)
                    Text(
                        text = "본 서비스는 식품의약품안전처 의약품안전사용서비스(DUR)를 기반으로 작동합니다.",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = gray400,
                        )
                    )
                    HeightSpacer(10.dp)
                    Text(
                        text = " 해당 약품에 대해 정부 제공 데이터가 없을 시, 부정확 할 수 있으니 반드시 의사와 상의하세요",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = gray400,
                        )
                    )
                }
            }
        } else {
            DURResultPage(
                navController = navController,
                durData = durData!!
            )
        }
    }
}

@Composable
fun DURResultPage(
    navController: NavController,
    durData: DurGptData
) {
    val scrollState = rememberScrollState()
    val baseColor = if (durData.durTrueInter) Color(0xFFFFF3F3) else Color(0xFFF1F6FE)
    val endColor = Color.White
    val steps = 12
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    val gradientColors = remember {
        List(steps) { i ->
            lerp(baseColor, endColor, i / (steps - 1).toFloat())
        }
    }

    BackHandler {
        navController.navigate("PillMainPage") {
            popUpTo("PillMainPage") {
                inclusive = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .drawBehind {
                val gradient = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = size.height
                )
                drawRect(brush = gradient)
            }
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HeightSpacer(18.dp)
        BackButton(
            horizontalPadding = 0.dp,
            verticalPadding = 18.dp,
            iconDrawable = R.drawable.btn_details_share,
            backgroundColor = Color.Transparent,
            onClick = {}
        ) {
            navController.navigate("PillMainPage") {
                popUpTo("PillMainPage") {
                    inclusive = true
                }
            }
        }
        Box(
            modifier = Modifier
                .height(30.dp)
                .background(
                    color = if (durData.durTrueInter) Color(0xFFFFE1E0) else Color(
                        0xFFDFECFF
                    ), shape = RoundedCornerShape(size = 100.dp)
                )
                .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AI DUR 분석결과",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = if (durData.durTrueInter) Color(0xFFEB2C28) else primaryColor,
                )
            )
        }
        HeightSpacer(12.dp)
        Text(
            text = if (durData.durTrueInter) "의사와 상담이 필요해요!" else "복약 시 주의할 점이 없어요!",
            style = TextStyle(
                fontSize = 26.sp,
                lineHeight = 33.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(24.dp)
        Image(
            imageVector =
                if (durData.durTrueInter) ImageVector.vectorResource(R.drawable.img_dur_not_ok)
                else ImageVector.vectorResource(R.drawable.img_dur_ok),
            contentDescription = "DUR 결과 복용 가능 여부"
        )
        HeightSpacer(28.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    .padding(start = 20.dp, top = 30.dp, end = 20.dp, bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DURText(
                    title = durData.drugA,
                    isOk = durData.durTrueA,
                    description = durData.durA
                )
                HeightSpacer(30.dp)
                DottedDivider()
                HeightSpacer(30.dp)
                DURText(
                    title = durData.drugB,
                    isOk = durData.durTrueB,
                    description = durData.durB
                )
                HeightSpacer(30.dp)
                DottedDivider()
                HeightSpacer(30.dp)
                DURText(
                    title = "상충작용 분석 결과",
                    isOk = durData.durTrueInter,
                    description = durData.interact
                )
                HeightSpacer(30.dp)
                Text(
                    text = "본 결과지는 식품의약품안전처 의약품안전사용서비스(DUR)와",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray400,
                    )
                )
                HeightSpacer(4.dp)
                Text(
                    text = "Pilltip의 AI 인프라를 기반으로 제공됩니다",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray400,
                    )
                )
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
        HeightSpacer(12.dp)
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            text = "확인"
        ) {
            navController.navigate("PillMainPage") {
                popUpTo("PillMainPage") {
                    inclusive = true
                }
            }
        }
    }
}

