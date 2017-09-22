package uk.ac.cam.jyy24.fjava.tick0;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ExternalSortTest {
    private Map<Integer, String> checksums = new HashMap<>();
    private Path path;

    @Before
    public void setup() throws FileNotFoundException, URISyntaxException {
        URL checksumURL = this.getClass().getResource("/checksum.txt");
        this.path = Paths.get(checksumURL.toURI()).getParent();
        BufferedReader br = new BufferedReader(new FileReader(checksumURL.getPath()));
        br.lines().forEach(line -> {
            String[] split = line.replace(" ","").split(":");
            checksums.put(Integer.parseInt(split[0]), split[1]);
        });
    }

    @Test
    public void test() throws IOException {
        long time = 0;
        for(int key: checksums.keySet()){
            String f1 = this.path.resolve(String.format("test%da.dat", key)).toString();
            String f2 = this.path.resolve(String.format("test%db.dat", key)).toString();
            long startingTime = System.nanoTime();
            ExternalSort.sort(f1, f2);
            long endingTime = System.nanoTime();
            long t = endingTime-startingTime;
            System.out.println("Time taken to run test " + key + ": " + t + "ns");
            time += t;
            String actual = ExternalSort.checkSum(f1);
            assertEquals(String.format("Error in checksum for File Set %d", key), checksums.get(key), actual);
        }
        System.out.println("Total Time: " + time + "ns");
    }
}
