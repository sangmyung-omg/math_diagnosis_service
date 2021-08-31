package com.tmax.WaplMath.Recommend.event.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDeleteEvent {
    private String userID;
}
