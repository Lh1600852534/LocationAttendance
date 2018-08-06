package lh.henu.edu.cn.locationattendance.net;

import com.google.gson.Gson;

public class ProtocalObj {

	public String toJson() {
		Gson g = new Gson();
		return g.toJson(this);
	}
	public Object fromJson(String json) {
		Gson g = new Gson();
		return g.fromJson(json, this.getClass());
	}
}
