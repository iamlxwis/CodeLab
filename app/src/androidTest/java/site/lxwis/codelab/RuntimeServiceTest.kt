package site.lxwis.codelab

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeServiceTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var session: RunSession

    @Before fun setUp() = main { session = RunSession(instrumentation.targetContext) }
    @After fun tearDown() = main { session.close() }
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync { action() }

    private fun await(condition: () -> Boolean) {
        val until = SystemClock.elapsedRealtime() + 12_000
        while (SystemClock.elapsedRealtime() < until) {
            var ready = false
            main { ready = condition() }
            if (ready) return
            SystemClock.sleep(25)
        }
        main { fail("Runtime condition timed out. Status: ${session.status}; output: ${session.transcript.takeLast(200)}") }
    }

    @Test fun debuggerPausesStepsAndContinuesRealJavaScript() {
        main {
            session.run("debug.js", "let x = 1;\nlet y = x + 1;\nprint(y);",
                debug = true, breakpoints = listOf(2))
        }
        await { session.paused }
        main {
            assertTrue("Expected source line", session.pausedLine > 0)
            session.debugAction(step = true)
        }
        await { session.paused }
        main {
            assertTrue("Step should enter next source line", session.pausedLine >= 2)
            session.debugAction(step = false)
        }
        await { !session.running }
        main {
            assertEquals("Exit 0", session.status)
            assertTrue(session.transcript.contains("2"))
        }
    }

    @Test fun inputOutputErrorsAndRestart() {
        main { session.run("hello.js", "const name = readLine('Name?'); console.log('Hi', name); console.error('done')") }
        await { session.waiting }
        main {
            assertTrue(session.transcript.contains("Name?"))
            session.submit("Ada")
        }
        await { !session.running }
        main {
            assertEquals("Exit 0", session.status)
            assertTrue(session.transcript.contains("Hi Ada"))
            assertTrue(session.transcript.contains("! done"))
            session.run("bad.js", "throw Error('test failure')")
        }
        await { !session.running }
        main {
            assertEquals("Exit 1", session.status)
            assertTrue(session.transcript.contains("test failure"))
            assertTrue(session.transcript.contains("bad.js"))
        }
    }

    @Test fun stopsInfiniteLoopAndEnforcesOutputLimit() {
        main { session.run("loop.js", "print('started'); while(true) {}") }
        await { session.transcript.contains("started") }
        main {
            session.stop()
            assertTrue(session.status.contains("130"))
            session.run("flood.js", "while(true) print('line')")
        }
        await { !session.running }
        main {
            assertEquals("Exit 1", session.status)
            assertTrue(session.transcript.contains("output limit"))
            assertTrue(session.transcript.length < 70_000)
        }
    }
    @Test fun sqlRunsCompletelyOffline() {
        main {
            session.run("data.sql", """
                CREATE TABLE people (id INTEGER PRIMARY KEY, name TEXT);
                INSERT INTO people(name) VALUES ('Ada; Lovelace'), ('Grace Hopper');
                -- A semicolon in a comment; should not split the statement
                SELECT name FROM people ORDER BY id;
            """.trimIndent())
        }
        await { !session.running }
        main {
            assertEquals("SQLite transcript: " + session.transcript, "Exit 0", session.status)
            assertTrue(session.transcript.contains("Ada; Lovelace"))
            assertTrue(session.transcript.contains("Grace Hopper"))
            assertTrue(session.transcript.contains("2 rows"))
            session.run("invalid.sql", "SELECT * FROM nonexistent_table;")
        }
        await { !session.running }
        main {
            assertEquals("Exit 1", session.status)
            assertTrue(session.transcript.contains("nonexistent_table"))
        }
    }
}
