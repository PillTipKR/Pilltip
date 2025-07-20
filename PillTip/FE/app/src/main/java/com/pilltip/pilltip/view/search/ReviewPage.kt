package com.pilltip.pilltip.view.search

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.SearchComposable.DashedBorderBox
import com.pilltip.pilltip.composable.SearchComposable.EfficiencyRow
import com.pilltip.pilltip.composable.SearchComposable.RatingBar
import com.pilltip.pilltip.composable.SearchComposable.ReviewItemCard
import com.pilltip.pilltip.composable.SearchComposable.ReviewPhotoPicker
import com.pilltip.pilltip.composable.SearchComposable.ReviewRatingBar
import com.pilltip.pilltip.composable.SearchComposable.ReviewTextField
import com.pilltip.pilltip.composable.SearchComposable.StarRatingBar
import com.pilltip.pilltip.composable.SearchComposable.TagSection
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.buttonModifier
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.DetailDrugData
import com.pilltip.pilltip.model.search.ReviewStatsData
import com.pilltip.pilltip.model.search.ReviewTagRequest
import com.pilltip.pilltip.model.search.ReviewViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor

@Composable
fun ReviewTab(
    navController: NavController,
    detail: DetailDrugData,
    searchHiltViewModel: SearchHiltViewModel,
    reviewViewModel: ReviewViewModel
) {
    val reviewStats by searchHiltViewModel.reviewStats.collectAsState()
    LaunchedEffect(Unit) {
        searchHiltViewModel.fetchReviewStats(detail.id)
    }
    Column(

    ) {
        reviewStats?.let { ReviewStatisticsSection(navController, it) }
        HorizontalDivider(thickness = 10.dp, color = gray100)
        reviewStats?.let { ReviewSection(reviewStats, reviewViewModel, detail.id) }
        HeightSpacer(100.dp)

    }

}

@Composable
fun ReviewStatisticsSection(
    navController: NavController,
    reviewStats: ReviewStatsData
) {
    val nickname = UserInfoManager.getUserData(LocalContext.current)?.nickname
    Column(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 300.dp)
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "리뷰",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            WidthSpacer(4.dp)
            Text(
                text = reviewStats.total.toString(),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = primaryColor,
                )
            )
        }
        HeightSpacer(8.dp)
        Text(
            text = buildAnnotatedString {
                val likeCount = reviewStats.like.toString()
                withStyle(style = SpanStyle(color = primaryColor)) {
                    append(likeCount)
                }
                withStyle(style = SpanStyle(color = Color.Black)) {
                    append("명의 이용자들이 해당 약품에 만족하고 있어요!")
                }
            },
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600)
            )
        )

        HeightSpacer(18.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .background(color = gray050, shape = RoundedCornerShape(size = 16.dp))
                .padding(start = 22.dp, top = 24.dp, end = 22.dp, bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = reviewStats.ratingStatsResponse.average.toString(),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray800,
                    ),
                    modifier = Modifier.height(38.dp)
                )
                HeightSpacer(8.dp)
                StarRatingBar(
                    rating = reviewStats.ratingStatsResponse.average.toFloat()
                ) {}
                HeightSpacer(8.dp)
                Text(
                    text = "만족도 ${
                        reviewStats.ratingStatsResponse.average.toFloat().div(5)
                            .times(100).toInt()
                    }%",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray500,
                    )
                )
            }
            VerticalDivider(
                thickness = 1.dp,
                color = gray200,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 22.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ReviewRatingBar(
                    title = "5점",
                    total = reviewStats.total,
                    progress = reviewStats.ratingStatsResponse.ratingCounts["5"]?:0,
                )
                ReviewRatingBar(
                    title = "4점",
                    total = reviewStats.total,
                    progress = reviewStats.ratingStatsResponse.ratingCounts["4"] ?:0,
                )
                ReviewRatingBar(
                    title = "3점",
                    total = reviewStats.total,
                    progress = reviewStats.ratingStatsResponse.ratingCounts["3"] ?:0,
                )
                ReviewRatingBar(
                    title = "2점",
                    total = reviewStats.total,
                    progress = reviewStats.ratingStatsResponse.ratingCounts["2"] ?:0,
                )
                ReviewRatingBar(
                    title = "1점",
                    total = reviewStats.total,
                    progress = reviewStats.ratingStatsResponse.ratingCounts["1"] ?:0,
                )
            }
        }
        HeightSpacer(6.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .padding(start = 0.dp, top = 12.dp, end = 0.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            EfficiencyRow(
                title = "효과",
                description = reviewStats.tagStatsByType["EFFICACY"]?.mostUsedTagName
                    ?: "집계 중",
                percentage = reviewStats.tagStatsByType["EFFICACY"]?.mostUsedTagCount ?: 0,
                num = reviewStats.tagStatsByType["EFFICACY"]?.totalTagCount ?: 0
            )
            EfficiencyRow(
                title = "부작용",
                description = reviewStats.tagStatsByType["SIDE_EFFECT"]?.mostUsedTagName
                    ?: "집계 중",
                percentage = reviewStats.tagStatsByType["SIDE_EFFECT"]?.mostUsedTagCount ?: 0,
                num = reviewStats.tagStatsByType["SIDE_EFFECT"]?.totalTagCount ?: 0
            )
            EfficiencyRow(
                title = "기타",
                description = reviewStats.tagStatsByType["OTHER"]?.mostUsedTagName ?: "집계 중",
                percentage = reviewStats.tagStatsByType["OTHER"]?.mostUsedTagCount ?: 0,
                num = reviewStats.tagStatsByType["OTHER"]?.totalTagCount ?: 0
            )
        }
        HeightSpacer(12.dp)
        Text(
            text = "${nickname}님 맞춤 리뷰 ",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
            ),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        HeightSpacer(4.dp)
        DashedBorderBox(
            onRegisterClick = {
                navController.navigate("EssentialPage")
            }
        )
    }
}

