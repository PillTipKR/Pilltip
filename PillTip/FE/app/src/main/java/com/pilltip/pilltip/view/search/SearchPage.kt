package com.pilltip.pilltip.view.search

import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.SearchComposable.AutoCompleteList
import com.pilltip.pilltip.composable.SearchComposable.DashedBorderBox
import com.pilltip.pilltip.composable.SearchComposable.DrugSearchResultList
import com.pilltip.pilltip.composable.SearchComposable.ExpandableInfoBox
import com.pilltip.pilltip.composable.SearchComposable.ExportAndCopy
import com.pilltip.pilltip.composable.SearchComposable.PillSearchField
import com.pilltip.pilltip.composable.SearchComposable.SearchTag
import com.pilltip.pilltip.composable.SearchComposable.ZoomableImageDialog
import com.pilltip.pilltip.composable.SearchComposable.shareText
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.RecognizeSpeech
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.DetailDrugData
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.model.search.ReviewViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray300
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray600
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.search.Logic.removeMarkdown
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, FlowPreview::class)
@Composable
fun SearchPage(
    navController: NavController,
    logViewModel: LogViewModel,
    searchViewModel: SearchHiltViewModel
) {
    var inputText by remember { mutableStateOf("") }
    val recentSearches by logViewModel.recentSearches.collectAsState()
    val autoCompleted by searchViewModel.autoCompleted.collectAsState()
    val isLoading by searchViewModel.isAutoCompleteLoading.collectAsState()
    BackHandler {
        navController.navigate("PillMainPage") {
            popUpTo("PillMainPage") {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { inputText }
            .debounce(700)
            .distinctUntilChanged()
            .filter { it.isNotEmpty() }
            .collect { debouncedText ->
                if (debouncedText.isNotBlank()) {
                    searchViewModel.fetchAutoComplete(debouncedText, reset = true)
                }
            }
    }

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(horizontal = 22.dp, vertical = 18.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        PillSearchField(
            initialQuery = "",
            navController = navController,
            nowTyping = { inputText = it },
            searching = { inputText = it },
            onNavigateToResult = { query ->
                logViewModel.addSearchQuery(inputText)
                searchViewModel.fetchDrugSearch(query)
                navController.navigate("SearchResultsPage/${query}")
                Log.d("Query: ", query)
            }
        )
        HeightSpacer(28.dp)
        if (inputText.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최근 검색어",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(600),
                        color = gray700,
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "전체 삭제",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = gray600,
                        textDecoration = TextDecoration.Underline,
                    ),
                    modifier = Modifier.noRippleClickable {
                        logViewModel.clearSearchQueries()
                    }
                )
            }

            HeightSpacer(18.dp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                recentSearches.forEach { keyword ->
                    SearchTag(
                        keyword,
                        onNavigateToResult = {
                            logViewModel.addSearchQuery(keyword)
                            searchViewModel.fetchDrugSearch(keyword)
                            navController.navigate("SearchResultsPage/${keyword}")
                        },
                        onDelete = { logViewModel.deleteSearchQuery(keyword) }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                AutoCompleteList(
                    query = inputText,
                    searched = autoCompleted,
                    onClick = { selected ->
                        inputText = selected.value
                        searchViewModel.fetchDrugSearch(selected.value)
                        logViewModel.addSearchQuery(selected.value)
                        navController.navigate("SearchResultsPage/${selected.value}") {
                            popUpTo("MainPage") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onLoadMore = {
                        searchViewModel.fetchAutoComplete(inputText)
                    }
                )

                if (isLoading) {
                    HeightSpacer(40.dp)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(32.dp),
                        color = primaryColor,
                        strokeWidth = 3.dp
                    )
                }

                if (!isLoading && autoCompleted.isEmpty()) {
                    HeightSpacer(40.dp)
                    Text(
                        text = "검색 결과가 없어요",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 20.dp),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Medium,
                            color = gray500
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultsPage(
    navController: NavController,
    logViewModel: LogViewModel,
    searchViewModel: SearchHiltViewModel,
    initialQuery: String
) {
    Log.d("initialQuery: ", initialQuery)
    var inputText by remember { mutableStateOf(initialQuery) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val autoCompleted by searchViewModel.autoCompleted.collectAsState()
    val isLoading by searchViewModel.isLoading.collectAsState()
    val isAutoCompleteLoading by searchViewModel.isAutoCompleteLoading.collectAsState()
    val searchResults by searchViewModel.drugSearchResults.collectAsState()
    var hasUserTyped by remember { mutableStateOf(false) }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val dropdownWidth = screenWidth - 44.dp
    val listState = rememberLazyListState()

    var searchBoxOffset by remember { mutableStateOf(Offset.Zero) }
    var searchBoxSize by remember { mutableStateOf(IntSize.Zero) }

    val animatedHeight by animateDpAsState(
        targetValue = if (isDropdownExpanded) 450.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "dropdownHeight"
    )

    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.isNavigationBarVisible = true
    }

    LaunchedEffect(inputText, hasUserTyped) {
        if (!hasUserTyped) return@LaunchedEffect

        snapshotFlow { inputText }
            .debounce(600)
            .distinctUntilChanged()
            .filter { it.isNotBlank() }
            .collect {
                searchViewModel.fetchAutoComplete(it, reset = true)
                isDropdownExpanded = true
            }
    }

    LaunchedEffect(listState, inputText) {
        snapshotFlow { listState.layoutInfo }
            .map { it.visibleItemsInfo }
            .distinctUntilChanged()
            .collect { visibleItems ->
                val lastVisibleItem = visibleItems.lastOrNull()?.index ?: return@collect
                val totalItems = listState.layoutInfo.totalItemsCount

                if (lastVisibleItem >= totalItems - 1 && !isAutoCompleteLoading && isDropdownExpanded) {
                    searchViewModel.fetchAutoComplete(inputText, reset = false)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .padding(vertical = 18.dp)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    searchBoxOffset = coordinates.positionInRoot()
                    searchBoxSize = coordinates.size
                }
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .noRippleClickable {
                    navController.navigate("SearchPage") {
                        popUpTo("SearchPage") {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            PillSearchField(
                Modifier,
                inputText,
                navController = navController,
                nowTyping = {
                    inputText = it
                    if (!hasUserTyped) hasUserTyped = true
                },
                searching = {
                    inputText = it
                    if (!hasUserTyped) hasUserTyped = true
                },
                onNavigateToResult = { query ->
                    inputText = query
                    logViewModel.addSearchQuery(query)
                    searchViewModel.fetchDrugSearch(query)
                    hasUserTyped = false
                    isDropdownExpanded = false
                }
            )
            if (isDropdownExpanded) {
                Popup(
                    alignment = Alignment.TopCenter,
                    offset = IntOffset(0, (searchBoxOffset.y).toInt()),
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    Card(
                        modifier = Modifier
                            .width(dropdownWidth)
                            .heightIn(max = animatedHeight)
                            .background(Color.White, shape = RoundedCornerShape(12.dp))
                            .border(1.dp, gray200, RoundedCornerShape(12.dp)),
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .background(Color.White)
                                .fillMaxWidth()
                        ) {
                            items(autoCompleted) { result ->
                                Text(
                                    text = result.value,
                                    modifier = Modifier
                                        .noRippleClickable {
                                            inputText = result.value
                                            hasUserTyped = false
                                            searchViewModel.fetchDrugSearch(result.value)
                                            logViewModel.addSearchQuery(result.value)
                                            isDropdownExpanded = false
                                            navController.navigate("SearchResultsPage/${inputText}") {
                                                popUpTo("SearchPage") { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                        .padding(16.dp)
                                )
                            }

                            if (isAutoCompleteLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        HeightSpacer(16.dp)
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            searchResults.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "검색 결과가 없어요",
                        modifier = Modifier.align(Alignment.Center),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight.Medium,
                            color = gray500
                        )
                    )
                }
            }

            else -> {
                DrugSearchResultList(
                    searchViewModel,
                    drugs = searchResults,
                    navController = navController,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailPage(
    navController: NavController,
    searchViewModel: SearchHiltViewModel,
    reviewViewModel: ReviewViewModel
) {
    val tabs = listOf("약품정보", "보관방법", "리뷰")
    val pagerState = rememberPagerState(initialPage = 0) {
        tabs.size
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val detailState by searchViewModel.drugDetail.collectAsState()
    val nickname = UserInfoManager.getUserData(LocalContext.current)?.nickname
    val systemUiController = rememberSystemUiController()
    var isImageViewerOpen by remember { mutableStateOf(false) }
    SideEffect {
        systemUiController.isNavigationBarVisible = true
    }
    BackHandler {
        navController.popBackStack()
    }
    when (val detail = detailState) {
        null -> {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        else -> {
            val hasWarning = detail.durTags.any { it.isTrue }
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(WindowInsets.statusBars.asPaddingValues())
                        .padding(bottom = 91.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 16.dp)
                        ) {
                            Image(
                                imageVector = ImageVector.vectorResource(R.drawable.btn_details_left_arrow),
                                contentDescription = "뒤로가기 버튼",
                                modifier = Modifier
                                    .size(20.dp)
                                    .noRippleClickable { navController.popBackStack() }
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                imageVector = ImageVector.vectorResource(R.drawable.btn_details_share),
                                contentDescription = "공유 버튼",
                                modifier = Modifier
                                    .size(20.dp)
                                    .noRippleClickable { }
                            )
                            WidthSpacer(16.dp)
                            Image(
                                imageVector = ImageVector.vectorResource(R.drawable.btn_details_save),
                                contentDescription = "찜하기 버튼",
                                modifier = Modifier
                                    .size(20.dp)
                                    .noRippleClickable { }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .background(gray050),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!detail.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = detail.imageUrl,
                                    contentDescription = "약 이미지",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .noRippleClickable {
                                            isImageViewerOpen = true
                                        }
                                )
                                if (isImageViewerOpen) {
                                    ZoomableImageDialog(
                                        imageUrl = detail.imageUrl,
                                        onDismiss = { isImageViewerOpen = false }
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_default_pill),
                                        contentDescription = "기본 이미지"
                                    )
                                    WidthSpacer(30.dp)
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_default_pill),
                                        contentDescription = "기본 이미지"
                                    )
                                }

                            }
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 20.dp),
                        ) {
                            if (detail.tag == "COMMON") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
                                        contentDescription = "일반의약품",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    WidthSpacer(6.dp)
                                    Text(
                                        text = "일반의약품",
                                        fontSize = 12.sp,
                                        fontFamily = pretendard,
                                        fontWeight = FontWeight(500),
                                        color = primaryColor,
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_details_red_expert_pills),
                                        contentDescription = "전문의약품",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    WidthSpacer(6.dp)
                                    Text(
                                        text = "전문의약품",
                                        fontSize = 12.sp,
                                        fontFamily = pretendard,
                                        fontWeight = FontWeight(500),
                                        color = Color(0xFFEF524F),
                                    )
                                }

                            }
                            HeightSpacer(8.dp)
                            Text(
                                text = detail.name,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight(600),
                                    color = Color(0xFF000000)
                                )
                            )
                            HeightSpacer(6.dp)
                            Text(
                                text = detail.manufacturer,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontFamily = pretendard,
                                    fontWeight = FontWeight(500),
                                    color = gray500
                                )
                            )
                            HeightSpacer(16.dp)
                            HorizontalDivider(thickness = 0.5.dp, color = gray200)
                            HeightSpacer(16.dp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    imageVector = ImageVector.vectorResource(
                                        if (hasWarning) R.drawable.ic_search_dur_alert
                                        else R.drawable.ic_search_dur_ok
                                    ),
                                    contentDescription = "DUR 알림",
                                    modifier = Modifier.size(20.dp)
                                )
                                WidthSpacer(6.dp)
                                Text(
                                    text = if (hasWarning) "$nickname 님은 섭취에 주의가 필요한 약품이에요!"
                                    else "$nickname 님! 안심하고 복약하셔도 괜찮아요",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontFamily = pretendard,
                                        fontWeight = FontWeight(500),
                                        color = gray800,
                                    )
                                )
                            }
                            HeightSpacer(14.dp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                detail.durTags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .background(
                                                color = if (tag.isTrue) primaryColor else gray100,
                                                shape = RoundedCornerShape(size = 601.70361.dp)
                                            )
                                            .padding(
                                                start = 10.dp,
                                                top = 6.dp,
                                                end = 10.dp,
                                                bottom = 6.dp
                                            )
                                    ) {
                                        Text(
                                            text = tag.title,
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                fontFamily = pretendard,
                                                fontWeight = FontWeight(500),
                                                color = if (tag.isTrue) Color.White else gray500,
                                            )
                                        )
                                    }
                                }

                            }
                        }
                        HorizontalDivider(thickness = 10.dp, color = gray100)
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White),
                            indicator = { tabPositions ->
                                SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                    color = gray800
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    modifier = Modifier
                                        .background(Color.White),
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    selectedContentColor = gray800,
                                    unselectedContentColor = gray300,
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight(600),
                                            fontFamily = pretendard
                                        )
                                    }
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) { page ->
                            when (page) {
                                0 -> DrugInfoTab(navController, detail, searchViewModel)
                                1 -> StorageInfoTab(navController, detail)
                                2 -> ReviewTab(
                                    navController,
                                    detail,
                                    searchViewModel,
                                    reviewViewModel
                                )
                            }
                        }
                    }

                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            clip = false
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .background(color = Color.White)
                            .padding(start = 22.dp, top = 12.dp, end = 22.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${detail.count}명이 해당 약품을 복약했어요",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontFamily = pretendard,
                                fontWeight = FontWeight(300),
                                color = gray700,
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = gray200
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        NextButton(
                            mModifier = Modifier
                                .weight(1f)
                                .padding(vertical = 16.dp)
                                .padding(start = 22.dp, bottom = 46.dp, end = 5.dp)
                                .height(58.dp)
                                .border(
                                    1.dp,
                                    primaryColor,
                                    shape = RoundedCornerShape(size = 12.dp)
                                ),
                            text = "복약정보 저장",
                            shape = 12,
                            buttonColor = Color.White,
                            textColor = primaryColor,
                            onClick = {
                                if (detail.isTaking == true) Toast.makeText(
                                    context,
                                    "이미 복약 중이에요!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                else navController.navigate(
                                    "DosagePage/${detail.id}/${
                                        Uri.encode(
                                            detail.name
                                        )
                                    }"
                                )
                            }
                        )
                        NextButton(
                            mModifier = Modifier
                                .weight(1f)
                                .padding(vertical = 16.dp)
                                .padding(start = 5.dp, bottom = 46.dp, end = 22.dp)
                                .height(58.dp),
                            text = "리뷰 쓰기",
                            shape = 12,
                            buttonColor = primaryColor,
                            textColor = Color.White,
                            onClick = {
                                navController.navigate("ReviewWritePage/${detail.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrugInfoTab(
    navController: NavController,
    detail: DetailDrugData,
    viewModel: SearchHiltViewModel
) {
    val context = LocalContext.current
    val nickname = UserInfoManager.getUserData(context)?.nickname
    val clipboardManager = LocalClipboardManager.current
    val permission = UserInfoManager.getUserData(LocalContext.current)?.permissions
    val gptAdvice by viewModel.gptAdvice.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { scrollState.value > 300 }
    }

    if (permission == true && gptAdvice == null) {
        LaunchedEffect(detail.id) {
            viewModel.fetchGptAdvice(detail)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 300.dp)
            .padding(horizontal = 22.dp, vertical = 30.dp)
    ) {
        HeightSpacer(12.dp)
        Text(
            text = "${nickname}님 AI 맞춤 안내",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = primaryColor,
            )
        )
        HeightSpacer(8.dp)
        Text(
            text = "입력해주신 정보를 바탕으로 작성되었어요!",
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray500,
            )
        )
        HeightSpacer(12.dp)
        if (permission == true) {
            if (gptAdvice != null) {
                ExpandableInfoBox(
                    item = gptAdvice!!,
                    collapsedHeight = 186.dp
                ) { gpt ->
                    Text(
                        text = removeMarkdown(gpt),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            lineHeight = 23.4.sp,
                            fontWeight = FontWeight(500),
                            color = gray800,
                        )
                    )
                }
            } else {
                val transition = rememberInfiniteTransition()
                val translateAnimation by transition.animateFloat(
                    initialValue = 360f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 1200,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HeightSpacer(20.dp)
                    Canvas(modifier = Modifier.size(size = 30.dp)) {
                        val startAngle = 5f
                        val sweepAngle = 350f

                        rotate(translateAnimation) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        primaryColor,
                                        primaryColor.copy(0f)
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f)
                                ),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(6 / 2f, 6 / 2f),
                                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                            )
                        }
                    }
                    HeightSpacer(20.dp)
                    Text(
                        text = "${nickname}님을 위한 맞춤 정보를 AI가 분석 중이에요",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(700),
                            color = gray800,
                            textAlign = TextAlign.Center,
                        )
                    )
                    HeightSpacer(8.dp)
                    Text(
                        text = "잠시만 기다려주세요",
                        style = TextStyle(
                            fontSize = 12.sp,
                            lineHeight = 19.6.sp,
                            fontFamily = pretendard,
                            fontWeight = FontWeight(600),
                            color = gray400,
                        )
                    )
                }
            }
        } else {
            DashedBorderBox(
                onRegisterClick = {
                    navController.navigate("EssentialPage")
                }
            )
        }
        HeightSpacer(32.dp)
        ExportAndCopy(
            headerText = "효능/효과",
            onCopyClicked = {
                val text = removeMarkdown(detail.effect.effect)
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "복사되었습니다", Toast.LENGTH_SHORT).show()
            },
            onExportClicked = {
                val text = removeMarkdown(detail.effect.effect)
                shareText(context, "효능/효과", text)
            }
        )
        ExpandableInfoBox(
            item = detail.effect,
            collapsedHeight = 186.dp
        ) { effect ->
            Text(
                text = removeMarkdown(effect.effect),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    lineHeight = 23.4.sp,
                    fontWeight = FontWeight(500),
                    color = gray800,
                )
            )
        }
        HeightSpacer(32.dp)
        ExportAndCopy(
            headerText = "상세성분",
            onCopyClicked = {
                val text = detail.ingredients.joinToString("\n") {
                    "- ${it.name} (${it.dose}${if (it.isMain) ", 주성분" else ""})"
                }
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "복사되었습니다", Toast.LENGTH_SHORT).show()
            },
            onExportClicked = {
                val text = detail.ingredients.joinToString("\n") {
                    "- ${it.name} (${it.dose}) ${if (it.isMain) "[주성분]" else ""}"
                }
                shareText(context, "상세성분", text)
            }
        )
        ExpandableInfoBox(
            items = detail.ingredients
        ) { ingredient ->
            Text(
                text = "${if (ingredient.isMain) "[주성분]" else "⸰ "} ${ingredient.name} (${ingredient.dose})",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    lineHeight = 23.4.sp,
                    fontWeight = FontWeight(500),
                    color = gray800,
                )
            )
        }
        HeightSpacer(32.dp)
        ExportAndCopy(
            headerText = "용량/용법",
            onCopyClicked = {
                val text = removeMarkdown(detail.usage.effect)
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "복사되었습니다", Toast.LENGTH_SHORT).show()
            },
            onExportClicked = {
                val text = removeMarkdown(detail.usage.effect)
                shareText(context, "용량/용법", text)
            }
        )
        ExpandableInfoBox(
            item = detail.usage,
            collapsedHeight = 186.dp
        ) { effect ->
            Text(
                text = removeMarkdown(effect.effect),
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 23.4.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800,
                )
            )
        }
        HeightSpacer(32.dp)
        ExportAndCopy(
            headerText = "주의사항",
            onCopyClicked = {
                val text = removeMarkdown(detail.caution.effect)
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "복사되었습니다", Toast.LENGTH_SHORT).show()
            },
            onExportClicked = {
                val text = removeMarkdown(detail.caution.effect)
                shareText(context, "주의사항", text)
            }
        )
        ExpandableInfoBox(
            item = detail.caution,
            collapsedHeight = 186.dp
        ) { effect ->
            Text(
                text = removeMarkdown(effect.effect),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    lineHeight = 23.4.sp,
                    fontWeight = FontWeight(500),
                    color = gray800,
                )
            )
        }
        HeightSpacer(100.dp)
    }
}

