package com.lingjoin.nlpir;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.IOException;

/**
 * The interface Ictclas native.
 */
public interface IctclasNative extends Library {

    /**
     * The constant INSTANCE.
     */
    IctclasNative INSTANCE = getInstance();

    /**
     * Gets instance.
     *
     * @return the instance
     */
    static IctclasNative getInstance() {
        String name = "NLPIR";
        try {
            name = Native.extractFromResourcePath(name, IctclasNative.class.getClassLoader()).getAbsolutePath();
        } catch (IOException ignored) {
        }
        return Native.load(name, IctclasNative.class);
    }

    /**
     * Nlpir init boolean.
     *
     * @param sDataPath    the s data path
     * @param encoding     the encoding
     * @param sLicenceCode the s licence code
     * @return the boolean
     */
    boolean NLPIR_Init(String sDataPath, int encoding, String sLicenceCode);

    /**
     * Nlpir paragraph process string.
     *
     * @param sParagraph the s paragraph
     * @param bPOSTagged the b pos tagged
     * @return the string
     */
    String NLPIR_ParagraphProcess(String sParagraph, int bPOSTagged);

    /**
     * Nlpir add user word int.
     *
     * @param userWord the user word
     * @return the int
     */
    int NLPIR_AddUserWord(String userWord);

    /**
     * Nlpir import user dict int.
     *
     * @param dictFileName the dict file name
     * @param bOverwrite   the b overwrite
     * @return the int
     */
    int NLPIR_ImportUserDict(String dictFileName, boolean bOverwrite);

    /**
     * Nlpir get last error msg string.
     *
     * @return the string
     */
    String NLPIR_GetLastErrorMsg();

    /**
     * Nlpir exit boolean.
     *
     * @return the boolean
     */
    boolean NLPIR_Exit();

    /**
     * Nlpir tokenizer 4 ir string.
     *
     * @param text         the text
     * @param bFineSegment the b fine segment
     * @return the string
     */
    String NLPIR_Tokenizer4IR(String text, Boolean bFineSegment);

}


