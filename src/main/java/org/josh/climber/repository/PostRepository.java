package org.josh.climber.repository;

import org.josh.climber.model.UserModel;
import org.josh.climber.model.posts.PostsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PostRepository extends JpaRepository<PostsModel, Long> {
    List<PostsModel> findAllByOrderByCreatedAtDesc();
    List<PostsModel> findAllByUserOrderByCreatedAtDesc(UserModel user);

    @Query("SELECT p FROM PostsModel p " +
           "WHERE p.user.userId IN " +
           "(SELECT f.following.userId FROM FollowModel f WHERE f.follower.userId = :userId) " +
           "ORDER BY p.createdAt DESC")
    List<PostsModel> findAllByFollowedUsersOrderByCreatedAtDesc(@Param("userId") Long userId);

}
