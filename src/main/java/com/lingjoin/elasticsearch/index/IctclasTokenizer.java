package com.lingjoin.elasticsearch.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingjoin.elasticsearch.util.Access;
import com.lingjoin.elasticsearch.util.Configuration;
import com.lingjoin.nlpir.IctclasNative;
import com.lingjoin.nlpir.NlpirException;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.env.Environment;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.lingjoin.elasticsearch.index.IctclasAnalysisPlugin.LOGGER;

/**
 * The type Ictclas tokenizer.
 */
public final class IctclasTokenizer extends Tokenizer {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    private static boolean initState = false;

    // 分词方法, 是否进行细拆分, true用于索引, false用于搜索
    private final boolean fineSegment;


    /**
     * 分词结果类
     */
    static class TokenResult {
        /**
         * 词对应开始位置
         */
        int begin;
        /**
         * 词对应结束位置
         */
        int end;
        /**
         * 词性
         */
        String pos;
        /**
         * 词
         */
        String text;

        /**
         * Instantiates a new Token result.
         *
         * @param begin begin
         * @param end   end
         * @param pos   pos
         * @param text  text
         */
        @ConstructorProperties({"begin", "end", "pos", "text"})
        public TokenResult(int begin, int end, String pos, String text) {
            this.begin = begin;
            this.end = end;
            this.pos = pos;
            this.text = text;
        }

        /**
         * 将分词结果 json 转换为类
         *
         * @param json the json
         * @return the list
         */
        public static List<TokenResult> parse(String json) {
            ObjectMapper objectMapper = new ObjectMapper();
            return Access.doPrivileged(() -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<>() {
                    });
                } catch (JsonProcessingException e) {
                    LOGGER.error("Jackson parse json failed: {}", e.getMessage(), e);
                    return null;
                }
            });
        }
    }

    /**
     * 分词初始化
     *
     * @param data         词典路径
     * @param encoding     编码 0：GBK；1：UTF-8
     * @param sLicenceCode 授权码，默认为""
     * @param userDict     用户词典文件
     * @param bOverwrite   用户词典引入方式
     * @param fineSegment  the fine segment
     * @throws NlpirException the nlpir exception
     */
    private IctclasTokenizer(String data, int encoding, String sLicenceCode, String userDict, boolean bOverwrite, boolean fineSegment) throws NlpirException {
        this.fineSegment = fineSegment;
        this.init(data, encoding, sLicenceCode, userDict, bOverwrite);
    }

    /**
     * Instantiates a new Ictclas tokenizer.
     *
     * @param configuration the configuration
     * @param environment   the environment
     * @param fineSegment   the fine segment
     * @throws NlpirException the nlpir exception
     */
    public IctclasTokenizer(Configuration configuration, Environment environment, boolean fineSegment) throws NlpirException {
        this(
                Configuration.getPluginPath(environment).toString(), 1, configuration.getLicenseCode(),
                Optional.ofNullable(configuration.getUserDict())
                        .map(dict -> Configuration.getUserDictionaryPath(environment, dict))
                        .map(Path::toAbsolutePath)
                        .map(Path::toString)
                        .orElse(null),
                configuration.isOverWrite(), fineSegment
        );
    }

    /**
     * 分词组件初始化, 全局只能进行一次
     *
     * @param data         词典路径
     * @param encoding     编码 0：GBK；1：UTF-8
     * @param sLicenceCode 授权码，默认为""
     * @param userDict     用户词典文件
     * @param bOverwrite   用户词典引入方式
     */
    private void init(String data, int encoding, String sLicenceCode, String userDict, boolean bOverwrite) throws NlpirException {
        if (IctclasTokenizer.initState)
            return;
        LOGGER.info("NLPIR 初始化");
        IctclasTokenizer.initState = IctclasNative.INSTANCE.NLPIR_Init(data, encoding, sLicenceCode);
        if (!IctclasTokenizer.initState) {
            String errorMsg = IctclasNative.INSTANCE.NLPIR_GetLastErrorMsg();
            LOGGER.error("NLPIR 初始化失败, {}", errorMsg);
            throw new NlpirException(errorMsg);
        }
        // TODO 导入词典
        if (userDict != null && !userDict.isEmpty() && !userDict.equals("\"\"")) {
            int state = IctclasNative.INSTANCE.NLPIR_ImportUserDict(userDict, bOverwrite);
            if (state == 0)
                try {
                    throw new NlpirException(IctclasNative.INSTANCE.NLPIR_GetLastErrorMsg());
                } catch (NlpirException e) {
                    LOGGER.error("Import user dict failed", e);
                }
        }
    }

    // 存储 tokenResults 读取进度
    private int cursor = 0;
    // 存储当前文本分词结果, 空表示当前文本为空或者没有开始分词
    private List<TokenResult> tokenResults = null;

    @Override
    public boolean incrementToken() throws IOException {
        // 完成后的初始化状态
        if (tokenResults != null && tokenResults.size() <= cursor) {
            cursor = 0;
            tokenResults = null;
            return false;
        }
        // 获取分词结果, 并进行判断是否有token
        this.getTokenResults(input);
        if (this.tokenResults == null) {
            cursor = 0;
            return false;
        }
        // 有分词结果, 开始进行输出
        clearAttributes();
        TokenResult currentToken = this.tokenResults.get(cursor);
        termAtt.append(currentToken.text);
        offsetAtt.setOffset(correctOffset(currentToken.begin), correctOffset(currentToken.end));
        typeAtt.setType(currentToken.pos);
        cursor++;
        return true;
    }

    /**
     * 获取 Reader 中的内容并进行分词, 将分词内容进行保存
     *
     * @param reader the reader
     * @throws IOException the io exception
     */
    private void getTokenResults(Reader reader) throws IOException {
        char[] arr = new char[1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        String targetString = buffer.toString();
        LOGGER.debug("Tokenizer Input: {}", targetString);
        if (!targetString.isEmpty()) {
            String segmentResult = IctclasNative.INSTANCE.NLPIR_Tokenizer4IR(targetString, fineSegment);
            this.tokenResults = TokenResult.parse(segmentResult);
            LOGGER.debug("Tokenizer Output: {}", segmentResult);
        } else {
            LOGGER.debug("Tokenizer Input is empty pass tokenization");
        }

    }
}
