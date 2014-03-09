package org.chuck.expt.repository;


import javax.persistence.Query;




import javax.persistence.TypedQuery;

import org.chuck.expt.model.Answer;
import org.chuck.expt.model.Choice;
import org.chuck.expt.model.Question;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

import java.util.List;


//Probably should extend org.springframework.data.jpa.repository.JpaRepository
@Repository
public class QuestionRepositoryImpl extends BasicRepositoryImpl implements QuestionRepository  {
	

	//CHUCK!! Without fetch the choices collection doesn't get populated!!
	@Transactional(readOnly = true)
	/**
	 * Use this rather than findById if you want to get the choices associated with the question all in one select
	 * 
	 */
	public Question findQuestionById(Long id) {
		Query query = eM.createQuery("select question FROM Question question inner join fetch question.choices WHERE question.id = :id");
	    query.setParameter("id", id);
		return (Question) query.getSingleResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<Question> findQuestionsByText(String text) {
		Query query = eM.createQuery("select DISTINCT question FROM Question question inner join  question.choices WHERE question.text LIKE :text");
		query.setParameter("text", text + "%");
		System.out.println("Issuing search by text");
		return  query.getResultList();
	}
	
	
	//??What does this return??
	@SuppressWarnings("unchecked")
	public void xx(Long id) {
		//Returns Object[] with [Answer, Question] and the Answer already has a Question whose non-lazy fields are already populated
		TypedQuery<Object[]> query = eM.createQuery("select answer, question from Answer answer, Question question WHERE answer.id =:id and answer.question.id = question.id", Object[].class).
				setParameter("id", id);
		Object[] obj =  query.getSingleResult();
		System.out.println("retrieved Object[] result");
		Answer answer = (Answer) obj[0];
		Question question = (Question) obj[1];
		System.out.println(answer.getQuestion().getText());
		System.out.println(question.getText());
		
		System.out.println("HERE WE ARE: " + obj);
		
		//Here we just get the Answer, but again it's Question has all the question's non-lazy-loaded fields populated
		TypedQuery<Answer> answerWithQuestionInsideQuery = eM.createQuery("select answer from Answer answer, Question question WHERE answer.id =:id and answer.question.id = question.id", Answer.class)
				.setParameter("id", id);
		Answer answerWithQuestionInside = answerWithQuestionInsideQuery.getSingleResult();
		System.out.println("retrieved Answer result");
		System.out.println(answerWithQuestionInside.getQuestion().getText());
		
		TypedQuery<Answer> allChoicesQuery = eM.createQuery("select answer from Answer answer, Question question, Choice choice WHERE answer.id =:id and answer.question.id = question.id and choice.question = question.id",
				Answer.class)
				.setParameter("id", id);
		Answer allChoices = allChoicesQuery.getSingleResult();
		//!!Strangely this works with the first Answer above!! Are we doing eager loading my mistake?
		//CHUCK!! AHA! That's because we're in the same transaction in which the Answer was created, and that
		//saved Answer, which has had its relations populated before saving,  is cached during the course
		//of the transaction. I'll check, but I think we won't get this kind or miracle in a different transaction.  
		System.out.println("Executed allChoices query");
		System.out.println("questionId is " + allChoices.getQuestion().getId() + " with text " + allChoices.getQuestion().getText());
		System.out.println("Chosen answer is " + allChoices.getChoice().getId() + " " +  allChoices.getChoice().getText());
		for(Choice choice : allChoices.getQuestion().getChoices()) {
			System.out.println("Choice " + choice.getId() + " " + choice.getText() + " with questionId " + allChoices.getQuestion().getId());
		}
		
	}
	


}
