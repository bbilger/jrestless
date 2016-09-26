package com.jrestless.core.container.handler;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.StatusType;

class SimpleContainerResponse {
	
	private StatusType statusType;
	private String body;
	private Map<String, List<String>> headers;
	SimpleContainerResponse(StatusType statusType, String body, Map<String, List<String>> headers) {
		super();
		this.statusType = statusType;
		this.body = body;
		this.headers = headers;
	}
	StatusType getStatusType() {
		return statusType;
	}
	String getBody() {
		return body;
	}
	Map<String, List<String>> getHeaders() {
		return headers;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + ((statusType == null) ? 0 : statusType.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleContainerResponse other = (SimpleContainerResponse) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (statusType == null) {
			if (other.statusType != null)
				return false;
		} else if (!statusType.equals(other.statusType))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SimpleContainerResponse [statusType=" + statusType + ", body=" + body + ", headers=" + headers + "]";
	}
}