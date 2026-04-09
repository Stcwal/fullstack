package backend.fullstack.document.api.dto;

import java.time.LocalDateTime;

import backend.fullstack.document.domain.Document;
import backend.fullstack.document.domain.DocumentCategory;

/**
 * Response DTO for document metadata (excludes file content).
 */
public record DocumentResponse(
        Long id,
        String title,
        String description,
        DocumentCategory category,
        String fileName,
        String contentType,
        Long fileSize,
        String uploadedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Maps a {@link Document} entity to a response DTO.
     *
     * @param doc the document entity
     * @return the response DTO
     */
    public static DocumentResponse from(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getDescription(),
                doc.getCategory(),
                doc.getFileName(),
                doc.getContentType(),
                doc.getFileSize(),
                doc.getUploadedByName(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
