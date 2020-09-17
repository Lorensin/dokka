@file:Suppress("FunctionName")

package org.jetbrains.dokka.analysis

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaConfiguration.DokkaSourceSet
import org.jetbrains.dokka.DokkaSourceSetID
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.utilities.DokkaLogger

fun KotlinAnalysis(configuration: DokkaConfiguration, logger: DokkaLogger): KotlinAnalysis {
    val environments = configuration.sourceSets.associateWith { sourceSet ->
        createEnvironmentAndFacade(
            logger = logger,
            configuration = configuration,
            sourceSet = sourceSet
        )
    }

    return KotlinAnalysisImpl(environments)
}

@Deprecated(message = "Construct using DokkaConfiguration and logger",
    replaceWith = ReplaceWith("KotlinAnalysis(context.configuration, context.logger)")
)
fun KotlinAnalysis(context: DokkaContext): KotlinAnalysis = KotlinAnalysis(context.configuration, context.logger)

interface KotlinAnalysis : SourceSetDependent<EnvironmentAndFacade> {
    override fun get(key: DokkaSourceSet): EnvironmentAndFacade
    operator fun get(sourceSetID: DokkaSourceSetID): EnvironmentAndFacade
}

internal class KotlinAnalysisImpl(
    private val environments: SourceSetDependent<EnvironmentAndFacade>
) : KotlinAnalysis, SourceSetDependent<EnvironmentAndFacade> by environments {

    override fun get(key: DokkaSourceSet): EnvironmentAndFacade {
        return environments[key] ?: throw IllegalStateException("Missing EnvironmentAndFacade for sourceSet $key")
    }

    override fun get(sourceSetID: DokkaSourceSetID): EnvironmentAndFacade {
        return environments.entries.first { (sourceSet, _) -> sourceSet.sourceSetID == sourceSetID }.value
    }
}
