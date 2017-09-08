package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ExternalSort {
	private static final int BLOCK_NUM = 16;
	private static final int BUF_SIZE = InputStreamBuffer.getBufSize();


	public static void sort(String f1, String f2) throws IOException {
		File input = new File(f1);
		File output = new File(f2);
		File a = input;
		long length = a.length();
		// long blockSize = BUF_SIZE / BLOCK_NUM;
		long blockSize = 4;

		while(blockSize <= length){
			long offset = 0;
			DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output), BUF_SIZE));
			while (offset < length) {
				mergeSort(input, outputStream, blockSize, offset);
				offset += BLOCK_NUM * blockSize;
			}
			outputStream.flush();
			outputStream.close();
			blockSize *= BLOCK_NUM;
			if(blockSize < length) {
				File tmp = output;
				output = input;
				input = tmp;
			}
		}
		if (!a.equals(output)){
			Files.copy(output.toPath(), a.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void quickSort(File input, DataOutputStream outputStream, int blockSize, long offset) throws IOException {
	    long size = input.length() - offset;
	   	InputStreamBuffer isb;
	   	int[] arr;
		if(size < blockSize){
			isb = new InputStreamBuffer(input.getPath(), offset, size);
			arr = new int[(int) (size/4)];
		} else {
			isb = new InputStreamBuffer(input.getPath(), offset, blockSize);
			arr = new int[blockSize/4];
		}
		for(int i = 0; i < arr.length; i++){
			arr[i] = isb.pop();
		}
		Arrays.sort(arr);
		for(int i = 0; i < arr.length; i++){
			outputStream.writeInt(arr[i]);
		}
	}

	private static void mergeSort(File input, DataOutputStream outputStream, long blockSize, long offset) throws IOException {
		PriorityQueue<InputStreamBuffer> q = new PriorityQueue<>(BLOCK_NUM, (isb1, isb2) -> (isb1.peek() - isb2.peek()));

		for(int i = 0; i < BLOCK_NUM; i++){
			long blockOffset = i * blockSize + offset;
			long len = input.length();
			if(blockOffset < len){
				if(len - blockOffset < blockSize){
					q.add(new InputStreamBuffer(input.getPath(), blockOffset, len - blockOffset));
				} else {
					q.add(new InputStreamBuffer(input.getPath(), blockOffset, blockSize));
				}
			}
		}
		while (!q.isEmpty()){
			InputStreamBuffer isb = q.poll();
			if(!isb.done()){
				outputStream.writeInt(isb.pop());
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
		sort(f1, f2);
		System.out.println("The checksum is: "+checkSum(f1));
	}
}

