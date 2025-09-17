package com.example.demo.service;

import com.example.demo.builder.userbuilder.FriendRequestBuilder;
import com.example.demo.dto.friendrequestdto.FriendRequestDTO;
import com.example.demo.entity.FriendRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.FriendRequestRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;


    public String sendFriendRequest(FriendRequestDTO friendRequestDTO) throws Exception {
        Optional<User> sender = userRepository.findById(friendRequestDTO.getSenderId());
        if (sender.isEmpty()) {
            throw new Exception("Sender not found with id: " + friendRequestDTO.getSenderId());
        }

        Optional<User> receiver = userRepository.findById(friendRequestDTO.getReceiverId());
        if (receiver.isEmpty()) {
            throw new Exception("Receiver not found with id: " + friendRequestDTO.getReceiverId());
        }

        FriendRequest saveFriendRequest = FriendRequestBuilder.generateFriendRequestFromDTO(friendRequestDTO,
                FriendRequest.Status.PENDING, sender.get(), receiver.get());
        friendRequestRepository.save(saveFriendRequest);

        return "Friend request sent successfully with id: " + saveFriendRequest.getId();
    }

    public String acceptFriendRequest(FriendRequestDTO friendRequestDTO) throws Exception {
        Optional<FriendRequest> friendRequestOpt = friendRequestRepository.findBySenderIdAndReceiverId(
                friendRequestDTO.getSenderId(), friendRequestDTO.getReceiverId());
        if (friendRequestOpt.isEmpty())
        {
            throw new Exception("Friend request not found");
        }

        FriendRequest friendRequest = friendRequestOpt.get();
        if (!friendRequest.getSender().getId().equals(friendRequestDTO.getSenderId()))
        {
            throw new Exception("Sender ID does not match");
        }

        if ("accepted".equalsIgnoreCase(friendRequestDTO.getStatus()))
        {
            friendRequest.setStatus(FriendRequest.Status.ACCEPTED);
            friendRequestRepository.save(friendRequest);
            return "Friend request accepted";
        }
        else if ("rejected".equalsIgnoreCase(friendRequestDTO.getStatus()))
        {
            friendRequest.setStatus(FriendRequest.Status.REJECTED);
            friendRequestRepository.save(friendRequest);
            return "Friend request rejected";
        }
        else
        {
            return "Invalid status";
        }
    }

    public List<String> getFriends(Long userId) {
        List<FriendRequest> sentRequests = friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequest.Status.ACCEPTED);
        List<FriendRequest> receivedRequests = friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.Status.ACCEPTED);

        List<String> friends = sentRequests.stream()
                .map(request -> userRepository.findById(request.getReceiver().getId())
                        .map(User::getName)
                        .orElse("Unknown"))
                .collect(Collectors.toList());

        friends.addAll(receivedRequests.stream()
                .map(request -> userRepository.findById(request.getSender().getId())
                        .map(User::getName)
                        .orElse("Unknown"))
                .toList());

        return friends;
    }

    public List<Long> getFriendsId(Long userId) {
        List<FriendRequest> sentRequests = friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequest.Status.ACCEPTED);
        List<FriendRequest> receivedRequests = friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequest.Status.ACCEPTED);

        List<Long> friends = sentRequests.stream()
                .map(request -> request.getReceiver().getId())
                .collect(Collectors.toList());

        friends.addAll(receivedRequests.stream()
                .map(request -> request.getSender().getId())
                .collect(Collectors.toList()));

        return friends;
    }
}