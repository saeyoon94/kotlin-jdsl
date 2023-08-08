package com.linecorp.kotlinjdsl.render.jpql.serializer.impl

import com.linecorp.kotlinjdsl.querymodel.jpql.predicate.impl.JpqlAnd
import com.linecorp.kotlinjdsl.render.RenderContext
import com.linecorp.kotlinjdsl.render.jpql.serializer.JpqlRenderSerializer
import com.linecorp.kotlinjdsl.render.jpql.serializer.JpqlSerializer
import com.linecorp.kotlinjdsl.render.jpql.writer.JpqlWriter
import kotlin.reflect.KClass

class JpqlAndSerializer : JpqlSerializer<JpqlAnd> {
    override fun handledType(): KClass<JpqlAnd> {
        return JpqlAnd::class
    }

    override fun serialize(part: JpqlAnd, writer: JpqlWriter, context: RenderContext) {
        val delegate = context.getValue(JpqlRenderSerializer)

        writer.write("")

        writer.writeEach(part.predicates, separator = " AND ") {
            delegate.serialize(it, writer, context)
        }
    }
}
