package com.pilltip.pilltip.composable.MainComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.DosageScheduleDetail
import com.pilltip.pilltip.model.search.TakingPillSummary
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween

@Composable
fun ProfileTagButton(
    text: String,
    image: Int = 0,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) primaryColor else primaryColor050
    val textColor = if (selected) Color.White else primaryColor
    val borderColor = if (selected) primaryColor else primaryColor

    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(size = 100.dp)
            )
            .height(30.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 100.dp))
            .padding(horizontal = if (image == 0) 14.dp else 12.dp, vertical = 8.dp)
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = textColor,
            )
        )
        if (image != 0) {
            WidthSpacer(6.dp)
            Image(
                imageVector = ImageVector.vectorResource(image),
                contentDescription = "태그 버튼"
            )
        }
    }
}

@Composable
fun DrugSummaryCard(
    pill: TakingPillSummary,
    onDelete: (TakingPillSummary) -> Unit = {},
    onEdit: (TakingPillSummary) -> Unit = {},
    onClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .border(width = 0.5.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
            .padding(0.25.dp)
            .fillMaxWidth()
            .height(93.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            .noRippleClickable {
                onClick()
            }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pill.medicationName,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight.W700,
                        color = Color.Black
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Box {
                    Image(
                        imageVector = ImageVector.vectorResource(R.drawable.btn_vertical_dots),
                        contentDescription = "드롭다운 메뉴",
                        modifier = Modifier
                            .noRippleClickable {
                                menuExpanded = true
                            }
                    )
                    MaterialTheme(
                        shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier
                                .shadow(0.dp)
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(0.5.dp, gray200, RoundedCornerShape(12.dp)),
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "수정",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = pretendard,
                                            fontWeight = FontWeight(400),
                                            color = Color.Black,
                                        )
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit(pill)
                                }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = gray200)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "삭제",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontFamily = pretendard,
                                            fontWeight = FontWeight(400),
                                            color = Color.Black,
                                        )
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete(pill)
                                }
                            )
                        }

                    }

                }
            }

            HeightSpacer(12.dp)
            Text(
                text = "복약 시작일 | ${pill.startDate}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = gray500
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = "복약 종료일 | ${pill.endDate}",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = gray500
                )
            )
        }
    }
}

@Composable
fun HealthCard(
    title: String,
    descriptionHeader: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .border(width = 0.5.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
            .padding(0.25.dp)
            .fillMaxWidth()
            .height(71.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            .noRippleClickable { onClick() }
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color(0xFF000000),
            )
        )
        HeightSpacer(10.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = descriptionHeader,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray700,
                )
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_login_vertical_divider),
                contentDescription = "수직선",
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = description,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray500,
                )
            )
        }
    }
}

@Composable
fun DrugManagementRowTab(
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray600,
                textAlign = TextAlign.Justify,
            ),
            modifier = Modifier.width(84.dp)
        )
        Text(
            text = description,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800,
                textAlign = TextAlign.Justify,
            )
        )
    }
}

fun DosageScheduleDetail.toDisplayStrings(): Pair<String, String> {
    val periodLabel = if (period == "AM") "오전" else "오후"
    val timeStr = String.format("%s %d:%02d", periodLabel, hour, minute)
    val alarmStr = if (alarmOnOff) "[알람 O]" else "[알람 X]"
    return timeStr to alarmStr
}

@Composable
fun PushNotificationToggle(
    showAlert: Boolean,
    onAlertDismiss: () -> Unit
) {
    LaunchedEffect(showAlert) {
        if (showAlert) {
            delay(3000)
            onAlertDismiss()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showAlert,
            enter = fadeIn(tween(300)) + slideInVertically(),
            exit = fadeOut(tween(500)) + slideOutVertically()
        ) {
            Row(
                modifier = Modifier
                    .border(
                        width = 0.5.dp,
                        color = gray200,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        color = Color(0xFF434956),
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_search_dur_ok),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "알림을 끄면 복약 알림을 받아볼 수 없어요!",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}