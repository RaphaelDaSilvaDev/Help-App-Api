package com.raphaelsilva.help.app.service

import com.raphaelsilva.help.app.dto.form.AnswerForm
import com.raphaelsilva.help.app.dto.form.AnswerLikeForm
import com.raphaelsilva.help.app.dto.view.AnswerLikeView
import com.raphaelsilva.help.app.dto.view.AnswerSimpleView
import com.raphaelsilva.help.app.dto.view.AnswerWithChildren
import com.raphaelsilva.help.app.dto.view.AnswerWithChildrenCountView
import com.raphaelsilva.help.app.exception.NotFoundException
import com.raphaelsilva.help.app.mapper.answer.AnswerCompleteViewMapper
import com.raphaelsilva.help.app.mapper.answer.AnswerFormMapper
import com.raphaelsilva.help.app.mapper.answer.AnswerLikeViewMapper
import com.raphaelsilva.help.app.mapper.answer.AnswerSimpleViewMapper
import com.raphaelsilva.help.app.model.Answer
import com.raphaelsilva.help.app.model.PostStatus
import com.raphaelsilva.help.app.model.User
import com.raphaelsilva.help.app.repository.AnswerRepository
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class AnswerService(
    private val answerRepository: AnswerRepository,
    private val postService: PostService,
    private val userService: UserService,
    private val answerFormMapper: AnswerFormMapper,
    private val answerLikeViewMapper: AnswerLikeViewMapper,
    private val answerWhitQuantityViewMapper: AnswerCompleteViewMapper,
    private val answerSimpleViewMapper: AnswerSimpleViewMapper,
    private val notFoundMessage: String = "Answer not found!",
) {
    fun create(answerForm: AnswerForm): AnswerWithChildrenCountView {
        val answer = answerFormMapper.map(answerForm)
        val children = answerForm.answerId?.let {
            getByIdPure(it)
        }
        answer.answer = children
        answerRepository.save(answer)
        answer.post?.id?.let { postService.changeStatus(PostStatus.NOT_SOLVED, it) }
        val answerWithChildren = AnswerWithChildren(answer, ArrayList())
        return answerWhitQuantityViewMapper.map(answerWithChildren)
    }

    fun getById(id: Long): AnswerWithChildrenCountView {
        val answer = answerRepository.findById(id).orElseThrow { NotFoundException(notFoundMessage) }
        val answersChildren = getAllChildrenById(id)
        val answerWithChildren = AnswerWithChildren(answer, answersChildren)
        return answerWhitQuantityViewMapper.map(answerWithChildren)
    }

    fun getAllChildrenById(id: Long): List<AnswerSimpleView> {
        return answerRepository.getAllAnswerChildren(id).stream().map { answer ->
            answerSimpleViewMapper.map(answer)
        }.collect(Collectors.toList())
    }

    fun getByIdPure(id: Long): Answer {
        return answerRepository.findById(id).orElseThrow { NotFoundException(notFoundMessage) }
    }

    fun getByPostId(postId: Long): List<AnswerWithChildrenCountView> {
        val answer = answerRepository.getAllByPostId(postId).stream().map { answer ->
            val answersChildren = answer.id?.let { answerId -> getAllChildrenById(answerId) }
            answersChildren?.let { child ->
                val answersWhitChildren = AnswerWithChildren(answer, child)
                answerWhitQuantityViewMapper.map(answersWhitChildren)
            }
        }.collect(Collectors.toList())
        return answer
    }

    fun getAnswerByAnswerFather(id: Long): List<AnswerWithChildrenCountView> {
        val answer = answerRepository.getAnswerByAnswerFather(id).stream().map { answer ->
            val answersChildren = answer.id?.let { answerId -> getAllChildrenById(answerId) }
            answersChildren?.let { child ->
                val answersWhitChildren = AnswerWithChildren(answer, child)
                answerWhitQuantityViewMapper.map(answersWhitChildren)
            }
        }.collect(Collectors.toList())
        return answer
    }

    fun addLike(answerLikeForm: AnswerLikeForm): AnswerLikeView {
        val answer = getByIdPure(answerLikeForm.answerId)
        val user = userService.getUserByIdPure(answerLikeForm.authorId)
            val updatedAnswer = Answer(
                id = answer.id,
                message = answer.message,
                likes = listOf(user),
                answer = answer.answer,
                author = answer.author,
                isSolution = answer.isSolution,
                createdAt = answer.createdAt,
                post = answer.post,
            )
            answerRepository.save(updatedAnswer)
            return answerLikeViewMapper.map(answerSimpleViewMapper.map(updatedAnswer), user)
        }

    fun removeLike(answerLikeForm: AnswerLikeForm){
        answerRepository.deleteLike(answerLikeForm.authorId, answerLikeForm.answerId)
    }
}