@Composable
fun StorageInfoTab(
    navController: NavController,
    detail: DetailDrugData
) {
    Column(
        modifier = Modifier
            .height(350.dp)
            .padding(horizontal = 22.dp, vertical = 30.dp)
    ) {
        HeightSpacer(12.dp)
        Text(
            text = "보관방법",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray800,
            )
        )
        HeightSpacer(8.dp)
        Text(
            text = "아래 보관방법을 잘 지켜주세요!",
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = gray500,
            )
        )
        HeightSpacer(12.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
                .background(color = gray050, shape = RoundedCornerShape(size = 16.dp))
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = "서늘/건조",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800,
                    textAlign = TextAlign.Center,
                )
            )
            VerticalDivider(thickness = 0.7.dp)
            Text(
                text = "습기 X",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800,
                    textAlign = TextAlign.Center,
                )
            )
            VerticalDivider(thickness = 0.7.dp)
            Text(
                text = "실온",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800,
                    textAlign = TextAlign.Center,
                )
            )
            VerticalDivider(thickness = 0.7.dp)
            Text(
                text = "밀폐용기",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = gray800,
                    textAlign = TextAlign.Center,
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AudioSearchPage(
    navController: NavController,
    searchViewModel: SearchHiltViewModel,
    logViewModel: LogViewModel
) {
    val allKeywords = List(5) { index -> "키워드~${index + 1}" }
    val selectedKeywords = remember { mutableStateListOf<String>() }

    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = RecognizeSpeech(),
        onResult = { result: String? ->
            result?.let { ttsText ->
                searchViewModel.fetchDrugSearch(ttsText)
                logViewModel.addSearchQuery(ttsText)
                navController.popBackStack()
                navController.navigate("SearchResultsPage/${ttsText}") {
                    popUpTo("MainPage") { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                speechLauncher.launch(Unit)
            } else {
                Toast.makeText(context, "마이크 권한이 필요해요", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        BackButton(
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.popBackStack()
        }
        HeightSpacer(50.dp)
        Text(
            text = "어떤 점이 궁금하신가요?",
            style = TextStyle(
                fontSize = 24.sp,
                lineHeight = 33.6.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(700),
                color = Color(0xFF323439),
            )
        )
        HeightSpacer(20.dp)
        Text(
            text = "음성으로 검색해보세요!",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(600),
                color = gray400,
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .noRippleClickable {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        speechLauncher.launch(Unit)
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.btn_audio),
                contentDescription = "뒤로가기 버튼",
                modifier = Modifier.zIndex(1f)
            )
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.ic_audio_shadow),
                contentDescription = "버튼 그림자",
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}








