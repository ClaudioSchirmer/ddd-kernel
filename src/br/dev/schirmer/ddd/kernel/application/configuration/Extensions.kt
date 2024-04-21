package br.dev.schirmer.ddd.kernel.application.configuration

fun dddKernel(configuration: SharedConfiguration.() -> Unit) {
    Configuration.apply(configuration)
}