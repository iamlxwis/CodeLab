package site.lxwis.codelab;

import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Judge0-compatible online compilation. No source is sent until the user confirms a cloud run. */
public final class CloudJudge0 {
    public static final String DEFAULT_ENDPOINT = "https://ce.judge0.com";

    public static final class Language {
        public final int id;
        public final String name;
        public Language(int id, String name) { this.id = id; this.name = name; }
    }

    public static final class Result {
        public final int exitCode;
        public final String output;
        public final String status;
        public Result(int code, String output, String status) {
            this.exitCode = code; this.output = output; this.status = status;
        }
    }

    public static String validateUrl(String raw) {
        try {
            URI uri = new URI(raw.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getHost().isEmpty() || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null || uri.getPort() > 65535)
                throw new IllegalArgumentException("Use a valid HTTPS Judge0 endpoint.");
            String url = uri.toString().replaceAll("/+$", "");
            if (url.length() > 256) throw new IllegalArgumentException("Endpoint URL is too long.");
            return url;
        } catch (Exception error) {
            throw new IllegalArgumentException("Use a valid HTTPS Judge0 endpoint.");
        }
    }

    public static String extension(String filename) {
        String ext = filename.toLowerCase(java.util.Locale.ROOT).replaceAll("^.*\\.", "");
        if (ext.equals("cc") || ext.equals("cxx")) return "cpp";
        if (ext.equals("mjs")) return "js";
        if (ext.equals("kts")) return "kt";
        return ext;
    }

    /** Judge0 language names are checked at runtime; never assume stable numerical IDs. */
    static String detect(String value) {
        String n = value.trim().toLowerCase(java.util.Locale.ROOT);
        if (n.startsWith("c++")) return "cpp";
        if (n.startsWith("c#")) return "cs";
        if (n.startsWith("c (") || n.equals("c")) return "c";
        if (n.startsWith("javascript")) return "js";
        if (n.startsWith("typescript")) return "ts";
        if (n.startsWith("java (") || n.equals("java")) return "java";
        if (n.startsWith("python")) return "py";
        if (n.startsWith("kotlin")) return "kt";
        if (n.startsWith("rust")) return "rs";
        if (n.startsWith("go (") || n.equals("go")) return "go";
        if (n.startsWith("ruby")) return "rb";
        if (n.startsWith("php")) return "php";
        if (n.startsWith("swift")) return "swift";
        if (n.startsWith("dart")) return "dart";
        if (n.startsWith("scala")) return "scala";
        if (n.startsWith("bash")) return "sh";
        if (n.startsWith("perl")) return "pl";
        if (n.startsWith("haskell")) return "hs";
        if (n.startsWith("lua")) return "lua";
        if (n.startsWith("r (") || n.equals("r")) return "r";
        return null;
    }

    public static Map<String, Language> fetchLanguages(String url, String header, String credential) throws IOException {
        HttpURLConnection conn = connect(url, "/languages", "GET", header, credential);
        try {
            JSONArray languages = new JSONArray(readBody(conn));
            Map<String, Language> found = new LinkedHashMap<>();
            for (int i = 0; i < languages.length(); i++) {
                JSONObject entry = languages.optJSONObject(i);
                if (entry == null) continue;
                String name = entry.optString("name");
                int id = entry.optInt("id", -1);
                String ext = detect(name);
                if (id > 0 && ext != null && !found.containsKey(ext))
                    found.put(ext, new Language(id, name));
            }
            if (found.isEmpty()) throw new IOException("No supported languages returned by this Judge0 server.");
            return Collections.unmodifiableMap(found);
        } catch (JSONException e) {
            throw new IOException("Compiler endpoint returned an invalid language list.", e);
        } finally { conn.disconnect(); }
    }

