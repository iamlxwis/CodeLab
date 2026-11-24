package site.lxwis.codelab;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ProjectStoreTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private ProjectStore store;
    private File root;

    @Before public void setup() throws Exception {
        root = temp.newFolder("workspace");
        store = new ProjectStore(root);
        store.createProject("First");
    }

    @Test public void nearMaximumFileSurvivesSaveRestartAndBoundedSearch() throws Exception {
        // A large source file exercises real on-disk persistence without
        // claiming that an EditText can render multi-megabyte inputs smoothly.
        String source = "const n = 1234;\n".repeat(16000);
        assertTrue(source.getBytes(StandardCharsets.UTF_8).length > 220000);
        assertTrue(source.getBytes(StandardCharsets.UTF_8).length < 262144);
        store.save("First", "main.js", source, true);
        store.remember("First", Collections.singletonList("main.js"), "main.js");
        ProjectStore restored = new ProjectStore(root);
        assertEquals(source, restored.read("First", "main.js"));
        assertEquals("main.js", restored.restore().getActive());
        assertEquals(200, restored.searchText("First", "const n = 1234", true).size());
    }

    @Test public void lineByLineSearchPreservesLineNumbersAndResultCap() throws Exception {
        store.createFile("First", "notes.txt", "alpha\nMATCH\nlast match\n");
        List<ProjectStore.SearchHit> hits =
            store.searchText("First", "match", false, Collections.singletonList("notes.txt"));
        assertEquals(2, hits.size());
        assertEquals(2, hits.get(0).line);
        assertEquals(3, hits.get(1).line);
    }

    @Test public void failedSavePreservesPriorRevision() throws Exception {
        store.save("First", "main.js", "print('preserved');");
        char[] huge = new char[262145];
        Arrays.fill(huge, 'z');
        assertThrows(IOException.class, () -> store.save("First", "main.js", new String(huge)));
        assertEquals("print('preserved');", store.read("First", "main.js"));
    }

    @Test public void rejectedSaveCannotCreatePhantomFileHistory() throws Exception {
        String original = store.read("First", "main.js");
        int before = store.versions("First", "main.js").size();
        char[] huge = new char[262145];
        Arrays.fill(huge, 'x');
        assertThrows(IOException.class, () -> store.save("First", "main.js", new String(huge), true));
        assertThrows(IOException.class, () -> store.save("First", "main.js", null, true));
        assertEquals(original, store.read("First", "main.js"));
        assertEquals("Rejected saves cannot add phantom history snapshots",
            before, store.versions("First", "main.js").size());
    }

    @Test public void restartRestoresOnlySurvivingTabs() throws Exception {
        store.createFile("First", "lib/λ.js", "print('你好');\n");
        store.remember("First", Arrays.asList("main.js", "lib/λ.js"), "main.js");
        store.deleteFile("First", "main.js");
        ProjectStore reopened = new ProjectStore(root);
        WorkspaceSession session = reopened.restore();
        assertEquals("First", session.getProject());
        assertEquals(Collections.singletonList("lib/λ.js"), session.getTabs());
        assertEquals("lib/λ.js", session.getActive());
        assertEquals("print('你好');\n", reopened.read("First", "lib/λ.js"));
        reopened.deleteProject("First");
        assertNull(reopened.restore().getProject());
    }

    @Test public void moveCollisionsDoNotOverwrite() throws Exception {
        store.createFile("First", "lib/code.js", "original");
        assertThrows(IOException.class, () -> store.renameFile("First", "lib/code.js", "main.js"));
        assertThrows(IOException.class, () -> store.createProject("First"));
        store.renameFile("First", "lib/code.js", "src/index.js");
        store.renameProject("First", "Second");
        assertEquals("original", store.read("Second", "src/index.js"));
    }

    @Test public void duplicateFileCreateNeverOverwritesExistingSource() throws Exception {
        store.save("First", "main.js", "print('keep this');", true);
        assertThrows(IOException.class, () -> store.createFile("First", "main.js"));
        assertEquals("print('keep this');", store.read("First", "main.js"));
    }

    @Test public void roundTripIncludesNestedFilesAndUtf8() throws Exception {
        store.createFile("First", "src/nested/app.js", "print('שלום');");
        ByteArrayOutputStream zip = new ByteArrayOutputStream();
        store.exportZip("First", zip);
        store.importZip("Copy", new ByteArrayInputStream(zip.toByteArray()));
        assertEquals(store.files("First"), store.files("Copy"));
        for (String path : store.files("First"))
            assertEquals(store.read("First", path), store.read("Copy", path));
    }

    private byte[] zip(String name, byte[] bytes) throws Exception {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(raw)) {
            zip.putNextEntry(new ZipEntry("valid.js"));
            zip.write("before error".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry(name));
            zip.write(bytes);
            zip.closeEntry();
        }
        return raw.toByteArray();
    }

    @Test public void bulkImportIsTransactionalAndCannotOverwriteExistingFiles() throws Exception {
        String before = store.read("First", "main.js");
        assertThrows(IOException.class, () ->
            store.importFilesAtomically("First", Arrays.asList("new.js", "broken.js"), index -> {
                if (index == 0) return new ByteArrayInputStream("new".getBytes(StandardCharsets.UTF_8));
                throw new IOException("Simulated provider read failure");
            }));
        assertFalse(store.files("First").contains("new.js"));
        assertFalse(store.files("First").contains("broken.js"));
        assertThrows(IOException.class, () ->
            store.importFilesAtomically("First", Collections.singletonList("main.js"),
                index -> new ByteArrayInputStream("overwrite".getBytes(StandardCharsets.UTF_8))));
        assertEquals(before, store.read("First", "main.js"));
        store.importFilesAtomically("First", Arrays.asList("a.js", "b.js"),
            index -> new ByteArrayInputStream(("source-" + index).getBytes(StandardCharsets.UTF_8)));
        assertEquals("source-0", store.read("First", "a.js"));
        assertEquals("source-1", store.read("First", "b.js"));
    }

    @Test public void archiveCannotInjectGitOrUnexpectedAppMetadata() throws Exception {
        for (String path : Arrays.asList(".git/config", "src/.git/hooks/pre-commit",
                ".codelab/credentials", "src/.codelab/config")) {
            assertThrows(IOException.class, () ->
                store.importZip("Unsafe", new ByteArrayInputStream(zip(path, new byte[]{1}))));
            assertFalse(store.projects().contains("Unsafe"));
        }
    }

    @Test public void invalidArchivesNeverPublishPartialProjects() throws Exception {
        for (String path : Arrays.asList("../escape", "a/../../escape", "/outside", "C:\\foo", "a//b")) {
            byte[] archive = zip(path, new byte[]{1});
            assertThrows(Exception.class, () -> store.importZip("New", new ByteArrayInputStream(archive)));
            assertEquals(Collections.singletonList("First"), store.projects());
        }
        byte[] bomb = zip("huge", new byte[262145]);
        assertThrows(IOException.class, () -> store.importZip("New", new ByteArrayInputStream(bomb)));
        assertThrows(IOException.class, () -> store.importZip("New", new ByteArrayInputStream(new byte[]{1,2})));
        assertEquals(Collections.singletonList("First"), store.projects());
    }

    @Test public void invalidNamesAndPathsCannotEscape() throws Exception {
        for (String name : Arrays.asList("..", "  ", "/foo", "x\\y", "a\nb"))
            assertThrows(IllegalArgumentException.class, () -> store.createProject(name));
        assertThrows(IllegalArgumentException.class, () -> store.createFile("First", "../../session.properties"));
        assertEquals(Collections.singletonList("main.js"), store.files("First"));
    }
    @Test public void starterTemplatesContainRunnableProjectFiles() throws Exception {
        store.createProject("Queries", "SQLite");
        assertEquals(Collections.singletonList("main.sql"), store.files("Queries"));
        assertTrue(store.read("Queries", "main.sql").contains("CREATE TABLE"));

        store.createProject("JavaScripts", "Java");
        assertEquals(Collections.singletonList("Main.java"), store.files("JavaScripts"));
        assertTrue(store.read("JavaScripts", "Main.java").contains("public static void main"));

        store.createProject("PyOnline", "Python");
        assertEquals(Collections.singletonList("main.py"), store.files("PyOnline"));
        assertTrue(store.read("PyOnline", "main.py").contains("print("));
        store.createProject("CppOnline", "C++");
        assertEquals(Collections.singletonList("main.cpp"), store.files("CppOnline"));
        assertTrue(store.read("CppOnline", "main.cpp").contains("int main()"));
        store.createProject("RustOnline", "Rust");
        assertEquals(Collections.singletonList("main.rs"), store.files("RustOnline"));
        store.createProject("KotlinOnline", "Kotlin");
        assertEquals(Collections.singletonList("Main.kt"), store.files("KotlinOnline"));

        store.createProject("LuaScripts", "Lua");
        assertEquals(Collections.singletonList("main.lua"), store.files("LuaScripts"));
        assertTrue(store.read("LuaScripts", "main.lua").contains("readLine"));

        store.createProject("Website", "Web");
        assertEquals(Arrays.asList("index.html", "script.js", "style.css"), store.files("Website"));
        assertTrue(store.read("Website", "index.html").contains("script.js"));

        assertThrows(IllegalArgumentException.class, () -> store.createProject("Invalid", "Brainfuck"));
        assertFalse(store.projects().contains("Invalid"));
    }
}