@Composable
fun ReviewSection(
    reviewStats: ReviewStatsData?,
    reviewViewModel: ReviewViewModel,
    id: Long
) {
    val reviewListData by reviewViewModel.reviewListData.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        reviewViewModel.loadReviews(id)
    }

    LaunchedEffect(remember { derivedStateOf { listState.firstVisibleItemIndex } }) {
        val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        val totalItemCount = reviewListData?.content?.size ?: 0

        if (lastVisibleItemIndex == totalItemCount - 1) {
            reviewViewModel.loadReviews(id)
        }
    }
    val localHeight = LocalConfiguration.current.screenHeightDp
    LazyColumn(
        modifier = Modifier.height(if (reviewStats?.total == 0) 200.dp else localHeight.dp),
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, top = 24.dp, bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "리뷰",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = gray800,
                        )
                    )
                    WidthSpacer(4.dp)
                    Text(
                        text = "${reviewStats?.total ?: 0}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = primaryColor,
                        )
                    )
                }
            }

        }

        reviewListData?.content?.let { reviews ->
            items(reviews) { review ->
                ReviewItemCard(review, reviewViewModel)
                HorizontalDivider(thickness = 4.dp, color = gray100)
            }
        }
    }
}

@Composable
fun ReviewWritePage(
    navController: NavController,
    reviewViewModel: ReviewViewModel,
    id: Long
) {
    val efficacyTags = listOf("효과 빠름", "통증 완화", "수면 도움", "염증 완화")
    val sideEffectTags = listOf("졸림", "메스꺼움", "두통", "피로감")
    val otherTags = listOf("복용 편함", "맛이 괜찮음", "포장 간편함", "가격 저렴함")
    var context = LocalContext.current

    val selectedEfficacy = remember { mutableStateListOf<String>() }
    val selectedSideEffect = remember { mutableStateListOf<String>() }
    val selectedOther = remember { mutableStateListOf<String>() }
    val reviewImages = remember { mutableStateListOf<Uri>() }
    var reviewText by remember { mutableStateOf("") }

    var rating by remember { mutableStateOf(0f) }
    var nickname = UserInfoManager.getUserData(context)?.nickname
    BackHandler {
        navController.popBackStack()
    }
    Column(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BackButton(
            title = "리뷰 작성하기",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.popBackStack()
        }
        HeightSpacer(30.dp)
        Text(
            text = "${nickname}님, 복약 만족도는 어떠셨나요?",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color(0xFF000000),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        HeightSpacer(8.dp)
        Text(
            text = "솔직한 답변이 많은 분들께 큰 도움이 돼요!",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(400),
                color = gray600,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        HeightSpacer(19.dp)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RatingBar(rating = rating) { rating = it }
        }
        HeightSpacer(30.dp)
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 18.dp),
            thickness = 1.dp,
            color = gray100
        )
        HeightSpacer(12.dp)
        TagSection("효능", efficacyTags, selectedEfficacy)
        HeightSpacer(16.dp)
        TagSection("부작용", sideEffectTags, selectedSideEffect)
        HeightSpacer(16.dp)
        TagSection("기타", otherTags, selectedOther)
        HeightSpacer(24.dp)

        ReviewPhotoPicker(
            onImagesChanged = { newUris ->
                reviewImages.clear()
                reviewImages.addAll(newUris)
            }
        )
        HeightSpacer(26.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "내용을 입력해주세요 (선택)",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(600),
                    color = gray800,
                )
            )
            Text(
                text = "${reviewText.length} / 200",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray400,
                )
            )
        }
        HeightSpacer(16.dp)
        ReviewTextField(
            nickname = nickname.toString(),
            text = reviewText,
            onTextChange = { if (it.length <= 200) reviewText = it }
        )

        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = buttonModifier,
            text = "등록하기"
        ) {
            reviewViewModel.createReview(
                drugId = id,
                rating = rating,
                content = reviewText,
                tags = ReviewTagRequest(
                    efficacy = selectedEfficacy.toList(),
                    side_Effect = selectedSideEffect.toList(),
                    other = selectedOther.toList()
                ),
                context = context,
                imageUris = reviewImages.toList(),
                onSuccess = {
                    navController.popBackStack()
                    Toast.makeText(context, "리뷰 등록 완료!", Toast.LENGTH_SHORT).show()
                },
                onError = { errorMsg ->
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

