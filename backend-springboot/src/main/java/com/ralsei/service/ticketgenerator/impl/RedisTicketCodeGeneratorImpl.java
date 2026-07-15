package com.ralsei.service.ticketgenerator.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ralsei.repository.PassengerTicketRepository;
import com.ralsei.service.ticketgenerator.TicketCodeGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Provides the redis ticket code generator impl component for the application.
 */
public class RedisTicketCodeGeneratorImpl implements TicketCodeGenerator {

    private static final DateTimeFormatter DATE_PREFIX_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final String TICKET_SEQUENCE_KEY_PREFIX = "ticket_seq:";
    private static final int MAX_GENERATION_ATTEMPTS = 100;

    private final StringRedisTemplate redisTemplate;
    private final PassengerTicketRepository passengerTicketRepository;

    @Override
    /**
     * Executes the generate passenger ticket code operation.
     *
     * @return the operation result
     */
    public String generatePassengerTicketCode() {
        String datePrefix = LocalDate.now().format(DATE_PREFIX_FORMATTER);
        String redisKey = TICKET_SEQUENCE_KEY_PREFIX + datePrefix;

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            Long sequence = redisTemplate.opsForValue().increment(redisKey);
            if (sequence == null) {
                throw new IllegalStateException("Cannot generate passenger ticket code");
            }

            if (sequence == 1L) {
                redisTemplate.expire(redisKey, ttlUntilAfterMidnight());
            }

            String ticketCode = String.format("PA%s%04d", datePrefix, sequence);
            if (!passengerTicketRepository.existsByTicketCode(ticketCode)) {
                return ticketCode;
            }
        }

        throw new IllegalStateException("Cannot generate unique passenger ticket code");
    }

    private Duration ttlUntilAfterMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDay = now.toLocalDate().plusDays(1).atStartOfDay().plusHours(1);
        return Duration.between(now, nextDay);
    }
}
