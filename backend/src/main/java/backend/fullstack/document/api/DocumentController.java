package backend.fullstack.document.api;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import backend.fullstack.config.ApiResponse;
import backend.fullstack.config.JwtPrincipal;
import backend.fullstack.document.api.dto.DocumentResponse;
import backend.fullstack.document.application.DocumentService;
import backend.fullstack.document.domain.Document;
import backend.fullstack.document.domain.DocumentCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for document storage management.
 * Provides upload, download, list, and delete operations for organizational documents
 * such as policies, training materials, and certifications.
 */
@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document storage for policies, training materials, and certifications")
@SecurityRequirement(name = "Bearer Auth")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Lists all documents for the authenticated user's organization.
     *
     * @param principal the authenticated user
     * @param category  optional category filter
     * @return list of document metadata
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(
            summary = "List documents",
            description = "Returns all documents for the organization, optionally filtered by category"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> listDocuments(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(required = false) @Parameter(description = "Filter by document category") DocumentCategory category
    ) {
        List<DocumentResponse> documents = documentService
                .listDocuments(principal.organizationId(), category)
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved", documents));
    }

    /**
     * Downloads a document by its ID.
     *
     * @param principal the authenticated user
     * @param id        the document ID
     * @return the file content as a byte stream
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(
            summary = "Download a document",
            description = "Downloads the file content of a specific document"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File downloaded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<byte[]> downloadDocument(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        Document doc = documentService.getDocument(id, principal.organizationId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(doc.getContentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(doc.getFileName()).build()
        );
        headers.setContentLength(doc.getFileSize());

        return ResponseEntity.ok().headers(headers).body(doc.getFileData());
    }

    /**
     * Gets document metadata by its ID (without file content).
     *
     * @param principal the authenticated user
     * @param id        the document ID
     * @return the document metadata
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STAFF','SUPERVISOR')")
    @Operation(
            summary = "Get document metadata",
            description = "Returns metadata for a specific document without the file content"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document metadata retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        Document doc = documentService.getDocument(id, principal.organizationId());
        return ResponseEntity.ok(ApiResponse.success("Document retrieved", DocumentResponse.from(doc)));
    }

    /**
     * Uploads a new document.
     *
     * @param principal   the authenticated user
     * @param file        the file to upload (max 10 MB)
     * @param title       document title
     * @param description optional description
     * @param category    document category
     * @return the created document metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SUPERVISOR')")
    @Operation(
            summary = "Upload a document",
            description = "Uploads a new document (max 10 MB). Accepts policies, training materials, "
                    + "certifications, inspection reports, and other documents."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploaded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — STAFF cannot upload")
    })
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("category") DocumentCategory category
    ) {
        Document doc = documentService.uploadDocument(
                principal.organizationId(),
                principal.userId(),
                title,
                description,
                category,
                file
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded", DocumentResponse.from(doc)));
    }

    /**
     * Deletes a document by its ID.
     *
     * @param principal the authenticated user
     * @param id        the document ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    @Operation(
            summary = "Delete a document",
            description = "Permanently deletes a document. Only ADMIN and SUPERVISOR can delete."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @AuthenticationPrincipal JwtPrincipal principal,
            @PathVariable Long id
    ) {
        documentService.deleteDocument(id, principal.organizationId());
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }
}
