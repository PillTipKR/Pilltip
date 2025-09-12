package com.pilltip.pilltip.view.friend

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.AppBar
import com.pilltip.pilltip.composable.AuthComposable.AgeField
import com.pilltip.pilltip.composable.AuthComposable.BodyProfile
import com.pilltip.pilltip.composable.AuthComposable.ProfileGenderPick
import com.pilltip.pilltip.composable.AuthComposable.ProfileStepDescription
import com.pilltip.pilltip.composable.AuthComposable.RoundTextField
import com.pilltip.pilltip.composable.BackButton
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.WhiteScreenModifier
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.UserInfoManager
import com.pilltip.pilltip.model.search.CreateProfileRequest
import com.pilltip.pilltip.model.search.UserProfileViewModel
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.model.signUp.TokenManager
import com.pilltip.pilltip.model.signUp.UserProfile
import com.pilltip.pilltip.ui.theme.gray200
import com.pilltip.pilltip.ui.theme.gray500
import com.pilltip.pilltip.ui.theme.gray700
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor
import com.pilltip.pilltip.view.friend.logic.calculateBirthDate
import com.pilltip.pilltip.view.questionnaire.Logic.toKoreanGender

@Composable
fun ChildrenPage(
    navController: NavController,
    signUpViewModel: SignUpViewModel
) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()
    var userData by remember { mutableStateOf(UserInfoManager.getUserData(context)) }
    var refreshKey by remember { mutableStateOf(0) }
    var childrenList by remember { mutableStateOf(UserInfoManager.getUserData(context)?.userList.orEmpty()) }
    var accessToken = TokenManager.getAccessToken(context)
    val isMainProfile by signUpViewModel.isMainProfile.collectAsState()

    LaunchedEffect(Unit) {
        signUpViewModel.updateIsMainProfile(UserInfoManager.getUserData(context))
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    Box(
        modifier = WhiteScreenModifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(horizontal = 22.dp)
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                BackButton(
                    horizontalPadding = 0.dp,
                    verticalPadding = 0.dp,
                    title = "가족 관리"
                ) {
                    navController.popBackStack()
                }
            }
            items(childrenList) { child ->
                ChildrenCard(child, onClick = {
                    UserInfoManager.selectProfile(context, child.userId)
                    signUpViewModel.updateIsMainProfile(userData)
                    childrenList = UserInfoManager.getUserData(context)?.userList.orEmpty()
                    Toast
                        .makeText(
                            context,
                            "[${child.nickname}] 계정으로 전환했어요",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                    signUpViewModel.fetchMyInfo(
                        token = accessToken.toString(),
                        profileId = child.userId
                    ) { userData ->
                        UserInfoManager.saveUserData(context, userData)
                        signUpViewModel.updateIsMainProfile(userData)
                        refreshKey++
                    }
                },
                    onDelete = {
                        signUpViewModel.deleteProfile(
                            token = accessToken.toString(),
                            profileId = child.userId,
                            onSuccess = {
                                Toast.makeText(context, "프로필을 삭제했어요", Toast.LENGTH_SHORT).show()
                                childrenList = UserInfoManager.getUserData(context)?.userList.orEmpty()
                            },
                            onError = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
                HeightSpacer(6.dp)
            }
        }
        if (isMainProfile) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 22.dp)
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(color = primaryColor)
                    .noRippleClickable{
                        navController.navigate("ChildProfilePage")
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "자녀 추가", tint = Color.White)
            }
        }
    }
}

