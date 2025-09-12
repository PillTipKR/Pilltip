package com.pilltip.pilltip.composable

import android.icu.text.CaseMap.Title
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.ui.theme.backgroundColor
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.auth.logic.InputType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

/**
 * 세로 간격을 띄우기 위한 Spacer입니다.
 * @param height (Dp)세로 간격을 지정합니다.
 * @author 김기윤
 */
@Composable
fun HeightSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

/**
 * 가로 간격을 띄우기 위한 Spacer입니다.
 * @param width (Dp)가로 간격을 지정합니다.
 * @author 김기윤
 */
@Composable
fun WidthSpacer(width: Dp) {
    Spacer(modifier = Modifier.width(width))
}

/**
 * 하이라이트 라인을 표시하는 Composable입니다.
 * @param text : String | 텍스트 입력 여부를 받습니다.
 * @param isFocused : Boolean | 포커스 여부를 받습니다.
 * @param isAllConditionsValid : Boolean | 모든 조건이 만족되었는지 여부를 받습니다.
 */
@Composable
fun HighlightingLine(text: String, isFocused: Boolean, isAllConditionsValid: Boolean = true) {
    val fillPercentage = if (isFocused) 1f else 0f
    val animatedFillPercentage by animateFloatAsState(targetValue = fillPercentage, label = "")

    if (!isFocused) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = 24.dp)
                .background(Color(0xFFBFBFBF))
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedFillPercentage)
                    .height(2.dp)
                    .background(
                        if (text.isEmpty()) primaryColor
                        else {
                            if (isAllConditionsValid)
                                primaryColor
                            else
                                Color(0xFFE43D45)
                        }
                    )
            )
        }
    }
}

/**
 * 회원가입 페이지에서 조건 만족 여부를 시각화할 때 사용하는 Composable입니다.
 * @param description : String | 조건을 작성합니다.
 * @param isValid : Boolean | 조건을 만족하는지 여부를 판별하는 값입니다.
 */
@Composable
fun Guideline(description: String, isValid: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .height(3.dp)
                .width(3.dp)
                .background(color = Color(0xFFBFBFBF), shape = CircleShape),
        )
        Text(
            text = description,
            fontFamily = pretendard,
            fontSize = 14.sp,
            fontWeight = FontWeight.W400,
            color = Color(0xFFBFBFBF),
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            letterSpacing = (-0.3).sp,
            modifier = Modifier
                .weight(1f)
        )
        Image(
            imageVector = if (isValid) {
                ImageVector.vectorResource(id = R.drawable.ic_green_checkmark)
            } else {
                ImageVector.vectorResource(id = R.drawable.ic_red_checkmark)
            },
            contentDescription = "status_icon",
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * 두 줄에 걸쳐 작성되는 Title Text Composable입니다.
 * @param upperTextLine 윗 줄에 작성되는 Title Text입니다.
 * @param lowerTextLine 아랫 줄에 작성되는 Title Text입니다.
 * @param padding : Dp | Title Text의 horizontal 패딩을 설정합니다.
 * author : 김기윤
 */
@Composable
fun DoubleLineTitleText(
    upperTextLine: String = "Upper TextLine",
    lowerTextLine: String = "Lower TextLine",
    padding: Dp = 24.dp,
    textHeight : Dp = 40.dp,
    fontSize : Int = 28
) {
    Text(
        text = upperTextLine,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .height(textHeight)
            .wrapContentHeight(Alignment.CenterVertically),
        style = TextStyle(
            fontFamily = pretendard,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            letterSpacing = (-0.3).sp,
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    )
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .height(textHeight)
            .wrapContentHeight(Alignment.CenterVertically),
        text = lowerTextLine,
        fontSize = fontSize.sp,
        fontFamily = pretendard,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF000000),
        letterSpacing = (-0.3).sp,
    )
}

/**
 * 한 줄 짜리 제목 Composable 입니다.
 * @param titleText 기본적으로 사용될 텍스트 입니다.
 * @param padding horizontal padding 기본값 24로 설정되어 있습니다.
 * author : 김기윤
 */
@Composable
fun SingleLineTitleText(titleText: String = "", padding: Dp = 24.dp) {
    Text(
        text = titleText,
        fontFamily = pretendard,
        fontWeight = FontWeight.W700,
        fontSize = 22.sp,
        color = gray800,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        style = TextStyle(
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        )
    )
}

