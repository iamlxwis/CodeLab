package site.lxwis.codelab

import androidx.compose.ui.graphics.Color

internal data class LanguageVisual(val logo: Int?, val color: Color, val monogram: String? = null)

private val languageVisuals = mapOf(
    "js" to LanguageVisual(R.drawable.ic_lang_javascript, Color(0xfff7df1e)),
    "lua" to LanguageVisual(R.drawable.ic_lang_lua, Color(0xff2b3eab)),
    "java" to LanguageVisual(R.drawable.ic_lang_java, Color(0xffe76f00)),
    "sql" to LanguageVisual(R.drawable.ic_lang_sqlite, Color(0xff1d83b5)),
    "html" to LanguageVisual(R.drawable.ic_lang_html5, Color(0xffe34f26)),
    "css" to LanguageVisual(R.drawable.ic_lang_css, Color(0xff663399)),
    "py" to LanguageVisual(R.drawable.ic_lang_python, Color(0xff3776ab)),
    "c" to LanguageVisual(R.drawable.ic_lang_c, Color(0xff466c9a)),
    "cpp" to LanguageVisual(R.drawable.ic_lang_cplusplus, Color(0xff00599c)),
    "cs" to LanguageVisual(R.drawable.ic_lang_cs, Color(0xff68217a)),
    "ts" to LanguageVisual(R.drawable.ic_lang_typescript, Color(0xff3178c6)),
    "kt" to LanguageVisual(R.drawable.ic_lang_kotlin, Color(0xff7f52ff)),
    "rs" to LanguageVisual(R.drawable.ic_lang_rust, Color(0xffa74528)),
    "go" to LanguageVisual(R.drawable.ic_lang_go, Color(0xff00a7ca)),
    "php" to LanguageVisual(R.drawable.ic_lang_php, Color(0xff777bb4)),
    "rb" to LanguageVisual(R.drawable.ic_lang_ruby, Color(0xffcc342d)),
    "swift" to LanguageVisual(R.drawable.ic_lang_swift, Color(0xfff05138)),
    "sh" to LanguageVisual(R.drawable.ic_lang_gnubash, Color(0xff438944)),
    "dart" to LanguageVisual(R.drawable.ic_lang_dart, Color(0xff0175c2)),
    "scala" to LanguageVisual(R.drawable.ic_lang_scala, Color(0xffde3423)),
    "pl" to LanguageVisual(R.drawable.ic_lang_pl, Color(0xff212177)),
    "hs" to LanguageVisual(R.drawable.ic_lang_hs, Color(0xff5e5185)),
    "r" to LanguageVisual(R.drawable.ic_lang_r, Color(0xff1f65b7)),
    "ex" to LanguageVisual(R.drawable.ic_lang_ex, Color(0xff380a4d)),
    "erl" to LanguageVisual(R.drawable.ic_lang_erl, Color(0xffa90533)),
    "ml" to LanguageVisual(R.drawable.ic_lang_ml, Color(0xfff18803)),
    "f90" to LanguageVisual(R.drawable.ic_lang_f90, Color(0xff734f96)),
    "cob" to LanguageVisual(R.drawable.ic_lang_cob, Color(0xff000000)),
    "pas" to LanguageVisual(R.drawable.ic_lang_pas, Color(0xffe78a33)),
    "asm" to LanguageVisual(R.drawable.ic_lang_asm, Color(0xff4f708b)),
    "m" to LanguageVisual(R.drawable.ic_lang_m, Color(0xff0b5a9d)),
    "groovy" to LanguageVisual(R.drawable.ic_lang_groovy, Color(0xff619cbc)),
    "fs" to LanguageVisual(R.drawable.ic_lang_fs, Color(0xff378bba)),
    "vb" to LanguageVisual(R.drawable.ic_lang_vb, Color(0xff004e8c)),
    "plg" to LanguageVisual(R.drawable.ic_lang_plg, Color(0xfff46c30)),
    "jl" to LanguageVisual(R.drawable.ic_lang_jl, Color(0xff28a745)),
    "zig" to LanguageVisual(R.drawable.ic_lang_zig, Color(0xfff7a41d)),
    "nim" to LanguageVisual(R.drawable.ic_lang_nim, Color(0xffffe953)),
    "cr" to LanguageVisual(R.drawable.ic_lang_cr, Color(0xff000000)),
    "d" to LanguageVisual(R.drawable.ic_lang_d, Color(0xffba595e)),
    "adb" to LanguageVisual(R.drawable.ic_lang_adb, Color(0xff02f88c)),
    "lisp" to LanguageVisual(R.drawable.ic_lang_lisp, Color(0xff855cb0)),
    "scm" to LanguageVisual(R.drawable.ic_lang_scm, Color(0xff9f1d21)),
    "clj" to LanguageVisual(R.drawable.ic_lang_clj, Color(0xff5881d8)),
    "cljs" to LanguageVisual(R.drawable.ic_lang_cljs, Color(0xff96ca4b)),
    "elm" to LanguageVisual(R.drawable.ic_lang_elm, Color(0xff34495e)),
    "purs" to LanguageVisual(R.drawable.ic_lang_purs, Color(0xff7259a6)),
    "re" to LanguageVisual(R.drawable.ic_lang_re, Color(0xffdd4b39)),
    "res" to LanguageVisual(R.drawable.ic_lang_res, Color(0xffe6484f)),
    "ps1" to LanguageVisual(R.drawable.ic_lang_ps1, Color(0xff1e2a3a)),
    "fish" to LanguageVisual(R.drawable.ic_lang_fish, Color(0xff4075a5)),
    "zsh" to LanguageVisual(R.drawable.ic_lang_zsh, Color(0xffe85b22)),
    "tcl" to LanguageVisual(R.drawable.ic_lang_tcl, Color(0xffb52034)),
    "awk" to LanguageVisual(R.drawable.ic_lang_awk, Color(0xff0a094d)),
    "sol" to LanguageVisual(R.drawable.ic_lang_sol, Color(0xff77758f)),
    "move" to LanguageVisual(null, Color(0xff3b8568), "MV"),
    "vhd" to LanguageVisual(null, Color(0xff85503f), "VHD"),
    "v" to LanguageVisual(R.drawable.ic_lang_v, Color(0xff37767c)),
    "sv" to LanguageVisual(R.drawable.ic_lang_sv, Color(0xff4874a2)),
    "cu" to LanguageVisual(R.drawable.ic_lang_cu, Color(0xff76b900)),
    "glsl" to LanguageVisual(R.drawable.ic_lang_glsl, Color(0xff5586a4)),
    "hlsl" to LanguageVisual(null, Color(0xff694c92), "HLS"),
    "graphql" to LanguageVisual(R.drawable.ic_lang_graphql, Color(0xffe434aa)),
    "tex" to LanguageVisual(R.drawable.ic_lang_tex, Color(0xff000000)),
    "nix" to LanguageVisual(R.drawable.ic_lang_nix, Color(0xff5277c3)),
    "hcl" to LanguageVisual(R.drawable.ic_lang_hcl, Color(0xff7546c3)),
    "yaml" to LanguageVisual(R.drawable.ic_lang_yaml, Color(0xffcb171e)),
    "toml" to LanguageVisual(R.drawable.ic_lang_toml, Color(0xff9c4121)),
    "json" to LanguageVisual(R.drawable.ic_lang_json, Color(0xff766328)),
    "xml" to LanguageVisual(R.drawable.ic_lang_xml, Color(0xff005fad)),
    "md" to LanguageVisual(R.drawable.ic_lang_md, Color(0xff000000)),
    "sass" to LanguageVisual(R.drawable.ic_lang_sass, Color(0xffcc6699)),
    "scss" to LanguageVisual(R.drawable.ic_lang_scss, Color(0xffb34780)),
    "less" to LanguageVisual(R.drawable.ic_lang_less, Color(0xff294683)),
    "svelte" to LanguageVisual(R.drawable.ic_lang_svelte, Color(0xffff3e00)),
    "vue" to LanguageVisual(R.drawable.ic_lang_vue, Color(0xff41b883)),
    "astro" to LanguageVisual(R.drawable.ic_lang_astro, Color(0xffff5d01)),
    "jsx" to LanguageVisual(R.drawable.ic_lang_jsx, Color(0xff61dafb)),
    "tsx" to LanguageVisual(R.drawable.ic_lang_tsx, Color(0xff3178c6)),
    "pug" to LanguageVisual(R.drawable.ic_lang_pug, Color(0xff56332b)),
    "hbs" to LanguageVisual(R.drawable.ic_lang_hbs, Color(0xff000000)),
    "dockerfile" to LanguageVisual(R.drawable.ic_lang_dockerfile, Color(0xff019bc6)),
    "mk" to LanguageVisual(R.drawable.ic_lang_mk, Color(0xff5c745b)),
    "ini" to LanguageVisual(null, Color(0xff6c7078), "INI"),
    "bas" to LanguageVisual(null, Color(0xffb06535), "BAS"),
    "st" to LanguageVisual(null, Color(0xff815eb0), "ST"),
    "apl" to LanguageVisual(R.drawable.ic_lang_apl, Color(0xff24a148)),
    "fth" to LanguageVisual(R.drawable.ic_lang_fth, Color(0xff8d4248)),
    "wl" to LanguageVisual(R.drawable.ic_lang_wl, Color(0xffd71816)),
    "idr" to LanguageVisual(R.drawable.ic_lang_idr, Color(0xff6a3b7e)),
    "agda" to LanguageVisual(null, Color(0xff508094), "AGD"),
    "lean" to LanguageVisual(R.drawable.ic_lang_lean, Color(0xff4684c6)),
    "pony" to LanguageVisual(null, Color(0xff926c26), "PNY"),
    "gd" to LanguageVisual(R.drawable.ic_lang_gd, Color(0xff478cbf)),
    "qs" to LanguageVisual(R.drawable.ic_lang_qs, Color(0xff7645a2)),
)

internal fun languageVisual(extension: String): LanguageVisual? = languageVisuals[when (extension) {
    "mjs", "cjs" -> "js"
    "pyw" -> "py"
    "kts" -> "kt"
    "htm" -> "html"
    "sqlite", "db" -> "sql"
    "cc", "cxx", "hpp", "hxx" -> "cpp"
    "h" -> "c"
    "jsonl" -> "json"
    "bash" -> "sh"
    "yml" -> "yaml"
    "f", "f95" -> "f90"
    else -> extension
}]
