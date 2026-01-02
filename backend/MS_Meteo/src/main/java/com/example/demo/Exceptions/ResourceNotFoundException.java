package com.example.demo.Exceptions;

public class ResourceNotFoundException extends RuntimeException {
	
	public ResourceNotFoundException(String message) {
		super(message);
	}
	
	public ResourceNotFoundException(String resourceName, Long id) {
		super(resourceName + " avec l'ID " + id + " non trouvé(e)");
	}
}
