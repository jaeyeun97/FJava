package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ExternalSort {
	private static final int BLOCK_NUM = 256;
	private static final int BUF_SIZE = InputStreamBuffer.getBufSize();
	private static final int FIRST_BLOCK_SIZE = 1835008;
	private static final int BITS_PER_BYTE = 8;

	public static void sort(String f1, String f2) throws IOException {
		File input = new File(f1);
		File output = new File(f2);
		File a = input;
		long length = a.length();
		long blockSize = FIRST_BLOCK_SIZE / BLOCK_NUM;
		// long blockSize = 4;

		while(true){
			long offset = 0;
			DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output), BUF_SIZE));
			while (offset < length) {
				if(blockSize * BLOCK_NUM <= FIRST_BLOCK_SIZE){
					firstSort(input, outputStream, (int) blockSize * BLOCK_NUM, offset);
				} else {
					mergeSort(input, outputStream, blockSize, offset);
				}
				offset += BLOCK_NUM * blockSize;
			}
			outputStream.close();

			blockSize *= BLOCK_NUM;
			if(blockSize < length) {
				File tmp = output;
				output = input;
				input = tmp;
			} else {
				break;
			}
		}
		if (!a.equals(output)){
			Files.copy(output.toPath(), a.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void firstSort(File input, DataOutputStream outputStream, int blockSize, long offset) throws IOException {
	    long size = input.length() - offset;
	   	InputStreamBuffer isb;
	   	int[] arr;
		if(size < blockSize){
			isb = new InputStreamBuffer(input.getPath(), offset, size, false);
			arr = isb.readArray((int) size);
		} else {
			isb = new InputStreamBuffer(input.getPath(), offset, blockSize, false);
			arr = isb.readArray(blockSize);
		}
		isb.close();
		// Arrays.sort(arr);
		radixSort(arr);
		ByteBuffer buf = ByteBuffer.allocate(arr.length * 4);
		buf.asIntBuffer().put(arr);
		outputStream.write(buf.array());
	}

	private static void mergeSort(File input, DataOutputStream outputStream, long blockSize, long offset) throws IOException {
		long len = input.length();
		long blockOffset = offset;

		PriorityQueue<InputStreamBuffer> q = new PriorityQueue<>((int) (len/blockSize+1), (isb1, isb2) -> (Integer.compare(isb1.peek(), isb2.peek())));

		for(int i = 0; i < BLOCK_NUM; i++){
		    InputStreamBuffer isb = null;
			if(blockOffset < len){
				if(len - blockOffset < blockSize){
					isb = new InputStreamBuffer(input.getPath(), blockOffset, len - blockOffset);
				} else {
					isb = new InputStreamBuffer(input.getPath(), blockOffset, blockSize);
				}
				q.offer(isb);
				blockOffset += blockSize;
			}
		}
		while (!q.isEmpty()){
			InputStreamBuffer isb = q.poll();
			outputStream.writeInt(isb.pop());
			if(!isb.done()){
				q.add(isb);
			} else {
				isb.close();
			}
		}
	}

	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1)
				;

			String computed = "";
			for(byte v : md.digest())
				computed += byteToHex(v);

			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

	public static void main(String[] args) throws Exception {
		String f1 = args[0];
		String f2 = args[1];
		long startingTime = System.currentTimeMillis();
		sort(f1, f2);
		long endingTime = System.currentTimeMillis();
		System.out.println("Time taken to run:" + (endingTime-startingTime) + "ms");
		System.out.println("The checksum is: "+checkSum(f1));
	}

	/**
	 * Code from http://algs4.cs.princeton.edu/code/
	 *
	 * Rearranges the array of 32-bit integers in ascending order.
	 * This is about 2-3x faster than Arrays.sort().
	 *
	 * @param a the array to be sorted
	 */
	public static void radixSort(int[] a) {
		final int BITS = 32;                 // each int is 32 bits
		final int R = 1 << BITS_PER_BYTE;    // each bytes is between 0 and 255
		final int MASK = R - 1;              // 0xFF
		final int w = BITS / BITS_PER_BYTE;  // each int is 4 bytes

		int n = a.length;
		int[] aux = new int[n];

		for (int d = 0; d < w; d++) {

			// compute frequency counts
			int[] count = new int[R+1];
			for (int i = 0; i < n; i++) {
				int c = (a[i] >> BITS_PER_BYTE*d) & MASK;
				count[c + 1]++;
			}

			// compute cumulates
			for (int r = 0; r < R; r++)
				count[r+1] += count[r];

			// for most significant byte, 0x80-0xFF comes before 0x00-0x7F
			if (d == w-1) {
				int shift1 = count[R] - count[R/2];
				int shift2 = count[R/2];
				for (int r = 0; r < R/2; r++)
					count[r] += shift1;
				for (int r = R/2; r < R; r++)
					count[r] -= shift2;
			}

			// move data
			for (int i = 0; i < n; i++) {
				int c = (a[i] >> BITS_PER_BYTE*d) & MASK;
				aux[count[c]++] = a[i];
			}

			// copy back
			for (int i = 0; i < n; i++)
				a[i] = aux[i];
		}
	}
}

