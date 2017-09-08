package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;

class InputStreamBuffer {
	private static final int BUF_SIZE = 32784;

	private Integer last;
	private long length;
	private long counter = 0;
	private DataInputStream inputStream;

	public InputStreamBuffer (String path, long offset, long length) throws IOException {
	    RandomAccessFile file = new RandomAccessFile(path, "r");
	    int bufSize = (length < BUF_SIZE)? (int) length: BUF_SIZE;
		this.inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getFD()),bufSize));
		inputStream.skip(offset);
		this.length = length;
		if(this.length > 0) {
			this.last = inputStream.readInt();
			counter+=4;
		}
	}

	public int peek() {
		return last;
	}
	public int pop() throws IOException {
		if (counter > length) {
			throw new OutOfBoundsException();
		}
		int result = peek();
		if (counter < length) {
			this.last = inputStream.readInt();
		}
		counter+=4;
		return result;
	}

	public void close() throws IOException {
		inputStream.close();
	}

	public boolean done() {
		return this.counter > this.length;
	}

	public static int getBufSize() {
		return BUF_SIZE;
	}
}
