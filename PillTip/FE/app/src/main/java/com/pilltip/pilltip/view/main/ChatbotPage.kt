package com.pilltip.pilltip.view.main

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pilltip.pilltip.composable.HeightSpacer
import com.pilltip.pilltip.model.search.AgentChatViewModel
import com.pilltip.pilltip.model.search.ChatRole
import kotlinx.coroutines.launch

@Composable
fun ChatbotPage(
    viewModel: AgentChatViewModel,
    navController: NavController,
) {
    val messages by viewModel.messages.collectAsState()
    val statuses by viewModel.statusEvents.collectAsState()   // ★ 코드별 상태칩
    val isStreaming by viewModel.isStreaming.collectAsState() // ★ 진행바

    var input by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 새 메시지/델타가 붙을 때 자동 스크롤
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // ★ 코드별 상태 타임라인 (칩들)
        StatusTimeline(statuses)

        // ★ 스트리밍 진행바 (선택)
        if (isStreaming) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                when (msg.role) {
                    ChatRole.User -> UserBubble(msg.text)
                    ChatRole.Assistant -> AssistantBubble(msg.text, msg.streaming)
                    ChatRole.System -> SystemChip(msg.text)
                }
                HeightSpacer(6.dp)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                placeholder = { Text("메시지를 입력하세요") }
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val text = input.trim()
                    if (text.isNotEmpty()) {
                        viewModel.send(text, session = 1)
                        input = ""
                        // 전송 직후 맨 아래로
                        scope.launch {
                            if (messages.isNotEmpty()) {
                                listState.scrollToItem(messages.lastIndex)
                            }
                        }
                    }
                }
            ) {
                Text("전송")
            }
        }
    }
}

@Composable
private fun StatusTimeline(items: List<com.pilltip.pilltip.model.search.StatusEvent>) {
    if (items.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(items) { s ->
            AssistChip(
                onClick = {},
                label = { Text("${s.code}: ${s.message}") }
            )
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp)
                .widthIn(max = 320.dp)
        ) {
            Text(text)
        }
    }
}

@Composable
private fun AssistantBubble(text: String, streaming: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
                .widthIn(max = 320.dp)
        ) {
            // ★ 핵심: 본문이 비어있고 스트리밍 중일 때만 ... 애니메이션
            if (streaming && text.isEmpty()) {
                EllipsisDots()
            } else {
                val tail = if (streaming) "▌" else ""
                Text(
                    text = text + tail,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
private fun EllipsisDots() {
    var dots by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            dots = (dots + 1) % 4   // 0 → 1 → 2 → 3 → 0 ...
            kotlinx.coroutines.delay(300) // 300ms 간격 (원하는 속도로 조절)
        }
    }
    Text(".".repeat(dots.coerceIn(0, 3)))
}

@Composable
private fun SystemChip(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        AssistChip(onClick = {}, label = { Text(text) })
    }
}