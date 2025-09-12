package com.pilltip.pilltip.view.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.DrugLogCard
import com.pilltip.pilltip.composable.DrugLogDetailSection
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.UserSelectorBar
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalenderPage(
    navController: NavController,
    hiltViewModel: SearchHiltViewModel
) {
    val selectedDate by hiltViewModel.selectedDate.collectAsState()
    val logData by hiltViewModel.dailyDosageLog.collectAsState()
    val selectedDrugLog = hiltViewModel.selectedDrugLog
    val localHeight = LocalConfiguration.current.screenHeightDp
    val systemUiController = rememberSystemUiController()

    val friends by hiltViewModel.friendList.collectAsState()
    var isFriend by remember { mutableStateOf(false) }
    val selectedFriendId = if (hiltViewModel.isFriendView) hiltViewModel.targetFriendId else null

    SideEffect {
        systemUiController.setStatusBarColor(
            color = gray050,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    LaunchedEffect(selectedDate) {
        selectedDate.let {
            hiltViewModel.fetchDailyDosageLog(it)
        }
    }

    LaunchedEffect(Unit) {
        hiltViewModel.fetchFriendList()
    }

    BackHandler(enabled = hiltViewModel.selectedDrugLog != null) {
        hiltViewModel.selectedDrugLog = null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(localHeight.dp)
            .background(gray050)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "복약일정",
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
        CalendarView(
            selectedDate = selectedDate,
            onDateSelected = {
                hiltViewModel.updateSelectedDate(it)
                hiltViewModel.selectedDrugLog = null
            }
        )
        HeightSpacer(10.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    spotColor = gray600,
                    ambientColor = gray600,
                    clip = false,
                    shape = RoundedCornerShape(size = 30.dp)
                )
                .background(
                    Color.White,
                    shape = RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .padding(horizontal = 22.dp)
        ) {
            HeightSpacer(36.dp)
            Text(
                text = if (hiltViewModel.selectedDrugLog == null) "${selectedDate?.monthValue}월 ${selectedDate?.dayOfMonth}일"
                else hiltViewModel.selectedDrugLog!!.medicationName,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = primaryColor,
                )
            )
            HeightSpacer(10.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "복약 완료율",
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = gray800,
                    )
                )
                val animatedPercent by animateIntAsState(
                    targetValue = if (hiltViewModel.selectedDrugLog == null)  logData?.percent ?: 0
                    else hiltViewModel.selectedDrugLog!!.percent,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = FastOutSlowInEasing
                    )
                )
                Text(
                    text = "${animatedPercent}%",
                    style = TextStyle(
                        fontSize = 28.sp,
                        lineHeight = 42.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(700),
                        color = gray800,
                    )
                )
            }
            HeightSpacer(24.dp)
            val animatedProgress by animateFloatAsState(
                targetValue = if (hiltViewModel.selectedDrugLog == null) (logData?.percent ?: 0) / 100f
                else (hiltViewModel.selectedDrugLog!!.percent) / 100f,
                animationSpec = tween(
                    durationMillis = 700,
                    easing = FastOutSlowInEasing
                )
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = primaryColor,
                trackColor = Color(0x29787880),
            )
            HeightSpacer(36.dp)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gray050)
                .padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UserSelectorBar(
                currentId = selectedFriendId,
                friends = friends,
                onUserSelected = { selectedId ->
                    if (selectedId == null) {
                        hiltViewModel.isFriendView = false
                        hiltViewModel.targetFriendId = null
                    } else {
                        hiltViewModel.isFriendView = true
                        hiltViewModel.targetFriendId = selectedId
                    }
                    hiltViewModel.fetchDailyDosageLog(hiltViewModel.selectedDate.value)
                }
            )
            if (logData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                if (selectedDrugLog == null) {
                    logData?.perDrugLogs?.forEach { drug ->
                        DrugLogCard(drug, hiltViewModel, selectedDate)
                    }
                } else {
                    DrugLogDetailSection(selectedDrugLog, hiltViewModel, selectedDate)
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }

    val daysInMonth = remember(displayedMonth) {
        val firstDayOfMonth = displayedMonth.atDay(1)
        val lastDay = displayedMonth.lengthOfMonth()
        val dayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

        val prevPadding = List(dayOfWeek) { "" }
        val days = (1..lastDay).map { it.toString() }
        (prevPadding + days).chunked(7)
    }

    val calendarDays = remember(displayedMonth) {
        val firstDayOfMonth = displayedMonth.atDay(1)
        val lastDayOfMonth = displayedMonth.atEndOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val days = mutableListOf<CalendarDay>()
        val prevMonth = displayedMonth.minusMonths(1)
        val prevMonthEnd = prevMonth.atEndOfMonth().dayOfMonth
        val totalCells = 42
        for (i in startDayOfWeek downTo 1) {
            days.add(
                CalendarDay(
                    date = prevMonth.atDay(prevMonthEnd - i + 1),
                    isCurrentMonth = false
                )
            )
        }
        for (i in 1..lastDayOfMonth.dayOfMonth) {
            days.add(
                CalendarDay(
                    date = displayedMonth.atDay(i),
                    isCurrentMonth = true
                )
            )
        }
        val nextMonth = displayedMonth.plusMonths(1)
        for (i in 1..(totalCells - days.size)) {
            days.add(
                CalendarDay(
                    date = nextMonth.atDay(i),
                    isCurrentMonth = false
                )
            )
        }
        days.chunked(7)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                var triggered = false
                detectHorizontalDragGestures(
                    onDragEnd = { triggered = false }
                ) { _, dragAmount ->
                    if (!triggered) {
                        when {
                            dragAmount > 30 -> {
                                displayedMonth = displayedMonth.minusMonths(1)
                                triggered = true
                            }

                            dragAmount < -30 -> {
                                displayedMonth = displayedMonth.plusMonths(1)
                                triggered = true
                            }
                        }
                    }
                }
            }
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_calender_previous),
                contentDescription = "전월 이동",
                modifier = Modifier.noRippleClickable {
                    displayedMonth = displayedMonth.minusMonths(1)
                }
            )
            Text(
                text = displayedMonth.format(DateTimeFormatter.ofPattern("yyyy년 MMMM")),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_calender_next),
                contentDescription = "익월 이동",
                modifier = Modifier.noRippleClickable {
                    displayedMonth = displayedMonth.plusMonths(1)
                }
            )
        }
        HeightSpacer(12.dp)
        Row(
            Modifier
                .height(48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                Text(
                    text = it,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray400,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.width(40.dp)
                )
            }
        }
        calendarDays.forEach { week ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { day ->
                    val isSelected = selectedDate == day.date
                    val backgroundColor = when {
                        isSelected -> primaryColor
                        selectedDate == null && day.date == LocalDate.now() -> primaryColor
                        else -> Color.Transparent
                    }
                    val textColor = when {
                        isSelected -> Color.White
                        selectedDate == null && day.date == LocalDate.now() -> Color.White
                        !day.isCurrentMonth -> gray200
                        else -> gray800
                    }
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp)
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .noRippleClickable {
                                onDateSelected(day.date)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.date.dayOfMonth.toString(),
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 16.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(500),
                                color = textColor
                            )
                        )
                    }
                }
            }
        }
    }
}

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)
