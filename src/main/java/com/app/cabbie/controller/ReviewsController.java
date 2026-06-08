// Purpose: REST API controller for managing ride reviews and ratings from passengers and drivers.
// Notes: Handles review creation, retrieval by driver/admin with role-based access control.

package com.app.cabbie.controller;

import com.app.cabbie.dto.RatingDTO;
import com.app.cabbie.model.Review;
import com.app.cabbie.service.ReviewAndRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewsController {


     private final ReviewAndRatingService ratingService;

     // Purpose: Creates a new review/rating for a ride participant (passenger or driver).
     // Behavior: Validates user role, delegates to service, returns CREATED on success or INTERNAL_SERVER_ERROR on failure.
     @PostMapping("/create")
     @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
     public ResponseEntity<?> createReview(@RequestBody RatingDTO dto){
        try {
            ratingService.createReview(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Review Created");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while creating review:"+e);
        }
     }

     // Purpose: Retrieves all reviews/ratings submitted for a specific driver.
     // Behavior: Confirms driver ownership via Spring Security, returns list of Review objects or error on failure.
     @GetMapping("/driver/{driverId}")
     @PreAuthorize("hasAnyRole('DRIVER','ADMIN') and #driverId==principal.id")
     public ResponseEntity<?> getAllReviewsByDriverId(@PathVariable Long driverId){
        try {
            List<Review> allReviewsOfDriver =ratingService.getDriverReviews(driverId);
            return ResponseEntity.status(HttpStatus.OK).body(allReviewsOfDriver);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while getting reviews:"+e);
        }
     }


     // Purpose: Retrieves all reviews in the system (admin-only endpoint).
     // Behavior: Enforces ADMIN role, returns complete list of Review objects or error on exception.
     @GetMapping
     @PreAuthorize("hasAnyRole('ADMIN')")
     public ResponseEntity<?> getAllReviews(){
        try {
            List<Review> allReviews =ratingService.getAllReviews();
            return ResponseEntity.status(HttpStatus.OK).body(allReviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while getting reviews:"+e);
        }
    }

}
