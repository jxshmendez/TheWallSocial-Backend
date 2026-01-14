package org.josh.climber.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.josh.climber.DTO.posts.request.CreatePostRequestDTO;
import org.josh.climber.DTO.posts.response.PostDetailDTO;
import org.josh.climber.DTO.posts.response.PostSummaryDTO;
import org.josh.climber.DTOMapper.posts.PostDetailDTOMapper;
import org.josh.climber.DTOMapper.posts.PostSummaryDTOMapper;
import org.josh.climber.model.UserModel;
import org.josh.climber.model.posts.PostLikesModel;
import org.josh.climber.model.posts.PostsModel;
import org.josh.climber.repository.PostLikesRepository;
import org.josh.climber.repository.PostRepository;
import org.josh.climber.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikesRepository postLikesRepository;
    private final PostSummaryDTOMapper postSummaryDTOMapper;
    private final PostDetailDTOMapper postDetailDTOMapper;
    
    /**
     * Get all posts for the social feed (only from users the current user follows)
     */
    public List<PostDetailDTO> getAllPosts(Long currentUserId){
        List<PostsModel> posts = postRepository.findAllByFollowedUsersOrderByCreatedAtDesc(currentUserId);
        return posts.stream()
                .map(post -> postDetailDTOMapper.toDTO(post, currentUserId))
                .toList();
    }

    /**
     * Get all posts by specific user
     */
    public List<PostSummaryDTO> getPostsByUser(Long userId){
        UserModel user  = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return postRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(postSummaryDTOMapper::toDTO)
                .toList();
    }

    /**
     * Get single post by ID
     */
    public PostDetailDTO getPostById(Long postId, Long currentUserId){
        PostsModel post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return postDetailDTOMapper.toDTO(post, currentUserId);
    }

    /**
     * Create a new post
     */
    public PostSummaryDTO createPost(CreatePostRequestDTO dto, String username) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        PostsModel post = PostsModel.builder()
                .caption(dto.caption())
                .visibility(dto.visibility())
                .user(user)
                .build();

        PostsModel saved = postRepository.save(post);
        return postSummaryDTOMapper.toDTO(saved);
    }

    /**
     * Delete a post by ID
     */
    public void deletePost(Long postId, String username){
        PostsModel post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!Objects.equals(post.getUser().getUserId(), user.getUserId())) {
            throw new SecurityException("Not authorised to delete this post");
        }

        postRepository.delete(post);
    }

    /**
     * Like or unlike a post
     */
    public PostDetailDTO toggleLike(Long postId, String username) {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PostsModel post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        boolean alreadyLiked = postLikesRepository.existsByPostsAndUser(post, user);

        if (alreadyLiked) {
            postLikesRepository.deleteByPostsAndUser(post, user);
        } else {
            PostLikesModel like = PostLikesModel.builder()
                    .posts(post)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            postLikesRepository.save(like);
        }

        PostsModel refreshed = postRepository.findById(post.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return postDetailDTOMapper.toDTO(refreshed, user.getUserId());
    }

}
