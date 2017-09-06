package com.test.updown.callback;

public interface UpAndDownListener {
	void onClose();
	void onOpen();
	void onAssignHeight(int height);
}
