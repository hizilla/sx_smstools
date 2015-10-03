package com.ken.nethelper.bean;

public class SMS {

	private String target ;
	private String content ;
	private String time ;
	
	public SMS( String target , String content , String time)
	{
		this.setTarget(target) ;
		this.setContent(content) ;
		this.setTime(time);
	}

	public SMS() {
		// TODO 自动生成的构造函数存根
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
	
	
}
