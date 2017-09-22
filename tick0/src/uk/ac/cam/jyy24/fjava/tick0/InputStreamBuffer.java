package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;
import java.nio.ByteBuffer;

class InputStreamBuffer {
	private static final int BUF_SIZE = 131072;

	private Integer last;
	private long length;
	private long counter = 0;
	private DataInputStream inputStream;

	public InputStreamBuffer (String path, long offset, long length) throws IOException {
	   this(path, offset, length, true);
	}

	public InputStreamBuffer (String path, long offset, long length, boolean forQ) throws IOException {
	    RandomAccessFile file = new RandomAccessFile(path, "r");
	    int bufSize = (length < BUF_SIZE)? (int) length : BUF_SIZE;

		this.length = length;

		this.inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file.getFD()),bufSize));
		this.counter = 0;

		inputStream.skip(offset);
		if(length > 0 && forQ) {
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

	public int[] readArray(int byteSize) throws IOException {
		byte[] buf = new byte[byteSize];
		inputStream.readFully(buf);
		int[] arr = new int[byteSize / 4];
		ByteBuffer.wrap(buf).asIntBuffer().get(arr);
		return arr;
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
