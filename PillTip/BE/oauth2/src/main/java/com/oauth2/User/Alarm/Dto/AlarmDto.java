package com.oauth2.User.Alarm.Dto;

public record AlarmDto (
  Long userId,
  String FCMToken
) {}
