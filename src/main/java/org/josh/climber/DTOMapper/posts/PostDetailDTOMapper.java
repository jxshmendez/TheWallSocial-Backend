package org.josh.climber.DTOMapper.posts;

import org.josh.climber.DTO.posts.response.PostDetailDTO;
import org.josh.climber.DTO.route.response.RoutePreviewDTO;
import org.josh.climber.DTO.session.response.SessionPreviewDTO;
import org.josh.climber.model.posts.MediaType;
import org.josh.climber.model.posts.PostsModel;
import org.springframework.stereotype.Component;

@Component
public class PostDetailDTOMapper {

    public PostDetailDTO toDTO(PostsModel post, Long currentUserId) {
        if (post == null) return null;

        // Get first media item for preview
        String mediaPreviewUrl = null;
        MediaType primaryMediaType = null;
        Boolean hasVideo = false;
        Boolean hasMultipleMedia = false;
        
        if (post.getMediaList() != null && !post.getMediaList().isEmpty()) {
            var firstMedia = post.getMediaList().get(0);
            mediaPreviewUrl = firstMedia.getThumbnailUrl() != null 
                ? firstMedia.getThumbnailUrl() 
                : firstMedia.getMediaUrl();
            primaryMediaType = firstMedia.getMedia();
            hasVideo = firstMedia.getMedia() == MediaType.VIDEO;
            hasMultipleMedia = post.getMediaList().size() > 1;
        }

        // Author info
        Long authorId = post.getUser() != null ? post.getUser().getUserId() : null;
        String authorUsername = post.getUser() != null ? post.getUser().getUsername() : null;
        String authorAvatarUrl = post.getUser() != null ? post.getUser().getAvatarUrl() : null;

        // Session preview
        SessionPreviewDTO session = null;
        if (post.getSession() != null) {
            session = new SessionPreviewDTO(
                post.getSession().getSessionId(),
                post.getSession().getSessionDate(),
                post.getSession().getDurationMinutes()
            );
        }

        // Route preview
        RoutePreviewDTO route = null;
        if (post.getRoutes() != null) {
            route = new RoutePreviewDTO(
                post.getRoutes().getRouteId(),
                post.getRoutes().getName(),
                post.getRoutes().getGrade()
            );
        }

        // Engagement stats
        Integer likeCount = post.getPostLikes() != null ? post.getPostLikes().size() : 0;
        Integer commentCount = post.getPostComments() != null 
            ? (int) post.getPostComments().stream().filter(c -> !c.isDeleted()).count() 
            : 0;

        // Check if current user liked this post
        Boolean isLikedByCurrentUser = false;
        if (currentUserId != null && post.getPostLikes() != null) {
            isLikedByCurrentUser = post.getPostLikes().stream()
                .anyMatch(like -> like.getUser() != null && 
                    like.getUser().getUserId() == currentUserId);
        }

        return new PostDetailDTO(
            post.getPostId(),
            post.getCaption(),
            post.getVisibility(),
            post.getCreatedAt(),
            mediaPreviewUrl,
            primaryMediaType,
            authorId,
            authorUsername,
            authorAvatarUrl,
            session,
            route,
            likeCount,
            commentCount,
            hasVideo,
            hasMultipleMedia,
            isLikedByCurrentUser
        );
    }

    // Overload without currentUserId for when user context isn't available
    public PostDetailDTO toDTO(PostsModel post) {
        return toDTO(post, null);
    }
}
