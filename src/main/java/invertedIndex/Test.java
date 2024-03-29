/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author ehab
 */
public class Test {
    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();
        String currentPath = System.getProperty("user.dir");
        //|**  change it to your collection directory 
        //|**  in windows "C:\\tmp11\\rl\\collection\\"       
//        String files = "/home/ehab/tmp11/rl/collection/";
        String files = currentPath + "/src/main/java/invertedIndex/tmp11/rl/collection/";
        File file = new File(files);
        //|** String[] 	list()
        //|**  Returns an array of strings naming the files and directories in the directory denoted by this abstract pathname.
        System.out.println(files);
        String[] fileList = file.list();
        System.out.println(fileList.length);
        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }
        index.buildIndex(fileList);
        index.store("index");
        index.printDictionary();

        String test3 = "data  should plain greatest comif"; // data  should plain greatest comif
        System.out.println("Boo0lean Model result = \n" + index.find_24_01(test3));

        String phrase = "";

        Posting lst =  null ;
        do {
            System.out.println("Print search phrase: ");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            phrase = in.readLine();
            String[] words = phrase.split("\\W+");
            boolean isExist = true ;
            for (String word : words) {
                if(!index.index.containsKey(word)) {
                    isExist = false ;
                    break;
                }
                Posting current = index.index.get(word).pList ;
                if(lst == null) {
                    lst = current ;
                }else{
                    lst = index.intersect(lst , current) ;
                }
            }

            if(isExist)index.printPostingList(lst) ;
            else System.out.println("does not exist");
/// -3- **** complete here ****
        } while (!phrase.isEmpty());
    }
}
