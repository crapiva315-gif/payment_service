package dev.alexeev.payment_service.mapper;

import dev.alexeev.payment_service.dto.CreatePaymentRequest;
import dev.alexeev.payment_service.dto.PaymentResponse;
import dev.alexeev.payment_service.dto.PaymentStatusDto;
import dev.alexeev.payment_service.entity.Payment;
import dev.alexeev.payment_service.entity.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "timestamp", ignore = true)
  @Mapping(target = "status", ignore = true)
  Payment toEntity(CreatePaymentRequest request);

  PaymentResponse toResponse(Payment payment);

  List<PaymentResponse> toResponses(List<Payment> payments);

  PaymentStatus toEntityStatus(PaymentStatusDto status);
}