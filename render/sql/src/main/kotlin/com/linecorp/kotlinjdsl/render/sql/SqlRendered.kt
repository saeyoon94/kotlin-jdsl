package com.linecorp.kotlinjdsl.render.sql

data class SqlRendered(
    val query: String,
    val parameters: SqlRenderedParameters,
)
