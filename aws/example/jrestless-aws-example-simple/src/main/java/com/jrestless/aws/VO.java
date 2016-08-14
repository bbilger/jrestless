package com.jrestless.aws;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VO<T> {
	private T value;

	public VO() {
	}

	public VO(T value) {
		this.value = value;
	}

	@XmlElement
	public T getValue() {
		return value;
	}
}
