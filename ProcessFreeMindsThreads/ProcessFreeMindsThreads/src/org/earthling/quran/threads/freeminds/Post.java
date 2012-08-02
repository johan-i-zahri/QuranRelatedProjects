package org.earthling.quran.threads.freeminds;

import java.util.HashMap;
import java.util.Map;

public class Post implements Comparable<Post>{
	private String name;
	private int postNo;
	
	private static Map<String,Post> map= new HashMap<String,Post>();
	private Post(){		
	}
	private Post(String name, int postNo) {
		this.name = name;
		this.postNo = postNo;
	}
	public static Post getInstance(String name, int postNo){
		String key = name.trim()+";"+postNo;
		Post p = map.get(key);
		if(p==null) {
			p = new Post(name.trim(),postNo);
			map.put(key, p);
		}
		return p;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPostNo() {
		return postNo;
	}
	public void setPostNo(int postNo) {
		this.postNo = postNo;
	}
	@Override
	public int compareTo(Post o) {
		if(name.equals(o.name))
			return Integer.valueOf(postNo).compareTo(o.postNo);
		else
			return name.compareTo(o.name);
	}
}
