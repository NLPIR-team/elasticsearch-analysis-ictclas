package com.lingjoin.nlpir;


class IctclasNativeTest {

    public static void main(String[] args) {
        IctclasNative instance = IctclasNative.INSTANCE;
        boolean b = instance.NLPIR_Init("", 1, "");
        System.out.println(b);
        System.out.println("aaaaaaa");
        String result = instance.NLPIR_ParagraphProcess("这是什么", 0);
        System.out.println(result);
    }
}