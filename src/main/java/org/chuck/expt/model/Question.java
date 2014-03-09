package org.chuck.expt.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name="questions")
public class Question extends BaseEntity{
	
	@Column(name="text")
	private String text;


	@OneToMany(cascade = CascadeType.ALL,   mappedBy = "question")
	private List<Choice> choices;

	public String getText() {
		return text;
	}

	public Question setText(String text) {
		this.text = text;
		return this;
	}

	public java.util.List<Choice> getChoices() {
		return choices;
	}
	//See http://stackoverflow.com/questions/1795649/jpa-persisting-a-one-to-many-relationship
	public Question addChoice(Choice choice) {
		if(choices == null)
			choices = new ArrayList<Choice>();
		choice.setQuestion(this);
		choices.add(choice);
		return this;
		
	}	
	

}
