package com.example.demo.Exceptions;

public class ServiceCommunicationException extends RuntimeException {
	
	public ServiceCommunicationException(String message) {
		super(message);
	}
	
	public ServiceCommunicationException(String serviceName, Throwable cause) {
		super("Erreur de communication avec le service " + serviceName, cause);
	}
}