@Composable
fun ChildProfilePage(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel,
    signUpViewModel: SignUpViewModel
) {
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    val accessToken = TokenManager.getAccessToken(context)
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var year by remember { mutableStateOf(0) }
    var month by remember { mutableStateOf(0) }
    var day by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    val isMainProfile by signUpViewModel.isMainProfile.collectAsState()

    LaunchedEffect(Unit) {
        signUpViewModel.updateIsMainProfile(UserInfoManager.getUserData(context))
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.White,
            darkIcons = true
        )
    }

    val isFormValid = nickname.isNotBlank()
            && gender.isNotBlank()
            && year > 0 && month > 0 && day > 0
            && height.length >= 2
            && weight.length >= 2

    Column(
        modifier = WhiteScreenModifier
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        HeightSpacer(50.dp)
        AppBar(
            horizontalPadding = 0.dp,
            LNB = R.drawable.btn_left_gray_arrow,
            LNBDesc = "뒤로가기 버튼",
            LNBClickable = { navController.popBackStack() },
            TitleText = "프로필 등록",
        )
        HeightSpacer(36.dp)
        ProfileStepDescription("닉네임")
        HeightSpacer(12.dp)
        RoundTextField(
            text = nickname,
            textChange = { nickname = it },
            placeholder = "닉네임을 입력해주세요",
            isLogin = false
        )
        HeightSpacer(28.dp)
        ProfileStepDescription("성별")
        HeightSpacer(12.dp)
        ProfileGenderPick(select = { gender = it })
        HeightSpacer(28.dp)
        ProfileStepDescription("연령")
        HeightSpacer(12.dp)
        AgeField(
            ageChange = { selectedYear, selectedMonth, selectedDay ->
                year = selectedYear
                month = selectedMonth
                day = selectedDay
            },
            displayYear = year,
            displayMonth = month,
            displayDay = day,
        )
        HeightSpacer(28.dp)
        Row {
            ProfileStepDescription("연령")
            WidthSpacer(4.dp)
            Image(
                imageVector = ImageVector.vectorResource(R.drawable.btn_profile_question),
                contentDescription = "물음표 아이콘을 누를 시 간단한 알림 문구를 표출합니다"
            )
        }
        HeightSpacer(12.dp)
        BodyProfile { selectedHeight, selectedWeight ->
            height = selectedHeight
            weight = selectedWeight
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "등록된 정보는 나중에 수정할 수 있어요!",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = pretendard,
                fontWeight = FontWeight(500),
                color = primaryColor,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )
        NextButton(
            mModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .padding(bottom = 46.dp)
                .height(58.dp),
            buttonColor = if (isFormValid && isMainProfile) Color(0xFF397CDB) else Color(0xFFCADCF5),
            onClick = {
                if (height.length >= 2 && weight.length >= 2) {
                    val (birthDate, age) = calculateBirthDate(1995, 7, 20)
                    userProfileViewModel.createProfile(
                        CreateProfileRequest(
                            nickname = nickname,
                            gender = gender,
                            birthDate = birthDate,
                            age = age,
                            height = height.toInt(),
                            weight = weight.toInt()
                        ),
                        onSuccess = {
                            signUpViewModel.fetchMyInfo(accessToken.toString()) { userData ->
                                UserInfoManager.saveUserData(context, userData)
                                Toast.makeText(
                                    context,
                                    "자녀 등록 완료!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            }
                        },
                        onError = { e ->
                            Log.e("Profile", "생성 실패: ${e.message}")
                            navController.popBackStack()
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun ChildrenCard(
    child: UserProfile,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = gray200,
                shape = RoundedCornerShape(size = 12.dp)
            )
            .padding(0.25.dp)
            .fillMaxWidth()
            .height(71.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 12.dp))
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
            .noRippleClickable {
                onClick()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = child.nickname,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF000000),
                )
            )
            WidthSpacer(4.dp)
            if (child.isSelected) {
                Text(
                    text = "사용 중",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = primaryColor,
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "삭제",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray500,
                ),
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.noRippleClickable {
                    onDelete()
                }
            )
        }

        HeightSpacer(8.dp)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = child.gender.toKoreanGender(),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(400),
                    color = gray700,
                )
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .border(width = 1.dp, color = gray200)
                    .width(0.dp)
                    .height(10.dp)
            )
            Text(
                text = "${child.birthDate} (만 ${child.age}세)",
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
