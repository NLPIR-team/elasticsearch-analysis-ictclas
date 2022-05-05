package com.lingjoin.elasticsearch.index;

import com.lingjoin.elasticsearch.util.Configuration;
import com.lingjoin.nlpir.NlpirException;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

import static com.lingjoin.elasticsearch.index.IctclasAnalysisPlugin.LOGGER;

/**
 * Ictclas tokenizer factory.
 */
public class IctclasTokenizerFactory extends AbstractTokenizerFactory {


    final private Configuration configuration;

    final private boolean fineSegment;
    final private Environment environment;

    /**
     * Instantiates a new Ictclas tokenizer factory.
     *
     * @param indexSettings the index settings
     * @param environment   the environment
     * @param settings      the settings
     * @param name          the name
     * @param fineSegment   the fine segment
     */
    protected IctclasTokenizerFactory(IndexSettings indexSettings, Environment environment, Settings settings, String name, boolean fineSegment) {
        super(indexSettings, settings, name);
        this.environment = environment;
        this.fineSegment = fineSegment;
        this.configuration = Configuration.getConfiguration(environment);
    }

    /**
     * Gets ictclas search tokenizer factory.
     *
     * @param indexSettings the index settings
     * @param environment   the environment
     * @param name          the name
     * @param settings      the settings
     * @return the ictclas search tokenizer factory
     */
    public static IctclasTokenizerFactory getIctclasSearchTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        // 搜索时不用切分过细, 增加搜索速度
        return new IctclasTokenizerFactory(indexSettings, environment, settings, name, false);
    }

    /**
     * Gets ictclas index tokenizer factory.
     *
     * @param indexSettings the index settings
     * @param environment   the environment
     * @param name          the name
     * @param settings      the settings
     * @return the ictclas index tokenizer factory
     */
    public static IctclasTokenizerFactory getIctclasIndexTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        // 索引时使用细切分粒度, 增加搜索准确度
        return new IctclasTokenizerFactory(indexSettings, environment, settings, name, true);
    }

    @Override
    public Tokenizer create() {
        try {
            return new IctclasTokenizer(configuration, environment, fineSegment);
        } catch (NlpirException e) {
            LOGGER.error("Initialization Tokenizer failed", e);
            throw new RuntimeException(e);
        }
    }
}
