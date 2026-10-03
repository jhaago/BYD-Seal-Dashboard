package io.github.jhaago.sealdashboard.assistant

fun interface AssistantService { suspend fun respond(request: AssistantRequest): AssistantReply }
