package com.pilltip.pilltip.composable.MainComposable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.ui.theme.backgroundColor
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray300
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 메인 화면에 있는 로고 이미지 필드 컴포저블 입니다.
 * @param horizontalPadding: 좌우패딩을 설정합니다.
 */
@Composable
fun LogoField(
    horizontalPadding: Dp = 22.dp,
    verticalPadding: Dp = 15.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(54.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.btn_alarmbell),
            contentDescription = "alarm",
            modifier = Modifier
                .padding(1.dp)
                .height(24.dp)
                .noRippleClickable {
                    onClick()
                }
        )
    }
}

/**
 * 메인 화면에 있는 검색 필드 컴포저블 입니다.
 * @param horizontalPadding 좌우패딩을 설정합니다.
 */
@Composable
fun MainSearchField(
    horizontalPadding: Dp = 22.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .shadow(
                elevation = 5.dp,
                spotColor = gray600,
                ambientColor = Color(0x14000000),
                clip = false,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .background(color = Color(0xFFFDFDFD), shape = RoundedCornerShape(size = 12.dp))
            .fillMaxWidth()
            .height(44.dp)
            .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 12.dp)
            .noRippleClickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "어떤 약이 필요하신가요?",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray400,
            ),
            modifier = Modifier
                .height(19.dp)
                .weight(1f)
        )
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.btn_gray_searchfield_magnifier),
            contentDescription = "logo",
            modifier = Modifier
                .height(20.dp)
                .width(20.dp)
                .padding(1.dp)
        )
    }
}

/**
 * 메인 화면에 있는 작은 카드입니다.
 * @param HeaderText: title을 작성합니다.
 * @param SubHeaderText: desc을 작성합니다.
 * @param ImageField: 이미지를 추가해줍니다.
 */
@Composable
fun SmallTabCard(
    HeaderText: String = "테스트 문구",
    SubHeaderText: String = "테스트 문구\n테스트 문구",
    ImageField: Int,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .shadow(
                elevation = 5.dp,
                spotColor = gray600,
                ambientColor = Color(0x14000000),
                clip = false,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 14.dp)
            .noRippleClickable {
                onClick()
            },
        contentAlignment = Alignment.BottomEnd
    ) {
        if(ImageField == R.drawable.ic_main_shield) {
            Image(
                painter = painterResource(id = ImageField),
                contentDescription = "logo",
                modifier = Modifier.scale(1.1f).offset(x = 15.dp, y = (20).dp)
            )
        } else {
            Image(
                painter = painterResource(id = ImageField),
                contentDescription = "logo",
                modifier = Modifier
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            Text(
                text = HeaderText,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray500,
                )
            )
            Text(
                text = SubHeaderText,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 메인 화면에 있는 공지사항 카드입니다.
 * announcementText: 공지사항 카드 설명란
 */
@Composable
fun AnnouncementCard(announcementText: String = "TEST") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(82.dp)
            .shadow(
                elevation = 8.dp,
                spotColor = Color(0x14000000),
                ambientColor = Color(0x14000000)
            )
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 15.dp, top = 14.dp, end = 15.dp, bottom = 14.dp)
            .noRippleClickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_announcement),
            contentDescription = "megaphone",
            modifier = Modifier
                .offset(x = -2.dp)
                .width(47.dp)
                .height(41.dp)
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "공지사항",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF949BA8)
                )
            )
            HeightSpacer(4.dp)
            Text(
                text = announcementText,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF686D78)
                )
            )
        }
//        Spacer(modifier = Modifier.weight(1f))
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.btn_announce_arrow),
            contentDescription = "arrow",
            modifier = Modifier
                .padding(1.dp)
                .width(20.dp)
                .height(20.dp)
        )
    }
}

sealed class DosagePage {
    data class Overall(val dateText: String, val percent: Int) : DosagePage()
    data class PerDrug(val medicationName: String, val percent: Int) : DosagePage()
}

fun formatDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("M월 d일 E요일")
        .withLocale(Locale.KOREA)
    return date.format(formatter)
}

@Composable
fun DosageCard(title: String, percent: Int, horizontalPadding: Dp = 22.dp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .shadow(
                elevation = 6.dp,
                spotColor = gray400,
                ambientColor = Color(0x14000000),
                clip = false,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .height(132.dp)
            .background(color = Color.White, shape = RoundedCornerShape(12.dp))
            .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 24.dp)
            .noRippleClickable {
                onClick()
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_dosage_fire),
                contentDescription = "복약완료율"
            )
            WidthSpacer(6.dp)
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = primaryColor
                )
            )
        }
        HeightSpacer(6.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "복약 완료율",
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = gray800
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$percent%",
                style = TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 42.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = gray800
                )
            )
        }
        HeightSpacer(22.dp)
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = primaryColor,
            trackColor = gray200,
        )
    }
}

@Composable
fun FeatureButton(
    imageResource: Int,
    description: String,
    onClick: () -> Unit
) {
    var screenWidthDp = LocalConfiguration.current.screenWidthDp
    val boxSizeDp = (screenWidthDp * (54f / 375f)).dp
    val boxCornerRadius = (screenWidthDp * (14f / 375f)).dp
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(boxSizeDp)
                .height(boxSizeDp)
                .shadow(
                    elevation = 4.dp,
                    spotColor = gray600,
                    ambientColor = Color(0x14000000),
                    clip = false,
                    shape = RoundedCornerShape(size = boxCornerRadius)
                )
                .background(
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(size = boxCornerRadius)
                )
                .padding(start = 7.dp, top = 6.88867.dp, end = 7.dp, bottom = 7.11133.dp)
                .noRippleClickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                imageVector = ImageVector.vectorResource(imageResource),
                contentDescription = description,
//                modifier = Modifier.fillMaxSize()
            )
        }
        HeightSpacer(8.dp)
        Text(
            text = description,
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.8.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray800,
            )
        )
    }
}