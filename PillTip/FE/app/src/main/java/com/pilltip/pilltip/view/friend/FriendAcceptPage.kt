package com.pilltip.pilltip.view.friend

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.FriendComposable.FriendItem
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.model.search.SensitiveViewModel
import com.pilltip.pilltip.ui.theme.gray400
import com.pilltip.pilltip.ui.theme.gray800
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.auth.logic.EssentialTerms
import kotlinx.coroutines.delay

@Composable
fun FriendAcceptPage(
    inviteToken: String?,
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel,
    sensitiveViewModel: SensitiveViewModel

) {
    val context = LocalContext.current
    val result by searchHiltViewModel.friendAcceptResult.collectAsState()
    var isEssentialChecked by remember { mutableStateOf(false) }
    var isEssentialExpanded by remember { mutableStateOf(false) }
    var isOptionalExpanded by remember { mutableStateOf(false) }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
        systemUiController.isNavigationBarVisible = true
    }

    val essentialRotation by animateFloatAsState(
        targetValue = if (isEssentialExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "essential_arrow_rotation"
    )

    result?.let {
        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()

        LaunchedEffect(Unit) {
            delay(500)
            navController.navigate("PillMainPage") {
                popUpTo("PillMainPage") { inclusive = true }
            }
        }
    }
    BackHandler {
        navController.navigate("PillMainPage") {
            popUpTo(0) {
                inclusive = true
            }
        }
    }
    Column(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .padding(horizontal = 22.dp)
    ) {
        BackButton(
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("PillMainPage") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }

        HeightSpacer(62.dp)
        Image(
            imageVector = ImageVector.vectorResource(R.drawable.ic_details_blue_common_pills),
            contentDescription = "방패 이미지",
            modifier = Modifier
                .size(32.dp)
                .padding(1.dp)
        )
        HeightSpacer(12.dp)
        Text(
            text = "정보 보호를 위해 동의가 필요해요",
            fontSize = 24.sp,
            lineHeight = 33.8.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(700),
            color = gray800
        )
        HeightSpacer(12.dp)
        Text(
            text = "친구 추가 시 내 복약 정보를 친구와 공유해요",
            fontSize = 14.sp,
            lineHeight = 19.6.sp,
            fontFamily = pretendard,
            fontWeight = FontWeight(600),
            color = gray400,
        )
        HeightSpacer(34.dp)
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector =
                    if (!isEssentialChecked)
                        ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                    else
                        ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                contentDescription = "checkBtn",
                modifier = Modifier
                    .size(20.dp, 20.dp)
                    .noRippleClickable { isEssentialChecked = !isEssentialChecked }
            )
            WidthSpacer(8.dp)
            Text(
                text = "[필수] 친구 서비스 이용약관",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF686D78),
                ),
                modifier = Modifier.noRippleClickable {
                    isEssentialChecked = !isEssentialChecked
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                contentDescription = "description",
                modifier = Modifier
                    .noRippleClickable {
                        isEssentialExpanded = !isEssentialExpanded
                        isOptionalExpanded = false
                    }
                    .graphicsLayer(rotationZ = essentialRotation)
            )
        }
        AnimatedVisibility(visible = isEssentialExpanded) {
            EssentialTerms()
        }

        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            buttonColor = if (isEssentialChecked) primaryColor else Color(
                0xFFCADCF5
            ),
            text = "동의하기",
            onClick = {
                if (isEssentialChecked) {
                    if (!inviteToken.isNullOrEmpty()) {
                        sensitiveViewModel.updateSinglePermission("friend", true)
                        searchHiltViewModel.acceptFriendInvite(inviteToken)
                        navController.navigate("FriendListPage") {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FriendListPage(
    navController: NavController,
    searchHiltViewModel: SearchHiltViewModel
) {
    val friendList by searchHiltViewModel.friendList.collectAsState()

    LaunchedEffect(Unit) {
        searchHiltViewModel.fetchFriendList()
    }

    BackHandler {
        navController.navigate("PillMainPage/MyPage") {
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    Column(
        modifier = WhiteScreenModifier.statusBarsPadding().padding(horizontal = 22.dp)
    ) {
        BackButton(
            title = "내 친구 목록",
            horizontalPadding = 0.dp,
            verticalPadding = 0.dp
        ) {
            navController.navigate("PillMainPage/MyPage") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
        if (friendList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("등록된 친구가 없어요!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(friendList) { friend ->
                    FriendItem(friend)
                    HorizontalDivider()
                }
            }
        }
    }
}

