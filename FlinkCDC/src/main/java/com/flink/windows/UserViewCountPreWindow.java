package com.flink.windows;

import lombok.*;

/**
 * @Author wangziyu1
 * @Date 2022/11/8 11:42
 * @Version 1.0
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserViewCountPreWindow {

    public String username;
    public long count;
    public long windosStartTime;
    public long windosEndTime;
}
