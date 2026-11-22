package site.lxwis.codelab

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectFileKindTest {
    @Test fun recognizesCommonCodeAndWebFiles() {
        assertEquals(ProjectFileKind.Code, projectFileKind("app/src/Main.KT"))
        assertEquals(ProjectFileKind.Code, projectFileKind("main.js"))
        assertEquals(ProjectFileKind.Markup, projectFileKind("public/index.html"))
        assertEquals(ProjectFileKind.Stylesheet, projectFileKind("assets/theme.css"))
    }

    @Test fun categorizesDataImagesAndDocuments() {
        assertEquals(ProjectFileKind.Data, projectFileKind("config/settings.json"))
        assertEquals(ProjectFileKind.Image, projectFileKind("images/logo.PNG"))
        assertEquals(ProjectFileKind.Document, projectFileKind("README.md"))
        assertEquals(ProjectFileKind.Other, projectFileKind("LICENSE"))
    }

    @Test fun doesNotMistakeFolderExtensionsForFileExtensions() {
        assertEquals(ProjectFileKind.Code, projectFileKind("components.css/Main.java"))
    }
}
