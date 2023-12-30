//package com.myblogbackend.blog.repositories;
//
//import com.myblogbackend.blog.models.UserDeviceEntity;
//import com.myblogbackend.blog.models.UserEntity;
//import com.myblogbackend.blog.models.UserVerificationTokenEntity;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.UUID;
//
//public interface TokenRepository extends JpaRepository<UserVerificationTokenEntity, UUID> {
//    UserVerificationTokenEntity findByVerificationToken(String verToken);
//
//    UserVerificationTokenEntity findByUser(UserEntity userEntity);
//}
