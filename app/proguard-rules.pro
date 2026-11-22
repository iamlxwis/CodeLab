# Keep reflection-driven language runtimes and their transitive plugin entry points.
# Review individual keeps with R8 usage reports after establishing stable signing.
-keep class org.mozilla.javascript.** { *; }
-keep class org.luaj.vm2.** { *; }
-keep class bsh.** { *; }
-keep class org.eclipse.jgit.** { *; }
-keep class org.eclipse.jgit.internal.** { *; }
-keep class site.lxwis.codelab.*Engine { *; }
-keep class site.lxwis.codelab.*RuntimeService { *; }
-keep class site.lxwis.codelab.RunSession { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
-dontwarn org.slf4j.**
