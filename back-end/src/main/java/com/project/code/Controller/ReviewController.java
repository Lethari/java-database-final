package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/reviews", "/URL/reviews"})
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;

    public ReviewController(ReviewRepository reviewRepository, CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{storeId}/{productId}")
    public Map<String, List<ReviewResponse>> getReviews(
            @PathVariable long storeId,
            @PathVariable long productId) {
        List<Review> reviews = reviewRepository.findByStoreIdAndProductId(storeId, productId);
        Set<Long> customerIds = reviews.stream()
                .map(Review::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Customer> customers = customerRepository.findAllById(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Function.identity()));

        List<ReviewResponse> response = reviews.stream()
                .map(review -> {
                    Customer customer = customers.get(review.getCustomerId());
                    return new ReviewResponse(
                            customer == null ? null : customer.getName(),
                            review.getRating(),
                            review.getComment(),
                            review.getComment()
                    );
                })
                .toList();
        return Map.of("reviews", response);
    }

    public record ReviewResponse(String customerName, Integer rating, String comment, String review) {
    }
}
