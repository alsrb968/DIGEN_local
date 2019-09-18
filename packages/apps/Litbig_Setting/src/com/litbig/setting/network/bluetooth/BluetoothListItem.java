package com.litbig.setting.network.bluetooth;

public class BluetoothListItem{
	private String name;
	private String address;
	private int state = BluetoothDeviceConnect.STATE_NONE;
	private boolean paired;
	private int position;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public boolean isPaired() {
		return paired;
	}
	public void setPaired(boolean paired) {
		this.paired = paired;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
}
