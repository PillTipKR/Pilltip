package com.pilltip.pilltip.composable.MainComposable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor

@Composable
fun DURText(
    title : String = "A약품 성분 검사지",
    isOk : Boolean = true,
    description : String = "A약품, 또는 B 약품 탭의 해당 약품 성분 분석 결과지에는 A 또는 B 약품의 어떤 성분과, 내가 복약 중인 약품의 어떤 성분 간 상충이 발생하기에, 또는 과복용의 우려가 있기에 함께 복약해선 안 된다는 상세 설명이 나옴."
){
    Text(
        text = title,
        style = TextStyle(
            fontSize = 18.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = if(!isOk) primaryColor else Color(0xFFEB2C28),
        )
    )
    HeightSpacer(16.dp)
    Text(
        text = description,
        style = TextStyle(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(400),
            color = gray800,
            textAlign = TextAlign.Center,
        )
    )
}

@Composable
fun SleekRotatingArc(
    modifier: Modifier = Modifier,
    arcColor: Color = primaryColor,
    strokeWidth: Dp = 6.dp,
    size: Dp = 60.dp
) {
    val transition = rememberInfiniteTransition(label = "rotate")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "angle"
    )

    Canvas(modifier = modifier.size(size)) {
        val arcStroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

        val radiusOffset = strokeWidth.toPx() / 2
        val arcSize = size.toPx() - strokeWidth.toPx()

        drawArc(
            color = arcColor.copy(alpha = 0.1f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = arcStroke,
            topLeft = Offset(radiusOffset, radiusOffset),
            size = Size(arcSize, arcSize)
        )

        rotate(angle, pivot = center) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(arcColor, arcColor.copy(alpha = 0f))
                ),
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = arcStroke,
                topLeft = Offset(radiusOffset, radiusOffset),
                size = Size(arcSize, arcSize)
            )
        }
    }
}