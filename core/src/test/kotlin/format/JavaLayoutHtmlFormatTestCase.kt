package org.jetbrains.dokka.tests

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.name.Names
import org.jetbrains.dokka.DocumentationNode
import org.jetbrains.dokka.DocumentationOptions
import org.jetbrains.dokka.DokkaLogger
import org.jetbrains.dokka.Formats.JavaLayoutHtmlFormatDescriptorBase
import org.jetbrains.dokka.Formats.JavaLayoutHtmlFormatGenerator
import org.jetbrains.dokka.Generator
import org.jetbrains.dokka.Utilities.bind
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.URI

abstract class JavaLayoutHtmlFormatTestCase {

    abstract val formatDescriptor: JavaLayoutHtmlFormatDescriptorBase

    @get:Rule
    var folder = TemporaryFolder()

    var options =
        DocumentationOptions(
            "",
            "java-layout-html",
            apiVersion = null,
            languageVersion = null,
            generateIndexPages = false,
            noStdlibLink = false
        )

    val injector: Injector by lazy {
        Guice.createInjector(Module { binder ->
            binder.bind<File>().annotatedWith(Names.named("outputDir")).toInstance(folder.apply { create() }.root)

            binder.bind<DocumentationOptions>().toProvider { options }
            binder.bind<DokkaLogger>().toInstance(object : DokkaLogger {
                override fun info(message: String) {
                    println(message)
                }

                override fun warn(message: String) {
                    println("WARN: $message")
                }

                override fun error(message: String) {
                    println("ERROR: $message")
                }

            })

            formatDescriptor.configureOutput(binder)
        })
    }


    fun buildPagesAndReadInto(model: DocumentationNode, nodes: List<DocumentationNode>, sb: StringBuilder) =
        with(injector.getInstance(Generator::class.java)) {
            this as JavaLayoutHtmlFormatGenerator
            buildPages(listOf(model))
            val byLocations = nodes.groupBy { mainUri(it) }
            byLocations.forEach { (loc, _) ->
                sb.appendln("<!-- File: $loc -->")
                sb.append(folder.root.toURI().resolve(URI("/").relativize(loc)).toURL().readText())
            }
        }

}