package com.oauth2.User.Friend.Repository;

import com.oauth2.User.Friend.Entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    void deleteAllByFriendId(Long id);
    void deleteAllByUserId(Long id);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);



    List<Friend> findAllByUserId(Long userId);

}
