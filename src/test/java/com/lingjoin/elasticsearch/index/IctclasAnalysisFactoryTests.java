package com.lingjoin.elasticsearch.index;


import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.indices.analysis.AnalysisFactoryTestCase;

import java.util.HashMap;
import java.util.Map;

public class IctclasAnalysisFactoryTests extends AnalysisFactoryTestCase {

    public IctclasAnalysisFactoryTests( ) {
        super(new IctclasAnalysisPlugin());
    }

    static class IctclasIndexTokenizerFactoryTest extends IctclasTokenizerFactory{

        public IctclasIndexTokenizerFactoryTest(IndexSettings indexSettings, Environment environment, Settings settings, String name) {
            super(indexSettings, environment, settings, name, true);
        }
    }
    static class IctclasSearchTokenizerFactoryTest extends IctclasTokenizerFactory{
        public IctclasSearchTokenizerFactoryTest(IndexSettings indexSettings, Environment environment, Settings settings, String name) {
            super(indexSettings, environment, settings, name, false);
        }
    }

    @Override
    protected Map<String, Class<?>> getTokenizers() {
        Map<String, Class<?>> tokenizers = new HashMap<>(super.getTokenizers());
        tokenizers.put("ictclas_index", IctclasIndexTokenizerFactoryTest.class);
        tokenizers.put("ictclas_search", IctclasSearchTokenizerFactoryTest.class);
        return tokenizers;
    }

}