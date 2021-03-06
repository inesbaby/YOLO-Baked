package com.example.payload;

public class SaveFaceResponse {

	private String UserTaged;
	private byte[] faceData;

	public SaveFaceResponse(String userTaged, byte[] faceData) {
		super();
		UserTaged = userTaged;
		this.faceData = faceData;
	}

	public String getUserTaged() {
		return UserTaged;
	}

	public void setUserTaged(String userTaged) {
		UserTaged = userTaged;
	}

	public byte[] getFaceData() {
		return faceData;
	}

	public void setFaceData(byte[] faceData) {
		this.faceData = faceData;
	}

}
