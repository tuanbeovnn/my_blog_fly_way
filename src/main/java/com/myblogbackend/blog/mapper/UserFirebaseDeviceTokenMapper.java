package com.myblogbackend.blog.mapper;

import com.myblogbackend.blog.models.UserDeviceFireBaseTokenEntity;
import com.myblogbackend.blog.request.UserFirebaseDeviceRequest;
import com.myblogbackend.blog.response.UserFirebaseDeviceResponse;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserFirebaseDeviceTokenMapper {
    UserDeviceFireBaseTokenEntity toUserFirebaseDeviceTokenEntity(UserFirebaseDeviceRequest userFirebaseDeviceRequest);


    UserFirebaseDeviceResponse toUserFirebaseDeviceTokenResponse(UserDeviceFireBaseTokenEntity userDeviceFireBaseTokenEntity);
}

