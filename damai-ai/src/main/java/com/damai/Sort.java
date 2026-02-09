package com.damai;

import lombok.SneakyThrows;

import java.util.Arrays;

//快速排序，归并排序
public class Sort {
    public static void main(String[] args) {
//        int[] arr = {0,4,2,6,3,7,1,9,5};
//        int[] sorted = {1,2,3,4,6,6,7,8,9,10};
//        quickSort(arr,0,arr.length-1);
//        System.out.println(Arrays.toString(arr));
        threadPrint();
    }
    static int cnt = 0;
    public static void threadPrint(){
        Object object = new Object();
        Thread t1 = new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
               while(cnt<=100){
                   synchronized (object){
                       if(cnt%2==1){
                           object.wait();
                       }
                       System.out.println(cnt++);
                       object.notifyAll();
                   }
               }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                while(cnt<=100){
                    synchronized (object){
                        if(cnt%2==0){
                            object.wait();
                        }
                        System.out.println(cnt++);
                        object.notifyAll();
                    }
                }
            }
        });
        t1.start();
        t2.start();
    }

    public static int leftMost(int[] arr,int target){
        int left = 0;
        int right = arr.length;
        while(left<right){
            int mid = (left+right)>>>1;
            if(arr[mid]<target){
                left = mid+1;
            }
            else{
                right = mid;
            }
        }
// 在 leftMost 中，“第一个元素的索引” 和 “最左侧插入点” 在数值上是重合的；
// 但在 rightMost 中，“最后一个元素的索引” 和 “最右侧插入点” 在数值上相差了 1。
        return left;
    }
    public static int rightMost(int[] arr,int target){
        int left = 0;
        int right = arr.length;
        while(left<right){
            int mid = (left+right)>>>1;
            if(arr[mid]>target){
                right = mid;
            }
            else{
                left = mid+1;
            }
        }
        // 【统一化处理】
        // 如果 left 前面那个元素刚好是 target，说明找到了，返回最右侧索引
        if (left > 0 && arr[left - 1] == target) {
            return left - 1;
        }

        // 如果没找到，直接返回 left，此时它就是应当插入的位置
        return left;
    }
    public static void timeSort(int[] arr,int left,int right){
        if(left>=right){
            return;
        }
        int mid = (left+right)>>>1;
        timeSort(arr,left,mid);
        timeSort(arr,mid+1,right);
        int[] tmp = new int[right-left+1];
        int i = left;
        int j = mid+1;
        int index = 0;
        while(i<=mid&&j<=right){
            if(arr[i]<arr[j]){
                tmp[index++] = arr[i++];
            }
            else{
                tmp[index++] = arr[j++];
            }
        }
        while(i<=mid){
            tmp[index++] = arr[i++];
        }
        while(j<=right){
            tmp[index++] = arr[j++];
        }
        for(int k = 0;k<index;k++){
            arr[left+k] = tmp[k];
        }
    }
    //如果 Pivot 在大数那一侧（右侧）：就和 大数区的开头 换。
    //如果 Pivot 在小数那一侧（左侧）：就和 小数区的结尾 换。
    public static void quickSort(int[] arr,int left,int right){
        if(left>=right){
            return;
        }
        int pivot = arr[right];
        int j = left;
        for(int i = left;i<right;i++){
            if(arr[i]<pivot){
                int tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                j++;
            }
        }
        arr[right] = arr[j];
        arr[j] = pivot;
        quickSort(arr,left,j-1);
        quickSort(arr,j+1,right);

    }

}
