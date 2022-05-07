package com.lingjoin.elasticsearch.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Ictclas analysis plugin.
 */
public class IctclasAnalysisPlugin extends Plugin implements AnalysisPlugin {
    /**
     * The constant LOGGER.
     */
    public static final Logger LOGGER = LogManager.getLogger();

    /**
     * The constant PLUGIN_NAME.
     */
    public static final String PLUGIN_NAME = "analysis-ictclas";

    // TODO Filter 停用词
    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new HashMap<>();
        extra.put("ictclas_search", IctclasTokenizerFactory::getIctclasSearchTokenizerFactory);
        extra.put("ictclas_index", IctclasTokenizerFactory::getIctclasIndexTokenizerFactory);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new HashMap<>();
        extra.put("ictclas_search", IctclasAnalyzerProvider::getIctclasSearchAnalyzerProvider);
        extra.put("ictclas_index", IctclasAnalyzerProvider::getIctclasIndexAnalyzerProvider);
        return extra;
    }

}
