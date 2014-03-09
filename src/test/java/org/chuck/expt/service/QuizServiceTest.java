package org.chuck.expt.service;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;











import java.util.List;








import static org.junit.Assert.*;

@ContextConfiguration(locations = {"/spring/business-config.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("jpa")
public class QuizServiceTest {
	
	@Autowired
	private QuizService quizService;
	
	@Autowired 
	private JpaTransactionManager transactionManager;
	

	private TransactionTemplate transactionTemplate;
	
	@Before
	public void makeTransactionTemplate() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}
	
	
	//Chuck!! The other tests should be like this one. Using @Transactional is good because it
	//rolls everything back for you, but bad because you don't see what happens when you're 
	//objects are detached!!  Now if you're using OpenEntityManagerInView/OpenSessionInView you won't get 
	//can't-do-lazy-initialization-outside-of-transaction errors, but you'll be hitting the 
	//database more than you expect.
	@Test
	public void retrieveOutsideOfTransaction() {
		final Question question = transactionTemplate.execute(new TransactionCallback<Question>() {

			public Question doInTransaction(TransactionStatus status) {
				Question question = new Question().setText("Well?");
				Choice choiceA = new Choice().setText("a");
				Choice choiceB = new Choice().setText("b");
				question.addChoice(choiceA);
				question.addChoice(choiceB);
				quizService.save(question);
				return question;
			}
		});

		Question retrieved = quizService.findQuestionById(question.getId());
		System.out.println("retrieving outside any transaction");
		assertEquals(2, retrieved.getChoices().size());
		for(int i = 0; i < 2; i++) {
			Choice choice = retrieved.getChoices().get(i);
			assertNotNull(choice.getId());
			assertEquals(question.getId(), choice.getQuestion().getId());

			assertEquals(question.getChoices().get(i).getText(), choice.getText());
			assertEquals(question.getChoices().get(i).getId(), choice.getId());

		}
		System.out.println("Done retrieving in other transaction");
		quizService.remove(Question.class, question.getId());
		assertTrue(quizService.findAll(Question.class).isEmpty());
		assertTrue("Delete cascading from Question to choices didn't happen", quizService.findAll(Choice.class).isEmpty());
		
	}
	
	@Test
	@Transactional
	public void createWithChoices() {
		System.out.println("Yo!!!");
		Question question = new Question().setText("Well?");
		Choice choiceA = new Choice().setText("a");
		Choice choiceB = new Choice().setText("b");
		question.addChoice(choiceA);
		question.addChoice(choiceB);
		quizService.save(question);

		Question qx = quizService.findById(Question.class, question.getId());
		System.out.println("Got the question by id");
		qx.getChoices().get(0).getText();
		System.out.println("Should have seen some database activity");
		
		
		List<Question> questionsForText = quizService.findBQuestionsByText("We");
		assertEquals(1, questionsForText.size());
		assertEquals(2, quizService.findAll(Choice.class).size());
		Question retrieved = quizService.findQuestionById(question.getId());
		assertEquals("Well?", retrieved.getText());
		assertNotNull(retrieved.getId());
		
		assertEquals(2, retrieved.getChoices().size());
		for(int i = 0; i < 2; i++) {
			Choice choice = retrieved.getChoices().get(i);
			assertNotNull(choice.getId());
			assertEquals(question.getId(), choice.getQuestion().getId());
			if(i == 0) {
				assertEquals(choiceA.getText(), choice.getText());
				assertEquals(choiceA.getId(), choice.getId());
			}
			else if(i == 1) {
				assertEquals(choiceB.getText(), choice.getText());
				assertEquals(choiceB.getId(), choice.getId());
			}
		}
		System.out.println("@@@@@@@@@@");
		
		
	}
	
	@Test
	@Transactional
	public void createSeriesOfQuestions() {
		Question question = new Question().setText("Well?");
		Question questionNext1 = new Question().setText("After Well? A");
		Question questionNext2 = new Question().setText("After Well? B");
		
		Choice choiceA = new Choice().setText("a").setNextQuestion(questionNext1);
		Choice choiceB = new Choice().setText("b").setNextQuestion(questionNext2);
		
		question.addChoice(choiceA).addChoice(choiceB);
		
		quizService.save(questionNext1);
		quizService.save(questionNext2);
		quizService.save(question);
		
		List<Question> byText = quizService.findBQuestionsByText("We");
		assertEquals(1, byText.size());
		Question root = byText.iterator().next();
		assertEquals("Well?", root.getText());
		List<Choice> choices = root.getChoices();
		assertEquals(2, choices.size());
		
		assertEquals("a", choices.get(0).getText());
		System.out.println(" Choice A text " + choices.get(0).getNextQuestion().getText());
		assertEquals("After Well? A", choices.get(0).getNextQuestion().getText());
		assertEquals("After Well? B", choices.get(1).getNextQuestion().getText());
		
		
	}
	
	@Test
	@Transactional
	public void persistAnswerOnlyHoldingReferencesToIdsRatherThanToObjects() {
		User user = new User().setUsername("Chuck");
		quizService.save(user);
		Question question = new Question().setText("Well?").addChoice(new Choice().setText("a")).addChoice(new Choice().setText("b"));
		quizService.save(question);
	
		Question root = quizService.findQuestionById(question.getId());  
		System.out.println("Any database activity until end of test, other than inserting answer?");
		Choice chosen = root.getChoices().get(0);
		
		
		//Typically we'll have ids and don't want to waste time retrieving the full obj
		//!!!saveAnswer has a trick that actually gets an entity reference for each given id!!
		Answer willBeGivenIdUponSave = new Answer();
		quizService.saveAnswer(willBeGivenIdUponSave, user.getId(), root.getId(), chosen.getId());
		
		System.out.println("Retrieving answer by id");
		//------> CHUCK!  Looking at console output I see that no Hibernate: select shows up here, so 
		//this answer is just being retrieved from the transaction cache!
		Answer retirevedAnswer = quizService.findById(Answer.class, willBeGivenIdUponSave.getId());
		assertEquals("Chuck", retirevedAnswer.getUser().getUsername());
		assertEquals("Well?", retirevedAnswer.getQuestion().getText());
		assertEquals("a", retirevedAnswer.getChoice().getText());
		
		System.out.println("done persistAnswer");
		
		System.out.println("here's the experiment!!");
		//TODO CHUCK!! I need to try some complex joins!!
		quizService.xx(retirevedAnswer.getId());
		
	}
	
	
}
