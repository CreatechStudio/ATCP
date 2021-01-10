1. ## Abstract

   *indi.atatc.packages.basics* is a module customized for every project developed by ATATC™. 
   Only general classes and methods will be introduced in this passage. 

2. ## Structures

   All the default structures are not thread-safe. If you're using in multiple threads, use the ones of the inner class called *Concurrent* instead. 

   1. ### List

      This is a redeveloped two-way linked list, benchmarking the LinkedList of native Java. The function and usage method are basically consistent with LinkedList. Therefore only the differences will be mentioned. 

      1. #### Performance Comparsion

         |                                 | List | LinkedList |
         | ------------------------------- | ---- | ---------- |
         | Sequential Write(100,000 times) |      |            |
         |                                 |      |            |
         |                                 |      |            |

         

