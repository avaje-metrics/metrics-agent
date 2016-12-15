package org.avaje.metric.agent;

/**
 * Thrown when a Class does not require entity or transaction enhancement.
 */
class NoEnhancementRequiredException extends RuntimeException {

	private static final long serialVersionUID = 7222178323991228946L;

	NoEnhancementRequiredException() {
		super();
	}
	
	NoEnhancementRequiredException(String msg) {
		super(msg);
	}
}
