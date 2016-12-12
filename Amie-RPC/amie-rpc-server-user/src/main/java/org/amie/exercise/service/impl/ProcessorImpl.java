package org.amie.exercise.service.impl;

import org.amie.exercise.annotation.RPCService;
import org.amie.exercise.service.Processor;
import org.springframework.stereotype.Component;

@RPCService(parentClass=Processor.class)
@Component
public class ProcessorImpl implements Processor{

	public String hello(String name) {
		return "hello " + name;
	}

	public String process(String name) {
		return "process "+ name;
	}



}
