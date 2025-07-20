package com.pilltip.pilltip.view.auth.logic

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.pilltip.pilltip.R
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.WidthSpacer
import com.pilltip.pilltip.composable.noRippleClickable
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.ui.theme.gray100
import com.pilltip.pilltip.ui.theme.pretendard
import com.pilltip.pilltip.ui.theme.primaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermBottomSheet(
    vm: SignUpViewModel = hiltViewModel(),
    navController: NavController,
    onDismiss: () -> Unit
) {
    var isEssentialChecked by remember { mutableStateOf(false) }
    var isAgeChecked by remember { mutableStateOf(false) }
    var isOptionalChecked by remember { mutableStateOf(false) }
    var isEssentialExpanded by remember { mutableStateOf(false) }
    var isOptionalExpanded by remember { mutableStateOf(false) }

    val essentialRotation by animateFloatAsState(
        targetValue = if (isEssentialExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "essential_arrow_rotation"
    )

    val optionalRotation by animateFloatAsState(
        targetValue = if (isOptionalExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "optional_arrow_rotation"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 8.dp, bottom = 11.dp)
                    .width(48.dp)
                    .height(5.dp)
                    .background(
                        color = Color(0xFFE2E4EC),
                        shape = RoundedCornerShape(size = 12.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            HeightSpacer(16.dp)
            Text(
                text = "본인인증을 위해 동의가 필요해요",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    fontFamily = pretendard,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF323439),
                ),
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 10.dp)
            )
            HeightSpacer(12.dp)
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(imageVector =
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
                    text = "[필수] 필팁 서비스 이용약관 동의",
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
                        }
                        .graphicsLayer(rotationZ = essentialRotation)
                )
            }
            AnimatedVisibility(visible = isEssentialExpanded) {
                EssentialTerms()
            }
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(imageVector =
                    if (!isAgeChecked)
                        ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                    else
                        ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                    contentDescription = "checkBtn",
                    modifier = Modifier
                        .size(20.dp, 20.dp)
                        .noRippleClickable { isAgeChecked = !isAgeChecked }
                )
                WidthSpacer(8.dp)
                Text(
                    text = "[필수] 만 14세 이상입니다.",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = Color(0xFF686D78),
                    ),
                    modifier = Modifier.noRippleClickable {
                        isAgeChecked = !isAgeChecked
                    }
                )
            }
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector =
                    if (!isOptionalChecked)
                        ImageVector.vectorResource(R.drawable.btn_gray_checkmark)
                    else
                        ImageVector.vectorResource(R.drawable.btn_blue_checkmark),
                    contentDescription = "checkBtn",
                    modifier = Modifier
                        .size(20.dp, 20.dp)
                        .noRippleClickable { isOptionalChecked = !isOptionalChecked }
                )
                WidthSpacer(8.dp)
                Text(
                    text = "[선택] 마케팅 수신동의",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = pretendard,
                        fontWeight = FontWeight(500),
                        color = Color(0xFF686D78),
                    ),
                    modifier = Modifier.noRippleClickable { isOptionalChecked = !isOptionalChecked }
                )
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    imageVector = ImageVector.vectorResource(R.drawable.btn_announce_arrow),
                    contentDescription = "grayCheck",
                    modifier = Modifier
                        .noRippleClickable {
                            isOptionalExpanded = !isOptionalExpanded
                        }
                        .graphicsLayer(rotationZ = optionalRotation)
                )
            }
            AnimatedVisibility(visible = isOptionalExpanded) {
                OptionalTerms()
            }
            HeightSpacer(16.dp)
            NextButton(
                mModifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(58.dp),
                text = "확인",
                buttonColor = if (isEssentialChecked && isAgeChecked) Color(0xFF348ADF) else Color(0xFFCADCF5),
                onClick = {
                    if(isEssentialChecked && isAgeChecked){
                        vm.updateTermsOfServices(true)
                        onDismiss()
                        navController.navigate("PhoneAuthPage")
                    }
                }
            )
        }
    }
}

@Composable
fun containsSequentialNumbers(password: String, length: Int = 3): Boolean {
    if (password.length < length || password.isEmpty()) return true
    var checkDigit = ""
    for (char in password) {
        if (char.isDigit()) {
            checkDigit += char
            if (checkDigit.length >= length && isSequential(checkDigit))
                return true
        } else
            checkDigit = ""
    }
    return false
}

fun isSequential(substring: String): Boolean {
    val nums = substring.map { it - '0' }
    if (nums.zipWithNext().all { it.second - it.first == 1 }) return true
    if (nums.zipWithNext().all { it.second - it.first == -1 }) return true

    return false
}

/**
 * 사용자 IME 입력 타입을 설정하는 enum입니다.
 */
enum class InputType {
    TEXT, EMAIL, PASSWORD, NUMBER
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    BoxWithConstraints(modifier = modifier) {
        val boxSize = maxWidth / 7
        val space = (maxWidth - boxSize * 6) / 5

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { focusRequester.requestFocus() },
                horizontalArrangement = Arrangement.spacedBy(space)
            ) {
                (0 until 6).forEach { index ->
                    val char = otpText.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .height(72.dp)
                            .width(boxSize)
                            .aspectRatio(1f)
                            .border(
                                width = 1.5.dp,
                                color = if (char.isNotEmpty()) primaryColor else gray100,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(if(char.isEmpty()) gray100 else Color.White, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.W500,
                                color = primaryColor
                            )
                        )
                    }
                }
            }
            BasicTextField(
                value = otpText,
                onValueChange = {
                    if (it.length <= 6 && it.all { ch -> ch.isDigit() })
                        onOtpTextChange(it)
                },
                modifier = Modifier
                    .alpha(0f)
                    .height(72.dp)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .focusable()
                    .align(Alignment.Center),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                ),
            )
        }
    }
}

