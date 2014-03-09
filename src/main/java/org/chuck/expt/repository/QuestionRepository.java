package org.chuck.expt.repository;


import org.chuck.expt.model.Question;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends BasicRepository {
    //@Query("select question FROM Question question left join fetch question.choices WHERE question.id = :id")
	Question findQuestionById(Long id);
    
    //@Query("select question FROM Question question left join fetch question.choices WHERE question.text LIKE :text")
	List<Question> findQuestionsByText(String text);
	
	
	void xx(Long id);
	
}
