package backend.fullstack.document.application;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import backend.fullstack.document.domain.Document;
import backend.fullstack.document.domain.DocumentCategory;
import backend.fullstack.document.infrastructure.DocumentRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

/**
 * Service for managing document uploads, retrieval, and deletion.
 */
@Service
@Transactional(readOnly = true)
public class DocumentService {

    /** Maximum allowed file size: 10 MB */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            OrganizationRepository organizationRepository,
            UserRepository userRepository
    ) {
        this.documentRepository = documentRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lists all documents for an organization, optionally filtered by category.
     *
     * @param organizationId the organization scope
     * @param category       optional category filter
     * @return list of documents (without file content)
     */
    public List<Document> listDocuments(Long organizationId, DocumentCategory category) {
        if (category != null) {
            return documentRepository.findByOrganization_IdAndCategoryOrderByCreatedAtDesc(
                    organizationId, category);
        }
        return documentRepository.findByOrganization_IdOrderByCreatedAtDesc(organizationId);
    }

    /**
     * Retrieves a single document by ID, scoped to the organization.
     *
     * @param id             the document ID
     * @param organizationId the organization scope
     * @return the document entity including file data
     * @throws IllegalArgumentException if the document is not found
     */
    public Document getDocument(Long id, Long organizationId) {
        return documentRepository.findByIdAndOrganization_Id(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
    }

    /**
     * Uploads a new document.
     *
     * @param organizationId the organization scope
     * @param userId         the uploading user's ID
     * @param title          document title
     * @param description    optional description
     * @param category       document category
     * @param file           the uploaded file
     * @return the persisted document entity
     */
    @Transactional
    public Document uploadDocument(Long organizationId, Long userId,
                                   String title, String description,
                                   DocumentCategory category, MultipartFile file) {
        validateFile(file);

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        byte[] fileData;
        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploaded file", e);
        }

        Document document = Document.builder()
                .organization(org)
                .uploadedBy(user)
                .title(title)
                .description(description)
                .category(category)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .fileData(fileData)
                .build();

        return documentRepository.save(document);
    }

    /**
     * Deletes a document by ID, scoped to the organization.
     *
     * @param id             the document ID
     * @param organizationId the organization scope
     * @throws IllegalArgumentException if the document is not found
     */
    @Transactional
    public void deleteDocument(Long id, Long organizationId) {
        Document document = documentRepository.findByIdAndOrganization_Id(id, organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + id));
        documentRepository.delete(document);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + " MB");
        }
    }
}
