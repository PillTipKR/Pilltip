package com.pilltip.pilltip.view.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.DrugLogCard
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.ui.theme.gray050
import com.pilltip.pilltip.ui.theme.gray300
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray900
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationPage(
    navController: NavController,
    hiltViewModel: SearchHiltViewModel
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val logData by hiltViewModel.dailyDosageLog.collectAsState()
    val selectedDrugLog = hiltViewModel.selectedDrugLog
    val tabs = listOf("전체", "미복용", "복용완료")
    val pagerState = rememberPagerState(initialPage = 0) {
        tabs.size
    }
    val coroutineScope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    LaunchedEffect(selectedDate) {
        selectedDate?.let {
            hiltViewModel.fetchDailyDosageLog(it)
        }
    }

    BackHandler {
        navController.navigate("PillMainPage") {
            popUpTo("PillMainPage") {
                inclusive = true
            }
        }
    }
    Column(
        modifier = WhiteScreenModifier.statusBarsPadding()
    ) {
        BackButton(
            title = "알림",
            verticalPadding = 0.dp,
            navigationTo = {
                navController.navigate("PillMainPage") {
                    popUpTo("PillMainPage") {
                        inclusive = true
                    }
                }
            }
        )
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = primaryColor
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White),
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    selectedContentColor = gray900,
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
                0 -> AllTab(navController, hiltViewModel)
                1 -> UnReadTab(navController, hiltViewModel)
                2 -> ReadTab(navController, hiltViewModel)
            }
        }
    }
}

@Composable
fun AllTab(
    navController: NavController,
    hiltViewModel: SearchHiltViewModel

) {
    val logData by hiltViewModel.dailyDosageLog.collectAsState()
    LaunchedEffect(Unit) {
        hiltViewModel.fetchDailyDosageLog(LocalDate.now())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gray050)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            logData?.perDrugLogs?.forEach { drug ->
                DrugLogCard(drug, hiltViewModel, LocalDate.now())
                HeightSpacer(6.dp)
            }
        }
    }
}

@Composable
fun UnReadTab(
    navController: NavController,
    hiltViewModel: SearchHiltViewModel
) {
    val logData by hiltViewModel.dailyDosageLog.collectAsState()
    LaunchedEffect(Unit) {
        hiltViewModel.fetchDailyDosageLog(LocalDate.now())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gray050)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gray050)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                logData?.perDrugLogs?.forEach { drug ->
                    DrugLogCard(drug, hiltViewModel, LocalDate.now(), isNotification = true)
                    HeightSpacer(6.dp)
                }
            }
        }
    }
}

@Composable
fun ReadTab(
    navController: NavController,
    hiltViewModel: SearchHiltViewModel
) {
    val logData by hiltViewModel.dailyDosageLog.collectAsState()
    LaunchedEffect(Unit) {
        hiltViewModel.fetchDailyDosageLog(LocalDate.now())
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gray050)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            logData?.perDrugLogs?.forEach { drug ->
                drug.dosageSchedule
                DrugLogCard(drug, hiltViewModel, LocalDate.now(), isNotification = true, isRead = true)
                HeightSpacer(6.dp)
            }
        }
    }
}