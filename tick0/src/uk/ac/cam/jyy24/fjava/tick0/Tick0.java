package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;

public class Tick0 {
    public static void main(String[] arg) throws IOException {
        // RandomAccessFile f1 = new RandomAccessFile("data/test10a.dat","r");
        RandomAccessFile f2 = new RandomAccessFile("data/test17a.dat","r");
        int j = -1;

        for(int i = 0; i < f2.length(); i+=4){
            int k = f2.readInt();
            if(j > k)
                System.out.printf("%d > %d\n", j, k);
            j = k;
        }
        System.out.println(f2.length());
    }
}
