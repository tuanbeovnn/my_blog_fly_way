package com.myblogbackend.blog.repositories;


import com.myblogbackend.blog.models.UserDeviceFireBaseTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FirebaseUserRepository extends JpaRepository<UserDeviceFireBaseTokenEntity, UUID> {

}
