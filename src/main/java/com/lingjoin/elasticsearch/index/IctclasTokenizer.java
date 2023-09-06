package com.lingjoin.elasticsearch.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingjoin.elasticsearch.util.Access;
import com.lingjoin.elasticsearch.util.Configuration;
import com.lingjoin.nlpir.IctclasNative;
import com.lingjoin.nlpir.NlpirException;
import com.sun.jna.Native;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.env.Environment;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private final PositionIncrementAttribute positionAtt = addAttribute(PositionIncrementAttribute.class);

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
     * Instantiates a new Ictclas tokenizer.
     *
     * @param configuration the configuration
     * @param environment   the environment
     * @param fineSegment   the fine segment
     * @throws NlpirException the nlpir exception
     */
    public IctclasTokenizer(Configuration configuration, Environment environment, boolean fineSegment) throws NlpirException {
        this.fineSegment = fineSegment;
        if (!initState) {
            IctclasAnalysisPlugin.LOGGER.info("Set jna.tmpdir in IctclasAnalysisPlugin");
            Access.doPrivileged(() -> System.setProperty("jna.tmpdir", environment.tmpFile().toString()));
            // https://github.com/java-native-access/jna/blob/5.10.0/src/com/sun/jna/Native.java#L846C3-L846C3
            // 设置 jna.encoding 为 UTF-8, 在Elasticsearch 7.16.0 后从 5.7.0 升级到 5.10.0, 会造成转码的错误
            Access.doPrivileged(() -> System.setProperty("jna.encoding", StandardCharsets.UTF_8.name()));
            LOGGER.info("Set jna.encoding: {}", Native.getDefaultStringEncoding());
            init(
                    Configuration.getPluginPath(environment).toString(), configuration.getLicenseCode(),
                    Optional.ofNullable(configuration.getUserDict())
                            .map(dict -> Configuration.getUserDictionaryPath(environment, dict))
                            .map(Path::toAbsolutePath)
                            .map(Path::toString)
                            .orElse(null),
                    configuration.isOverWrite()
            );
        }
    }

    /**
     * 分词组件初始化, 全局只能进行一次
     *
     * @param data         词典路径
     * @param sLicenceCode 授权码，默认为""
     * @param userDict     用户词典文件
     * @param bOverwrite   用户词典引入方式
     */
    private static synchronized void init(String data, String sLicenceCode, String userDict, boolean bOverwrite) throws NlpirException {
        if (IctclasTokenizer.initState)
            return;
        LOGGER.info("NLPIR 初始化");
        IctclasTokenizer.initState = IctclasNative.INSTANCE.NLPIR_Init(data, 1, sLicenceCode);
        if (!IctclasTokenizer.initState) {
            String errorMsg = IctclasNative.INSTANCE.NLPIR_GetLastErrorMsg();
            LOGGER.error("NLPIR 初始化失败, {}", errorMsg);
            throw new NlpirException(errorMsg);
        }
        LOGGER.info("NLPIR 初始化成功");
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
    private int endPosition = 0;
    // 上一个 positionAtt 不会 0 的位置, 用于比较是否加入了回溯的token
    private int lastBeginPosition = 0;
    private int lastEndPosition = 0;
    // 存储当前文本分词结果, 空表示当前文本为空或者没有开始分词
    private List<TokenResult> tempTokenResults;

    private void setTokenResults(List<TokenResult> tokenResults) {
        tempTokenResults = tokenResults;
    }

    private List<TokenResult> getTokenResults() {
        return Optional.ofNullable(this.tempTokenResults).orElse(List.of());
    }

    @Override
    public boolean incrementToken() {
        // 获取分词结果, 并进行判断是否有token
        // this.getTokenResults(input);
        if (this.getTokenResults().isEmpty()) {
            cursor = 0;
            return false;
        }

        // 若当前的 Token 的 end 比存储的最大的 endPosition 要小或者想等, 那么后者应该只是前者更细的分词, 需要将
        // PositionIncrementAttribute 设置为 0
        // 使用循环用于跳过位置信息错误的 token
        while (true) {
            // 若 cursor 大于 tokenResults 的长度, 说明已经获取完当前数据
            if (this.getTokenResults().size() <= cursor) {
                return false;
            }
            // 清理当前的位置信息, 并开始解析数据
            clearAttributes();
            // 获取token数据, 并解析位置信息
            TokenResult currentToken = this.getTokenResults().get(cursor);
            // 过滤掉在超过上一个 PositionAtt 不为0的 token, 否则会抛出异常
            if (lastBeginPosition > currentToken.begin) {
                cursor++;
                continue;
            }
            // 若当前 Token 的位置在上一个 PositionAtt 为非0 的 token 内部, 则设置当前的 positionAtt 为 0, 为前者的细粒度分词
            // 否则为顺序的下一个分词结果, 设置 PositionAtt 为 1 并更新上一个 positionAtt=1 的token 位置
            if (lastEndPosition >= currentToken.end) {
                positionAtt.setPositionIncrement(0);
            } else {
                positionAtt.setPositionIncrement(1);
                lastBeginPosition = currentToken.begin;
                lastEndPosition = currentToken.end;
            }
            LOGGER.debug(String.format(
                    "[%s] start:%s end:%s lastMax:%s position:%s",
                    currentToken.text, currentToken.begin, currentToken.end, lastEndPosition, positionAtt.getPositionIncrement()
            ));
            // 更新其他位置信息
            termAtt.append(currentToken.text);
            offsetAtt.setOffset(correctOffset(currentToken.begin), correctOffset(currentToken.end));
            typeAtt.setType(currentToken.pos);
            cursor++;
            this.endPosition = currentToken.end;
            return true;
        }
    }

    @Override
    public void end() throws IOException {
        super.end();
        int finalOffset = correctOffset(this.endPosition);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        // 分词, 并重置 cursor
        this.getTokenResults(input);
        cursor = 0;
        endPosition = 0;
        lastBeginPosition = 0;
        lastEndPosition = 0;
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
            LOGGER.debug("Tokenizer Input: {}", targetString);
            String segmentResult = IctclasNative.INSTANCE.NLPIR_Tokenizer4IR(targetString, fineSegment);
            this.setTokenResults(TokenResult.parse(segmentResult));
            LOGGER.debug("Tokenizer Output: {}", segmentResult);
        } else {
            LOGGER.debug("Tokenizer Input is empty pass tokenization");
        }

    }
}
