package com.pilltip.pilltip.view.search

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.AuthComposable.AgeField
import com.pilltip.pilltip.composable.AuthComposable.RoundTextField
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.DoubleLineTitleText
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.SearchComposable.DayField
import com.pilltip.pilltip.composable.SearchComposable.TimeField
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.DosageSchedule
import com.pilltip.pilltip.model.search.RegisterDosageRequest
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.questionnaire.Logic.DosageEntry
import com.pilltip.pilltip.view.search.Logic.mapPeriod
import java.time.LocalDate

@Composable
fun DosagePage(
    navController: NavController,
    searchViewModel: SearchHiltViewModel,
    drugId: Long,
    drugName: String
) {
    val editingPill by searchViewModel.pillDetail.collectAsState()
    val context = LocalContext.current
    var alarmName by remember { mutableStateOf("") }
    var startYear by remember { mutableStateOf(0) }
    var startMonth by remember { mutableStateOf(0) }
    var startDay by remember { mutableStateOf(0) }
    var endYear by remember { mutableStateOf(0) }
    var endMonth by remember { mutableStateOf(0) }
    var endDay by remember { mutableStateOf(0) }
    var selectedDays by remember { mutableStateOf(List(7) { false }) }
    var doseAmount by remember { mutableStateOf("") }
    var dosageList by remember { mutableStateOf(mutableListOf<DosageEntry>()) }
    val dropdownStates = remember { mutableStateListOf<Boolean>() }
    var scrollState = rememberScrollState()
    val isFormValid = startYear != 0 && startMonth != 0 && startDay != 0
            && endYear != 0 && endMonth != 0 && endDay != 0
            && selectedDays.any { it }
    val isEditMode = editingPill != null
    BackHandler {
        searchViewModel.clearPillDetail()
        navController.popBackStack()
    }

    LaunchedEffect(isEditMode) {
        editingPill?.let {
            alarmName = it.alarmName
            val start = LocalDate.parse(it.startDate)
            val end = LocalDate.parse(it.endDate)
            startYear = start.year
            startMonth = start.monthValue
            startDay = start.dayOfMonth
            endYear = end.year
            endMonth = end.monthValue
            endDay = end.dayOfMonth
            selectedDays = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").map { day ->
                it.daysOfWeek.contains(day)
            }
            doseAmount = it.dosageAmount.toString()
            dosageList = it.dosageSchedules.map { s ->
                DosageEntry(
                    amPm = if (s.period == "AM") "AM" else "PM",
                    hour = s.hour,
                    minute = s.minute,
                    alarm_on_off = s.alarmOnOff,
                    dosageUnit = s.dosageUnit
                )
            }.toMutableList()
        }
    }

    LaunchedEffect(dosageList.size) {
        while (dropdownStates.size < dosageList.size) {
            dropdownStates.add(false)
        }
    }

    Box(
        modifier = WhiteScreenModifier.navigationBarsPadding()
    ) {
        Column(
            modifier = WhiteScreenModifier
                .padding(horizontal = 22.dp)
                .verticalScroll(scrollState)
        ) {
            BackButton(horizontalPadding = 0.dp) {
                if (isEditMode) {
                    searchViewModel.clearPillDetail()
                    navController.popBackStack()
                } else {
                    navController.navigate("DetailPage")
                }
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
            DoubleLineTitleText(
                "복약 일정이",
                "어떻게 되시나요?",
                padding = 0.dp,
                textHeight = 33.8.dp,
                fontSize = 26
            )
            HeightSpacer(52.dp)
            Text(
                text = "복약 일정",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            HeightSpacer(12.dp)
            DayField(
                selectedDays = selectedDays,
                onDayToggle = { index ->
                    selectedDays = selectedDays.toMutableList().also {
                        it[index] = !it[index]
                    }
                }
            )
            HeightSpacer(28.dp)
            if (selectedDays.any { it }) {
                Text(
                    text = "복약량",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800,
                    )
                )
                HeightSpacer(12.dp)
                RoundTextField(
                    text = doseAmount,
                    textChange = { doseAmount = it },
                    placeholder = "1회 복용량을 알려주세요",
                    isLogin = false,
                    keyboardType = KeyboardType.Number
                )
            }
            HeightSpacer(28.dp)
            if (selectedDays.any { it } && doseAmount != "") {
                Text(
                    text = "복약 기간",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800,
                    )
                )
                HeightSpacer(12.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AgeField(
                        placeholder = "복약 시작일",
                        displayYear = startYear,
                        displayMonth = startMonth,
                        displayDay = startDay,
                        ageChange = { y, m, d ->
                            val newStart = LocalDate.of(y, m, d)
                            val endSet = endYear != 0 && endMonth != 0 && endDay != 0
                            startYear = y
                            startMonth = m
                            startDay = d
                            if (endSet) {
                                val end = LocalDate.of(endYear, endMonth, endDay)
                                if (end.isBefore(newStart)) {
                                    endYear = startYear
                                    endMonth = startMonth
                                    endDay = startDay
                                    Toast.makeText(
                                        context,
                                        "시작일이 변경되어 종료일이 초기화됐어요.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    AgeField(
                        placeholder = "복약 종료일",
                        displayYear = endYear,
                        displayMonth = endMonth,
                        displayDay = endDay,
                        ageChange = { year, month, day ->
                            val startSet = startYear != 0 && startMonth != 0 && startDay != 0

                            val end = LocalDate.of(year, month, day)
                            val start = LocalDate.of(startYear, startMonth, startDay)

                            if (!startSet || !end.isBefore(start)) {
                                endYear = year
                                endMonth = month
                                endDay = day
                            } else {
                                endYear = 0
                                endMonth = 0
                                endDay = 0
                                Toast.makeText(context, "종료일자는 시작일자보다 이후여야 해요.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                        onBeforeOpen = {
                            val startSet = startYear != 0 && startMonth != 0 && startDay != 0
                            if (!startSet) {
                                Toast.makeText(context, "먼저 시작일을 선택해 주세요.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            startSet
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        NextButton(
            mModifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 22.dp)
                .height(58.dp),
            text = "다음",
            buttonColor = if (isFormValid) primaryColor else Color(0xFFC5DBFB)
        ) {
            val daysOfWeekLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
            val selectedWeekdays = daysOfWeekLabels.filterIndexed { i, _ -> selectedDays[i] }

            val request = RegisterDosageRequest(
                medicationId = drugId,
                medicationName = drugName,
                startDate = "%04d-%02d-%02d".format(startYear, startMonth, startDay),
                endDate = "%04d-%02d-%02d".format(endYear, endMonth, endDay),
                alarmName = if(alarmName.isNotEmpty()) alarmName else "",
                dosageAmount = doseAmount.toDouble(),
                daysOfWeek = selectedWeekdays,
                dosageSchedules = if (dosageList.isNotEmpty()) {
                    dosageList.map {
                        DosageSchedule(
                            hour = it.hour!!,
                            minute = it.minute!!,
                            period = mapPeriod(it.amPm!!),
                            alarmOnOff = it.alarm_on_off ?: true,
                            dosageUnit = it.dosageUnit
                        )
                    }
                } else emptyList()
            )
            if (isEditMode) {
                searchViewModel.setPendingRequest(request)
                navController.navigate("DosageAlarmPage/${true}")
            } else {
                if (isFormValid) {
                    searchViewModel.setPendingRequest(request)
                    navController.navigate("DosageAlarmPage/${false}")
                } else {
                    Toast.makeText(context, "모든 항목을 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun DosageAlarmPage(
    navController: NavController,
    searchViewModel: SearchHiltViewModel,
    isEditMode : Boolean
) {
    Log.d("mode", isEditMode.toString())
    val editingPill by searchViewModel.pillDetail.collectAsState()
    val request = searchViewModel.pendingDosageRequest
    var name by remember { mutableStateOf("") }
    var dosageList by remember { mutableStateOf(mutableListOf<DosageEntry>()) }
    val context = LocalContext.current
    LaunchedEffect(editingPill!=null) {
        request?.let {
            name = it.alarmName
            dosageList = it.dosageSchedules.map { schedule ->
                DosageEntry(
                    amPm = if (schedule.period == "AM") "AM" else "PM",
                    hour = schedule.hour,
                    minute = schedule.minute,
                    alarm_on_off = schedule.alarmOnOff,
                    dosageUnit = schedule.dosageUnit
                )
            }.toMutableList()
        }
    }
    fun isFormValid(request: RegisterDosageRequest, dosageList: List<DosageEntry>): Boolean {
        if (request.medicationId <= 0) return false
        if (request.medicationName.isBlank()) return false
        if (request.startDate.isBlank() || request.endDate.isBlank()) return false
        if (request.alarmName.isBlank() || request.alarmName.length > 20) return false
        if (request.daysOfWeek.isEmpty()) return false
        if (dosageList.isEmpty()) return false

        dosageList.forEach { entry ->
            if (entry.hour == null || entry.minute == null || entry.amPm.isNullOrBlank()) {
                return false
            }
        }

        return true
    }

    val isFormValid by remember(name, dosageList) {
        derivedStateOf {
            val currentRequest = request?.copy(
                alarmName = name,
                dosageSchedules = emptyList()
            )
            currentRequest != null && isFormValid(currentRequest, dosageList)
        }
    }

    Column(
        modifier = WhiteScreenModifier
            .systemBarsPadding()
    ) {
        BackButton(
            horizontalPadding = 22.dp,
            iconDrawable = R.drawable.btn_vertical_dots
        ) {
            navController.navigate("DosagePage/$request.medication_id/$request.medication_name")
        }
        HeightSpacer(36.dp)
        Text(
            text = "약 알림 추가",
            style = TextStyle(
                fontSize = 22.sp,
                lineHeight = 33.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = gray800,
            ),
            modifier = Modifier.padding(horizontal = 22.dp)
        )
        HeightSpacer(28.dp)
        Text(
            text = "약 별칭",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            ),
            modifier = Modifier.padding(horizontal = 22.dp)
        )
        HeightSpacer(12.dp)
        RoundTextField(
            modifier = Modifier.padding(horizontal = 22.dp),
            text = name,
            textChange = {
                name = it
            },
            placeholder = "별칭을 적어주세요. 예) 두통약",
            isLogin = false
        )
        HeightSpacer(10.dp)
        Text(
            text = "최대 20자",
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray500,
            ),
            modifier = Modifier.padding(horizontal = 22.dp)
        )
        HeightSpacer(36.dp)
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = gray050,
            bottomBar = {
                Column(){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRippleClickable {
                                if (dosageList.size <= 9) {
                                    dosageList = dosageList.toMutableList().apply {
                                        add(DosageEntry())
                                    }
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            "최대 10개까지 일정을 생성할 수 있어요",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 22.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "추가", tint = primaryColor)
                        WidthSpacer(8.dp)
                        Text(
                            text = "복약 일정 추가하기",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    NextButton(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 22.dp)
                            .height(58.dp),
                        text = "다음",
                        buttonColor = if (isFormValid) primaryColor else Color(0xFFC5DBFB)
                    ){
                        val filledRequest = request?.copy(
                            alarmName = name,
                            dosageSchedules = dosageList.map {
                                DosageSchedule(
                                    hour = it.hour!!,
                                    minute = it.minute!!,
                                    period = mapPeriod(it.amPm!!),
                                    alarmOnOff = it.alarm_on_off ?: true,
                                    dosageUnit = it.dosageUnit
                                )
                            }
                        )
                        if(isEditMode){
                            if (filledRequest != null) {
                                searchViewModel.modifyDosage(editingPill!!.medicationId, filledRequest)
                            }
                            Toast.makeText(context, "복약 일정이 수정되었어요!", Toast.LENGTH_SHORT).show()
                            searchViewModel.clearPillDetail()
                            navController.navigate("PillMainPage") {
                                popUpTo(0) { inclusive = true }
                            }

                            navController.navigate("MyDrugInfoPage") {
                                launchSingleTop = true
                            }

                        } else {
                            if(request!=null && isFormValid){
                                if (filledRequest != null) {
                                    searchViewModel.registerDosage(filledRequest)
                                }
                                Toast.makeText(context, "복약 일정이 등록되었어요!", Toast.LENGTH_SHORT).show()
                                searchViewModel.fetchDosageSummary()
                                navController.navigate("DetailPage") {
                                    popUpTo(
                                        navController.previousBackStackEntry?.destination?.route ?: ""
                                    ) {
                                        inclusive = true
                                    }
                                }
                            }
                        }

                    }
                }

            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                dosageList.forEachIndexed { index, entry ->
                    HeightSpacer(12.dp)
                    TimeField(
                        initialAmPm = entry.amPm,
                        initialHour = entry.hour,
                        initialMinute = entry.minute,
                        timeChange = { amPm, hour, minute ->
                            dosageList = dosageList.toMutableList().apply {
                                this[index] = entry.copy(amPm = amPm, hour = hour, minute = minute)
                            }
                        },
                        alarmChecked = entry.alarm_on_off ?: true,
                        onAlarmToggle = { isChecked ->
                            dosageList = dosageList.toMutableList().apply {
                                this[index] = entry.copy(alarm_on_off = isChecked)
                            }
                        }
                    )
                }
                HeightSpacer(150.dp)
            }
        }
    }
}