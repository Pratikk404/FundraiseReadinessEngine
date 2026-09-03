package com.fundraise.engine.service;

import com.fundraise.engine.entity.User;
import com.fundraise.engine.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DocumentRepository documentRepository;
    private final FindingRepository findingRepository;
    private final ReadinessScoreRepository readinessScoreRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // User stats
        long totalUsers = userRepository.count();
        long founderCount = userRepository.countByRole(User.Role.FOUNDER);
        long advisorCount = userRepository.countByRole(User.Role.ADVISOR);
        long adminCount = userRepository.countByRole(User.Role.ADMIN);

        stats.put("users", Map.of(
                "total", totalUsers,
                "founders", founderCount,
                "advisors", advisorCount,
                "admins", adminCount
        ));

        // Company stats
        long totalCompanies = companyRepository.count();
        stats.put("companies", Map.of(
                "total", totalCompanies
        ));

        // Document stats
        long totalDocuments = documentRepository.count();
        stats.put("documents", Map.of(
                "total", totalDocuments
        ));

        // Finding stats
        long totalFindings = findingRepository.count();
        long unresolvedFindings = findingRepository.countByResolvedFalse();
        long resolvedFindings = totalFindings - unresolvedFindings;
        stats.put("findings", Map.of(
                "total", totalFindings,
                "unresolved", unresolvedFindings,
                "resolved", resolvedFindings
        ));

        // Score stats
        long totalScoreEntries = readinessScoreRepository.count();
        stats.put("scores", Map.of(
                "totalEntries", totalScoreEntries
        ));

        return stats;
    }
}
