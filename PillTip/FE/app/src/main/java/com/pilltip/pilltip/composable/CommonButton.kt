package com.pilltip.pilltip.composable

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.R
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray300
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.ui.theme.primaryColor050

/**
 * 주로 화면 아랫 부분에 있는 버튼입니다. 앱 전반에서 주로 사용합니다.
 * @param mModifier 버튼을 만들 때 사용하는 Modifier입니다. 따로 정의 돤 buttonModifier를 기본으로 사용합니다.
 * @param text 버튼에 들어갈 텍스트입니다.
 * @param buttonColor 버튼의 색을 설정합니다. 기본 색은 Color(0xFF397CDB)입니다.
 * @param textColor 버튼의 텍스트 색을 설정합니다. 기본 색은 Color.White입니다.
 * @param onClick 버튼을 누르면 실행되는 함수입니다. 주로 네비게이션 관련 함수가 작성됩니다.
 */
@Composable
fun NextButton(
    mModifier: Modifier = buttonModifier,
    text: String = "다음",
    buttonColor: Color = Color(0xFF397CDB),
    textColor: Color = Color.White,
    textSize: Int = 18,
    shape : Int = 16,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = mModifier,
        shape = RoundedCornerShape(size = shape.dp),
        colors = ButtonDefaults.buttonColors(buttonColor),
    ) {
        Text(
            text = text,
            fontFamily = pretendard,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = textSize.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}

@Composable
fun ButtonWithLogo(
    backgroundColor: Color,
    textColor: Color,
    textSize: Int,
    textWeight: Int,
    buttonText: String,
    logoResourceId: Int? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 16.dp))
            .noRippleClickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (logoResourceId != null) {
            Image(
                imageVector = ImageVector.vectorResource(id = logoResourceId),
                contentDescription = "Button Logo"
            )
        } else Box(modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = buttonText,
            color = textColor,
            fontSize = textSize.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(textWeight)
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.size(20.dp))
    }
}

/**
 * 뒤로가기 버튼입니다.
 * @param title 뒤로가기 버튼 옆에 보여지는 텍스트입니다.
 * @param horizontalPadding 뒤로가기 Row 양 쪽의 가로 패딩입니다.
 * @param verticalPadding 뒤로가기 Row 위아래의 세로 패딩입니다.
 * @param iconDrawable 뒤로가기 버튼 옆에 보여지는 아이콘으로, 한정자를 통해 R.drawable...만 입력 받습니다.
 * @param navigationTo 뒤로가기 버튼을 누르면 실행되는 함수로, navigationTo = ({ navController.navigate(route)}) 형식으로 작성합니다.
 */
@Composable
fun BackButton(
    title: String = "",
    isVisible: Boolean = true,
    horizontalPadding: Dp = 18.dp,
    verticalPadding: Dp = 18.dp,
    backgroundColor: Color = Color.White,
    @DrawableRes iconDrawable: Int = 0,
    onClick: () -> Unit ={},
    navigationTo: () -> Unit ={}
) {
    Row(
        modifier = Modifier
            .background(color = backgroundColor)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .height(57.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(isVisible == true) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.btn_black_arrow),
                contentDescription = "backButtonIcon",
                modifier = Modifier.noRippleClickable {
                    navigationTo()
                }
            )
        } else {
            Box(modifier = Modifier.width(20.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF121212),
                    textAlign = TextAlign.Center
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (iconDrawable != 0) {
            Image(
                imageVector = ImageVector.vectorResource(id = iconDrawable),
                contentDescription = "logo",
                colorFilter = ColorFilter.tint(Color.Black),
                modifier = Modifier.noRippleClickable {
                    onClick()
                }
            )
        } else {
            Box(modifier = Modifier.width(20.dp))
        }
    }
}

/**
 * 선택 버튼입니다.
 * @param text 버튼에 보여지는 텍스트 입니다.
 * @param verticalPadding 뒤로가기 Row 위아래의 세로 패딩입니다.
 * @param imageSource 한정자를 통해 R.drawable...만 입력 받습니다.
 */
@Composable
fun SelectButton(
    text: String = "",
    widthValue: Int,
    imageSource: Int,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .width(widthValue.dp)
            .height(222.dp)
            .background(
                color = Color(0xFFEEF4FC),
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(start = 17.dp, top = 22.dp, end = 17.dp, bottom = 18.dp)
            .noRippleClickable({
                onClick()
            }),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = imageSource),
                contentDescription = text
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = text,
                modifier = Modifier
                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF397CDB),
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
        }
    }
}

@Composable
fun TagButton(
    keyword: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedBackgroundColor: Color = primaryColor050,
    selectedTextColor: Color = primaryColor,
    selectedBorderColor: Color = primaryColor,
    unselectedBackgroundColor: Color = Color.White,
    unselectedTextColor: Color = gray800,
    unselectedBorderColor: Color = gray300
) {
    val backgroundColor = if (isSelected) selectedBackgroundColor else unselectedBackgroundColor
    val textColor = if (isSelected) selectedTextColor else unselectedTextColor
    val borderColor = if (isSelected) selectedBorderColor else unselectedBorderColor

    Box(
        modifier = Modifier
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(size = 100.dp))
            .height(33.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(size = 100.dp))
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            keyword,
            fontSize = 14.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(500),
            color = textColor
        )
    }
}

@Composable
fun IosButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 13.dp else 0.dp,
        animationSpec = tween(durationMillis = 250)
    )
    Box(
        modifier = Modifier
            .padding(0.64516.dp)
            .width(34.dp)
            .height(20.dp)
            .background(
                color = if (checked) primaryColor else Color(0xFFEAEAEA),
                shape = RoundedCornerShape(30.dp)
            )
            .noRippleClickable { onCheckedChange(!checked) },
    ) {

        Box(
            modifier = Modifier
                .size(21.dp)
                .padding(1.dp)
                .offset(x = thumbOffset),
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.btn_image),
                contentDescription = "Custom Thumb",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}