package com.pilltip.pilltip.view.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.pilltip.pilltip.model.search.AgentChatViewModel
import com.pilltip.pilltip.model.search.ChatMessage
import com.pilltip.pilltip.model.search.ChatRole
import com.pilltip.pilltip.model.search.StatusEvent
import kotlinx.coroutines.launch

@Composable
fun ChatbotPage(
    viewModel: AgentChatViewModel,
    navController: NavController,
) {
    val messages by viewModel.messages.collectAsState()
    val statuses by viewModel.statusEvents.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val agentError by viewModel.agentError.collectAsState()

    var input by remember { mutableStateOf("") }
    val focus = LocalFocusManager.current

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 새 "아이템"이 추가되면 자동 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
    }
    // 마지막 메시지의 텍스트가 스트리밍으로 갱신될 때도 스크롤
    LaunchedEffect(messages.lastOrNull()?.id, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 상단 진행바 (스트리밍 중일 때)
        if (isStreaming) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        }

        // 메시지 타임라인
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                when (msg.role) {
                    ChatRole.User -> {
                        UserBubble(msg.text)
                        Spacer(Modifier.height(8.dp))
                    }
                    ChatRole.Assistant -> {
                        AssistantRunBlock(
                            assistantMsg = msg,
                            allStatuses = statuses
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    ChatRole.System -> {
                        // 지금 스타일에서는 숨김. 필요하면 노출.
                        // SystemChip(msg.text)
                    }
                }
            }
        }

        // 에러가 있다면 하단 고정 에러 바 + 재시도
        agentError?.let { errorMsg ->
            ErrorRetryBar(
                message = errorMsg,
                onRetry = {
                    focus.clearFocus()
                    viewModel.retry()
                    // 재시도 즉시 스크롤 맨 아래로
                    scope.launch {
                        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
                    }
                }
            )
        }

        // 입력창
        ChatInputBar(
            value = input,
            onValueChange = { input = it },
            onSend = {
                val text = input.trim()
                if (text.isNotEmpty()) {
                    focus.clearFocus()
                    viewModel.send(text, session = 1)
                    input = ""
                    scope.launch {
                        if (messages.isNotEmpty()) listState.scrollToItem(messages.lastIndex)
                    }
                }
            }
        )
    }
}

/* -----------------------------------------------------------
 * Assistant “한 런” 블록: 상태 라인(위) + 말풍선 + 완료 라인(아래)
 * ----------------------------------------------------------- */

@Composable
private fun AssistantRunBlock(
    assistantMsg: ChatMessage,
    allStatuses: List<StatusEvent>
) {
    // 내 말풍선(targetId)에 해당하는 상태만 모아서 시간순 정렬
    val myStatuses = remember(assistantMsg.id, allStatuses) {
        allStatuses
            .filter { it.targetId == assistantMsg.id }
            .sortedBy { it.ts }
    }

    val hasFinal = remember(myStatuses) { myStatuses.any { it.code.equals("FINAL", true) } }
    val hasError = remember(myStatuses) { myStatuses.any { it.code.equals("ERROR", true) } }
    val errorText = remember(myStatuses) {
        myStatuses.lastOrNull { it.code.equals("ERROR", true) }?.message
    }

    Column(Modifier.fillMaxWidth()) {
        // 위쪽: 진행 상태 라인들 (FINAL/ERROR 제외)
        myStatuses
            .filter { it.code.uppercase() !in setOf("FINAL", "ERROR") }
            .forEach { s ->
                DashedStatusLine(
                    text = s.message.ifBlank { statusCodeToHuman(s.code) },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

        // 본문 말풍선 (CHUNK 누적)
        AssistantBubble(
            text = assistantMsg.text,
            streaming = assistantMsg.streaming
        )

        // 아래쪽: 에러 or 완료 라인
        when {
            hasError -> {
                DashedStatusLine(
                    text = (errorText ?: "오류가 발생했어요."),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    tone = StatusTone.Error
                )
            }
            hasFinal && !assistantMsg.streaming -> {
                DashedStatusLine(
                    text = "답변을 마쳤어요.",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    tone = StatusTone.Success
                )
            }
        }
    }
}

/* -----------------------------------------------------------
 * 말풍선들
 * ----------------------------------------------------------- */

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp)
                .widthIn(max = 360.dp)
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AssistantBubble(text: String, streaming: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
                .widthIn(max = 360.dp)
        ) {
            if (streaming && text.isEmpty()) {
                EllipsisDots()
            } else {
                val tail = if (streaming) "▌" else ""
                Text(
                    text = text + tail,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* -----------------------------------------------------------
 * 상태 라인 / 에러-성공 톤
 * ----------------------------------------------------------- */

private enum class StatusTone { Neutral, Error, Success }

@Composable
private fun DashedStatusLine(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral
) {
    val color = when (tone) {
        StatusTone.Neutral -> MaterialTheme.colorScheme.outline
        StatusTone.Error -> MaterialTheme.colorScheme.error
        StatusTone.Success -> MaterialTheme.colorScheme.primary
    }
    Text(
        text = "---$text---",
        modifier = modifier.fillMaxWidth(),
        style = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = color
        )
    )
}

/* 점점점 로더 */
@Composable
private fun EllipsisDots() {
    var dots by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            dots = (dots + 1) % 4  // 0..3
            kotlinx.coroutines.delay(300)
        }
    }
    Text(".".repeat(dots.coerceIn(0, 3)))
}

/* -----------------------------------------------------------
 * 입력 바 + 에러/재시도 바
 * ----------------------------------------------------------- */

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            maxLines = 6,
            placeholder = { Text("메시지를 입력하세요") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = onSend) { Text("전송") }
    }
}

@Composable
private fun ErrorRetryBar(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = onRetry,
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
            ) {
                Text("다시 시도", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/* -----------------------------------------------------------
 * 코드→문구 백업 맵핑
 * ----------------------------------------------------------- */

private fun statusCodeToHuman(code: String): String = when (code.uppercase()) {
    "INTENT" -> "질문 의도를 파악했어요."
    "DUR_CHECK_START" -> "복용 상호작용(DUR)을 확인 중이에요."
    "DUR_CHECK_RESULT" -> "DUR 확인 결과를 정리하고 있어요."
    "TOOL_START" -> "도구 실행을 마쳤어요. 답변을 정리할게요."
    "FINAL" -> "답변을 마쳤어요."
    "ERROR" -> "오류가 발생했어요."
    else -> code
}