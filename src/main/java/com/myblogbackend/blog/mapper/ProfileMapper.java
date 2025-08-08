package com.myblogbackend.blog.mapper;

import org.springframework.stereotype.Component;

import com.myblogbackend.blog.models.ProfileEntity;
import com.myblogbackend.blog.models.SocialLinks;
import com.myblogbackend.blog.response.UserResponse.ProfileResponseDTO;
import com.myblogbackend.blog.response.UserResponse.SocialLinksDTO;

@Component
public class ProfileMapper {

    public ProfileResponseDTO toProfileResponseDTO(final ProfileEntity profileEntity) {
        if (profileEntity == null) {
            return createEmptyProfileResponse();
        }

        return ProfileResponseDTO.builder()
                .bio(profileEntity.getBio() != null ? profileEntity.getBio() : "")
                .website(profileEntity.getWebsite() != null ? profileEntity.getWebsite() : "")
                .location(profileEntity.getLocation() != null ? profileEntity.getLocation() : "")
                .avatarUrl(profileEntity.getAvatarUrl() != null ? profileEntity.getAvatarUrl() : "")
                .social(toSocialLinksDTO(profileEntity.getSocial()))
                .build();
    }

    private SocialLinksDTO toSocialLinksDTO(final SocialLinks socialLinks) {
        if (socialLinks == null) {
            return createEmptySocialLinksDTO();
        }

        return SocialLinksDTO.builder()
                .twitter(socialLinks.getTwitter() != null ? socialLinks.getTwitter() : "")
                .linkedin(socialLinks.getLinkedin() != null ? socialLinks.getLinkedin() : "")
                .github(socialLinks.getGithub() != null ? socialLinks.getGithub() : "")
                .build();
    }

    private ProfileResponseDTO createEmptyProfileResponse() {
        return ProfileResponseDTO.builder()
                .bio("")
                .website("")
                .location("")
                .avatarUrl("")
                .social(createEmptySocialLinksDTO())
                .build();
    }

    private SocialLinksDTO createEmptySocialLinksDTO() {
        return SocialLinksDTO.builder()
                .twitter("")
                .linkedin("")
                .github("")
                .build();
    }
}
