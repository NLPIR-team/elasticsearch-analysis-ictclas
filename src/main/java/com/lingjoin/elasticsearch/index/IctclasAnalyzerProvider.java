package com.lingjoin.elasticsearch.index;

import com.lingjoin.elasticsearch.util.Configuration;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

/**
 * The type Ictclas analyzer provider.
 */
public class IctclasAnalyzerProvider extends AbstractIndexAnalyzerProvider<IctclasAnalyzer> {

    private final IctclasAnalyzer analyzer;

    /**
     * Constructs a new analyzer component, with the index name and its settings and the analyzer name.
     *
     * @param indexSettings the settings and the name of the index
     * @param environment   the env
     * @param name          The analyzer name
     * @param settings      settings
     */
    private IctclasAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings, boolean fineSegment) {
        super(name, settings);
        // this.fineSegment = fineSegment;
        // this.configuration = Configuration.getConfiguration(environment);
        analyzer = new IctclasAnalyzer(Configuration.getConfiguration(environment), environment, fineSegment);
    }

    /**
     * Gets ictclas search analyzer provider.
     *
     * @param indexSettings the index settings
     * @param env           the env
     * @param name          the name
     * @param settings      the settings
     * @return the ictclas search analyzer provider
     */
    public static IctclasAnalyzerProvider getIctclasSearchAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IctclasAnalyzerProvider(indexSettings, env, name, settings, false);
    }

    /**
     * Gets ictclas index analyzer provider.
     *
     * @param indexSettings the index settings
     * @param env           the env
     * @param name          the name
     * @param settings      the settings
     * @return the ictclas index analyzer provider
     */
    public static IctclasAnalyzerProvider getIctclasIndexAnalyzerProvider(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        return new IctclasAnalyzerProvider(indexSettings, env, name, settings, true);
    }

    @Override
    public IctclasAnalyzer get() {
        return analyzer;
    }
}
