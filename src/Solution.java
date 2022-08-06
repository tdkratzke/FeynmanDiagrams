import java.util.*;
import java.math.*;

/** Test a check-in. */
public class Solution {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        long testCase = input.nextLong();
        input.close();
        System.out.println((long)(lines(testCase, ((testCase/2)+1))/Math.pow(2, (double)testCase/2)));
    }

    static long lines(long testCase, long halfTestCase){

        if(testCase == halfTestCase){
            return halfTestCase;
        }
        else{
            return (testCase * lines(testCase - 1, halfTestCase));
        }
    }
}