/**
 * Title 아래 세부 설명사항을 작성하는 Composable입니다.
 * @param description : String | 세부 설명사항을 작성합니다.
 * author : 김기윤
 */
@Composable
fun TitleDescription(description: String = "TitleDescription") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = description,
            fontSize = 16.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFAFB8C1)
        )
    }
}

/**
 * 텍스트를 작성하면 나타나는 라벨 텍스트를 설정하는 Composable입니다.
 * @param labelText : String | 라벨 텍스트를 설정합니다.
 * @param padding : Dp | 라벨 텍스트의 start 패딩을 설정합니다.
 * @param bottomPadding : Dp | 라벨 텍스트의 bottom 패딩을 설정합니다.
 * author : 김기윤
 */
@Composable
fun LabelText(labelText: String = "", padding: Dp = 24.dp, bottomPadding: Dp = 8.dp) {
    Text(
        text = labelText,
        fontSize = 12.sp,
        fontFamily = pretendard,
        fontWeight = FontWeight.Medium,
        color = Color(0xFFAFB8C1),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = padding)
            .padding(bottom = bottomPadding)
    )
}

/**
 * place holder를 지원하는 TextField Composable입니다.
 * @param placeHolder : String | 텍스트 필드의 place holder를 설정합니다.
 * @param inputText : String | 텍스트 필드에 입력된 텍스트를 설정합니다.
 * @param padding : Dp | 텍스트 필드의 horizontal 패딩을 설정합니다.
 * @param inputType : InputType | 텍스트 필드의 입력 타입을 설정합니다. TEXT, EMAIL, PASSWORD, NUMBER 중 하나를 선택하여 입력합니다.
 * @param onTextChanged : (String) -> Unit | 텍스트 필드의 텍스트가 변경될 때 호출되는 콜백 함수입니다.
 * @param onFocusChanged : (Boolean) -> Unit | 텍스트 필드의 포커스 여부가 변경될 때 호출되는 콜백 함수입니다.
 * author : 김기윤
 */
@Composable
fun PlaceholderTextField(
    placeHolder: String = "",
    inputText: String,
    padding: Dp = 24.dp,
    inputType: InputType = InputType.TEXT,
    onTextChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = inputText,
        onValueChange = {
            onTextChanged(it)
        },
        cursorBrush = SolidColor(if (isFocused) Color.Blue else Color.Gray),
        keyboardOptions = if (inputType == InputType.TEXT)
            KeyboardOptions(
                imeAction = ImeAction.Next
            )
        else if (inputType == InputType.EMAIL)
            KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            )
        else if (inputType == InputType.PASSWORD) KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ) else {
            KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .focusable()
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChanged(isFocused)
            },
        textStyle = TextStyle(
            fontSize = 20.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(if (inputType == InputType.TEXT) 600 else 500),
            color = Color(0xFF121212),
            letterSpacing = 0.5.sp,
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = placeHolder,
                        style = TextStyle(
                            fontFamily = pretendard,
                            fontWeight = FontWeight.W500,
                            fontSize = 20.sp,
                            color = gray400
                        )
                    )
                }
                innerTextField()
                if (inputText.isNotEmpty()) {
                    Image(
                        imageVector = ImageVector.vectorResource(id = R.drawable.btn_textfield_eraseall),
                        contentDescription = "x_marker",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .clickable {
                                onTextChanged("")
                            }
                    )
                }
            }
        }
    )
}

