package com.myblogbackend.blog.services;

import java.util.UUID;

public interface FollowerService {
    void persistOrDelete(UUID userId);
}
