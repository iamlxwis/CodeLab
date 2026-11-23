package site.lxwis.codelab;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class StarterExamplesTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test public void everyCatalogLanguageHasAnExampleAndCanCreateAProject() throws Exception {
        ProjectStore store = new ProjectStore(folder.newFolder("all-languages"));
        int count = 0;
        for (SupportedLanguage language : LanguageCatalog.INSTANCE.getEntries()) {
            String name = language.getName();
            String ext = language.getExtension();
            String content = StarterExamples.example(name, StarterExamples.BEGINNER);
            assertFalse(name + " has an empty starter", content.isBlank());
            String project = "Language-" + count++;
            store.createProject(project, name);
            assertTrue(store.files(project).contains(StarterExamples.fileName(name, ext)));
            assertFalse(store.read(project, StarterExamples.fileName(name, ext)).isBlank());
        }
        assertTrue(count >= 90);
    }

    @Test public void helloAndBlankAreDifferentAndPreserveFileType() throws Exception {
        ProjectStore store = new ProjectStore(folder.newFolder("modes"));
        store.createProject("HelloPython", "Python", StarterExamples.HELLO);
        assertEquals("print(\"Hello, CodeLab!\")\\n".replace("\\n", "\n"),
            store.read("HelloPython", "main.py"));
        store.createProject("BlankRust", "Rust", StarterExamples.BLANK);
        assertEquals("", store.read("BlankRust", "main.rs"));
        store.createProject("HelloJson", "JSON", StarterExamples.HELLO);
        assertTrue(store.read("HelloJson", "main.json").contains("\"CodeLab\""));
        assertThrows(IllegalArgumentException.class,
            () -> store.createProject("BadMode", "Python", "Other"));
        assertFalse(store.projects().contains("BadMode"));
    }
}
