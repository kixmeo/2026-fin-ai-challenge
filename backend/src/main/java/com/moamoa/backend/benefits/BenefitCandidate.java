package com.moamoa.backend.benefits;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "benefit_candidates")
public class BenefitCandidate {

    @Id
    @Column(name = "benefit_id", length = 50)
    private String benefitId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(name = "required_docs_count", nullable = false)
    private int requiredDocsCount;

    // AI 서버 매칭(score) 요청에 그대로 실어보내는 불투명한 JSON 문자열 - 이 애플리케이션은 내부를 조회/해석하지 않음
    @Column(name = "eligibility_rule", nullable = false, length = 2000)
    private String eligibilityRule;

    // F3(설명/재질문)에서 AI 서버에 RAG 소스로 그대로 실어보내는 본문 - 아직 실제 공고 원문이 아니라
    // 자리표시 텍스트가 들어있음(V14 마이그레이션 참고). 콘텐츠 담당자가 실제 문서로 교체해야 함
    @Column(length = 2000)
    private String description;

    protected BenefitCandidate() {
    }

    public BenefitCandidate(String benefitId, String title, long amount, LocalDate deadline,
                             int requiredDocsCount, String eligibilityRule, String description) {
        this.benefitId = requireNonBlank(benefitId, "benefitId");
        this.title = requireNonBlank(title, "title");
        this.amount = amount;
        this.deadline = Objects.requireNonNull(deadline, "deadline must not be null");
        this.requiredDocsCount = requiredDocsCount;
        this.eligibilityRule = requireNonBlank(eligibilityRule, "eligibilityRule");
        this.description = description;
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public String getBenefitId() {
        return benefitId;
    }

    public String getTitle() {
        return title;
    }

    public long getAmount() {
        return amount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public int getRequiredDocsCount() {
        return requiredDocsCount;
    }

    public String getEligibilityRule() {
        return eligibilityRule;
    }

    public String getDescription() {
        return description;
    }
}
