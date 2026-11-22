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
# Desktop-only and optional integrations in the bundled JVM runtimes are not
# available on Android. They are not invoked by the Android execution paths.
-dontwarn java.applet.**
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn java.lang.ProcessHandle
-dontwarn java.lang.management.**
-dontwarn javax.management.**
-dontwarn javax.script.**
-dontwarn javax.servlet.**
-dontwarn javax.swing.**
-dontwarn org.apache.bcel.**
-dontwarn org.apache.bsf.**
-dontwarn org.ietf.jgss.**