@Composable
fun PillTipDatePicker(
    onDateSelected: (LocalDate) -> Unit
) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth by remember(yearMonth) {
        mutableStateOf(yearMonth.lengthOfMonth())
    }

    val yearListState = rememberLazyListState(yearMonth.year - 1900)
    val monthListState = rememberLazyListState(yearMonth.monthValue - 1)
    val today = LocalDate.now()
    val dayListState = rememberLazyListState(today.dayOfMonth - 1)

    val density = LocalDensity.current
    val threshold = remember { density.run { 20.dp.toPx() } }

    val year by remember {
        derivedStateOf {
            val index = yearListState.firstVisibleItemIndex
            val offset = yearListState.firstVisibleItemScrollOffset
            index + if (offset >= threshold) 1901 else 1900
        }
    }
    val month by remember {
        derivedStateOf {
            val index = monthListState.firstVisibleItemIndex
            val offset = monthListState.firstVisibleItemScrollOffset
            index + if (offset >= threshold) 2 else 1
        }
    }
    val day by remember {
        derivedStateOf {
            val offsetThreshold =
                if (dayListState.firstVisibleItemScrollOffset >= threshold) 1 else 0
            (dayListState.firstVisibleItemIndex + offsetThreshold + 1).coerceAtMost(daysInMonth)
        }
    }

    var selectedDayIndex by remember { mutableStateOf(0) }

    LaunchedEffect(dayListState) {
        snapshotFlow { dayListState.isScrollInProgress }
            .filter { !it }
            .collect {
                selectedDayIndex = dayListState.firstVisibleItemIndex
            }
    }

    LaunchedEffect(year, month) {
        yearMonth = YearMonth.of(year, month)
        if (day > daysInMonth) {
            dayListState.scrollToItem(daysInMonth - 1)
        }
    }

    LaunchedEffect(year, month, selectedDayIndex) {
        val safeDay = (selectedDayIndex + 1).coerceAtMost(YearMonth.of(year, month).lengthOfMonth())
        onDateSelected(LocalDate.of(year, month, safeDay))
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
                items = (1900..2025).toList(),
                selected = year,
                state = yearListState,
                label = "년",
                modifier = Modifier.weight(1f)
            )
            WheelColumn(
                items = (1..12).toList(),
                selected = month,
                state = monthListState,
                label = "월",
                modifier = Modifier.weight(1f)
            )
            WheelColumn(
                items = (1..daysInMonth).toList(),
                selected = selectedDayIndex + 1,
                state = dayListState,
                label = "일",
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



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelColumn(
    items: List<T>,
    selected: T,
    state: LazyListState,
    label: String,
    itemToString: (T) -> String = { it.toString() }, // 기본은 toString()
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(vertical = 80.dp),
        flingBehavior = rememberSnapFlingBehavior(state),
        modifier = modifier.fillMaxHeight()
    ) {
        itemsIndexed(items) { index, item ->
            val centerIndex = state.firstVisibleItemIndex
            val distance = abs(index - centerIndex).toFloat()

            val transition = updateTransition(targetState = distance, label = "wheel")

            val scale by transition.animateFloat(
                transitionSpec = {
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                },
                label = "scale"
            ) { dist ->
                (1f - dist * 0.1f).coerceIn(0.7f, 1.15f)
            }

            val alpha by transition.animateFloat(
                transitionSpec = {
                    spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                },
                label = "alpha"
            ) { dist ->
                (1f - dist * 0.3f).coerceIn(0.3f, 1f)
            }

            val fontWeight = if (item == selected) FontWeight.Bold else FontWeight.Normal

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {
                Text(
                    text = "${itemToString(item)}$label",
                    fontFamily = pretendard,
                    fontSize = 23.sp,
                    fontWeight = fontWeight,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun AppBar(
    horizontalPadding: Dp = 20.dp,
    backgroudnColor: Color = Color.White,
    LNB: Int? = null,
    LNBDesc: String = "LNB입니다.",
    LNBSize: Dp = 20.dp,
    LNBClickable: (() -> Unit)? = null,
    TitleText: String,
    TitleTextColor: Color = Color.Black,
    TitleTextSize: Int = 16,
    TitleTextWeight: FontWeight = FontWeight.W600,
    RNB: Int? = null,
    RNBDesc: String = "RNB입니다.",
    RNBSize: Dp = 20.dp,
    RNBClickable: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = horizontalPadding)
            .background(backgroudnColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (LNB != null) {
            Image(
                imageVector = ImageVector.vectorResource(LNB),
                contentDescription = LNBDesc,
                modifier = Modifier
                    .size(LNBSize)
                    .clickable(onClick = { LNBClickable?.invoke() })
            )
        } else Box(Modifier.size(RNBSize))

        Spacer(Modifier.weight(1f))
        Text(
            text = TitleText,
            color = TitleTextColor,
            fontFamily = pretendard,
            fontWeight = TitleTextWeight,
            fontSize = TitleTextSize.sp
        )
        Spacer(Modifier.weight(1f))
        if (RNB != null) {
            Image(
                imageVector = ImageVector.vectorResource(RNB),
                contentDescription = RNBDesc,
                modifier = Modifier
                    .size(RNBSize)
                    .clickable(onClick = { RNBClickable?.invoke() })
            )
        } else Box(Modifier.size(RNBSize))
    }
}

