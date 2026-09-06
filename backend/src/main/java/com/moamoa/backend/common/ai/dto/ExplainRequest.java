package com.moamoa.backend.common.ai.dto;

import java.util.List;

// AI 서버는 benefit_id가 아니라 benefit_title + source_documents(RAG 문서)를 기대함
public record ExplainRequest(String benefitTitle, String question, List<SourceDocument> sourceDocuments) {
}
