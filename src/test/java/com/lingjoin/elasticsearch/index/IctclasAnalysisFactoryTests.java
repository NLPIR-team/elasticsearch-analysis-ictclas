package com.lingjoin.elasticsearch.index;


import org.elasticsearch.indices.analysis.AnalysisFactoryTestCase;

import java.util.HashMap;
import java.util.Map;

public class IctclasAnalysisFactoryTests extends AnalysisFactoryTestCase {

    public IctclasAnalysisFactoryTests( ) {
        super(new IctclasAnalysisPlugin());
    }

    @Override
    protected Map<String, Class<?>> getTokenizers() {
        Map<String, Class<?>> tokenizers = new HashMap<>(super.getTokenizers());
        tokenizers.put("ictclas_index", IctclasAnalysisPlugin.class);
        return tokenizers;
    }

    @Override
    public void testTokenizers() {
        super.testTokenizers();
    }
}