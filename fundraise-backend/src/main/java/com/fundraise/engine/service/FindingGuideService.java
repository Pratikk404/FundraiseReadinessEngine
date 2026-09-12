package com.fundraise.engine.service;

import com.fundraise.engine.dto.FindingGuideDto;
import com.fundraise.engine.entity.FindingGuide;
import com.fundraise.engine.repository.FindingGuideRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindingGuideService {

    private final FindingGuideRepository findingGuideRepository;

    @PostConstruct
    public void seedGuides() {
        if (findingGuideRepository.count() > 0) {
            log.info("Finding guides already seeded, skipping...");
            return;
        }

        log.info("Seeding finding guides for 5 compliance rules...");

        findingGuideRepository.saveAll(List.of(
            FindingGuide.builder()
                .ruleId("DILUTION_SUM_100")
                .whyItMatters("Investors verify that total equity across all funding rounds sums to exactly 100%. If it doesn't, the cap table is considered unreliable and this is an immediate red flag during diligence. Most investors will not proceed until this is resolved.")
                .howToFix(List.of(
                    "Export your current cap table and list every equity event since incorporation",
                    "Sum all shares issued: founders' shares + investor shares + ESOP pool + any conversions",
                    "If the total is less than 100%, check for missing allocations — common causes are unrecorded ESOP top-ups, forgotten angel investments, or convertible notes that haven't been converted",
                    "If the total is more than 100%, you likely have duplicate entries or incorrect share counts",
                    "Update the cap table with the correct allocations",
                    "Re-run the compliance check to verify the fix"
                ))
                .whatGoodLooksLike("Example: Founders 70% + Seed Round 20% + ESOP Pool 10% = 100%. Every equity event is recorded, and the math adds up exactly.")
                .effortLevel(FindingGuide.EffortLevel.QUICK_FIX)
                .estimatedTime("30 minutes")
                .needsAdvisor(false)
                .relatedCases(List.of(
                    "InnovateHub Technologies had dilution summing to 94% — missing 6% ESOP allocation that was promised verbally but never recorded",
                    "FinServ Solutions had a similar gap from unrecorded convertible notes"
                ))
                .build(),

            FindingGuide.builder()
                .ruleId("DPIIT_RECOGNITION")
                .whyItMatters("DPIIT recognition is required to claim angel tax exemption under Section 56(2)(viib) of the Income Tax Act. Without it, any investment above fair market value is taxed as income — this can add crores to your tax liability and is a deal-killer for most investors.")
                .howToFix(List.of(
                    "Check if your company is registered on the DPIIT Startup India portal (https://www.startupindia.gov.in)",
                    "If not registered: apply for DPIIT recognition through the Startup India portal — you need Certificate of Incorporation, PAN, and a brief description of your innovation",
                    "Processing time is typically 2-4 weeks",
                    "Once recognized, ensure your recognition certificate is up to date and not lapsed",
                    "If recognition has lapsed: reapply through the portal with updated documents",
                    "After recognition, file for angel tax exemption under Section 56(2)(viib) before receiving investment"
                ))
                .whatGoodLooksLike("Your company has active DPIIT recognition, the certificate is valid, and you've applied for angel tax exemption under Section 56(2)(viib).")
                .effortLevel(FindingGuide.EffortLevel.NEEDS_ADVISOR)
                .estimatedTime("3-4 weeks")
                .needsAdvisor(true)
                .relatedCases(List.of(
                    "GreenEnergy India was missing DPIIT recognition — they applied through Startup India portal, received recognition in 3 weeks, then proceeded with their angel round"
                ))
                .build(),

            FindingGuide.builder()
                .ruleId("ESOP_CONSISTENCY")
                .whyItMatters("Informal ESOP promises (verbal, email, or WhatsApp) that aren't reflected in the cap table create legal liability. If you promise someone 1% of the company but it's not in the cap table, investors will flag this as a governance issue. It also means your actual dilution is higher than what your cap table shows.")
                .howToFix(List.of(
                    "Review all ESOP promises — check emails, Slack messages, offer letters, and verbal commitments",
                    "For each promise, determine: was it approved by the board? Is it in the ESOP pool? Is there a formal grant letter?",
                    "For board-approved grants not in the cap table: add them to the ESOP pool allocation",
                    "For informal promises: decide whether to formalize (add to cap table + issue grant letter) or retract (communicate to the employee)",
                    "If formalizing: pass a board resolution approving the ESOP grants, update the cap table, and issue formal grant letters",
                    "Update the ESOP cap table to match the board-approved pool size"
                ))
                .whatGoodLooksLike("All ESOP grants are board-approved, reflected in the cap table, and have formal grant letters. The ESOP pool size matches the sum of all individual grants.")
                .effortLevel(FindingGuide.EffortLevel.MODERATE)
                .estimatedTime("1-2 weeks")
                .needsAdvisor(false)
                .relatedCases(List.of(
                    "EduLearn Platforms had 3 informal ESOP promises (Marketing Head, Senior Engineer, Product Manager) — they formalized all 3 via board resolution and updated the cap table",
                    "InnovateHub Technologies had a CTO candidate with a verbal 0.6% ESOP promise — they chose to retract after learning the legal implications"
                ))
                .build(),

            FindingGuide.builder()
                .ruleId("SHARE_CLASS_CONSISTENCY")
                .whyItMatters("If your side letters or investor agreements promise different rights than what's in your incorporation documents, investors will flag this as a governance risk. It means your legal docs are inconsistent, which can lead to disputes and delays during fundraising.")
                .howToFix(List.of(
                    "Compare your share classes as declared in the incorporation documents (MOA/AOA) against what's in your cap table",
                    "Check side letters and investor agreements for any rights that differ from the declared share classes",
                    "If you have a share class in the cap table that isn't declared in the incorporation docs: either declare it in the MOA/AOA or remove it from the cap table",
                    "If side letters promise different rights: update the side letters to match the incorporation docs, or amend the MOA/AOA to reflect the promised rights",
                    "For any changes to share class structure, you'll need a board resolution and possibly shareholder approval",
                    "Engage your company secretary or legal advisor for the amendment process"
                ))
                .whatGoodLooksLike("Every share class in the cap table (Ordinary, Preferred_A, ESOP Pool, etc.) is declared in the MOA/AOA with matching rights descriptions. Side letters don't contradict the incorporation docs.")
                .effortLevel(FindingGuide.EffortLevel.MODERATE)
                .estimatedTime("1-2 weeks")
                .needsAdvisor(false)
                .relatedCases(List.of(
                    "FinServ Solutions had Preferred_B shares in events but not declared in docs — they amended the MOA to include the class",
                    "A Series A company had a side letter promising anti-dilution rights not in the SHA — they updated the SHA to match"
                ))
                .build(),

            FindingGuide.builder()
                .ruleId("VALUATION_CONSISTENCY")
                .whyItMatters("If the price per share in your cap table doesn't match the declared round valuation, investors will assume either the math is wrong or there's hidden dilution. This kills trust in the cap table and can delay or derail a fundraise.")
                .howToFix(List.of(
                    "For each funding round, verify: Price Per Share × Total Shares = Round Valuation",
                    "If the numbers don't match, check for: rounding errors, missing shares, or incorrect price per share",
                    "Common cause: the valuation was rounded during the round but the cap table uses the exact number — update the cap table to match the agreed valuation",
                    "Another cause: additional shares were issued after the round (bridge notes, convertible instruments) — update the round valuation to reflect the fully diluted cap table",
                    "Update the cap table with the corrected price per share or valuation",
                    "Re-run the compliance check to verify the fix"
                ))
                .whatGoodLooksLike("For every funding round, Price Per Share × Fully Diluted Shares = Round Valuation. The math checks out exactly, and there are no unexplained discrepancies.")
                .effortLevel(FindingGuide.EffortLevel.QUICK_FIX)
                .estimatedTime("20 minutes")
                .needsAdvisor(false)
                .relatedCases(List.of(
                    "A Seed-stage company had ₹15/share in cap table but ₹12.50/share based on ₹2Cr valuation — they had rounded up during the round, corrected the cap table"
                ))
                .build()
        ));

        log.info("Seeded 5 finding guides successfully");
    }

    public Optional<FindingGuideDto> getGuideForRule(String ruleId) {
        return findingGuideRepository.findByRuleId(ruleId)
                .map(this::toDto);
    }

    public Map<String, FindingGuideDto> getAllGuides() {
        return findingGuideRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(
                        FindingGuide::getRuleId,
                        this::toDto
                ));
    }

    private FindingGuideDto toDto(FindingGuide guide) {
        return FindingGuideDto.builder()
                .ruleId(guide.getRuleId())
                .whyItMatters(guide.getWhyItMatters())
                .howToFix(guide.getHowToFix())
                .whatGoodLooksLike(guide.getWhatGoodLooksLike())
                .effortLevel(guide.getEffortLevel().name())
                .estimatedTime(guide.getEstimatedTime())
                .needsAdvisor(guide.isNeedsAdvisor())
                .relatedCases(guide.getRelatedCases())
                .build();
    }
}
