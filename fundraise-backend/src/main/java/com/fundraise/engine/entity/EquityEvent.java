package com.fundraise.engine.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "equity_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String roundName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentType instrumentType;

    @Column(nullable = false)
    private Long sharesIssued;

    private BigDecimal pricePerShare;

    @Column(nullable = false)
    private LocalDate eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    private Document sourceDocument;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum InstrumentType {
        COMMON, PREFERRED_A, PREFERRED_B, ESOP_POOL, CONVERTIBLE_NOTE, SAFE
    }
}
