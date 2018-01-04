package sort;

import java.util.Random;

/**
 * 快速排序
 */
public class QuickSort {

    public static void main(String[] args) {
        Random random = new Random();
        int size = 20;
        int array[] = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(1000);
        }
        for (int i : array) {
            System.out.print(i + ",");
        }
        System.out.println();
        sort(array, 0, size);
        for (int i : array) {
            System.out.print(i + ",");
        }
    }

    public static void sort(int[] array, int start, int end) {
        int size = end - start;
        if (size <= 1) {
            return;
        }
        int[] tmpArray = new int[size];
        int mid = (end + start) / 2;
        int paceSetter = array[mid];
        int tmpStart = 0;
        int tmpEnd = size - 1;
        for (int i = start; i < end; i++) {
            if (i == mid) {
                continue;
            }
            if (array[i] <= paceSetter) {
                tmpArray[tmpStart++] = array[i];
            } else  {
                tmpArray[tmpEnd--] = array[i];
            }
        }
        tmpArray[tmpEnd] = paceSetter;
        System.arraycopy(tmpArray, 0, array, start, size);
        sort(array, start, start + tmpStart);
        sort(array, end - (size - tmpEnd), end);
    }

}
