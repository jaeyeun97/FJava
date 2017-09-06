package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ExternalSort {

	private static final int BUF_SIZE = 2*1024576;
	private static String a;

	public static void sort(String f1, String f2) throws IOException {
		a = f1;
		sortRound(f1, f2, 0);
	}

	private static void sortRound(String input, String output, int round) throws IOException {
		RandomAccessFile inputFile = new RandomAccessFile(input, "r");
		RandomAccessFile outputFile = new RandomAccessFile(output, "rw");
		DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile.getFD())));

		long roundSize = (long) (BUF_SIZE * Math.pow(2, round));
		long blockSize = roundSize / 2;
		long len = inputFile.length();
		long arrNum = len / roundSize;
		long rem = len % roundSize;

		inputFile.close();

		if( rem != 0) arrNum++;

		for(long i = 0; i < arrNum; i++) {
		    if( len - roundSize*i < roundSize ){
		    	long size = len - roundSize*i;
				sortOneRound(input, outputStream, i*roundSize, blockSize, size);
			} else {
		    	sortOneRound(input, outputStream, i*roundSize, blockSize, roundSize);
			}
		}
		outputStream.flush();
		outputFile.close();

		if (len > roundSize){
			sortRound(output, input, round+1);
		} else if (!a.equals(output)){
		    Files.copy(Paths.get(output), Paths.get(a), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	private static void sortOneRound(String input, DataOutputStream outputStream, long offset, long blockSize, long size) throws IOException {
	    RandomAccessFile inputFile = new RandomAccessFile(input, "r");
		DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile.getFD())));

		inputStream.skip(offset);

		if(size <= BUF_SIZE) {
			// Assert: size % 4 = 0
			int[] arr = readArray(inputStream, (int) (size / 4));
			// Quick Sort
			Arrays.sort(arr);
			writeArray(outputStream, arr);
		}
		else if (size <= blockSize) {
			for(int i = 0; i < size; i+=4){
				outputStream.writeInt(inputStream.readInt());
			}
		}
		else {
			RandomAccessFile secondInputFile = new RandomAccessFile(input, "r");
			DataInputStream secondInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(secondInputFile.getFD())));
			secondInputStream.skip(offset + blockSize);
			long cnt1 = 0, cnt2 = blockSize; // 0 <= cnt1 < blockSize, blockSize <= cnt2 < size
			int n = inputStream.readInt();
			int m = secondInputStream.readInt();

			while(true) {
				if(n <= m){
					outputStream.writeInt(n);
					cnt1 += 4;
					if(cnt1 < blockSize) n = inputStream.readInt();
					else break;
				} else if (n > m){
					outputStream.writeInt(m);
					cnt2 += 4;
					if(cnt2 < size) m = secondInputStream.readInt();
					else break;
				}
			}

			if(cnt1 < blockSize){
				outputStream.writeInt(n); // write stream[cnt1]
				for(long k = cnt1+4; k < blockSize; k+=4){
					outputStream.writeInt(inputStream.readInt());
				}
			}
			else if(cnt2 < size){
				outputStream.writeInt(m); // write stream[cnt2]
				for(long k = cnt2+4; k < size; k+=4){
					outputStream.writeInt(secondInputStream.readInt());
				}
			}
			secondInputFile.close();
		}
		inputFile.close();
	}

	public static int[] readArray(DataInputStream inputStream, int arrSize) throws IOException {
		int[] arr = new int[arrSize];
		for(int i = 0; i < arrSize; i++){
			arr[i] = inputStream.readInt();
		}
		return arr;
	}
	public static void writeArray(DataOutputStream outputStream, int[] arr) throws IOException {
		for(int i = 0; i < arr.length; i++){
		    outputStream.writeInt(arr[i]);
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
