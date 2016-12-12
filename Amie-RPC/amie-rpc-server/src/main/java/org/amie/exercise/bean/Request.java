package org.amie.exercise.bean;

import java.io.Serializable;

public class Request implements Serializable{
    private String requestId;
    private String parentClassName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getParentClassName() {
		return parentClassName;
	}
	public void setParentClassName(String parentClassName) {
		this.parentClassName = parentClassName;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	public Object[] getParameters() {
		return parameters;
	}
	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
    
    

}
