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
    private static final int MAX_ASSET_BYTES = 2 * 1024 * 1024;
    private static final Set<String> BINARY_EXTENSIONS = new HashSet<>(Arrays.asList(
        "png", "jpg", "jpeg", "gif", "webp", "ico", "bmp", "avif",
        "woff", "woff2", "ttf", "otf", "mp3", "ogg", "wav", "mp4", "webm"));

    public static boolean isBinaryAsset(String path) {
        String extension = path.substring(path.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return BINARY_EXTENSIONS.contains(extension);
    }

    public static int importLimitFor(String path) {
        return isBinaryAsset(path) ? MAX_ASSET_BYTES : MAX_FILE_BYTES;
    }
    private static final int MAX_ARCHIVE_BYTES = 16 * 1024 * 1024;
    private static final int MAX_ENTRIES = 1024;
    private final File projects;
    private final File scratch;
    private final File trash;
    private final File history;
    private final File session;

    public ProjectStore(File root) {
        projects = new File(root, "projects");
        scratch = new File(root, "pending");
        trash = new File(root, "trash");
        history = new File(root, "history");
        session = new File(root, "session.properties");
        ensureDir(projects);
        ensureDir(scratch);
        ensureDir(trash);
        ensureDir(history);
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

    public synchronized File projectDirectory(String name) { return project(name); }

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
            if (".git".equals(child.getName()) || ".codelab".equals(child.getName())) continue;
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

    public static final class ProjectMetadata {
        public final String template, description, entrypoint;
        public ProjectMetadata(String template, String description, String entrypoint) {
            this.template = template;
            this.description = description;
            this.entrypoint = entrypoint;
        }
    }

    private static ProjectMetadata readMetadata(File dir) throws IOException {
        File record = new File(dir, ".codelab/project.properties");
        if (!record.isFile()) return new ProjectMetadata("Imported", "", "");
        if (record.length() > 8192) throw new IOException("Project settings are too large.");
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(record.toPath(), StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return new ProjectMetadata(props.getProperty("template", "Imported"),
            props.getProperty("description", ""), props.getProperty("entrypoint", ""));
    }

    private void writeMetadata(File dir, ProjectMetadata metadata) throws IOException {
        Properties props = new Properties();
        props.setProperty("template", metadata.template);
        props.setProperty("description", metadata.description);
        props.setProperty("entrypoint", metadata.entrypoint);
        StringWriter writer = new StringWriter();
        props.store(writer, "CodeLab project metadata");
        writeText(new File(dir, ".codelab/project.properties"), writer.toString());
    }

    public synchronized ProjectMetadata metadata(String projectName) throws IOException {
        return readMetadata(project(projectName));
    }

    public synchronized ProjectMetadata configureProject(String projectName,
            String description, String entrypoint) throws IOException {
        if (description == null || description.length() > 500)
            throw new IOException("Description must be 500 characters or fewer.");
        String target = entrypoint == null ? "" : entrypoint;
        if (!target.isEmpty() && !files(projectName).contains(target))
            throw new IOException("Select an existing file as the entrypoint.");
        ProjectMetadata previous = metadata(projectName);
        ProjectMetadata updated = new ProjectMetadata(previous.template, description, target);
        writeMetadata(project(projectName), updated);
        return updated;
    }

    /** Reuse the bounded and path-validated archive pipeline for a safe project copy. */
    public synchronized void duplicateProject(String from, String to) throws IOException {
        ByteArrayOutputStream archive = new ByteArrayOutputStream();
        exportZip(from, archive);
        importZip(to, new ByteArrayInputStream(archive.toByteArray()));
    }

    public synchronized String createProject(String name, String template) throws IOException {
        if (!"Web".equals(template) && LanguageCatalog.extensionFor(template) == null)
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
            } else if ("JavaScript".equals(template)) {
                writeText(new File(stage, "main.js"),
                    "// Run this file, then enter your name in the console.\n" +
                    "const name = readLine(\"What is your name? \");\n" +
                    "print(\"Hello, \" + name + \"!\");\n");
            } else {
                // Added languages receive an empty, correctly extended source file.
                // File editing is available offline, execution is not implied.
                String extension = LanguageCatalog.extensionFor(template);
                if (extension == null) throw new IllegalArgumentException("Unsupported project template.");
                String fileName = ("Dockerfile".equals(template) || "Makefile".equals(template))
                    ? template : "main." + extension;
                writeText(new File(stage, fileName), "");
            }
            List<String> sourceFiles = new ArrayList<>();
            collect(stage, stage, sourceFiles, false);
            writeMetadata(stage, new ProjectMetadata(template, "",
                sourceFiles.isEmpty() ? "" : sourceFiles.get(0)));
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
        writeText(target, text, false);
    }

    private void writeText(File target, String text, boolean durable) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_FILE_BYTES) throw new IOException("Editor files are limited to 256 KiB.");
        ensureDir(target.getParentFile());
        File tmp = File.createTempFile("save-", ".tmp", scratch);
        try {
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                out.write(bytes);
                if (durable) out.getFD().sync();
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

    private void writeBytes(File target, byte[] bytes, int limit) throws IOException {
        if (bytes.length > limit) throw new IOException("Imported asset is too large.");
        ensureDir(target.getParentFile());
        File tmp = File.createTempFile("asset-", ".tmp", scratch);
        try {
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                out.write(bytes);
                out.getFD().sync();
            }
            Files.move(tmp.toPath(), target.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally { tmp.delete(); }
    }

    public synchronized byte[] readAsset(String projectName, String path) throws IOException {
        File target = resolve(project(projectName), path);
        if (!target.isFile()) throw new IOException("Asset does not exist.");
        if (target.length() > importLimitFor(path))
            throw new IOException("Asset exceeds its supported size.");
        return Files.readAllBytes(target.toPath());
    }

    public synchronized String read(String project, String path) throws IOException {
        File target = resolve(project(project), path);
        if (!target.isFile()) throw new IOException("File does not exist.");
        if (isBinaryAsset(path)) throw new IOException("Binary assets cannot be edited as text.");
        if (target.length() > MAX_FILE_BYTES) throw new IOException("Editor files are limited to 256 KiB.");
        return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(Files.readAllBytes(target.toPath()))).toString();
    }

    private File historyDirectory(String projectName, String path) throws IOException {
        resolve(project(projectName), path);
        try {
            byte[] key = (projectName + (char) 0 + path).getBytes(StandardCharsets.UTF_8);
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(key);
            StringBuilder hex = new StringBuilder();
            for (byte part : digest) hex.append(String.format(Locale.ROOT, "%02x", part & 255));
            return new File(history, hex.toString());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("Unable to index file history.", e);
        }
    }

    public static final class FileVersion {
        public final String id;
        public final long savedAt;
        public final long bytes;
        public FileVersion(String id, long savedAt, long bytes) {
            this.id = id;
            this.savedAt = savedAt;
            this.bytes = bytes;
        }
    }

    public synchronized List<FileVersion> versions(String projectName, String path)
            throws IOException {
        File dir = historyDirectory(projectName, path);
        File[] files = dir.listFiles((base, name) -> name.matches("[0-9]{13}-[a-f0-9-]{36}\\.txt"));
        List<FileVersion> result = new ArrayList<>();
        if (files != null) for (File file : files) {
            result.add(new FileVersion(file.getName(), Long.parseLong(file.getName().substring(0, 13)), file.length()));
        }
        result.sort((a, b) -> Long.compare(b.savedAt, a.savedAt));
        return result;
    }

    private void snapshot(String projectName, String path, File source, boolean forced)
            throws IOException {
        File dir = historyDirectory(projectName, path);
        List<FileVersion> existing = versions(projectName, path);
        if (!forced && !existing.isEmpty()
                && System.currentTimeMillis() - existing.get(0).savedAt < 20000L) return;
        ensureDir(dir);
        File snap = new File(dir, System.currentTimeMillis() + "-" + UUID.randomUUID() + ".txt");
        Files.copy(source.toPath(), snap.toPath());
        existing = versions(projectName, path);
        for (int i = 12; i < existing.size(); i++) {
            if (!new File(dir, existing.get(i).id).delete()) throw new IOException("History cleanup failed.");
        }
    }

    public synchronized String readVersion(String projectName, String path, String versionId)
            throws IOException {
        if (versionId == null || !versionId.matches("[0-9]{13}-[a-f0-9-]{36}\\.txt"))
            throw new IOException("Invalid history identifier.");
        File version = new File(historyDirectory(projectName, path), versionId);
        if (!version.isFile() || version.length() > MAX_FILE_BYTES)
            throw new IOException("History snapshot no longer exists.");
        return StandardCharsets.UTF_8.newDecoder()
            .decode(ByteBuffer.wrap(Files.readAllBytes(version.toPath()))).toString();
    }

    public synchronized void restoreVersion(String projectName, String path, String versionId)
            throws IOException {
        String previous = readVersion(projectName, path, versionId);
        File target = resolve(project(projectName), path);
        snapshot(projectName, path, target, true);
        writeText(target, previous);
    }

    public synchronized void save(String project, String path, String text) throws IOException {
        save(project, path, text, false);
    }

    public synchronized void save(String project, String path, String text, boolean durable) throws IOException {
        File target = resolve(project(project), path);
        if (!target.isFile()) throw new IOException("File does not exist.");
        if (read(project, path).equals(text)) {
            // Autosave can have renamed the file without fsync. A lifecycle/explicit
            // flush must still durably sync that previously written content.
            if (durable) {
                try (RandomAccessFile fd = new RandomAccessFile(target, "rw")) { fd.getFD().sync(); }
            }
            return;
        }
        snapshot(project, path, target, false);
        writeText(target, text, durable);
    }

    public synchronized void importSingleFile(String project, String path, InputStream input) throws IOException {
        File target = resolve(project(project), path);
        requireNew(target);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transfer(input, output, importLimitFor(path));
        byte[] bytes = output.toByteArray();
        if (isBinaryAsset(path)) {
            writeBytes(target, bytes, MAX_ASSET_BYTES);
            return;
        }
        final String content;
        if (bytes.length >= 2 && (bytes[0] & 255) == 255 && (bytes[1] & 255) == 254) {
            content = StandardCharsets.UTF_16LE.newDecoder()
                .decode(ByteBuffer.wrap(bytes, 2, bytes.length - 2)).toString();
        } else if (bytes.length >= 2 && (bytes[0] & 255) == 254 && (bytes[1] & 255) == 255) {
            content = StandardCharsets.UTF_16BE.newDecoder()
                .decode(ByteBuffer.wrap(bytes, 2, bytes.length - 2)).toString();
        } else {
            int start = bytes.length >= 3 && (bytes[0] & 255) == 239
                && (bytes[1] & 255) == 187 && (bytes[2] & 255) == 191 ? 3 : 0;
            content = StandardCharsets.UTF_8.newDecoder()
                .decode(ByteBuffer.wrap(bytes, start, bytes.length - start)).toString();
        }
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
        writeBytes(target, readAsset(fromProject, path), importLimitFor(destination));
    }

    public synchronized void duplicateFile(String project, String from, String to) throws IOException {
        File base = project(project);
        File source = resolve(base, from);
        File destination = resolve(base, to);
        if (!source.isFile()) throw new IOException("Source file does not exist.");
        requireNew(destination);
        writeBytes(destination, readAsset(project, from), importLimitFor(to));
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

    public static final class ReplacementSummary {
        public final int files, matches;
        public final List<String> paths;
        public ReplacementSummary(int files, int matches, List<String> paths) {
            this.files = files;
            this.matches = matches;
            this.paths = Collections.unmodifiableList(new ArrayList<>(paths));
        }
    }

    /** Preflight all edits before writing. Replacement text is always literal. */
    public synchronized ReplacementSummary replaceAcrossProject(
            String projectName, String query, String replacement, boolean caseSensitive,
            boolean regex, boolean apply) throws IOException {
        if (query == null || query.trim().isEmpty() || query.length() > 200)
            throw new IOException("Enter a search phrase up to 200 characters.");
        if (replacement == null || replacement.length() > 2000)
            throw new IOException("Replacement is limited to 2000 characters.");
        java.util.regex.Pattern pattern;
        try {
            pattern = java.util.regex.Pattern.compile(regex ? query : java.util.regex.Pattern.quote(query),
                caseSensitive ? 0 : java.util.regex.Pattern.CASE_INSENSITIVE |
                    java.util.regex.Pattern.UNICODE_CASE);
        } catch (java.util.regex.PatternSyntaxException e) {
            throw new IOException("Invalid search pattern: " + e.getDescription());
        }
        if (pattern.matcher("").find())
            throw new IOException("Search patterns matching empty text are unsupported.");

        LinkedHashMap<String, String> original = new LinkedHashMap<>();
        LinkedHashMap<String, String> modified = new LinkedHashMap<>();
        int totalMatches = 0;
        for (String path : files(projectName)) {
            String content;
            try { content = read(projectName, path); }
            catch (IOException e) { continue; }
            java.util.regex.Matcher matcher = pattern.matcher(content);
            StringBuffer output = new StringBuffer();
            int count = 0;
            while (matcher.find()) {
                if (++count > 5000 || ++totalMatches > 50000)
                    throw new IOException("Too many matches to replace safely.");
                matcher.appendReplacement(output,
                    java.util.regex.Matcher.quoteReplacement(replacement));
            }
            if (count == 0) continue;
            matcher.appendTail(output);
            String changed = output.toString();
            if (changed.getBytes(StandardCharsets.UTF_8).length > MAX_FILE_BYTES)
                throw new IOException("Replacement would exceed the file limit in " + path);
            original.put(path, content);
            modified.put(path, changed);
        }

        if (apply) {
            List<String> completed = new ArrayList<>();
            try {
                for (Map.Entry<String, String> entry : modified.entrySet()) {
                    save(projectName, entry.getKey(), entry.getValue());
                    completed.add(entry.getKey());
                }
            } catch (IOException error) {
                for (int i = completed.size() - 1; i >= 0; i--) {
                    String path = completed.get(i);
                    try { writeText(resolve(project(projectName), path), original.get(path)); }
                    catch (IOException rollback) { error.addSuppressed(rollback); }
                }
                throw error;
            }
        }
        return new ReplacementSummary(modified.size(), totalMatches,
            new ArrayList<>(modified.keySet()));
    }

    /** Bounded, offline project-wide literal search. Invalid/binary files are skipped. */
    public synchronized List<SearchHit> searchText(String projectName, String query, boolean caseSensitive)
            throws IOException {
        if (query == null || query.trim().isEmpty() || query.length() > 200)
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
        File metadata = new File(base, ".codelab/project.properties");
        if (metadata.isFile()) {
            paths.add(".codelab/");
            paths.add(".codelab/project.properties");
        }
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
                        int size = transfer(verified, out, Math.min(importLimitFor(path), left));
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
