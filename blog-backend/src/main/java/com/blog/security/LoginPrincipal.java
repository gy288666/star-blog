package com.blog.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 认证后的当前用户信息（SecurityContext principal）。 */
@Data
@AllArgsConstructor
public class LoginPrincipal {

    private Long id;
    private String username;
    private String nickname;
    private String role;
}
