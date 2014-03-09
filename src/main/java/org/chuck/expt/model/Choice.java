package org.chuck.expt.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "choices")
public class Choice extends BaseEntity {
	@Column
	private String text;

	@ManyToOne(optional = false)
	@JoinColumn(name = "questionid")
	private Question question;

	@ManyToOne(optional = true)
	@JoinColumn(name = "next_questionid")
	private Question nextQuestion;

	public String getText() {
		return text;
	}

	public Choice setText(String text) {
		this.text = text;
		return this;
	}

	public Question getQuestion() {
		return question;
	}

	public Choice setQuestion(Question question) {
		this.question = question;
		return this;
	}

	public Question getNextQuestion() {
		return nextQuestion;
	}

	public Choice setNextQuestion(Question nextQuestion) {
		this.nextQuestion = nextQuestion;
		return this;
	}

}
