package site.lxwis.codelab;

import org.junit.Test;
import static org.junit.Assert.*;

public class CloudJudge0Test {
    @Test public void recognizesCloudCompilerLanguageFamilies() {
        assertEquals("cpp", CloudJudge0.detect("C++ (GCC 12.2.0)"));
        assertEquals("c", CloudJudge0.detect("C (GCC 11.3.0)"));
        assertEquals("cs", CloudJudge0.detect("C# (Mono 6.12.0.122)"));
        assertEquals("java", CloudJudge0.detect("Java (OpenJDK 17.0.6)"));
        assertEquals("js", CloudJudge0.detect("JavaScript (Node.js 18.15.0)"));
        assertEquals("py", CloudJudge0.detect("Python (3.11.0)"));
        assertEquals("rs", CloudJudge0.detect("Rust (1.68.2)"));
        assertEquals("go", CloudJudge0.detect("Go (1.16.2)"));
        assertEquals("kt", CloudJudge0.detect("Kotlin (1.8.20)"));
        assertEquals("ts", CloudJudge0.detect("TypeScript (5.0.3)"));
        assertNull(CloudJudge0.detect("Brainfuck (2.7)"));
    }
    @Test public void requiresHttpsWithoutCredentialsInUrl() {
        assertEquals("https://ce.judge0.com", CloudJudge0.validateUrl("https://ce.judge0.com/"));
        assertThrows(IllegalArgumentException.class,
            () -> CloudJudge0.validateUrl("http://example.com"));
        assertThrows(IllegalArgumentException.class,
            () -> CloudJudge0.validateUrl("https://user:pass@example.com"));
        assertThrows(IllegalArgumentException.class,
            () -> CloudJudge0.validateUrl("https://example.com?token=secret"));
    }
}
