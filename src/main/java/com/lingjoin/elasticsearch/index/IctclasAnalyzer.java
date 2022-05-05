package com.lingjoin.elasticsearch.index;

import com.lingjoin.elasticsearch.util.Configuration;
import com.lingjoin.nlpir.NlpirException;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.env.Environment;

/**
 * Ictclas analyzer.
 */
public class IctclasAnalyzer extends Analyzer {
    private final Environment environment;
    private final Configuration configuration;
    private final boolean fineSegment;

    /**
     * Instantiates a new Ictclas analyzer.
     *
     * @param configuration the configuration
     * @param environment   the environment
     * @param fineSegment   the fine segment
     */
    public IctclasAnalyzer(Configuration configuration, Environment environment, boolean fineSegment) {
        super();
        this.environment = environment;
        this.fineSegment = fineSegment;
        this.configuration = configuration;
    }


    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        try {
            IctclasAnalysisPlugin.LOGGER.info("New tokenizer IctclasAnalyzer for {}", fieldName);
            IctclasAnalysisPlugin.LOGGER.info("PluginPath: {}", Configuration.getPluginPath(environment));
            return new TokenStreamComponents(new IctclasTokenizer(configuration, environment, fineSegment));
        } catch (NlpirException e) {
            IctclasAnalysisPlugin.LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }
}
