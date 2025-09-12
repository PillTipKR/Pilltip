package com.pilltip.pilltip.composable.AuthComposable

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.PillTipDatePicker
import com.pilltip.pilltip.composable.WheelColumn
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun LoginButton(
    text: String,
    sourceImage: Int,
    borderColor: Color,
    backgroundColor: Color,
    fontColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(size = 100.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 100.dp))
            .padding(start = 0.dp, top = 16.dp, end = 0.dp, bottom = 16.dp)
            .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(sourceImage),
                contentDescription = "Login icon image"
            )
            WidthSpacer(12.dp)
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = fontColor,
            )
        }
    }
}

@Composable
fun ProfileStepDescription(
    Title: String
) {
    Text(
        text = Title,
        fontSize = 16.sp,
        fontFamily = pretendard,
        fontWeight = FontWeight(600),
        color = gray800
    )
}

@Composable
fun RoundTextField(
    modifier: Modifier = Modifier,
    text: String,
    textChange: (String) -> Unit,
    placeholder: String,
    isLogin: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }
    BasicTextField(
        value = text,
        onValueChange = {
            if (it.length <= 50) textChange(it)
        },
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (isLogin) {
                    if (text.isNotEmpty()) gray050 else Color.White
                } else {
                    if (text.isEmpty()) gray050 else primaryColor
                },
                shape = RoundedCornerShape(size = 12.dp)
            )
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                color = if (isLogin && text.isEmpty()) gray050 else Color.White,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            },
        visualTransformation = if (isVisible && placeholder == "비밀번호 입력") PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }
        ),
        textStyle = TextStyle(
            fontSize = 17.sp,
            color = Color.Black,
            fontFamily = pretendard,
            fontWeight = FontWeight.W500
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.W400,
                            color = gray500
                        )
                    )
                }
                if (text.isNotEmpty() && placeholder == "비밀번호 입력") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            imageVector = ImageVector.vectorResource(R.drawable.btn_login_visiblility),
                            contentDescription = "비밀번호 확인",
                            modifier = Modifier.noRippleClickable {
                                isVisible = !isVisible
                            }
                        )
                    }
                }
                innerTextField()
            }
        },
    )
}

@Composable
fun ProfileGenderPick(
    select: (String) -> Unit
) {
    var gender by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = if (gender == "FEMALE") primaryColor else gray500,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .weight(1f)
                .height(43.dp)
                .background(
                    color = if (gender == "FEMALE") Color(0xFFF1F6FE) else Color.White,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp)
                .noRippleClickable {
                    gender = "FEMALE"
                    select(gender)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "여성",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800
            )
        }
        WidthSpacer(8.dp)
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = if (gender == "MALE") primaryColor else gray500,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .weight(1f)
                .height(43.dp)
                .background(
                    color = if (gender == "MALE") Color(0xFFF1F6FE) else Color.White,
                    shape = RoundedCornerShape(size = 12.dp)
                )
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp)
                .noRippleClickable {
                    gender = "MALE"
                    select(gender)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "남성",
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800
            )
        }
    }
}

@Composable
fun AgeField(
    placeholder: String = "생년월일을 입력해주세요",
    ageChange: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    displayYear: Int = 0,
    displayMonth: Int = 0,
    displayDay: Int = 0,
    onBeforeOpen: () -> Boolean = { true }
) {
    var selectedYear by remember { mutableStateOf(0) }
    var selectedMonth by remember { mutableStateOf(0) }
    var selectedDay by remember { mutableStateOf(0) }
    var showSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = gray200,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .height(51.dp)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            .noRippleClickable {
                if (onBeforeOpen()) {
                    showSheet = true
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (displayYear == 0 && displayMonth == 0 && displayDay == 0)
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = gray600,
                )
            else
                Text(
                    text = "%04d년 %02d월 %02d일".format(displayYear, displayMonth, displayDay),
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight.W400,
                    color = Color.Black,
                )

            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_right_gray_arrow),
                contentDescription = "Age BottomSheet 표출",
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showSheet) {
        AgeBottomSheet(
            selectedValue = { year, month, day ->
                selectedYear = year
                selectedMonth = month
                selectedDay = day
                showSheet = false
                ageChange(selectedYear, selectedMonth, selectedDay)
            },
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AgeBottomSheet(
    selectedValue: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        var pickedDate by remember { mutableStateOf(LocalDate.now()) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            HeightSpacer(16.dp)
            PillTipDatePicker(
                onDateSelected = { localDate ->
                    pickedDate = localDate
                }
            )
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
                        selectedValue(pickedDate.year, pickedDate.monthValue, pickedDate.dayOfMonth)
                        onDismiss()
                    }
                }
            )
        }
    }
}

