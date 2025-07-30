package com.oauth2.User.Alarm.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.User.Alarm.Domain.FCMToken;
import com.oauth2.User.Alarm.Repository.FCMTokenRepository;
import com.oauth2.Util.Exception.CustomException.MissingFCMTokenException;
import com.oauth2.User.Alarm.Dto.AlarmMessageConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final FCMTokenRepository fcmTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(AlarmService.class);
    private final AccountRepository accountRepository;

    public void sendMedicationAlarm(String fcmToken, Long id, String alertTitle, String pillName) {
        // FCM 토큰 검증
        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            logger.warn("FCM token is null or empty for dosage log ID: {}", id);
            return;
        }

        Message message = Message.builder()
                .setToken(fcmToken)
                .putData("logId", String.valueOf(id))
                .putData("title", alertTitle)
                .putData("body", pillName + AlarmMessageConstants.MEDICATION_TIME_MESSAGE)
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            logger.error("An error occurred in AlarmService", e);
        }
    }

    public void sendFriendMedicationReminder(
            FCMToken friendFcmToken,
            Long logId,
            String senderNickname,
            String pillName,
            LocalDateTime scheduledTime
    ) {
        if (friendFcmToken.getFCMToken() == null || friendFcmToken.getFCMToken().isEmpty())
            throw new MissingFCMTokenException();

        if (LocalDateTime.now().isBefore(scheduledTime))
            throw new IllegalStateException(AlarmMessageConstants.DOSAGE_TIME_NOT_PASSED);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String timeString = scheduledTime.format(formatter);

        String title = senderNickname + AlarmMessageConstants.FRIEND_WORRY_TITLE_TEMPLATE;
        String body = timeString + AlarmMessageConstants.FRIEND_WORRY_BODY_TEMPLATE + pillName + AlarmMessageConstants.FRIEND_WORRY_BODY_END;

        Message message = Message.builder()
                .setToken(friendFcmToken.getFCMToken())
                .putData("logId", String.valueOf(logId))
                .putData("title", title)
                .putData("body", body)
                .putData("pillName", pillName)
                .putData("from", senderNickname)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            logger.error(AlarmMessageConstants.FRIEND_ALARM_SEND_FAILED, e);
        }
    }


    @Transactional
    public void getToken(Long accountId, String token){
        // 해당 아이디 가진 유저가 존재하는지 검사
        Account account = accountRepository.findById(accountId).orElse(null);
        if(account != null) {
            FCMToken existingToken = fcmTokenRepository.findById(account.getId()).orElse(null);

            if (existingToken != null) {
                // 2. 기존 객체 수정
                existingToken.setFCMToken(token);
                existingToken.setLoggedIn(true);
                fcmTokenRepository.save(existingToken);
            } else {
                // 3. 새로 생성
                FCMToken fcmToken = new FCMToken(token);
                fcmToken.setAccount(account);
                fcmTokenRepository.save(fcmToken);
            }
        }
    }
}
