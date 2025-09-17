package com.example.demo.dto.friendrequestdto;
import lombok.Data;

@Data
public class FriendRequestDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String status;
}