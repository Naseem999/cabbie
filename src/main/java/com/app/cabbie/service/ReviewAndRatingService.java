package com.app.cabbie.service;

import com.app.cabbie.dto.KafkaEventDTO;
import com.app.cabbie.dto.RatingDTO;
import com.app.cabbie.model.Driver;
import com.app.cabbie.model.Review;
import com.app.cabbie.model.User;
import com.app.cabbie.repository.DriverRepository;
import com.app.cabbie.repository.ReviewRepository;
import com.app.cabbie.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewAndRatingService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final KafkaProducerService producerService;

    // Persist a new review and notify the target user via Kafka.
    // Builds a Review from RatingDTO, saves it and publishes a KafkaEventDTO to inform the target.
    @Transactional
    public String createReview(RatingDTO dto){
        User reviewer=userRepository.findById(dto.getReviewerId()).orElseThrow(()-> new RuntimeException("User Not Found for Reviewer Id."));
        User targetUser=userRepository.findById(dto.getTargetId()).orElseThrow(()-> new RuntimeException("User Not Found for Target Id."));

        try {
            Review review= Review.builder()
                    .reviewerId(reviewer)
                    .targetId(targetUser)
                    .rating(dto.getRating())
                    .comment(dto.getComment())
                    .build();
            Review savedReview=reviewRepository.save(review);
            KafkaEventDTO kafkaEventDTO= KafkaEventDTO.builder()
                    .userEmail(savedReview.getTargetId().getEmail())
                    .userId(savedReview.getReviewerId().getId())
                    .title("New review received")
                    .message(savedReview.getReviewerId().getName()+" gave " +savedReview.getRating()+"-star review to you.")
                    .build();
            producerService.sendRideNotification(kafkaEventDTO);
            return "review Created";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    // Retrieve reviews for a given driver by resolving the driver's user id.
    // Looks up the driver->user mapping then returns reviews where targetId equals that user id.
    public List<Review> getDriverReviews(Long driverId){
        try {
            Long userId=driverRepository.findUserIdByDriverId(driverId);
            return reviewRepository.findByTargetId(userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Return all reviews stored in the system (admin/analytics use-cases).
    // Delegates to the ReviewRepository and preserves existing RuntimeException semantics.
    public List<Review> getAllReviews(){
        try {
            return reviewRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
