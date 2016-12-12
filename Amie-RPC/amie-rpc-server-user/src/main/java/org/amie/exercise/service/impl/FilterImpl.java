package org.amie.exercise.service.impl;

import org.amie.exercise.annotation.RPCService;
import org.amie.exercise.service.Filter;
import org.springframework.stereotype.Component;

@RPCService(parentClass=Filter.class)
@Component
public class FilterImpl implements Filter{

	public String filter(String id) {		
		return "filter "+ id;
	}

}
