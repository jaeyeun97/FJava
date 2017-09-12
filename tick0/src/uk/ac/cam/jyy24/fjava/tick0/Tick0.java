package uk.ac.cam.jyy24.fjava.tick0;

import java.io.*;

public class Tick0 {
    public static void main(String[] args) throws IOException {
        RandomAccessFile f2 = new RandomAccessFile("tick0/build/resources/test/test14a.dat","r");
        DataInputStream is = new DataInputStream(new BufferedInputStream(new FileInputStream(f2.getFD())));
        int a = is.readInt();
        System.out.println(f2.length());
        for(int i = 4; i < f2.length(); i+=4){
            int b = is.readInt();
            if(a > b) {
                System.out.printf("%d > %d @ %d\n", a, b, i/4);
            }
            a = b;
        }
    }
}