    public static Result execute(String url, String header, String credential, Language language,
                                 String source, String stdin) throws IOException, InterruptedException {
        if (language == null || language.id <= 0) throw new IOException("No verified cloud runtime for this language.");
        if (source.length() > 128000) throw new IOException("Online scripts are limited to 128,000 characters.");
        JSONObject input = new JSONObject();
        try {
            input.put("language_id", language.id);
            input.put("source_code", encode(source));
            input.put("stdin", encode(stdin));
        } catch (JSONException ex) { throw new IOException("Could not serialize source code.", ex); }
        HttpURLConnection post = connect(url, "/submissions?base64_encoded=true&wait=false", "POST", header, credential);
        String token;
        try {
            byte[] body = input.toString().getBytes(StandardCharsets.UTF_8);
            post.setDoOutput(true);
            post.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            post.setFixedLengthStreamingMode(body.length);
            try (OutputStream out = post.getOutputStream()) { out.write(body); }
            JSONObject response = new JSONObject(readBody(post));
            token = response.optString("token");
            if (!token.matches("[A-Za-z0-9_-]{8,128}")) throw new IOException("Compiler did not return a submission token.");
        } catch (JSONException e) {
            throw new IOException("Compiler returned an invalid submission response.", e);
        } finally { post.disconnect(); }
        long started = System.nanoTime();
        while (!Thread.currentThread().isInterrupted() &&
                (System.nanoTime() - started) < 50_000_000_000L) {
            Thread.sleep(800);
            HttpURLConnection poll = connect(url, "/submissions/" + token + "?base64_encoded=true",
                "GET", header, credential);
            try {
                JSONObject response = new JSONObject(readBody(poll));
                JSONObject status = response.optJSONObject("status");
                int statusId = status == null ? response.optInt("status_id", 0) : status.optInt("id");
                if (statusId <= 2) continue;
                String description = status == null ? "Finished" : status.optString("description", "Finished");
                StringBuilder transcript = new StringBuilder();
                appendField(transcript, response, "stdout", "");
                appendField(transcript, response, "compile_output", "Compiler: ");
                appendField(transcript, response, "stderr", "Error: ");
                appendField(transcript, response, "message", "Runtime: ");
                if (transcript.length() == 0) transcript.append("No output\n");
                int code = response.isNull("exit_code") ? (statusId == 3 ? 0 : 1) :
                    response.optInt("exit_code", statusId == 3 ? 0 : 1);
                return new Result(code, transcript.toString(), description);
            } catch (JSONException e) {
                throw new IOException("Compiler returned invalid result JSON.", e);
            } finally { poll.disconnect(); }
        }
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Online run cancelled.");
        throw new IOException("Online compilation exceeded the request time limit.");
    }

    private static void appendField(StringBuilder dst, JSONObject response, String field, String prefix) {
        String encoded = response.optString(field, "");
        if (encoded == null || encoded.isEmpty() || "null".equals(encoded)) return;
        try {
            String result = new String(Base64.decode(encoded, Base64.DEFAULT), StandardCharsets.UTF_8);
            if (!result.isEmpty()) {
                dst.append(prefix).append(result, 0, Math.min(result.length(), 64000 - Math.min(64000, dst.length())));
                if (dst.length() != 0 && dst.charAt(dst.length() - 1) != '\n') dst.append('\n');
            }
        } catch (IllegalArgumentException ignored) { dst.append(prefix).append("Output encoding error\n"); }
    }

    private static String encode(String text) {
        return Base64.encodeToString(text.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }

    private static HttpURLConnection connect(String url, String path, String method,
                                             String header, String credential) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(validateUrl(url) + path).openConnection();
        c.setRequestMethod(method);
        c.setInstanceFollowRedirects(false);
        c.setConnectTimeout(8000);
        c.setReadTimeout(15000);
        c.setRequestProperty("Accept", "application/json");
        if (credential != null && !credential.isEmpty()) {
            if (!header.equals("X-Auth-Token") && !header.equals("Authorization") &&
                    !header.equals("X-RapidAPI-Key")) throw new IOException("Unsupported authentication header.");
            c.setRequestProperty(header, credential);
            if (header.equals("X-RapidAPI-Key"))
                c.setRequestProperty("X-RapidAPI-Host", new URL(validateUrl(url)).getHost());
        }
        return c;
    }

    private static String readBody(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300)
            throw new IOException("Compiler returned HTTP " + status +
                (status == 401 || status == 403 ? " — check authentication" : ""));
        try (InputStream stream = connection.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = stream.read(buf)) != -1) {
                if (out.size() + n > 512000) throw new IOException("Compiler response is too large.");
                out.write(buf, 0, n);
            }
            return out.toString("UTF-8");
        }
    }
    private CloudJudge0() {}
}
