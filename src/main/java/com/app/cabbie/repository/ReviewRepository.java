// Purpose: Spring Data JPA repository for database operations on Review entities.
// Notes: Provides custom queries to find reviews by reviewer ID or target ID for display and analytics.

package com.app.cabbie.repository;

import com.app.cabbie.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Purpose: Finds all reviews submitted by a specific reviewer (user who gave the rating).
    // Behavior: Returns List<Review>; uses derived query name to filter by reviewer_id.
    List<Review> findByReviewerId(Long reviewerId);

    // Purpose: Retrieves all reviews targeting a specific user (e.g., driver receiving ratings).
    // Behavior: Returns List<Review> via native SQL; filters by target_id for driver/user profile score.
    @Query(value = "select * from reviews where target_id =:target_id;",nativeQuery = true)
    List<Review> findByTargetId(@Param("target_id") Long targetId);
}
