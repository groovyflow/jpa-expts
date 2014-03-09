package org.chuck.expt.service;

import java.util.List;

import org.chuck.expt.model.Answer;
import org.chuck.expt.model.BaseEntity;
import org.chuck.expt.model.Question;
import org.chuck.expt.repository.QuestionRepository;
import org.springframework.transaction.annotation.Transactional;

public interface QuizService {


	<T extends BaseEntity> T findById(Class<T> clazz, Long id);

	Question findQuestionById(Long id);

	List<Question> findBQuestionsByText(String text);

	<T extends BaseEntity> List<T> findAll(Class<T> clazz);

	void saveAnswer(Answer answer, Long userId,
			Long questionId, Long choiceId);

	void save(BaseEntity entity);
	
	<T extends BaseEntity> void remove(Class<T> clazz, Long id);
	void remove(BaseEntity entity);	
	
	void xx(Long id);


}