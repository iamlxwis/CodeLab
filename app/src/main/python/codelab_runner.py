"""Execute one user script in a fresh namespace.

The Java bridge supplies a bounded console and stdin; exceptions are formatted
without revealing private Android filesystem paths. The process is killed after
each run by the host service, so interpreter and module state never leak.
"""
import builtins
import io
import sys
import traceback


class TerminalStream(io.TextIOBase):
    def __init__(self, terminal, error=False):
        self.terminal = terminal
        self.error = error
        self.pending = ""

    def writable(self):
        return True

    def write(self, text):
        text = str(text)
        self.pending += text
        while "\n" in self.pending:
            line, self.pending = self.pending.split("\n", 1)
            self.terminal.write(line.rstrip("\r"), self.error)
        if len(self.pending) >= 4096:
            self.terminal.write(self.pending[:4096], self.error)
            self.pending = self.pending[4096:]
        return len(text)

    def flush(self):
        if self.pending:
            self.terminal.write(self.pending, self.error)
            self.pending = ""


def run(filename, source, terminal):
    stdout = TerminalStream(terminal)
    stderr = TerminalStream(terminal, True)
    original_stdout, original_stderr = sys.stdout, sys.stderr
    original_input = builtins.input
    try:
        sys.stdout = stdout
        sys.stderr = stderr

        def console_input(prompt=""):
            return terminal.read(str(prompt))

        builtins.input = console_input
        namespace = {"__name__": "__main__", "__file__": filename}
        exec(compile(source, filename, "exec"), namespace, namespace)
        return 0
    except (Exception, SystemExit) as exc:
        if isinstance(exc, SystemExit):
            return int(exc.code) if isinstance(exc.code, int) else 0 if exc.code is None else 1
        for line in traceback.format_exception_only(type(exc), exc):
            stderr.write(line)
        return 1
    finally:
        stdout.flush()
        stderr.flush()
        builtins.input = original_input
        sys.stdout, sys.stderr = original_stdout, original_stderr
