package dev.alexeev.payment_service.controller;

import dev.alexeev.payment_service.dto.CreatePaymentRequest;
import dev.alexeev.payment_service.dto.PageResponse;
import dev.alexeev.payment_service.dto.PaymentResponse;
import dev.alexeev.payment_service.dto.TotalAmountResponse;
import dev.alexeev.payment_service.entity.PaymentStatus;
import dev.alexeev.payment_service.exception.ForbiddenException;
import dev.alexeev.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
  public PageResponse<PaymentResponse> search(
          @RequestParam(name = "userId", required = false) String userId,
          @RequestParam(name = "orderId", required = false) String orderId,
          @RequestParam(name = "status", required = false) PaymentStatus status,
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "size", defaultValue = "20") int size) {
    return paymentService.search(userId, orderId, status, page, size);
  }

  @GetMapping("/total/me")
  public TotalAmountResponse myTotal(
          @RequestHeader(USER_ID_HEADER) String currentUserId,
          @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
          @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return paymentService.totalForUser(currentUserId, from, to);
  }

  @GetMapping("/total")
  public TotalAmountResponse totalForAllUsers(
          @RequestHeader(name = USER_ROLE_HEADER, required = false) String role,
          @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
          @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    if (!ADMIN_ROLE.equalsIgnoreCase(role)) {
      throw new ForbiddenException("Admin role required");
    }
    return paymentService.totalForAllUsers(from, to);
  }
}
