package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.MessageEventFailedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageEventFailedRepository extends JpaRepository<MessageEventFailedEntity, UUID> {


}
