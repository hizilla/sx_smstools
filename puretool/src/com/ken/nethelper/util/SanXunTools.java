package com.ken.nethelper.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SanXunTools {
	
	public static final String getPWD(String content)
	{
		String ans = "password";
		Pattern pattern = Pattern.compile("ÃÜÂëÊÇ£º(.*),ÃÜÂë");
		Matcher matcher = pattern.matcher(content);
		
		while(matcher.find())
		{
			ans = matcher.group(1);
		}
		
		return ans;
	}
}
