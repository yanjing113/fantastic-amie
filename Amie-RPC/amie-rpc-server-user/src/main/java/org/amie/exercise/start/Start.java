package org.amie.exercise.start;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Start {
	@SuppressWarnings("resource")
	public static void main(String[] args){
		new ClassPathXmlApplicationContext(new String[]{
				"spring-start.xml"
		});
	}

}
