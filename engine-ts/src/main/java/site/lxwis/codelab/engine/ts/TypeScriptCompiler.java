package site.lxwis.codelab.engine.ts;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.json.JSONObject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Compiles single-file TypeScript in a network-disabled WebView with the bundled official compiler.
 * JavaScript runs separately in the companion's Rhino engine after transpilation.
 * There is no project filesystem access, package manager, or network exposure.
 */
public final class TypeScriptCompiler {
    private final Context context;
    public TypeScriptCompiler(Context context) { this.context = context.getApplicationContext(); }

    public String transpile(String source) throws Exception {
        if (source.length() > 128000) throw new IllegalArgumentException("TypeScript source limit exceeded");
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<String> response = new AtomicReference<>();
        final AtomicReference<WebView> browser = new AtomicReference<>();
        final Handler main = new Handler(Looper.getMainLooper());
        main.post(() -> {
            try {
                WebView view = new WebView(context);
                browser.set(view);
                WebSettings settings = view.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true); // Only app-bundled compiler assets
                settings.setAllowContentAccess(false);
                settings.setAllowFileAccessFromFileURLs(false);
                settings.setAllowUniversalAccessFromFileURLs(false);
                settings.setBlockNetworkLoads(true);
                settings.setDomStorageEnabled(false);
                view.addJavascriptInterface(new Object() {
                    @JavascriptInterface public void complete(String json) {
                        response.set(json);
                        done.countDown();
                    }
                }, "CodeLabBridge");
                view.setWebViewClient(new WebViewClient() {
                    @Override public void onPageFinished(WebView web, String url) {
                        web.evaluateJavascript("window.compileTs(" + JSONObject.quote(source) + ")", null);
                    }
                    @Override public void onReceivedError(WebView web,
                            android.webkit.WebResourceRequest request,
                            android.webkit.WebResourceError error) {
                        if (request.isForMainFrame()) {
                            response.set("{\"error\":\"Bundled TypeScript compiler could not load\"}");
                            done.countDown();
                        }
                    }
                });
                view.loadUrl("file:///android_asset/ts-compiler.html");
            } catch (Throwable failure) {
                response.set("{\"error\":\"TypeScript WebView unavailable\"}");
                done.countDown();
            }
        });
        boolean success = done.await(8, TimeUnit.SECONDS);
        main.post(() -> {
            WebView view = browser.get();
            if (view != null) view.destroy();
        });
        if (!success) throw new IllegalArgumentException("TypeScript transpilation timed out");
        JSONObject result = new JSONObject(response.get());
        String error = result.optString("error", "");
        if (!error.isEmpty()) throw new IllegalArgumentException(error.substring(0, Math.min(400, error.length())));
        String js = result.optString("js", "");
        if (js.length() > 250000) throw new IllegalArgumentException("Generated JavaScript is too large");
        return js;
    }
}
