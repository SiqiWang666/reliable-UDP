import java.util.Random;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;


public class test{
  public static void main(String[] args)
  {
      FileInputStream File1 = null;
      FileInputStream File2 = null;
      BufferedReader in = null;
      String sFile;

      if(args.length != 2)
      {
          System.out.println("The command line should be: java IOOperation testX.txt testX.txt");
          System.out.println("X should be one of the array: 1, 2, 3");
          System.exit(0);
      }

      try
      {
          File1 = new FileInputStream(args[0]);
          File2 = new FileInputStream(args[1]);

          try
          {

              if(File1.available() != File2.available() && 1 == 0)
              {
                  System.out.println(File1.available());
                  System.out.println(File2.available());
                  System.out.println(args[0] + " is not equal to " + args[1]);
              }
              else
              {
                  boolean tag = true;
                  int a = 0;
                  int b = 0;

                  while( (a = File1.read()) != -1 && (b = File2.read()) != -1)
                  {
                      a ++;
                      System.out.print((char)a);
                      // if(File1.read() != File2.read())
                      // {
                      //     tag = false;
                      //     break;
                      // }
                  }

                  if(tag == true)
                      System.out.println(args[0] + " equals to " + args[1]);
                  else
                      System.out.println(args[0] + " is not equal to " + args[1]);
              }
          }
          catch(IOException e)
          {
              System.out.println(e);
          }
      }
      catch (FileNotFoundException e)
      {
          System.out.println("File can't find..");
      }
      finally
      {

          try
          {
              if(File1 != null)
                  File1.close();
              if(File2 != null)
                  File2.close();
          }
          catch (IOException e)
          {
              System.out.println(e);
          }
      }
  }
}
