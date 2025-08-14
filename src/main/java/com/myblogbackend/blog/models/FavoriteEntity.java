package com.myblogbackend.blog.models;


import com.myblogbackend.blog.enums.FavoriteObjectType;
import com.myblogbackend.blog.enums.RatingType;
import com.myblogbackend.blog.models.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteEntity extends BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    private PostEntity post;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = true)
    private CommentEntity  comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FavoriteObjectType objectType = FavoriteObjectType.POST;
}

// rate limit - user  like post/comment
// do not allow user : too many requests if user sends like post/comment continuously
// create another class
