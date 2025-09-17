package com.example.demo.repository;

import com.example.demo.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findBySenderIdAndStatus(Long senderId, FriendRequest.Status status);
    List<FriendRequest> findByReceiverIdAndStatus(Long receiverId, FriendRequest.Status status);
    Optional<FriendRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
}