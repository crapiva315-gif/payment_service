package dev.alexeev.payment_service.controller;

import dev.alexeev.payment_service.dto.CreatePaymentRequest;
import dev.alexeev.payment_service.dto.DateRangeRequest;
import dev.alexeev.payment_service.dto.PageResponse;
import dev.alexeev.payment_service.dto.PaymentResponse;
import dev.alexeev.payment_service.dto.PaymentSearchRequest;
import dev.alexeev.payment_service.dto.TotalAmountResponse;
import dev.alexeev.payment_service.exception.ForbiddenException;
import dev.alexeev.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  static final String USER_ID_HEADER = "X-User-Id";
  static final String USER_ROLE_HEADER = "X-User-Role";
  private static final String ADMIN_ROLE = "ADMIN";

  private final PaymentService paymentService;

  @PostMapping
  public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
  }

  @GetMapping
  public PageResponse<PaymentResponse> search(@Valid PaymentSearchRequest request) {
    return paymentService.search(request);
  }

  @GetMapping("/total/me")
  public TotalAmountResponse myTotal(
          @RequestHeader(USER_ID_HEADER) String currentUserId,
          @Valid DateRangeRequest range) {
    return paymentService.totalForUser(currentUserId, range);
  }

  @GetMapping("/total")
  public TotalAmountResponse totalForAllUsers(
          @RequestHeader(name = USER_ROLE_HEADER, required = false) String role,
          @Valid DateRangeRequest range) {
    if (!ADMIN_ROLE.equalsIgnoreCase(role)) {
      throw new ForbiddenException("Admin role required");
    }
    return paymentService.totalForAllUsers(range);
  }
}