package backend.fullstack.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import backend.fullstack.document.application.DocumentService;
import backend.fullstack.document.domain.Document;
import backend.fullstack.document.domain.DocumentCategory;
import backend.fullstack.document.infrastructure.DocumentRepository;
import backend.fullstack.organization.Organization;
import backend.fullstack.organization.OrganizationRepository;
import backend.fullstack.user.User;
import backend.fullstack.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private OrganizationRepository organizationRepository;
    @Mock private UserRepository userRepository;

    private DocumentService service;

    @BeforeEach
    void setUp() {
        service = new DocumentService(documentRepository, organizationRepository, userRepository);
    }

    // ── listDocuments ──────────────────────────────────────────────

    @Test
    void listDocuments_withoutCategory_returnsAll() {
        Long orgId = 1L;
        List<Document> docs = List.of(Document.builder().id(1L).build());
        when(documentRepository.findByOrganization_IdOrderByCreatedAtDesc(orgId)).thenReturn(docs);

        List<Document> result = service.listDocuments(orgId, null);

        assertThat(result).hasSize(1);
        verify(documentRepository).findByOrganization_IdOrderByCreatedAtDesc(orgId);
    }

    @Test
    void listDocuments_withCategory_filtersResults() {
        Long orgId = 1L;
        DocumentCategory cat = DocumentCategory.POLICY;
        when(documentRepository.findByOrganization_IdAndCategoryOrderByCreatedAtDesc(orgId, cat))
                .thenReturn(List.of());

        List<Document> result = service.listDocuments(orgId, cat);

        assertThat(result).isEmpty();
        verify(documentRepository).findByOrganization_IdAndCategoryOrderByCreatedAtDesc(orgId, cat);
    }

    // ── getDocument ────────────────────────────────────────────────

    @Test
    void getDocument_found_returnsDocument() {
        Long orgId = 1L;
        Document doc = Document.builder().id(5L).title("Test").build();
        when(documentRepository.findByIdAndOrganization_Id(5L, orgId)).thenReturn(Optional.of(doc));

        Document result = service.getDocument(5L, orgId);

        assertThat(result.getTitle()).isEqualTo("Test");
    }

    @Test
    void getDocument_notFound_throwsException() {
        when(documentRepository.findByIdAndOrganization_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDocument(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document not found");
    }

    // ── uploadDocument ─────────────────────────────────────────────

    @Test
    void uploadDocument_validFile_savesDocument() throws Exception {
        Long orgId = 1L;
        Long userId = 2L;
        Organization org = Organization.builder().id(orgId).name("Test Org").organizationNumber("123456789").build();
        User user = User.builder().id(userId).firstName("Ola").lastName("Nordmann").build();

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(file.getOriginalFilename()).thenReturn("policy.pdf");
        when(file.getContentType()).thenReturn("application/pdf");

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> {
            Document d = inv.getArgument(0);
            d.setId(10L);
            return d;
        });

        Document result = service.uploadDocument(orgId, userId, "Hygiene Policy",
                "Our hygiene procedures", DocumentCategory.POLICY, file);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("Hygiene Policy");
        assertThat(result.getFileName()).isEqualTo("policy.pdf");
        assertThat(result.getCategory()).isEqualTo(DocumentCategory.POLICY);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());
        assertThat(captor.getValue().getFileData()).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void uploadDocument_emptyFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> service.uploadDocument(1L, 2L, "Title", null,
                DocumentCategory.OTHER, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File must not be empty");
    }

    @Test
    void uploadDocument_tooLargeFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(11 * 1024 * 1024L); // 11 MB

        assertThatThrownBy(() -> service.uploadDocument(1L, 2L, "Title", null,
                DocumentCategory.OTHER, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds maximum");
    }

    @Test
    void uploadDocument_nullFile_throwsException() {
        assertThatThrownBy(() -> service.uploadDocument(1L, 2L, "Title", null,
                DocumentCategory.OTHER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File must not be empty");
    }

    // ── deleteDocument ─────────────────────────────────────────────

    @Test
    void deleteDocument_found_deletesSuccessfully() {
        Document doc = Document.builder().id(5L).build();
        when(documentRepository.findByIdAndOrganization_Id(5L, 1L)).thenReturn(Optional.of(doc));

        service.deleteDocument(5L, 1L);

        verify(documentRepository).delete(doc);
    }

    @Test
    void deleteDocument_notFound_throwsException() {
        when(documentRepository.findByIdAndOrganization_Id(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteDocument(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document not found");
    }
}