@Composable
fun BodyProfile(
    selectedBodyProfile: (String, String) -> Unit
) {
    var selectedHeight by remember { mutableStateOf("입력없음") }
    var selectedWeight by remember { mutableStateOf("입력없음") }
    var showHeightPicker by remember { mutableStateOf(false) }
    var showWeightPicker by remember { mutableStateOf(false) }

    Row {
        Box(
            Modifier
                .border(width = 1.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
                .weight(1f)
                .height(59.dp)
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp)
                .noRippleClickable { showHeightPicker = true },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "키",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray800,
                    )
                )
                HeightSpacer(4.dp)
                Text(
                    text = "$selectedHeight cm",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = if (selectedHeight == "입력없음") FontWeight.W500 else FontWeight.W600,
                        color = if (selectedHeight == "입력없음") gray800 else primaryColor
                    )
                )
            }
        }
        WidthSpacer(8.dp)
        Box(
            Modifier
                .border(width = 1.dp, color = gray200, shape = RoundedCornerShape(size = 12.dp))
                .weight(1f)
                .height(59.dp)
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp)
                .noRippleClickable { showWeightPicker = true },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "몸무게",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray800
                    )
                )
                HeightSpacer(4.dp)
                Text(
                    text = "$selectedWeight kg",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = if (selectedWeight == "입력없음") FontWeight.W500 else FontWeight.W600,
                        color = if (selectedWeight == "입력없음") gray800 else primaryColor
                    )
                )
            }
        }
    }
    if (showHeightPicker) {
        PillTipHeightWeightBottomSheet(
            start = 20,
            end = 220,
            label = "cm",
            onSelected = {
                selectedHeight = "$it"
                showHeightPicker = false
            },
            onDismiss = { showHeightPicker = false },
            initial = 174
        )
    }
    if (showWeightPicker) {
        PillTipHeightWeightBottomSheet(
            start = 30,
            end = 300,
            label = "kg",
            onSelected = {
                selectedWeight = "$it"
                showWeightPicker = false
            },
            onDismiss = { showWeightPicker = false },
            initial = 68
        )
    }
    if (selectedHeight != "입력없음" && selectedWeight != "입력없음") selectedBodyProfile(
        selectedHeight,
        selectedWeight
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillTipHeightWeightBottomSheet(
    start: Int,
    end: Int,
    label: String,
    onSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    initial: Int
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf(start) }
    val scope = rememberCoroutineScope()

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
                    .padding(vertical = 8.dp)
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
            PillTipHeightWeightPicker(
                start = start,
                end = end,
                label = label,
                onSelected = { selected = it },
                initial = initial
            )
            HeightSpacer(16.dp)
            NextButton(
                mModifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(58.dp),
                text = "선택하기",
                buttonColor = primaryColor,
                onClick = {
                    onSelected(selected)
                    scope.launch {
                        bottomSheetState.hide()
                        onDismiss()
                    }
                }
            )
        }
    }
}


@Composable
fun PillTipHeightWeightPicker(
    start: Int,
    end: Int,
    label: String,
    onSelected: (Int) -> Unit,
    initial: Int
) {
    val Range = (start..end).toList()
    val initialIndex = (initial - start).coerceIn(0, Range.lastIndex)
    val ListState = rememberLazyListState(initialIndex)

    val density = LocalDensity.current
    val threshold = remember { density.run { 20.dp.toPx() } }

    val selected by remember {
        derivedStateOf {
            val index = ListState.firstVisibleItemIndex
            val offset = ListState.firstVisibleItemScrollOffset
            Range.getOrNull(index + if (offset >= threshold) 1 else 0) ?: 20
        }
    }

    LaunchedEffect(selected) {
        onSelected(selected)
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
                .background(Color(0x14787880), RoundedCornerShape(6.dp))
                .align(Alignment.Center)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(200.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelColumn(
                items = Range,
                selected = selected,
                state = ListState,
                label = label,
                modifier = Modifier.fillMaxWidth()
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