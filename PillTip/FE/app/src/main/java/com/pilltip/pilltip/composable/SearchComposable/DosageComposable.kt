package com.pilltip.pilltip.composable.SearchComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.IosButton
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.WheelColumn
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun TimeField(
    placeholder: String = "시간을 선택해주세요",
    initialAmPm: String?,
    initialHour: Int?,
    initialMinute: Int?,
    timeChange: (String, Int, Int) -> Unit,
    alarmChecked: Boolean,
    onAlarmToggle: (Boolean) -> Unit
) {
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }
    var selectedAmPm by remember { mutableStateOf<String?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(initialHour, initialMinute, initialAmPm) {
        if (selectedHour == null && initialHour != null) {
            selectedHour = initialHour
            selectedMinute = initialMinute
            selectedAmPm = initialAmPm
        }
    }

    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = gray200,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .fillMaxWidth()
            .height(51.dp)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .noRippleClickable { showSheet = true }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedHour == null || selectedMinute == null || selectedAmPm == null) {
                Text(
                    text = placeholder,
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = gray500,
                )
            } else {
                val hourStr = selectedHour.toString().padStart(2, '0')
                val minuteStr = selectedMinute.toString().padStart(2, '0')

                Text(
                    text = "$selectedAmPm ${hourStr}시 ${minuteStr}분",
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = Color.Black,
                )
            }

            IosButton(
                checked = alarmChecked,
                onCheckedChange = { onAlarmToggle(it) }
            )
        }
    }

    if (showSheet) {
        TimeBottomSheet(
            selectedValue = { isAm, hour, minute ->
                selectedAmPm = isAm
                selectedHour = hour
                selectedMinute = minute
                showSheet = false
                timeChange(selectedAmPm!!, hour, minute)
            },
            onDismiss = { showSheet = false }
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TimeBottomSheet(
    selectedValue: (String, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pickedHour by remember { mutableStateOf(0) }
    var pickedMinute by remember { mutableStateOf(0) }
    var pickedAmPm by remember { mutableStateOf("오전") }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                bottomSheetState.hide()
                onDismiss()
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
            HeightSpacer(16.dp)

            PillTipTimePicker { ampm, hour, minute ->
                pickedAmPm = ampm
                pickedHour = hour
                pickedMinute = minute
            }

            HeightSpacer(16.dp)

            NextButton(
                mModifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(58.dp),
                text = "선택하기",
                buttonColor = primaryColor,
                onClick = {
                    scope.launch {
                        bottomSheetState.hide()
                        selectedValue(pickedAmPm, pickedHour, pickedMinute)
                        onDismiss()
                    }
                }
            )
        }
    }
}


@Composable
fun PillTipTimePicker(
    onTimeSelected: (String, Int, Int) -> Unit
) {
    val amPmState = rememberLazyListState(0)
    val hourState = rememberLazyListState(0)
    val minuteState = rememberLazyListState(0)

    val amPmItems = listOf("오전", "오후")
    val amPm by remember {
        derivedStateOf {
            val index = amPmState.firstVisibleItemIndex.coerceIn(0, 1)
            amPmItems[index]
        }
    }

    var selectedHourIndex by remember { mutableStateOf(0) }
    var selectedMinuteIndex by remember { mutableStateOf(0) }

    LaunchedEffect(hourState) {
        snapshotFlow { hourState.isScrollInProgress }
            .filter { !it }
            .collect {
                selectedHourIndex = hourState.firstVisibleItemIndex
            }
    }

    LaunchedEffect(minuteState) {
        snapshotFlow { minuteState.isScrollInProgress }
            .filter { !it }
            .collect {
                selectedMinuteIndex = minuteState.firstVisibleItemIndex
            }
    }

    LaunchedEffect(amPm, selectedHourIndex, selectedMinuteIndex) {
        val hour = selectedHourIndex + 1
        val minute = selectedMinuteIndex
        onTimeSelected(amPm, hour, minute)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .align(Alignment.Center)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(200.dp)
        ) {
            WheelColumn(
                items = listOf("오전", "오후"),
                selected = amPm,
                state = amPmState,
                label = "",
                modifier = Modifier.weight(1f)
            )
            val hourItems = (1..12).map { it.toString() }
            WheelColumn(
                items = hourItems,
                selected = (selectedHourIndex + 1).toString(),
                state = hourState,
                label = "시",
                modifier = Modifier.weight(1f)
            )

            val minuteItems = (0..59).map { it.toString() }
            WheelColumn(
                items = minuteItems,
                selected = selectedMinuteIndex.toString(),
                state = minuteState,
                label = "분",
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White)
                    )
                )
        )
    }
}

@Composable
fun DayField(
    selectedDays: List<Boolean>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysOfWeek.forEachIndexed { index, day ->
            val isSelected = selectedDays[index]
            Box(
                modifier = Modifier
                    .border(width = 1.dp, color = if(isSelected) primaryColor else Color.Transparent, shape = CircleShape)
                    .padding(1.dp)
                    .width(28.dp)
                    .height(28.dp)
                    .noRippleClickable { onDayToggle(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    fontFamily = pretendard,
                    color = if (isSelected) primaryColor else gray600,
                    fontWeight = if(isSelected) FontWeight.W600 else FontWeight.W400,
                    fontSize = 14.sp
                )
            }
        }
    }
}

