package com.oauth2.Account.Service;

import com.oauth2.Account.Entity.Account;
import com.oauth2.Account.Repository.AccountRepository;
import com.oauth2.User.UserInfo.Entity.User;
import com.oauth2.User.UserInfo.Dto.UserListDto;
import com.oauth2.Util.Exception.CustomException.InvalidProfileIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public User findUserByProfileId(Long profileId, Long accountId) throws AccessDeniedException {
        Account account = accountRepository.findByIdWithUsers(accountId)
                .orElseThrow(InvalidProfileIdException::new);

        User user;
        if (profileId == 0) {
            user = account.getUsers().stream()
                    .filter(User::isMain)
                    .findFirst()
                    .orElseThrow(InvalidProfileIdException::new);
        } else {
            user = findUserByProfileIdPri(profileId, accountId);
        }

        return user;
    }


    @Transactional(readOnly = true)
    public User findUserByProfileIdPri(Long profileId, Long accountId) {
        Account account = accountRepository.findByIdWithUsers(accountId)
                .orElseThrow(InvalidProfileIdException::new);

        return account.getUsers().stream()
                .filter(u -> u.getId().equals(profileId))
                .findFirst()
                .orElseThrow(InvalidProfileIdException::new);
    }


    @Transactional
    public List<UserListDto> getUserList(Long accountId, Long profileId){
        List<UserListDto> userListDtos = new ArrayList<>();
        Account account = accountRepository.findByIdWithUsers(accountId).orElse(null);

        assert account != null;
        for (User users : account.getUsers()) {
            userListDtos.add(
                    new UserListDto(
                            users.getId(),
                            users.getNickname(),
                            users.getUserProfile() != null ? users.getUserProfile().getAge() : null,
                            users.getUserProfile() != null && users.getUserProfile().getGender() != null ? users.getUserProfile().getGender().name() : null,
                            users.getUserProfile() != null && users.getUserProfile().getBirthDate() != null ? users.getUserProfile().getBirthDate().toString() : null,
                            users.isMain(),
                            users.getId().equals(profileId)
                    )
            );
        }
        return userListDtos.stream()
                .sorted(Comparator.comparing(UserListDto::userId))
                .toList();
    }
}
