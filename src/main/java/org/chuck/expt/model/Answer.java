package org.chuck.expt.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "answers")
/**
 * Chuck!! The @ManyToOne options are important and surprising. On the spring-petclinic-master and springtrader-master
 * none of the @ManyToOnes have options. But these settings really matter.  
 * 
 * Here's what happens, at least when the JPA provider is Hibernate:
 * By default fecth = FetchType.EAGER on these many to ones. So on an EntityManger::find(Class, id) Hibernate
 * will issue many joins to retrieve them.  optional = true is another default, and if your ManyToOne is not
 * really optional, that means Hibernate is doing outer joins when all you need is inner joins.
 * 
 * I say this is surprising because I would have expected the defaults for fetch and optional to be:
 * @OneToMany(fetch = FetchType.LAZY, optional = true)  That is, I'm surprised that fecth is by default eager
 * on a OneToMany.  However, given a cache by ids it might make sense to have your initial  EntityManger::find be
 * so rich.  I would think that entities that are retrieved by something other than a find by id could be
 * cached by id, so you shouldn't be spoiling the advantage of caching by fetching through a query.
 * But now, the second time you want this entity, if you do the same query you'll bypass the cache, because
 * the cache certainly only caches by class and id. So in practice, if you want rich objects cached, maybe
 * eager fetch on @ManyToOne makes sense.  
 * 
 * BUT NOTE: @ManyToOne with fetchType.EAGER (again, that's the default!) will cause more joins than you expect.
 * For example, here we joined to get the Question, Choice, and User, but then we also joined to get the
 * choice's Question! 
 * 
 * I was also surprised because I didn't initially consider that optional = true would cause needless outer joins,
 * although of course that makes sense.
 * 
 * TODO: Need to see what @ManyToOne cascade default is.
 * 
 *
 */
public class Answer extends BaseEntity{
	@ManyToOne(optional = false)
	@JoinColumn(name = "questionId")
	private Question question;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "choiceId")
	private Choice choice;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "userid")
	private User user;

	public Question getQuestion() {
		return question;
	}

	public Answer setQuestion(Question question) {
		this.question = question;
		return this;
	}

	public Choice getChoice() {
		return choice;
	}

	public Answer setChoice(Choice choice) {
		this.choice = choice;
		return this;
	}
	
	public User getUser() {
		return user;
	}

	public Answer setUser(User user) {
		this.user = user;
		return this;
	}
	
}
