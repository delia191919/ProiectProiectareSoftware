package com.example.demo.builder.userbuilder;

import com.example.demo.dto.friendrequestdto.FriendRequestDTO;
import com.example.demo.entity.FriendRequest;
import com.example.demo.entity.User;

import java.time.LocalDateTime;

public class FriendRequestBuilder {

    public static FriendRequest generateFriendRequestFromDTO(FriendRequestDTO friendRequestDTO,
                                                             FriendRequest.Status status,
                                                             User sender, User receiver) {
        return FriendRequest.builder()
                .id(friendRequestDTO.getId())
                .sender(sender)
                .receiver(receiver)
                .status(status)
                .timeStamp(LocalDateTime.now())
                .build();
    }
}