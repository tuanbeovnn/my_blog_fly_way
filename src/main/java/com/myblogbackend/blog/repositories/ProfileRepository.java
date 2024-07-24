package com.myblogbackend.blog.repositories;

import com.myblogbackend.blog.models.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {


}
