package org.josh.climber.controller;

import lombok.RequiredArgsConstructor;
import org.josh.climber.DTO.posts.request.CreatePostRequestDTO;
import org.josh.climber.DTO.posts.response.PostDetailDTO;
import org.josh.climber.DTO.posts.response.PostSummaryDTO;
import org.josh.climber.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    /**
     * Get all posts for the social feed (only from users the current user follows)
     * Requires authentication - only logged in users can view posts
     */
    @GetMapping
    public ResponseEntity<List<PostDetailDTO>> getAllPosts() {
        Long currentUserId = (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getDetails();
        List<PostDetailDTO> posts = postService.getAllPosts(currentUserId);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get posts by a specific user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostSummaryDTO>> getPostsByUser(@PathVariable Long userId) {
        List<PostSummaryDTO> posts = postService.getPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }

    /**
     * Get a single post by ID
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailDTO> getPostById(@PathVariable Long postId) {
        Long currentUserId = (Long) SecurityContextHolder.getContext()
                .getAuthentication()
                .getDetails();
        
        PostDetailDTO post = postService.getPostById(postId, currentUserId);
        return ResponseEntity.ok(post);
    }

    /**
     * Create a new post
     */
    @PostMapping
    public ResponseEntity<PostSummaryDTO> createPost(@RequestBody CreatePostRequestDTO createPostDTO, Principal principal) {
        // principal.getName() = username from JWT
        PostSummaryDTO post = postService.createPost(createPostDTO, principal.getName());
        return ResponseEntity.ok(post);
    }

    /**
     * Delete a post by ID (only author or admin can delete)
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Principal principal) {
        postService.deletePost(postId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    /**
     * Like or unlike a post
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<PostDetailDTO> toggleLike(@PathVariable Long postId, Principal principal) {
        PostDetailDTO post = postService.toggleLike(postId, principal.getName());
        return ResponseEntity.ok(post);
    }
}
