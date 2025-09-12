package com.pilltip.pilltip.composable

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.model.search.DosageLogPerDrug
import com.pilltip.pilltip.model.search.DosageLogSchedule
import com.pilltip.pilltip.model.search.FriendListDto
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.gray900
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@Composable
fun DrugLogCard(
    drugLog: DosageLogPerDrug,
    searchHiltViewModel: SearchHiltViewModel,
    selectedDate: LocalDate,
    isNotification: Boolean = false,
    isRead: Boolean = false
) {
    val filteredSchedules = when {
        !isNotification -> drugLog.dosageSchedule
        isRead -> drugLog.dosageSchedule.filter { it.isTaken }
        else -> drugLog.dosageSchedule.filter { !it.isTaken }
    }

    if (filteredSchedules.isEmpty()) return

    Column(
        modifier = Modifier
            .border(width = 0.5.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
            .padding(0.25.dp)
            .fillMaxWidth()
            .background(color = Color.White, shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = drugLog.medicationName,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = Color.Black,
                )
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_right_gray_arrow),
                colorFilter = ColorFilter.tint(gray500),
                contentDescription = "해당 약품 세부 일정",
                modifier = Modifier.noRippleClickable {
                    searchHiltViewModel.selectedDrugLog = drugLog
                }
            )
        }

        HeightSpacer(28.dp)

        filteredSchedules.forEach { schedule ->
            DosageTimeItem(schedule, searchHiltViewModel, selectedDate)
            HeightSpacer(16.dp)
        }
    }
}

@Composable
fun DosageTimeItem(
    schedule: DosageLogSchedule,
    viewModel: SearchHiltViewModel,
    selectedDate: LocalDate
) {
    val time = schedule.scheduledTime.substring(11, 16)
    val context = LocalContext.current
    val displayTime = try {
        val parsed = LocalTime.parse(time)
        val hour = parsed.hour
        val minute = "%02d".format(parsed.minute)
        val period = if (hour < 12) "오전" else "오후"
        val displayHour = when {
            hour == 0 -> 12
            hour in 1..12 -> hour
            else -> hour - 12
        }
        "$period $displayHour:$minute"
    } catch (e: Exception) {
        schedule.scheduledTime
    }

    val statusText = when {
        schedule.isTaken -> "먹었어요"
        else -> "미뤘어요"
    }

    val statusColor = when {
        schedule.isTaken -> primaryColor
        else -> gray500
    }

    val statusBackgroundColor = when {
        schedule.isTaken -> primaryColor050
        else -> gray100
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = displayTime,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray900,
            )
        )
        Box(
            modifier = Modifier
                .background(
                    color = statusBackgroundColor,
                    shape = RoundedCornerShape(size = 1000.dp)
                )
                .padding(start = 10.dp, top = 6.dp, end = 10.dp, bottom = 6.dp)
                .noRippleClickable {
                    if (!viewModel.isFriendView) {
                        viewModel.toggleDosageTaken(
                            logId = schedule.logId,
                            onSuccess = {
                                viewModel.fetchDailyDosageLog(selectedDate)
                            },
                            onError = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
        ) {
            Text(
                text = statusText,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = statusColor,
                )
            )
        }
    }
}

@Composable
fun DrugLogDetailSection(
    drug: DosageLogPerDrug,
    viewModel: SearchHiltViewModel,
    selectedDate: LocalDate
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${selectedDate.monthValue}월 ${selectedDate.dayOfMonth}일 ${
                selectedDate.dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    Locale.KOREAN
                )
            }요일",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(16.dp)

        drug.dosageSchedule.forEach { schedule ->
            DosageTimeDetailItem(schedule, viewModel, selectedDate)
            HeightSpacer(16.dp)
        }
    }
}

