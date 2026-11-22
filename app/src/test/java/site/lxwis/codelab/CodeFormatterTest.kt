package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class CodeFormatterTest {
    @Test fun jsonPreservesStringContent() {
        val value = CodeFormatter.format("demo.json", """{"text":"a,b:{c}","enabled":true}""")
        assertTrue(value!!.contains("\"text\": \"a,b:{c}\""))
        assertTrue(value.contains("\n  \"enabled\": true"))
    }

    @Test fun javascriptSplitsStatementsWithoutSplittingStrings() {
        val value = CodeFormatter.format("index.js", """function main(){print("a;b");}""")
        assertTrue(value!!.contains("print(\"a;b\");"))
        assertTrue(value.contains("\n  print"))
    }

    @Test fun sqlPreservesQuotedKeywords() {
        val value = CodeFormatter.format("test.sql", "select 'from table'; select 1;")
        assertTrue(value!!.contains("SELECT 'from table';"))
    }

    @Test fun otherLanguagesAreNotFalselyReportedAsFormatted() {
        assertNull(CodeFormatter.format("main.lua", "print('hello')"))
    }
}
