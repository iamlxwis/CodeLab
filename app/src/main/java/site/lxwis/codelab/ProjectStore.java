package site.lxwis.codelab;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** App-private UTF-8 projects. Operations publish complete files or directories via rename. */
public final class ProjectStore {
    private static final int MAX_FILE_BYTES = 256 * 1024;
    private static final int MAX_ARCHIVE_BYTES = 16 * 1024 * 1024;
    private static final int MAX_ENTRIES = 1024;
    private final File projects;
    private final File scratch;
    private final File trash;
    private final File session;

    public ProjectStore(File root) {
        projects = new File(root, "projects");
        scratch = new File(root, "pending");
        trash = new File(root, "trash");
        session = new File(root, "session.properties");
        ensureDir(projects);
        ensureDir(scratch);
        ensureDir(trash);
    }

    private static void ensureDir(File dir) {
        if (!dir.isDirectory() && !dir.mkdirs() && !dir.isDirectory())
            throw new IllegalStateException("Cannot create workspace directory.");
    }

    private static String segment(String name) {
        if (name == null || name.trim().isEmpty() || !name.equals(name.trim()) || name.equals(".")
                || name.equals("..") || name.length() > 100 || name.matches(".*[/\\\\:].*")
                || name.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Enter a valid file or project name (up to 100 characters).");
        return name;
    }

    private File project(String name) {
        File dir = new File(projects, segment(name));
        if (!dir.isDirectory()) throw new IllegalArgumentException("Project does not exist.");
        return dir;
    }

    private static File resolve(File base, String path) throws IOException {
        if (path == null || path.length() > 512) throw new IOException("Invalid path.");
        String[] parts = path.split("/", -1);
        if (parts.length > 16) throw new IOException("Too many nested folders.");
        File file = base;
        for (String part : parts) file = new File(file, segment(part));
        if (!file.getCanonicalPath().startsWith(base.getCanonicalPath() + File.separator))
            throw new IOException("Path leaves the project.");
        return file;
    }

    public synchronized List<String> projects() {
        File[] list = projects.listFiles(File::isDirectory);
        if (list == null) throw new IllegalStateException("Cannot read projects.");
        Arrays.sort(list, Comparator.comparingLong(File::lastModified).reversed().thenComparing(File::getName));
        List<String> names = new ArrayList<>();
        for (File file : list) names.add(file.getName());
        return names;
    }

    private static void collect(File base, File dir, List<String> results, boolean includeDirectories) throws IOException {
        File[] children = dir.listFiles();
        if (children == null) throw new IOException("Cannot list directory.");
        Arrays.sort(children, Comparator.comparing(File::getName));
        for (File child : children) {
            if (Files.isSymbolicLink(child.toPath())) continue;
            String path = base.toPath().relativize(child.toPath()).toString();
            if (child.isDirectory()) {
                if (includeDirectories) results.add(path + "/");
                collect(base, child, results, includeDirectories);
            } else if (child.isFile()) results.add(path);
        }
    }

    public synchronized List<String> files(String project) throws IOException {
        File dir = project(project);
        List<String> names = new ArrayList<>();
        collect(dir, dir, names, false);
        return names;
    }

    public synchronized String createProject(String name) throws IOException {
        return createProject(name, "JavaScript");
    }

    /** Reuse the bounded and path-validated archive pipeline for a safe project copy. */
    public synchronized void duplicateProject(String from, String to) throws IOException {
        ByteArrayOutputStream archive = new ByteArrayOutputStream();
        exportZip(from, archive);
        importZip(to, new ByteArrayInputStream(archive.toByteArray()));
    }

    public synchronized String createProject(String name, String template) throws IOException {
        if (!Arrays.asList("JavaScript", "Java", "Lua", "SQLite", "Web",
                "Python", "C", "C++", "C#", "TypeScript", "Kotlin", "Rust", "Go", "PHP",
                "Ruby", "Swift", "Bash", "Dart", "Scala", "Perl", "Haskell", "R").contains(template))
            throw new IllegalArgumentException("Unsupported project template.");
        File dest = new File(projects, segment(name));
        requireNew(dest);
        File stage = Files.createTempDirectory(scratch.toPath(), "project-").toFile();
        try {
            if ("Python".equals(template)) {
                writeText(new File(stage, "main.py"), "print('Hello from CodeLab!')\n");
            } else             if ("C".equals(template)) {
                writeText(new File(stage, "main.c"), "#include <stdio.h>\nint main(void) {\n    puts(\"Hello from CodeLab!\");\n    return 0;\n}\n");
            } else             if ("C++".equals(template)) {
                writeText(new File(stage, "main.cpp"), "#include <iostream>\nint main() {\n    std::cout << \"Hello from CodeLab!\" << std::endl;\n    return 0;\n}\n");
            } else             if ("C#".equals(template)) {
                writeText(new File(stage, "Program.cs"), "using System;\npublic class Program {\n    public static void Main() {\n        Console.WriteLine(\"Hello from CodeLab!\");\n    }\n}\n");
            } else             if ("TypeScript".equals(template)) {
                writeText(new File(stage, "main.ts"), "const message: string = 'Hello from CodeLab!';\nconsole.log(message);\n");
            } else             if ("Kotlin".equals(template)) {
                writeText(new File(stage, "Main.kt"), "fun main() {\n    println(\"Hello from CodeLab!\")\n}\n");
            } else             if ("Rust".equals(template)) {
                writeText(new File(stage, "main.rs"), "fn main() {\n    println!(\"Hello from CodeLab!\");\n}\n");
            } else             if ("Go".equals(template)) {
                writeText(new File(stage, "main.go"), "package main\nimport \"fmt\"\nfunc main() {\n    fmt.Println(\"Hello from CodeLab!\")\n}\n");
            } else             if ("PHP".equals(template)) {
                writeText(new File(stage, "main.php"), "<?php\necho \"Hello from CodeLab!\\n\";\n");
            } else             if ("Ruby".equals(template)) {
                writeText(new File(stage, "main.rb"), "puts 'Hello from CodeLab!'\n");
            } else             if ("Swift".equals(template)) {
                writeText(new File(stage, "main.swift"), "print(\"Hello from CodeLab!\")\n");
            } else             if ("Bash".equals(template)) {
                writeText(new File(stage, "main.sh"), "#!/usr/bin/env bash\necho 'Hello from CodeLab!'\n");
            } else             if ("Dart".equals(template)) {
                writeText(new File(stage, "main.dart"), "void main() {\n  print('Hello from CodeLab!');\n}\n");
            } else             if ("Scala".equals(template)) {
                writeText(new File(stage, "Main.scala"), "object Main extends App {\n  println(\"Hello from CodeLab!\")\n}\n");
            } else             if ("Perl".equals(template)) {
                writeText(new File(stage, "main.pl"), "use strict;\nuse warnings;\nprint \"Hello from CodeLab!\\n\";\n");
            } else             if ("Haskell".equals(template)) {
                writeText(new File(stage, "Main.hs"), "main = putStrLn \"Hello from CodeLab!\"\n");
            } else             if ("R".equals(template)) {
                writeText(new File(stage, "main.r"), "cat('Hello from CodeLab!\\n')\n");
            } else             if ("Java".equals(template)) {
                writeText(new File(stage, "Main.java"),
                    "// Java beta: basic main-method console programs (interpreted locally).\n" +
                    "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        int total = 0;\n" +
                    "        for (int i = 1; i <= 4; i++) { total += i; }\n" +
                    "        System.out.println(\"Hello from CodeLab!\");\n" +
                    "        System.out.println(\"Sum = \" + total);\n" +
                    "    }\n" +
                    "}\n");
            } else if ("Lua".equals(template)) {
                writeText(new File(stage, "main.lua"),
                    "-- CodeLab offline Lua (LuaJ). Press Run.\n" +
                    "local name = readLine('What is your name? ')\n" +
                    "print('Hello, ' .. name .. '!')\n" +
                    "for i = 1, 3 do\n" +
                    "    print('Count ' .. i)\n" +
                    "end\n");
            } else if ("SQLite".equals(template)) {
                writeText(new File(stage, "main.sql"),
                    "-- Local SQLite: press Run to execute this file.\n" +
                    "CREATE TABLE tasks (id INTEGER PRIMARY KEY, title TEXT);\n" +
                    "INSERT INTO tasks(title) VALUES ('Write code'), ('Run it offline');\n" +
                    "SELECT * FROM tasks;\n");
            } else if ("Web".equals(template)) {
                writeText(new File(stage, "index.html"),
                    "<!doctype html>\n<html lang=\"en\">\n<head>\n" +
                    "  <meta charset=\"utf-8\">\n" +
                    "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "  <title>CodeLab</title>\n" +
                    "  <link rel=\"stylesheet\" href=\"style.css\">\n" +
                    "</head>\n<body>\n  <main>\n" +
                    "    <h1>Hello, CodeLab.</h1>\n" +
                    "    <p>A local project, ready to preview offline.</p>\n" +
                    "    <button id=\"hello\">Try it</button>\n" +
                    "    <p id=\"output\"></p>\n" +
                    "  </main>\n  <script src=\"script.js\"></script>\n</body>\n</html>\n");
                writeText(new File(stage, "style.css"),
                    ":root { color-scheme: dark; font-family: system-ui, sans-serif; }\n" +
                    "body { margin: 0; padding: 2rem; background: #101517; color: #e6eeed; }\n" +
                    "main { max-width: 42rem; margin: 3rem auto; }\n" +
                    "button { background: #83e5ca; color: #083329; padding: .8rem 1.3rem; border: 0; border-radius: .6rem; font-weight: bold; }\n");
                writeText(new File(stage, "script.js"),
                    "document.getElementById('hello').addEventListener('click', () => {\n" +
                    "  document.getElementById('output').textContent = 'Running locally.';\n" +
                    "  console.log('Preview JavaScript executed');\n" +
                    "});\n");
            } else {
                writeText(new File(stage, "main.js"),
                    "// Run this file, then enter your name in the console.\n" +
                    "const name = readLine(\"What is your name? \");\n" +
                    "print(\"Hello, \" + name + \"!\");\n");
            }
            moveNew(stage, dest);
            return name;
        } finally { clean(stage); }
    }

    private static void requireNew(File file) throws IOException {
        if (file.exists()) throw new IOException("That name is already in use.");
    }

    private static void moveNew(File from, File to) throws IOException {
        requireNew(to);
        ensureDir(to.getParentFile());
        Files.move(from.toPath(), to.toPath(), StandardCopyOption.ATOMIC_MOVE);
    }

    private void writeText(File target, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_FILE_BYTES) throw new IOException("Editor files are limited to 256 KiB.");
        ensureDir(target.getParentFile());
        File tmp = File.createTempFile("save-", ".tmp", scratch);
        try {
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                out.write(bytes);
                out.getFD().sync();
            }
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally { tmp.delete(); }
    }

