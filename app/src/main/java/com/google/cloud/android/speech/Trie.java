package com.google.cloud.android.speech;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Trie {

    // Alphabet size (# of symbols)
    //static final int ALPHABET_SIZE = 26;

    static TrieNode root;



    // If not present, inserts key into trie
    // If the key is prefix of trie node,
    // just marks leaf node
    static void insert(String key) {
        StringTokenizer st1 = new StringTokenizer(key, " ");
        int level;
        int length = st1.countTokens();
        int index;
        String curr;
        TrieNode pCrawl = root;
        for (level = 0; level < length; level++) {
            //index = key.charAt(level) - 'a';
            curr = st1.nextToken();
            //map.containsKey("vishal")

            if (!pCrawl.children.containsKey(curr)) {
                TrieNode obj = new TrieNode();
                pCrawl.children.put(curr, obj);
            }
            pCrawl = pCrawl.children.get(curr);
        }
        // mark last node as leaf
        pCrawl.isEndOfWord = true;
    }

    // Returns true if key presents in trie, else false
    static boolean search(String key, boolean prefix) {
        StringTokenizer st1 = new StringTokenizer(key, " ");
        int level;
        int length = st1.countTokens();
        int index;
        TrieNode pCrawl = root;
        String curr;
        for (level = 0; level < length; level++) {
            //index = key.charAt(level) - 'a';
            curr = st1.nextToken();
            if (!pCrawl.children.containsKey(curr))
                return false;

            pCrawl = pCrawl.children.get(curr);
        }
        if(prefix) return pCrawl != null;
        return (pCrawl != null && pCrawl.isEndOfWord);
    }

    // Driver
    public static void main(String args[]) {
        BufferedReader k = new BufferedReader(new InputStreamReader(System.in));
        String sentence = "Vanshika is Android God";
        insert(sentence);
        System.out.println(search("Vanshika",true));
        System.out.println(search("Vanshika",false));
        System.out.println(search("Vanshika is Android God",false));


    }

    // trie node
    static class TrieNode {
        //TrieNode[] children = new TrieNode[ALPHABET_SIZE];
        HashMap<String, TrieNode> children;

        // isEndOfWord is true if the node represents
        // end of a word
        boolean isEndOfWord;

        TrieNode() {
            isEndOfWord = false;
            //for (int i = 0; i < ALPHABET_SIZE; i++)
            //   children[i] = null;
        }
    }
}
// This code is contributed by Sumit Ghosh