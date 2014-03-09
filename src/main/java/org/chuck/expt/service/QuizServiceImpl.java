package org.chuck.expt.service;

import org.chuck.expt.repository.QuestionRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.chuck.expt.model.*;
import org.hibernate.tuple.entity.DynamicMapEntityTuplizer.BasicEntityNameResolver;

import java.util.List;

import com.itextpdf.text.pdf.PdfStructTreeController.returnType;


/**
 * !!!Would have preferred constructor injection, but we're using CGLib proxies here, and CGLib requires
 * a default constructor to work!  http://stackoverflow.com/questions/15223297/spring-social-superclass-has-no-null-constructors-but-no-arguments-were-given
 * Supposedly have @Transactional at the class level caused this, but in reality I had the same problem when I
 * removed that annotation. Of course I would prefer to have it there.
 */

@Service
@Transactional
public class QuizServiceImpl implements QuizService, BeanNameAware {

	@Autowired
	private QuestionRepository questionRepository;
	@Autowired
	private ApplicationContext applicationContext;
	private String beanName;

    public void setQuestionRepository(QuestionRepository questionRepository) {
    	this.questionRepository = questionRepository;
    }

    @Transactional(readOnly = true)
    public <T extends BaseEntity> T findById(Class<T> clazz, Long id) {
    	return this.questionRepository.findById(clazz, id);
    }

	@Transactional(readOnly = true)
	public Question findQuestionById(Long id) {
		return this.questionRepository.findQuestionById(id);
	}
	
	@Transactional(readOnly = true)
	public List<Question> findBQuestionsByText(String text) {
		return this.questionRepository.findQuestionsByText(text);
	}
	
	@Transactional(readOnly = true)
	public <T extends BaseEntity> List<T> findAll(Class<T> clazz) {
		return questionRepository.findAll(clazz);
	}
	
	@Transactional
	public void saveAnswer(Answer answer, Long userId, Long questionId, Long choiceId) {
		//Chuck If save() had a different transaction setting (e.g. REQUIRES_NEW), I'd 
		//need to get reference to the proxy and call theProxy.save().
		//That's what I'm attempting here
		getProxyReference().save(answer.setUser(questionRepository.stubReferenceForId(User.class, userId)).
		setQuestion(questionRepository.stubReferenceForId(Question.class, questionId)).
		setChoice(questionRepository.stubReferenceForId(Choice.class, choiceId)));
	}

	@Transactional
	public void save(BaseEntity entity) {
		questionRepository.save(entity);
	}
	
	@Transactional
	public <T extends BaseEntity> void remove(Class<T> clazz, Long id) {
		questionRepository.remove(clazz, id);
	}

	/**
	 * Only use this if you're already in a transaction!
	 */
	@Transactional
	public void remove(BaseEntity entity) {
		// TODO Auto-generated method stub
	}
	

	@Transactional(readOnly = true)
	public void xx(Long id) {
		questionRepository.xx(id);
	}
	
	public void setBeanName(String name) {
		this.beanName = name;
	}	

	//Chuck!! Getting the beanName as a BeanAware class and using it to find our Spring 
	//proxy didn't work when this service class didn't yet have a corresponding interface!
	//The bean name was correct, and the applicationContext was not null, but it failed anyway.
	private QuizService getProxyReference() {
		System.out.println("beanName is " + beanName);
		return this.applicationContext.getBean(beanName, QuizService.class);
	}

	
}
