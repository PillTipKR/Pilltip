package com.oauth2.Account.Dto;

import com.oauth2.Account.Entity.Account;
import com.oauth2.User.UserInfo.Entity.User;

public record CreateUserDto(
   Account account,
   User user
) {}
