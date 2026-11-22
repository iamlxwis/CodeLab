package site.lxwis.codelab;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.util.List;
import static org.junit.Assert.*;

public class ProjectStoreHistoryTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void savesPreviousContentAndCanRestore() throws Exception {
        ProjectStore store = new ProjectStore(temp.newFolder("workspace"));
        store.createProject("History", "JavaScript");
        String before = store.read("History", "main.js");
        store.save("History", "main.js", "print('new');\n");
        List<ProjectStore.FileVersion> versions = store.versions("History", "main.js");
        assertEquals(1, versions.size());
        assertEquals(before, store.readVersion("History", "main.js", versions.get(0).id));
        store.restoreVersion("History", "main.js", versions.get(0).id);
        assertEquals(before, store.read("History", "main.js"));
    }

    @Test public void projectSettingsSurviveDuplicationAndArchiveBackup() throws Exception {
        ProjectStore store = new ProjectStore(temp.newFolder("metadata-root"));
        store.createProject("Original", "JavaScript");
        assertEquals("JavaScript", store.metadata("Original").template);
        store.configureProject("Original", "A small example", "main.js");
        store.duplicateProject("Original", "Duplicated");
        assertEquals("A small example", store.metadata("Duplicated").description);
        assertEquals("main.js", store.metadata("Duplicated").entrypoint);
        java.io.ByteArrayOutputStream zip = new java.io.ByteArrayOutputStream();
        store.exportZip("Original", zip);
        store.importZip("Imported", new java.io.ByteArrayInputStream(zip.toByteArray()));
        assertEquals("A small example", store.metadata("Imported").description);
    }

    @Test public void deletedFileCanBeRecovered() throws Exception {
        ProjectStore store = new ProjectStore(temp.newFolder("workspace2"));
        store.createProject("Test", "JavaScript");
        store.createFile("Test", "src/util.js", "let x = 1;\n");
        store.deleteFile("Test", "src/util.js");
        assertEquals(1, store.trashItems().size());
        store.restoreTrash(store.trashItems().get(0).id);
        assertEquals("let x = 1;\n", store.read("Test", "src/util.js"));
    }

    @Test public void previewReplaceDoesNotEditFilesAndApplyKeepsHistory() throws Exception {
        ProjectStore store = new ProjectStore(temp.newFolder("workspace4"));
        store.createProject("Test", "JavaScript");
        store.createFile("Test", "src/notes.txt", "Hello there\nHello again\n");
        ProjectStore.ReplacementSummary preview =
            store.replaceAcrossProject("Test", "Hello ", "Hi ", true, false, false);
        assertEquals(2, preview.matches);
        assertEquals(1, preview.files);
        assertTrue(store.read("Test", "src/notes.txt").contains("Hello"));
        store.replaceAcrossProject("Test", "Hello ", "Hi ", true, false, true);
        assertEquals("Hi there\nHi again\n", store.read("Test", "src/notes.txt"));
        assertFalse(store.versions("Test", "src/notes.txt").isEmpty());
    }

    @Test public void importsUtf16BomAndNormalizesToUtf8() throws Exception {
        ProjectStore store = new ProjectStore(temp.newFolder("workspace5"));
        store.createProject("Test", "JavaScript");
        byte[] text = "hello\r\nworld".getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        byte[] withBom = new byte[text.length + 2];
        withBom[0] = (byte) 0xff;
        withBom[1] = (byte) 0xfe;
        System.arraycopy(text, 0, withBom, 2, text.length);
        store.importSingleFile("Test", "legacy.txt", new java.io.ByteArrayInputStream(withBom));
        assertEquals("hello\r\nworld", store.read("Test", "legacy.txt"));
    }

    @Test public void projectSearchFindsCorrectLine() throws Exception {
        ProjectStore store = new ProjectStore(temp.newFolder("workspace3"));
        store.createProject("Test", "JavaScript");
        store.createFile("Test", "src/util.js", "one\nTwo needle\nthree\n");
        List<ProjectStore.SearchHit> hits = store.searchText("Test", "NEEDLE", false);
        assertEquals(1, hits.size());
        assertEquals(2, hits.get(0).line);
        assertEquals("src/util.js", hits.get(0).path);
    }
}
