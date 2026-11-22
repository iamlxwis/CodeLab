package site.lxwis.codelab

import org.junit.Assert.*
import org.junit.Test

class SqlResultParserTest {
    @Test fun extractsLastResultSetAfterMutations() {
        val log = "OK · CREATE\nOK · INSERT\nid\ttitle\n1\tWrite code\n2\tRun it offline\n2 rows\n"
        val table = SqlResultParser.parse(log)
        assertNotNull(table)
        assertEquals(listOf("id", "title"), table!!.columns)
        assertEquals(2, table.rows.size)
        assertEquals(listOf("2", "Run it offline"), table.rows[1])
    }

    @Test fun handlesEmptyQueriesAndRejectsMalformedOutput() {
        val empty = SqlResultParser.parse("id\ttitle\n0 rows\n")
        assertNotNull(empty)
        assertTrue(empty!!.rows.isEmpty())
        assertNull(SqlResultParser.parse("! Statement 1: syntax error\n"))
        assertNull(SqlResultParser.parse("a\tb\nsingle\n1 row\n"))
        assertNull(SqlResultParser.parse("a\tb\n301 rows"))
    }
}
