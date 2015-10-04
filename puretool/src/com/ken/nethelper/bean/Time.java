package com.ken.nethelper.bean;

public class Time {

	private int hour ;
	private int min ;
	
	public Time(int hour , int min)
	{
		this.setHour(hour);
		this.setMin(min);
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}
	
}
