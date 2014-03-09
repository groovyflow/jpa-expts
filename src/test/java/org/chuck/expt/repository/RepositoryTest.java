package org.chuck.expt.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.chuck.expt.model.Answer;
import org.chuck.expt.model.Choice;
import org.chuck.expt.model.Question;
import org.chuck.expt.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;




@ContextConfiguration(locations = { "/spring/business-config.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("jpa")
public class RepositoryTest {

	@Autowired
	private JpaTransactionManager transactionManager;

	@Autowired
	private QuestionRepository questionRepository;

	private TransactionTemplate transactionTemplate;

	@Before
	public void makeTransactionTemplate() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Test
	public void retrieveOutsideOfTransaction() {
	 	//Create question with choices
		final Question question = transactionTemplate
				.execute(new TransactionCallback<Question>() {
					public Question doInTransaction(TransactionStatus status) {
						Question question = createQuestionWithChoices();
						questionRepository.save(question);
						return question;
					}
				});
		//Retrieve question with choices.  The Repository isn't a Service, it doesn't demarcate a 
		//transaction, so we need to make a transaction to retreive it. 
		Question retrieved = transactionTemplate
				.execute(new TransactionCallback<Question>() {
					public Question doInTransaction(TransactionStatus status) {
						return questionRepository.findQuestionById(question
								.getId());
					}
				});
		
		//We want to check that the question and choices were retrieved in the previous transaction, 
		//and we need to do that checking outside of any transaction.  That's because a lazy query for Choices
		//could, if we were inside a transaction, fool us into believing that our Choices were already loaded.
		//That can't happen outside a transaction, where an attempt at lazy retrieval will cause an exception.
		assertEquals(createQuestionWithChoices().getChoices().size(), retrieved.getChoices().size());
		for (int i = 0; i < createQuestionWithChoices().getChoices().size(); i++) {
			Choice choice = retrieved.getChoices().get(i);
			assertNotNull(choice.getId());
			assertEquals(question.getId(), choice.getQuestion().getId());

			assertEquals(question.getChoices().get(i).getText(),
					choice.getText());
			assertEquals(question.getChoices().get(i).getId(), choice.getId());

		}
		
		//Again, our Repository does not demarcate its own transactions, so we need to do our cleaning up
		//inside another transaction.
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				questionRepository.remove(Question.class, question.getId());
				assertTrue(questionRepository.findAll(Question.class).isEmpty());
				assertTrue("Delete cascading from Question to choices didn't happen",
						questionRepository.findAll(Choice.class).isEmpty());
				
			}
		});
		

	}
	
	@Test
	public <T> void persistAnswerOnlyHoldingReferencesToIdsRatherThanToObjects() {
		//User user = new User().setUsername("Chuck");
		final User user = new User().setUsername("Chuck");
		final Question createdQuestion = createQuestionWithChoices();
		//persist the objects that an Answer depends on
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				questionRepository.save(user);
				questionRepository.save(createdQuestion);
				
			}
		});
		final Choice chosenByUser = createdQuestion.getChoices().get(0);
		final Answer createdAnswer = transactionTemplate.execute(new TransactionCallback<Answer>() {
			public Answer doInTransaction(TransactionStatus status) {
				//Set the Answer's dependent objects with just the ids, which in real code might be all you have.
				//No need to fetch the entities corresponding to the ids. See the stubReferenceForId method.
				Answer answer = new Answer().setQuestion(questionRepository.stubReferenceForId(Question.class, createdQuestion.getId())).
				setChoice(questionRepository.stubReferenceForId(Choice.class, chosenByUser.getId())).
				setUser(questionRepository.stubReferenceForId(User.class, user.getId()));
				questionRepository.save(answer);
				return answer;
				
			}
			
		});
		final Answer retrievedAnswer = transactionTemplate.execute(new TransactionCallback<Answer>() {
			public Answer doInTransaction(TransactionStatus status) {
				System.out.println("xxx");
				return questionRepository.findById(Answer.class, createdAnswer.getId());
			}
		});
		
		//Chuck Looking at the console I see that entityManager.find automatically did a 
		//complex series of left outer joins to retrieve all this information in one select.
		//I see it did that because the @ManyToOnes in Answer didn't specify a fetch type, 
		//and the default must be fetch = FetchType.EAGER.  FindById failed on the first assertion
		//when I specified FetchType.LAZY for the Answer ManyToOnes.  
		assertEquals(user.getUsername(), retrievedAnswer.getUser().getUsername());
		assertEquals(createdQuestion.getText(), retrievedAnswer.getQuestion().getText());
		assertEquals(chosenByUser.getText(), retrievedAnswer.getChoice().getText());
		System.out.println("Cleaning up");
		//cleanup
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				questionRepository.remove(Answer.class, retrievedAnswer.getId());
				questionRepository.remove(User.class, user.getId());
				questionRepository.remove(Question.class, createdQuestion.getId());
				
			}
		});
		
	}	
	
	private Question createQuestionWithChoices() {
		return  new Question().setText("Well?").addChoice(new Choice().setText("a")).
				addChoice(new Choice().setText("b"));	
	}
}
