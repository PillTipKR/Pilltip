package com.oauth2.AgenticAI.Controller;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Service.AccountService;
import com.oauth2.AgenticAI.Agent.AgentOrchestrator;
import com.oauth2.AgenticAI.Dto.Orchestrator.ChatReq;
import com.oauth2.AgenticAI.Dto.Orchestrator.EventCode;
import com.oauth2.AgenticAI.Dto.Orchestrator.StreamEvent;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.Util.Exception.CustomException.InvalidProfileIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentOrchestrator orchestrator;
    private final AccountService accountService;

    @PostMapping(value = "/run", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamEvent>> run(
            @AuthenticationPrincipal Account account,
            @RequestBody ChatReq req,
            @RequestHeader(name = "X-Profile-Id", required = false, defaultValue = "0") Long profileId) {

        // 1) 블로킹 계층은 boundedElastic로
        Mono<User> userMono = Mono.fromCallable(() -> {
                    User user = accountService.findUserByProfileId(profileId, account.getId());
                    if (user == null) throw new InvalidProfileIdException();
                    return user;
                })
                .subscribeOn(Schedulers.boundedElastic());

        // 2) 세션 정규화는 오케스트레이터에 맡겨도 되지만, 여기서 해도 OK (ThreadLocal 쓰지 말 것)
        String rawSession = req.session();
        String text = req.userText() == null ? "" : req.userText().trim();

        if (text.isEmpty()) {
            return Flux.just(ServerSentEvent.<StreamEvent>builder()
                    .event("agent")
                    .data(StreamEvent.error(EventCode.BAD_REQUEST, "userText가 비어 있습니다."))
                    .build());
        }
        // 3) 유저 로드가 끝난 다음 오케스트레이터 실행
        return userMono.flatMapMany(user ->
                orchestrator.run(
                        rawSession, // 문자열 반환형, ThreadLocal 아님
                        text,
                        user.getId(),
                        user.getNickname()
                ).map(ev -> ServerSentEvent.<StreamEvent>builder().event("agent").data(ev).build())
        );
    }

}