@Composable
fun DosageTimeDetailItem(
    schedule: DosageLogSchedule,
    viewModel: SearchHiltViewModel,
    selectedDate: LocalDate
) {
    val context = LocalContext.current
    val time = schedule.scheduledTime.substring(11, 16)
    val displayTime = try {
        val parsed = LocalTime.parse(time)
        val hour = parsed.hour
        val minute = "%02d".format(parsed.minute)
        val period = if (hour < 12) "오전" else "오후"
        val displayHour = when {
            hour == 0 -> 12
            hour in 1..12 -> hour
            else -> hour - 12
        }
        "$period $displayHour:$minute"
    } catch (e: Exception) {
        schedule.scheduledTime
    }

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .border(width = 0.5.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
            .padding(0.25.dp)
            .fillMaxWidth()
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(
                start = 16.dp,
                top = 20.dp,
                end = 16.dp,
                bottom = if (expanded) 16.dp else 20.dp
            )
            .noRippleClickable {
                if (!viewModel.isFriendView) expanded = !expanded
            }
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayTime,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray900,
                )
            )
            Box(
                modifier = Modifier
                    .background(
                        color = if (schedule.isTaken) primaryColor050 else gray100,
                        shape = RoundedCornerShape(1000.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (schedule.isTaken) "먹었어요" else "미뤘어요",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = if (schedule.isTaken) primaryColor else gray500,
                    )
                )
            }
        }
        if (expanded) {
            HeightSpacer(20.dp)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(49.dp)
                        .background(color = gray100, shape = RoundedCornerShape(size = 12.dp))
                        .padding(start = 30.dp, top = 16.dp, end = 30.dp, bottom = 16.dp)
                        .noRippleClickable {
                            if (!schedule.isTaken) {
                                viewModel.fetchDosageLogMessage(
                                    logId = schedule.logId,
                                    onSuccess = { message ->
                                        Toast.makeText(
                                            context,
                                            "5분 뒤 복약 알림을 다시 드릴게요!",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                Toast.makeText(context, "이미 복약하셨어요!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "5분 미루기",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = gray700,
                        )
                    )
                }
                WidthSpacer(8.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(49.dp)
                        .background(color = primaryColor, shape = RoundedCornerShape(size = 12.dp))
                        .padding(start = 30.dp, top = 16.dp, end = 30.dp, bottom = 16.dp)
                        .noRippleClickable {
                            if (!schedule.isTaken) {
                                viewModel.toggleDosageTaken(
                                    logId = schedule.logId,
                                    onSuccess = {
                                        viewModel.fetchDailyDosageLog(selectedDate)
                                    },
                                    onError = {
                                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "먹었어요",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = Color(0xFFFFFFFF),
                        )
                    )
                }
//                NextButton(
//                    mModifier = Modifier
//                        .height(49.dp)
//                        .weight(1f),
//                    text = "미룰래요",
//                    textSize = 14,
//                    textColor = gray700,
//                    buttonColor = gray100
//                ) {
//                    viewModel.fetchDosageLogMessage(
//                        logId = schedule.logId,
//                        onSuccess = { message ->
//                            Toast.makeText(context, "5분 뒤 복약 알림을 다시 드릴게요!", Toast.LENGTH_SHORT)
//                                .show()
//                        },
//                        onError = { error ->
//                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
//                        }
//                    )
//                }
//                WidthSpacer(8.dp)
//                NextButton(
//                    mModifier = Modifier
//                        .weight(1f)
//                        .height(49.dp),
//                    text = if (schedule.isTaken) "안 먹었어요" else "먹었어요",
//                    textColor = Color.White,
//                    textSize = 14,
//                    buttonColor = primaryColor
//                ) {
//                    viewModel.toggleDosageTaken(
//                        logId = schedule.logId,
//                        onSuccess = {
//                            viewModel.fetchDailyDosageLog(selectedDate)
//                        },
//                        onError = {
//                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
//                        }
//                    )
//                }
            }
        }
    }
}

@Composable
fun UserSelectorBar(
    currentId: Long?,
    friends: List<FriendListDto>,
    onUserSelected: (Long?) -> Unit
) {
    val items =
        listOf<Pair<Long?, String>>(null to "나") + friends.map { it.friendId to it.nickName }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { (id, name) ->
            val isSelected = id == currentId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) primaryColor else gray100)
                    .clickable { onUserSelected(id) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = name,
                    color = if (isSelected) Color.White else gray800,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}
