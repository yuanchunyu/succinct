package edu.berkeley.cs.succinct.buffers;

import edu.berkeley.cs.succinct.StorageMode;
import edu.berkeley.cs.succinct.SuccinctIndexedFile;
import edu.berkeley.cs.succinct.SuccinctIndexedFileTest;
import edu.berkeley.cs.succinct.util.Source;
import edu.berkeley.cs.succinct.util.SuccinctConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class SuccinctIndexedFileBufferTest extends SuccinctIndexedFileTest {

  byte[] data;
  private String testFileRaw = this.getClass().getResource("/test_file").getFile();
  private String testFileSuccinct =
    this.getClass().getResource("/test_file").getFile() + ".idx.succinct";
  private String testFileSuccinctMin =
    this.getClass().getResource("/test_file").getFile() + ".idx.min.succinct";
  private String[] queryStrings = {"int", "include", "random", "random int"};

  @Override public String getQueryString(int i) {
    return queryStrings[i];
  }

  @Override public int numQueryStrings() {
    return queryStrings.length;
  }

  @Override public String getData() {
    return new String(data);
  }

  /**
   * Set up test.
   *
   * @throws Exception
   */
  public void setUp() throws Exception {
    super.setUp();

    File inputFile = new File(testFileRaw);

    data = new byte[(int) inputFile.length()];
    DataInputStream dis = new DataInputStream(new FileInputStream(inputFile));
    dis.readFully(data);
    fileData = new Source() {
      @Override public int length() {
        return data.length;
      }

      @Override public int get(int i) {
        return data[i];
      }
    };

    ArrayList<Integer> positions = new ArrayList<>();
    positions.add(0);
    for (int i = 0; i < fileData.length(); i++) {
      if (fileData.get(i) == '\n') {
        positions.add(i + 1);
      }
    }
    offsets = new int[positions.size()];
    for (int i = 0; i < offsets.length; i++) {
      offsets[i] = positions.get(i);
    }
    sIFile = new SuccinctIndexedFileBuffer(data, offsets);
  }

  /**
   * Test method: void readObject(ObjectInputStream ois)
   * Test method: void writeObject(ObjectOutputStream oos)
   *
   * @throws Exception
   */
  public void testSerializeDeserialize() throws Exception {

    // Serialize data
    FileOutputStream fOut = new FileOutputStream(testFileSuccinct);
    ObjectOutputStream oos = new ObjectOutputStream(fOut);
    oos.writeObject(sIFile);
    oos.close();

    // Deserialize data
    FileInputStream fIn = new FileInputStream(testFileSuccinct);
    ObjectInputStream ois = new ObjectInputStream(fIn);
    SuccinctIndexedFile sIFileRead = (SuccinctIndexedFileBuffer) ois.readObject();
    ois.close();

    assertNotNull(sIFileRead);
    assertEquals(sIFile.getNumRecords(), sIFileRead.getNumRecords());
    for (int i = 0; i < sIFile.getNumRecords(); i++) {
      assertTrue(Arrays.equals(sIFile.getRecordBytes(i), sIFileRead.getRecordBytes(i)));
    }
  }

  /**
   * Test method: void writeToFile(String path)
   * Test method: void memoryMap(String path)
   *
   * @throws Exception
   */
  public void testMemoryMap() throws Exception {
    ((SuccinctIndexedFileBuffer) sIFile).writeToFile(testFileSuccinctMin);
    SuccinctIndexedFile sIFileRead =
      new SuccinctIndexedFileBuffer(testFileSuccinctMin, StorageMode.MEMORY_MAPPED);

    assertNotNull(sIFileRead);
    assertEquals(sIFile.getNumRecords(), sIFileRead.getNumRecords());
    for (int i = 0; i < sIFile.getNumRecords(); i++) {
      assertTrue(Arrays.equals(sIFile.getRecordBytes(i), sIFileRead.getRecordBytes(i)));
    }
  }

  /**
   * Test method: void writeToFile(String path)
   * Test method: void readFromFile(String path)
   *
   * @throws Exception
   */
  public void testReadFromFile() throws Exception {
    ((SuccinctIndexedFileBuffer) sIFile).writeToFile(testFileSuccinctMin);
    SuccinctIndexedFile sIFileRead =
      new SuccinctIndexedFileBuffer(testFileSuccinctMin, StorageMode.MEMORY_ONLY);

    assertNotNull(sIFileRead);
    assertEquals(sIFile.getNumRecords(), sIFileRead.getNumRecords());
    for (int i = 0; i < sIFile.getNumRecords(); i++) {
      assertTrue(Arrays.equals(sIFile.getRecordBytes(i), sIFileRead.getRecordBytes(i)));
    }
  }

  /**
   * Test method: void construct(byte[] input, int[] offsets DataOutputStream out)
   *
   * @throws Exception
   */
  public void testConstruct() throws Exception {
    FileOutputStream fos = new FileOutputStream(testFileSuccinctMin);
    DataOutputStream os = new DataOutputStream(fos);

    SuccinctIndexedFileBuffer.construct(data, offsets, os, new SuccinctConfiguration());
    os.close();

    SuccinctIndexedFile sIFileRead =
      new SuccinctIndexedFileBuffer(testFileSuccinctMin, StorageMode.MEMORY_ONLY);

    assertNotNull(sIFileRead);
    assertEquals(sIFile.getNumRecords(), sIFileRead.getNumRecords());
    for (int i = 0; i < sIFile.getNumRecords(); i++) {
      assertTrue(Arrays.equals(sIFile.getRecordBytes(i), sIFileRead.getRecordBytes(i)));
    }
  }
}
