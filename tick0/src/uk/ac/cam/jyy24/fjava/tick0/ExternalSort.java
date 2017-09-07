package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.PriorityQueue;

public class ExternalSort {
	private static final int BLOCK_NUM = 16;
	private static final int BUF_SIZE = InputStreamBuffer.getBufSize();

	private static String a;

	public static void sort(String f1, String f2) throws IOException {
		a = f1;
		sortRound(f1, f2, 0);
	}

	private static void sortRound(String input, String output, int round) throws IOException {

		long len = new File(input).length();
		long roundSize = (long) (BUF_SIZE * Math.pow(BLOCK_NUM, round));
		long blockSize = roundSize / BLOCK_NUM;
		long arrNum = len / roundSize + ((len % roundSize == 0)? 0: 1);

		DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output), BUF_SIZE));

		for(long i = 0; i < arrNum; i++) {
			long size = len - roundSize*i;
		    if( size < roundSize ){
				sortBlock(input, outputStream, i*roundSize, blockSize, size);
			} else {
		    	sortBlock(input, outputStream, i*roundSize, blockSize, roundSize);
			}
		}
		outputStream.flush();
		outputStream.close();

		if (len > roundSize){
			sortRound(output, input, round+1);
		} else if (!a.equals(output)){
		    Files.copy(Paths.get(output), Paths.get(a), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	// each sortBlock should use the entire memory.
	private static void sortBlock(String input, DataOutputStream outputStream, long offset, long blockSize, long size) throws IOException {
	    //you can read `size` more bytes
		if(size <= BUF_SIZE) {
			InputStreamBuffer inputStream = new InputStreamBuffer(input, offset, size);
			int arrSize = (int) (size/4);
			int[] arr = new int[arrSize];
			for(int i = 0; i < arrSize; i++){
				arr[i] = inputStream.pop();
			}
			Arrays.sort(arr);
			for(int i = 0; i < arrSize; i++){
				outputStream.writeInt(arr[i]);
			}
		}
		else if (size <= blockSize) {
			InputStreamBuffer inputStream = new InputStreamBuffer(input, offset, size);
			for(int i = 0; i < size; i+=4){
				outputStream.writeInt(inputStream.pop());
			}
		}
		else {
			PriorityQueue<InputStreamBuffer> q = new PriorityQueue<>(BLOCK_NUM, (isb1, isb2) -> {
				int result = 0;
				try {
					 result = isb1.peek() - isb2.peek();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return result;
			});
			for(int i = 0; i < BLOCK_NUM; i++){
				long blockOffset = i * blockSize;
				if(blockOffset < size){
					long length = (size - blockOffset < blockSize)? size-blockOffset: blockSize;
					q.add(new InputStreamBuffer(input, offset + blockOffset, length));
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

	/*public static long getAvailableMemory() {
		System.gc();
		// http://stackoverflow.com/questions/12807797/java-get-available-memory
		Runtime r = Runtime.getRuntime();
		long allocatedMemory = r.totalMemory() - r.freeMemory();
		long presFreeMemory = r.maxMemory() - allocatedMemory;
		return presFreeMemory;
	}*/
}

