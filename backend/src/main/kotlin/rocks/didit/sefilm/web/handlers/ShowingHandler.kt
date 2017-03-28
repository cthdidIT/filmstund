package rocks.didit.sefilm.web.handlers

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import rocks.didit.sefilm.database.entities.User
import rocks.didit.sefilm.database.repositories.ShowingRepository
import rocks.didit.sefilm.domain.Bioklubbnummer
import rocks.didit.sefilm.json
import rocks.didit.sefilm.uuidMonoPathVariable
import java.util.*

@Component
class ShowingHandler(val repo: ShowingRepository) {
    fun findAll(req: ServerRequest) = ok().json().body(repo.findAll())
    fun findOne(req: ServerRequest) = repo.findOne(req.uuidMonoPathVariable("id"))
            .then { s -> ok().json().body(Mono.just(s)) }
            .otherwiseIfEmpty(notFound().build())
            .otherwise(IllegalArgumentException::class.java, { badRequest().build() })

    fun findBioklubbnummerForShowing(req: ServerRequest) =
            repo.findOne(req.uuidMonoPathVariable("id"))
                    .then { s ->
                        ok().json().body(BodyInserters.fromObject(shuffledBioklubbnummer(s.participants)))
                    }
                    .otherwiseIfEmpty(notFound().build())
                    .otherwise(IllegalArgumentException::class.java, { badRequest().build() })

    private fun shuffledBioklubbnummer(participants: Collection<User>): List<Bioklubbnummer> {
        val numbers = participants.map(User::bioklubbnummer).filterNotNull()
        Collections.shuffle(numbers)
        return numbers
    }
}