package com.fundraise.engine.rules;

import com.fundraise.engine.entity.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Context object passed to each ComplianceRule during evaluation.
 * Contains all the data a rule might need to make its determination.
 */
@Data
@Builder
public class ComplianceContext {

    private Company company;
    private List<EquityEvent> equityEvents;
    private List<EsopGrant> esopGrants;
    private List<ShareClass> shareClasses;
    private List<Document> documents;

    /**
     * Calculate total shares issued across all equity events
     */
    public long getTotalSharesIssued() {
        return equityEvents.stream()
                .mapToLong(EquityEvent::getSharesIssued)
                .sum();
    }

    /**
     * Calculate total ESOP shares (both board-approved and informal)
     */
    public long getTotalEsopShares() {
        return esopGrants.stream()
                .mapToLong(EsopGrant::getShares)
                .sum();
    }

    /**
     * Calculate total board-approved ESOP shares
     */
    public long getApprovedEsopShares() {
        return esopGrants.stream()
                .filter(EsopGrant::getBoardApproved)
                .mapToLong(EsopGrant::getShares)
                .sum();
    }

    /**
     * Get informal (not board-approved) ESOP grants
     */
    public List<EsopGrant> getInformalEsopGrants() {
        return esopGrants.stream()
                .filter(g -> !g.getBoardApproved())
                .toList();
    }

    /**
     * Calculate total shares from share classes
     */
    public long getTotalSharesFromClass() {
        return shareClasses.stream()
                .filter(sc -> sc.getTotalShares() != null)
                .mapToLong(ShareClass::getTotalShares)
                .sum();
    }

    /**
     * Check if company has DPIIT recognition
     */
    public boolean hasDpiitRecognition() {
        return company.getDpiitStatus() == Company.DpiitStatus.RECOGNIZED;
    }

    /**
     * Check if company has foreign investment documents
     */
    public boolean hasForeignInvestment() {
        return equityEvents.stream()
                .anyMatch(e -> e.getInstrumentType() == EquityEvent.InstrumentType.PREFERRED_A
                        || e.getInstrumentType() == EquityEvent.InstrumentType.PREFERRED_B
                        || e.getInstrumentType() == EquityEvent.InstrumentType.CONVERTIBLE_NOTE
                        || e.getInstrumentType() == EquityEvent.InstrumentType.SAFE);
    }
}
