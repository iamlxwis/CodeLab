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
    private final File root;
    private final File projects;
    private final File scratch;
    private final File session;

    public ProjectStore(File root) {
        this.root = root;
        projects = new File(root, "projects");
        scratch = new File(root, "pending");
        session = new File(root, "session.properties");
        ensureDir(projects);
        ensureDir(scratch);
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
        File dest = new File(projects, segment(name));
        requireNew(dest);
        File stage = Files.createTempDirectory(scratch.toPath(), "project-").toFile();
        try {
            String starter = "// Run this file, then enter your name in the console.\n"
                    + "const name = readLine(\"What is your name? \");\n"
                    + "print(\"Hello, \" + name + \"!\");\n";
            writeText(new File(stage, "main.js"), starter);
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

    public synchronized void deleteFile(String project, String path) throws IOException {
        discard(resolve(project(project), path));
    }

    public synchronized void renameProject(String from, String to) throws IOException {
        moveNew(project(from), new File(projects, segment(to)));
    }

    public synchronized void deleteProject(String name) throws IOException { discard(project(name)); }

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
