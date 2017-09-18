package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;

class InputStreamBuffer {
	private static final int BUF_SIZE = 262144;

	private Integer last;
	private long length;
	private long counter = 0;
	private DataInputStream inputStream;

	public InputStreamBuffer (String path, long offset, long length) throws IOException {
	    RandomAccessFile file = new RandomAccessFile(path, "r");
	    int bufSize = (length < BUF_SIZE)? (int) length : BUF_SIZE;

		this.length = length;

		this.inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getFD()),bufSize));
		this.counter = 0;

		inputStream.skip(offset);
		if(length > 0) {
			this.last = read();
		}
	}
	public InputStreamBuffer (String path, long offset, long length, int bufSize) throws IOException {
	    RandomAccessFile file = new RandomAccessFile(path, "r");

		this.length = length;
		this.inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getFD()),bufSize));
		this.counter = 0;

		inputStream.skip(offset);
		if(length > 0) {
			this.last = read();
		}
	}

	public int peek() {
		return last;
	}
	public int pop() throws IOException {
		if (counter > length) {
			throw new OutOfBoundsException();
		}
		if (counter < length) {
			int result = peek();
			this.last = read();
			return result;
		} else if (counter == length){
			counter+=4;
			return this.last;
		} else {
			throw new OutOfBoundsException();
		}
	}

	public void close() throws IOException {
		inputStream.close();
	}

	private int read() throws IOException {
		int result = inputStream.readInt();
		counter += 4;
		return result;
	}

	public boolean done() {
		return this.counter > this.length;
	}

	/*public int compareTo(InputStreamBuffer isb){
		return Integer.compare(this.peek(), isb.peek());
	}*/

	public static int getBufSize() {
		return BUF_SIZE;
	}
}
