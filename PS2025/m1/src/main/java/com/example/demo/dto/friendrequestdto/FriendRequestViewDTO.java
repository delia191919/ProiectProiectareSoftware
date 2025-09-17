package com.example.demo.dto.friendrequestdto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestViewDTO{

    private Long id;

    private Long senderId;

    private Long receiverId;

    private String status;
}
