package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ReviewController controller;

    @Test
    void returnsCommentRatingAndCustomerNameForReviews() throws Exception {
        Review review = new Review(7L, 12L, 3L, 5, "Excellent");
        Customer customer = new Customer();
        customer.setId(7L);
        customer.setName("Taylor Customer");
        when(reviewRepository.findByStoreIdAndProductId(3L, 12L)).thenReturn(List.of(review));
        when(customerRepository.findAllById(Set.of(7L))).thenReturn(List.of(customer));

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mockMvc.perform(get("/reviews/3/12").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].customerName").value("Taylor Customer"))
                .andExpect(jsonPath("$.reviews[0].rating").value(5))
                .andExpect(jsonPath("$.reviews[0].comment").value("Excellent"))
                .andExpect(jsonPath("$.reviews[0].review").value("Excellent"));
        mockMvc.perform(get("/URL/reviews/3/12").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].review").value("Excellent"));
    }
}
