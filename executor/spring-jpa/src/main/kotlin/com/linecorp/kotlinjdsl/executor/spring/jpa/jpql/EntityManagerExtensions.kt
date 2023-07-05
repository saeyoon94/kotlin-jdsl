package com.linecorp.kotlinjdsl.executor.spring.jpa.jpql

import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQuery
import com.linecorp.kotlinjdsl.querymodel.jpql.SelectQuery
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import jakarta.persistence.EntityManager
import jakarta.persistence.Query
import jakarta.persistence.TypedQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.query.QueryUtilsAdaptor
import org.springframework.data.support.PageableExecutionUtils
import kotlin.reflect.KClass

private val jpqlRenderer = JpqlRenderer()

fun EntityManager.createQuery(
    query: JpqlQuery<*>,
    context: RenderContext,
): Query {
    val rendered = jpqlRenderer.render(query, context)

    val renderedQuery = rendered.query
    val renderedParams = rendered.parameters

    return createQuery(renderedQuery).apply {
        setParameters(renderedParams)
    }
}

inline fun <reified T : Any> EntityManager.createQuery(
    query: JpqlQuery<*>,
    context: RenderContext,
): TypedQuery<T> {
    return createQuery(query, context, T::class)
}

fun <T : Any> EntityManager.createQuery(
    query: JpqlQuery<*>,
    context: RenderContext,
    resultClass: KClass<T>,
): TypedQuery<T> {
    val rendered = jpqlRenderer.render(query, context)

    val renderedQuery = rendered.query
    val renderedParams = rendered.parameters

    return createQuery(renderedQuery, resultClass.java).apply {
        setParameters(renderedParams)
    }
}

inline fun <reified T : Any> EntityManager.queryForPage(
    query: JpqlQuery<SelectQuery>,
    context: RenderContext,
    pageable: Pageable,
): Page<T> {
    return queryForPage(query, context, pageable, T::class)
}

fun <T : Any> EntityManager.queryForPage(
    query: JpqlQuery<SelectQuery>,
    context: RenderContext,
    pageable: Pageable,
    resultClass: KClass<T>,
): Page<T> {
    val rendered = jpqlRenderer.render(query, context)

    val renderedQuery = rendered.query
    val renderedParams = rendered.parameters

    val sortedQuery = QueryUtilsAdaptor.applySorting(renderedQuery, pageable.sort)
    val countQuery = QueryUtilsAdaptor.createCountQueryFor(renderedQuery, null, false)

    val sortedJpaQuery = createQuery(sortedQuery, resultClass.java).apply {
        setParameters(renderedParams)
    }

    val countJpaQuery = createQuery(countQuery, Long::class.java).apply {
        setParameters(renderedParams)
    }

    return PageableExecutionUtils.getPage(sortedJpaQuery.resultList, pageable) {
        val counts = countJpaQuery.resultList

        if (counts.size == 1) {
            counts.first()
        } else {
            counts.count().toLong()
        }
    }
}
