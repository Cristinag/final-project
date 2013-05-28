package com.testprioritization.domain.model;

public class Line {
	private final String file;
	private final String lineContents;
	private final Integer lineId;
	private final int lineNo;
	private final String project;

	public Line(int lineNo, String lineContents, String file, String project) {
		this.lineNo = lineNo;
		this.lineContents = lineContents;
		this.file = file;
		this.project = project;
		this.lineId = null;
	}

	public Line(int lineNo, String lineContents, String file, String project,
			Integer lineId) {
		this.lineNo = lineNo;
		this.lineContents = lineContents;
		this.file = file;
		this.lineId = lineId;
		this.project = project;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Line)) {
			return false;
		}
		Line anotherLine = (Line) object;
		return anotherLine.getLineContents().equals(lineContents)
				&& anotherLine.getFile().equals(file)
				&& anotherLine.getLineId() == lineId
				&& anotherLine.getLineNo() == lineNo;
	}

	public String getFile() {
		return file;
	}

	public String getLineContents() {
		return lineContents;
	}

	public Integer getLineId() {
		return lineId;
	}

	public int getLineNo() {
		return lineNo;
	}

	public String getProject() {
		return project;
	}

	@Override
	public String toString() {
		return "contents: " + lineContents + " id: " + lineId + " number: "
				+ lineNo + " file: " + file;
	}
}