    public synchronized void createFile(String project, String path) throws IOException {
        createFile(project, path, "");
    }

    public synchronized void createFile(String project, String path, String content) throws IOException {
        File target = resolve(project(project), path);
        requireNew(target);
        writeText(target, content);
    }

    public synchronized String read(String project, String path) throws IOException {
        File target = resolve(project(project), path);
        if (!target.isFile()) throw new IOException("File does not exist.");
        if (target.length() > MAX_FILE_BYTES) throw new IOException("Editor files are limited to 256 KiB.");
        return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(Files.readAllBytes(target.toPath()))).toString();
    }

    public synchronized void save(String project, String path, String text) throws IOException {
        File target = resolve(project(project), path);
        if (!target.isFile()) throw new IOException("File does not exist.");
        writeText(target, text);
    }

    public synchronized void importSingleFile(String project, String path, InputStream input) throws IOException {
        File target = resolve(project(project), path);
        requireNew(target);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transfer(input, output, MAX_FILE_BYTES);
        byte[] bytes = output.toByteArray();
        String content = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        writeText(target, content);
    }

    public synchronized void exportSingleFile(String project, String path, OutputStream output) throws IOException {
        File target = resolve(project(project), path);
        if (!target.isFile()) throw new IOException("File does not exist.");
        Files.copy(target.toPath(), output);
    }

    public synchronized void createFolder(String project, String path) throws IOException {
        File target = resolve(project(project), path);
        requireNew(target);
        if (!target.mkdirs()) throw new IOException("Unable to create folder.");
    }

    public synchronized void moveFile(String project, String from, String to) throws IOException {
        File base = project(project);
        File source = resolve(base, from);
        File destination = resolve(base, to);
        if (!source.isFile()) throw new IOException("Source file does not exist.");
        moveNew(source, destination);
    }

    public synchronized void copyToProject(String fromProject, String path,
                                            String toProject, String destination) throws IOException {
        File target = resolve(project(toProject), destination);
        requireNew(target);
        writeText(target, read(fromProject, path));
    }

    public synchronized void duplicateFile(String project, String from, String to) throws IOException {
        File base = project(project);
        File source = resolve(base, from);
        File destination = resolve(base, to);
        if (!source.isFile()) throw new IOException("Source file does not exist.");
        requireNew(destination);
        writeText(destination, read(project, from));
    }

    public synchronized void renameFile(String project, String from, String to) throws IOException {
        File dir = project(project);
        File source = resolve(dir, from);
        File dest = resolve(dir, to);
        if (dest.toPath().startsWith(source.toPath())) throw new IOException("Choose a different destination.");
        moveNew(source, dest);
    }

    private static void clean(File file) {
        if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) clean(child);
        }
        file.delete();
    }

    private void discard(File file) throws IOException {
        File pending = new File(scratch, "removed-" + UUID.randomUUID());
        moveNew(file, pending);
        clean(pending);
    }

    /** Soft-delete without overwriting. Items are available for manual recovery. */
    private void moveToTrash(File source, String projectName, String path, boolean entireProject) throws IOException {
        if (!source.exists()) throw new IOException("Item no longer exists.");
        String id = UUID.randomUUID().toString();
        Properties metadata = new Properties();
        metadata.setProperty("project", projectName);
        metadata.setProperty("path", path);
        metadata.setProperty("kind", entireProject ? "project" : source.isDirectory() ? "folder" : "file");
        metadata.setProperty("deleted", Long.toString(System.currentTimeMillis()));
        File record = new File(trash, id + ".properties");
        File payload = new File(trash, id + ".data");
        StringWriter writer = new StringWriter();
        metadata.store(writer, null);
        writeText(record, writer.toString());
        try { moveNew(source, payload); }
        catch (IOException ex) { record.delete(); throw ex; }
    }

    public static final class TrashItem {
        public final String id, project, path, kind;
        public final long deletedAt;
        public TrashItem(String id, String project, String path, String kind, long deletedAt) {
            this.id = id;
            this.project = project;
            this.path = path;
            this.kind = kind;
            this.deletedAt = deletedAt;
        }
    }

    public synchronized List<TrashItem> trashItems() {
        List<TrashItem> items = new ArrayList<>();
        File[] records = trash.listFiles((dir, name) -> name.endsWith(".properties"));
        if (records == null) return items;
        for (File record : records) {
            String id = record.getName().replace(".properties", "");
            if (!new File(trash, id + ".data").exists()) continue;
            Properties props = new Properties();
            try (Reader reader = Files.newBufferedReader(record.toPath(), StandardCharsets.UTF_8)) {
                props.load(reader);
                items.add(new TrashItem(id, props.getProperty("project", ""),
                    props.getProperty("path", ""), props.getProperty("kind", "file"),
                    Long.parseLong(props.getProperty("deleted", "0"))));
            } catch (IOException | NumberFormatException ignored) {}
        }
        items.sort((a, b) -> Long.compare(b.deletedAt, a.deletedAt));
        return items;
    }

    public synchronized void restoreTrash(String id) throws IOException {
        TrashItem found = null;
        for (TrashItem item : trashItems()) if (item.id.equals(id)) { found = item; break; }
        if (found == null) throw new IOException("Deleted item not found.");
        File destination = "project".equals(found.kind)
            ? new File(projects, segment(found.project))
            : resolve(project(found.project), found.path);
        moveNew(new File(trash, id + ".data"), destination);
        new File(trash, id + ".properties").delete();
    }

    public synchronized void emptyTrash() throws IOException {
        for (TrashItem item : trashItems()) {
            discard(new File(trash, item.id + ".data"));
            if (!new File(trash, item.id + ".properties").delete())
                throw new IOException("Unable to remove trash metadata.");
        }
    }

    public static final class SearchHit {
        public final String path, preview;
        public final int line;
        public SearchHit(String path, int line, String preview) {
            this.path = path;
            this.line = line;
            this.preview = preview;
        }
    }

    /** Bounded, offline project-wide literal search. Invalid/binary files are skipped. */
    public synchronized List<SearchHit> searchText(String projectName, String query, boolean caseSensitive)
            throws IOException {
        if (query == null || query.isBlank() || query.length() > 200)
            throw new IOException("Enter a search phrase (up to 200 characters).");
        List<SearchHit> hits = new ArrayList<>();
        List<String> paths = files(projectName);
        String needle = caseSensitive ? query : query.toLowerCase(Locale.ROOT);
        for (String path : paths) {
            String content;
            try { content = read(projectName, path); }
            catch (IOException ignored) { continue; }
            String[] lines = content.split("\\n", -1);
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if ((caseSensitive ? line : line.toLowerCase(Locale.ROOT)).contains(needle)) {
                    hits.add(new SearchHit(path, i + 1, line.trim().substring(0, Math.min(160, line.trim().length()))));
                    if (hits.size() >= 200) return hits;
                }
            }
        }
        return hits;
    }

    public synchronized List<String> folders(String projectName) throws IOException {
        List<String> paths = new ArrayList<>();
        File dir = project(projectName);
        collect(dir, dir, paths, true);
        paths.removeIf(path -> !path.endsWith("/"));
        return paths;
    }

    public synchronized void deleteFile(String project, String path) throws IOException {
        moveToTrash(resolve(project(project), path), project, path, false);
    }

    public synchronized void renameProject(String from, String to) throws IOException {
        moveNew(project(from), new File(projects, segment(to)));
    }

    public synchronized void deleteProject(String name) throws IOException {
        moveToTrash(project(name), name, "", true);
    }

    public synchronized void remember(String project, List<String> tabs, String active) throws IOException {
        Properties props = new Properties();
        if (project != null) props.setProperty("project", project);
        if (active != null) props.setProperty("active", active);
        int i = 0;
        for (String tab : new LinkedHashSet<>(tabs)) props.setProperty("tab." + i++, tab);
        StringWriter writer = new StringWriter();
        props.store(writer, null);
        writeText(session, writer.toString());
    }

    public synchronized WorkspaceSession restore() {
        WorkspaceSession empty = new WorkspaceSession(null, Collections.emptyList(), null);
        if (!session.isFile()) return empty;
        Properties props = new Properties();
        try (Reader in = Files.newBufferedReader(session.toPath(), StandardCharsets.UTF_8)) {
            props.load(in);
            String project = props.getProperty("project");
            if (project == null) return empty;
            File dir = project(project);
            List<String> tabs = new ArrayList<>();
            for (int i = 0; props.containsKey("tab." + i); i++) {
                String tab = props.getProperty("tab." + i);
                if (resolve(dir, tab).isFile()) tabs.add(tab);
            }
            String active = props.getProperty("active");
            if (!tabs.contains(active)) active = tabs.isEmpty() ? null : tabs.get(0);
            return new WorkspaceSession(project, tabs, active);
        } catch (IOException | IllegalArgumentException e) { return empty; }
    }

    public synchronized void exportZip(String project, OutputStream stream) throws IOException {
        File base = project(project);
        List<String> paths = new ArrayList<>();
        collect(base, base, paths, true);
        if (paths.size() > MAX_ENTRIES) throw new IOException("Export supports up to 1024 entries.");
        long total = 0;
        for (String path : paths) if (!path.endsWith("/")) total += resolve(base, path).length();
        if (total > MAX_ARCHIVE_BYTES) throw new IOException("Project exceeds the 16 MiB archive limit.");
        try (ZipOutputStream zip = new ZipOutputStream(stream)) {
            for (String path : paths) {
                zip.putNextEntry(new ZipEntry(path));
                if (!path.endsWith("/")) Files.copy(resolve(base, path).toPath(), zip);
                zip.closeEntry();
            }
        }
    }

    private static int transfer(InputStream in, OutputStream out, int limit) throws IOException {
        int total = 0;
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            total += n;
            if (total > limit) throw new IOException("Archive exceeds the allowed size.");
            out.write(buffer, 0, n);
        }
        return total;
    }

    public synchronized String importZip(String name, InputStream stream) throws IOException {
        File dest = new File(projects, segment(name));
        requireNew(dest);
        File stage = Files.createTempDirectory(scratch.toPath(), "import-").toFile();
        File archive = File.createTempFile("archive-", ".zip", scratch);
        try {
            try (FileOutputStream out = new FileOutputStream(archive)) { transfer(stream, out, MAX_ARCHIVE_BYTES + 1024 * 1024); }
            try (ZipFile zip = new ZipFile(archive)) {
                if (zip.size() > MAX_ENTRIES) throw new IOException("Archive contains too many entries.");
                Set<String> seen = new HashSet<>();
                int left = MAX_ARCHIVE_BYTES;
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String path = entry.getName();
                    if (entry.isDirectory()) path = path.substring(0, path.length() - 1);
                    File file = resolve(stage, path);
                    if (!seen.add(path)) throw new IOException("Duplicate archive entry.");
                    if (entry.isDirectory()) { ensureDir(file); continue; }
                    requireNew(file);
                    ensureDir(file.getParentFile());
                    try (InputStream in = zip.getInputStream(entry); FileOutputStream out = new FileOutputStream(file)) {
                        java.util.zip.CheckedInputStream verified = new java.util.zip.CheckedInputStream(in, new java.util.zip.CRC32());
                        int size = transfer(verified, out, Math.min(MAX_FILE_BYTES, left));
                        if (size != entry.getSize() || verified.getChecksum().getValue() != entry.getCrc())
                            throw new IOException("Damaged archive entry.");
                        left -= size;
                    }
                }
            }
            moveNew(stage, dest);
            return name;
        } finally { clean(stage); archive.delete(); }
    }
}
