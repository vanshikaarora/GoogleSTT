package com.google.cloud.android.speech;

import java.util.HashMap;
import java.util.StringTokenizer;

class TrieNode {

    // Alphabet size (# of symbols)
    //static final int ALPHABET_SIZE = 26;
    // If not present, inserts key into trie
    // If the key is prefix of trie node,
    // just marks leaf node
    HashMap<String, TrieNode> children;

    // isEndOfWord is true if the node represents
    // end of a word
    boolean isEndOfWord;

    TrieNode() {
        isEndOfWord = false;
        children = new HashMap<>();
    }

    void insert(String key) {
        StringTokenizer st1 = new StringTokenizer(key, " ");
        int level;
        int length = st1.countTokens();
        int index;
        String curr;
        TrieNode pCrawl = this;
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
    boolean search(String key, boolean prefix) {
        if (key != null) {
            StringTokenizer st1 = new StringTokenizer(key, " ");
            int level;
            int length = st1.countTokens();
            int index;
            TrieNode pCrawl = this;
            String curr;
            for (level = 0; level < length; level++) {
                //index = key.charAt(level) - 'a';
                curr = st1.nextToken();
                if (!pCrawl.children.containsKey(curr))
                    return false;

                pCrawl = pCrawl.children.get(curr);
            }
            if (prefix) return pCrawl != null;
            return (pCrawl != null && pCrawl.isEndOfWord);
        }
        return false;
    }
}

class tryTrie {
    public static void main(String args[]) {

        String sentence = "Vanshika is Android God";
        TrieNode root = new TrieNode();
        root.insert(sentence);
        System.out.println(root.search("Vanshika", true));
        System.out.println(root.search("Vanshika", false));
        System.out.println(root.search("Vanshika is Android God", false));
        System.out.println(root.search("Vanshika is Android", false));

    }
}